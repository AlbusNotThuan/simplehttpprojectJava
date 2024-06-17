package com.httpserver;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map; // Add this import statement

import org.json.JSONObject;

public class methodImplementation {
    public HttpResponse responseGET(HttpRequest request) throws Exception {
        var url = "assets/" + request.url().substring(1);
        if (url.equals("assets/")) {
            url = "assets/index.html";
        }

        Path filePath = Path.of(url);
        if (Files.exists(filePath)) {
            try {
                byte[] body = Files.readAllBytes(filePath);
                var headers = Map.of("Content-Type", List.of("text/html"),
                        "Content-Length", List.of(String.valueOf(body.length)));
                return new HttpResponse(200, headers, body);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            byte[] body = "File not found".getBytes(StandardCharsets.UTF_8);
            return new HttpResponse(404, Map.of("Content-Length", List.of(String.valueOf(body.length))), body);
        }

        byte[] body = "Internal Server Error".getBytes(StandardCharsets.UTF_8);
        return new HttpResponse(500, Map.of("Content-Length", List.of(String.valueOf(body.length))), body);
    }

    public HttpResponse responsePOST(HttpRequest request) {
        try {
            // Read the body of the request
            String body = new String(request.body(), StandardCharsets.UTF_8);
            String[] parts = body.split(";");
            String fileName = parts[0];
            String value = parts[1];
            Path filePath = Path.of("assets/" + fileName + ".txt");
            // Perform some action based on the body of the request
            Files.writeString(filePath, value + System.lineSeparator(), StandardOpenOption.APPEND);
            String responseBody = "User data added successfully";
            var headers = Map.of("Content-Type", List.of("text/plain"),
                    "Content-Length", List.of(String.valueOf(responseBody.length())));
            return new HttpResponse(200, headers, responseBody.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();

            // If an error occurs, return a 500 Internal Server Error response
            String responseBody = "Internal Server Error";
            var headers = Map.of("Content-Type", List.of("text/plain"),
                    "Content-Length", List.of(String.valueOf(responseBody.length())));
            return new HttpResponse(500, headers, responseBody.getBytes(StandardCharsets.UTF_8));
        }
    }

    public HttpResponse responsePUT(HttpRequest request) {
        try {
            // Read the body of the request
            String body = new String(request.body(), StandardCharsets.UTF_8);
            Path newFilePath = Path.of("assets/" + body + ".txt");
            Files.createFile(newFilePath);
            String responseBody = "Asset added successfully";
            var headers = Map.of("Content-Type", List.of("text/plain"),
                    "Content-Length", List.of(String.valueOf(responseBody.length())));
            return new HttpResponse(200, headers, responseBody.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();

            // If an error occurs, return a 500 Internal Server Error response
            String responseBody = "Internal Server Error";
            var headers = Map.of("Content-Type", List.of("text/plain"),
                    "Content-Length", List.of(String.valueOf(responseBody.length())));
            return new HttpResponse(500, headers, responseBody.getBytes(StandardCharsets.UTF_8));
        }
    }

    public HttpResponse responseDELETE(HttpRequest request) {
        try {
            String body = new String(request.body(), StandardCharsets.UTF_8);
            Path filePath = Path.of("assets/" + body + ".txt");
            // Perform some action based on the body of the request
            Files.delete(filePath);
            String responseBody = "Asset deleted successfully";
            var headers = Map.of("Content-Type", List.of("text/plain"),
                    "Content-Length", List.of(String.valueOf(responseBody.length())));
            return new HttpResponse(200, headers, responseBody.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();

            // If an error occurs, return a 500 Internal Server Error response
            String responseBody = "Internal Server Error";
            var headers = Map.of("Content-Type", List.of("text/plain"),
                    "Content-Length", List.of(String.valueOf(responseBody.length())));
            return new HttpResponse(500, headers, responseBody.getBytes(StandardCharsets.UTF_8));
        }

    }

    public HttpResponse responseHEAD(HttpRequest request) {
        try {
            // Determine the file requested
            String url = request.url().substring(1);
            if (url.equals("")) {
                url = "index.html";
            }

            // Create a Path object for the requested file
            Path filePath = Path.of("assets/" + url);

            // Check if the file exists
            if (!Files.exists(filePath)) {
                // If the file doesn't exist, return a 404 Not Found response
                return new HttpResponse(404, Map.of(), new byte[0]);
            }

            // If the file exists, return a 200 OK response with no body
            return new HttpResponse(200, Map.of(), new byte[0]);
        } catch (Exception e) {
            e.printStackTrace();

            // If an error occurs, return a 500 Internal Server Error response
            return new HttpResponse(500, Map.of(), new byte[0]);
        }
    }
}
