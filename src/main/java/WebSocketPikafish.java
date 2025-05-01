import io.quarkus.runtime.QuarkusApplication;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketPikafish implements QuarkusApplication {
    private static final Logger log = LoggerFactory.getLogger(WebSocketPikafish.class);

    @Inject
    PikafishConnector bean;

    @Override
    public int run(String... args) throws Exception {
        bean.openAndSendMessage();
        return 0;
    }
}
