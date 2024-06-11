import java.util.concurrent.Executors;

public class httpserverApp {

    public static void main(String args[]){
        System.out.println("Starting server...");
        System.out.println("Listening on port 8080: http://localhost:8080");
        var server = new SimpleHttpServer(Executors.newFixedThreadPool(100), 8080);
        server.start();
    }


}