import com.google.gson.Gson;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebSocketClient(path = "")
public class PikafishClientEndpoint {

    private static final Logger log = LoggerFactory.getLogger(PikafishClientEndpoint.class);

    @OnTextMessage
    public void consume(String message) {
        Gson gson = new Gson();
        Object[] strArray = gson.fromJson(message, Object[].class);
        if ("stdout".equals(strArray[0])) {
            log.info((String) strArray[1]);
        }
    }
}