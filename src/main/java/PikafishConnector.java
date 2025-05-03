import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.websockets.next.WebSocketClientConnection;
import io.quarkus.websockets.next.WebSocketConnector;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

@Singleton
public class PikafishConnector {

    private static final Logger log = LoggerFactory.getLogger(PikafishConnector.class);

    @ConfigProperty(name = "endpoint.uri")
    URI myUri;

    @ConfigProperty(name = "endpoint.headers.cookie")
    String endpointCookie;

    @Inject
    WebSocketConnector<PikafishClientEndpoint> connector;

    WebSocketClientConnection connection;

    String userInputCommand;

    Properties prop = new Properties();

    void onStart(@Observes StartupEvent ev) {
        log.info("The application is starting...");
        InputStream inputStream = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream("settings.txt");
        try {
            prop.load(inputStream);
            log.info(prop.getProperty("ENDPOINT_HEADERS_COOKIE"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    void openAndSendMessage() {
        log.info("Sending a message");
        connection = connector
                .baseUri(myUri)
                .addHeader("Cookie", endpointCookie)
                .connectAndAwait();
        Scanner sc = new Scanner(System.in);
        userInputCommand = sc.nextLine();
        while (!"quit".equals(userInputCommand)) {
            connection.sendTextAndAwait(List.of("stdin", String.format("%s\r", userInputCommand)));
            userInputCommand = sc.nextLine();
        }
    }

    void onStop(@Observes ShutdownEvent ev) {
        userInputCommand = "stop";
        connection.sendTextAndAwait(List.of("stdin", String.format("%s\r", userInputCommand)));
        log.info("The application is stopping...");
    }
}