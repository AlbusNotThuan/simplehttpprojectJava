import java.util.Map;
import java.util.List;

public record HttpResponse(int responseCode, 
            Map<String, 
            List<String>> headers, 
            byte[] body) {

}
