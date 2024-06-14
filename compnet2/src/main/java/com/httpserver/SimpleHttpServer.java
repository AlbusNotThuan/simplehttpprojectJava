package com.httpserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public class SimpleHttpServer implements httpserver {

    private final ExecutorService executors;
    private final int port;
    private static final int DEFAULT_PACKET_SIZE = 1024;
    private static final String HTTP_NEW_LINE = "\r\n";
    private static final String HTTP_HEAD_BODY = "\r\n\r\n";
    private static final int HTTP_HEAD_BODY_BYTES = HTTP_HEAD_BODY.getBytes(StandardCharsets.US_ASCII).length;
    private static final String CONTENT_LENGTH_HEADER = "content-length";

    /**
     * Constructs a SimpleHttpServer object.
     *
     * @param executors the executor service for handling incoming requests
     * @param port      the port number to listen on
     */
    public SimpleHttpServer(ExecutorService executors, int port) {
        this.executors = executors;
        this.port = port;
    }

    @Override
    public void start() {
        new Thread(() -> {
            try {
                ServerSocket server = new ServerSocket(port);

                while (true) {
                    var connection = server.accept();

                    executors.execute(() -> {
                        try {
                            handleRequest(connection);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void stop() {
        // TODO: Implement the stop method
    }

    /**
     * Handles an incoming request.
     *
     * @param connection the socket connection for the request
     * @throws Exception if an error occurs while handling the request
     */
    private void handleRequest(Socket connection) throws Exception {
        var requestOpt = readRequest(connection);

        var request = requestOpt.get();

        respondToRequest(connection, request);

        printRequest(request);

        closeConnection(connection);
    }

    /**
     * Reads an incoming request from the socket connection.
     *
     * @param connection the socket connection for the request
     * @return an Optional containing the HttpRequest if the request is valid, or an
     *         empty Optional if the request is invalid
     * @throws Exception if an error occurs while reading the request
     */
    private Optional<HttpRequest> readRequest(Socket connection) throws Exception {
        var stream = connection.getInputStream();
        var rawRequestHead = readRawRequestHead(stream);

        if (rawRequestHead.length == 0) {
            return Optional.empty();
        }

        var requestHead = new String(rawRequestHead, StandardCharsets.US_ASCII);
        var lines = requestHead.split(HTTP_NEW_LINE);

        var line = lines[0];
        String[] methodUrl = line.split(" ");
        String method = methodUrl[0];
        String url = methodUrl[1];

        var headers = readHeaders(lines);

        byte[] body;

        var bodyLength = getBodyLength(headers);

        if (bodyLength > 0) {
            var bodyStartIndex = requestHead.indexOf(HTTP_HEAD_BODY);

            if (bodyStartIndex > 0) {
                var readBody = Arrays.copyOfRange(rawRequestHead,
                        bodyStartIndex + HTTP_HEAD_BODY_BYTES, rawRequestHead.length);

                body = readBody(stream, readBody, bodyLength);
            } else {
                body = new byte[0];
            }

        } else {
            body = new byte[0];
        }

        return Optional.of(new HttpRequest(method, url, headers, body));
    }

    /**
     * Reads the raw request head from the input stream.
     *
     * @param stream the input stream to read from
     * @return the raw request head as a byte array
     * @throws Exception if an error occurs while reading the request head
     */
    private byte[] readRawRequestHead(InputStream stream) throws Exception {
        var toRead = stream.available();
        if (toRead == 0) {
            toRead = DEFAULT_PACKET_SIZE;
        }

        var buffer = new byte[toRead];
        var read = stream.read(buffer);
        if (read <= 0) {
            return new byte[0];
        }

        return read == toRead ? buffer : Arrays.copyOf(buffer, read);

    }

    /**
     * Reads the request body from the input stream.
     *
     * @param stream             the input stream to read from
     * @param readBody           the initial body bytes read from the request head
     * @param expectedBodyLength the expected length of the request body
     * @return the complete request body as a byte array
     * @throws IOException if an error occurs while reading the request body
     */
    private byte[] readBody(InputStream stream, byte[] readBody, int expectedBodyLength) throws IOException {
        if (readBody.length == expectedBodyLength) {
            return readBody;
        }

        var result = new ByteArrayOutputStream(expectedBodyLength);
        result.write(readBody);
        var readBytes = readBody.length;
        var buffer = new byte[DEFAULT_PACKET_SIZE];

        while (readBytes < expectedBodyLength) {
            var read = stream.read(buffer);

            if (read > 0) {
                result.write(buffer, 0, read);
                readBytes += read;
                break;
            }
        }

        return result.toByteArray();
    }

    /**
     * Responds to an incoming request.
     *
     * @param connection the socket connection for the request
     * @param request    the HttpRequest object representing the request
     * @throws Exception if an error occurs while responding to the request
     */
    private void respondToRequest(Socket connection, HttpRequest request) throws Exception {
        var method = request.method();
        var respondBuilder = new methodImplementation();
        HttpResponse res = null;
        switch (method) {
            case "GET":
                // System.out.println("method = GET");
                res = respondBuilder.responseGET(request);
                break;
            case "POST":
                // System.out.println("method = POST");
                res = respondBuilder.responsePOST(request);
                break;

            case "PUT":
                // System.out.println("method = PUT");
                res = respondBuilder.responsePUT(request);
                break;

            case "DELETE":
                // System.out.println("method = DELETE");
                res = respondBuilder.responseDELETE(request);
                break;

            case "HEAD":
                // System.out.println("method = HEAD");
                res = respondBuilder.responseHEAD(request);
                break;
            default:
                break;
        }

        // var res = requestRespond(request);
        var os = connection.getOutputStream();
        var resHead = new StringBuilder("HTTP/1.1 %d".formatted(res.responseCode()));

        res.headers().forEach((k, vs) -> vs.forEach(v -> resHead.append(HTTP_NEW_LINE)
                .append(k)
                .append(": ")
                .append(v)));

        resHead.append(HTTP_HEAD_BODY);

        os.write(resHead.toString().getBytes(StandardCharsets.UTF_8));

        if (res.body().length > 0) {
            os.write(res.body());
        }
    }

    /**
     * Generates a response for the given request.
     *
     * @param request the HttpRequest object representing the request
     * @return the HttpResponse object representing the response
     */
    private HttpResponse requestRespond(HttpRequest request) {
        var body = """
                <html>
                    <head>
                        <title>Java HTTP Server</title>
                    </head>
                    <body>
                        <h1>Hello from Java HTTP Server</h1>
                        <p>Method = %s</p>
                        <p>URL = %s</p>
                        <p>Headers = %s</p>
                        <p>Body = %s</p>
                    </body>
                """.getBytes(StandardCharsets.UTF_8);

        var headers = Map.of("Content-Type", List.of("text/html"),
                "Content-Length", List.of(String.valueOf(body.length)));
        return new HttpResponse(200, headers, body);
    }

    /**
     * Reads the headers from the request lines.
     *
     * @param lines the request lines
     * @return a map of header names to header values
     */
    private Map<String, List<String>> readHeaders(String[] lines) {
        var headers = new HashMap<String, List<String>>();

        for (int i = 1; i < lines.length; i++) {
            var line = lines[i];
            if (line.isEmpty()) {
                break;
            }
            var keyValue = line.split(":", 2);
            var key = keyValue[0].toLowerCase().trim();
            var value = keyValue[1].trim();

            headers.computeIfAbsent(key, k -> new ArrayList<>()).add(value);

        }

        return headers;

    }

    /**
     * Retrieves the length of the request body from the headers.
     *
     * @param headers the request headers
     * @return the length of the request body, or 0 if not specified
     */
    private int getBodyLength(Map<String, List<String>> headers) {
        try {
            return Integer.parseInt(headers.getOrDefault(CONTENT_LENGTH_HEADER, List.of("0")).get(0));
        } catch (Exception e) {
            return 0;
        }
    }

    private void printRequest(HttpRequest request) {
        System.out.println("Method: " + request.method());
        System.out.println("URL: " + request.url());
        // System.out.println("Headers: " + request.header());
        System.out.println("Body: " + new String(request.body(), StandardCharsets.UTF_8));
    }

    private void closeConnection(Socket connection) {
        try {
            System.out.println("Closing connection...");
            connection.close();
        } catch (Exception ignored) {

        }
    }
}
