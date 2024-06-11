import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map; // Add this import statement

public class methodImplementation {
    public HttpResponse responseGET(HttpRequest request) throws Exception{
        var url = "assets/" + request.url().substring(1);
        if (url.equals("assets/")) {
            url = "assets/index.html";
        }

        Path filePath = Path.of(url);
        if (Files.exists(filePath)){
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
            Path filePath = Path.of("assets/user.txt");
            // Perform some action based on the body of the request
            Files.writeString(filePath, body + System.lineSeparator(), StandardOpenOption.APPEND);
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

    public HttpResponse responsePUT(HttpRequest request){
        return null;
    }
}
