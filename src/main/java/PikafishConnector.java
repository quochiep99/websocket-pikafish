import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.websockets.next.WebSocketClientConnection;
import io.quarkus.websockets.next.WebSocketConnector;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Destroyed;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;

@Singleton
public class PikafishConnector {
    private static final Logger log = LoggerFactory.getLogger(PikafishConnector.class);

    URI myUri;

    String endpointCookie;

    @Inject
    WebSocketConnector<PikafishClientEndpoint> connector;

    WebSocketClientConnection connection;

    String userInputCommand;

    Properties prop = new Properties();

    void onStart(@Observes StartupEvent ev) {
//        log.info("The application is starting...");
        try {
            BufferedReader br = new BufferedReader(new FileReader("settings.txt"));
            prop.load(br);
            myUri = URI.create(prop.getProperty("ENDPOINT_URI"));
            endpointCookie = prop.getProperty("ENDPOINT_HEADERS_COOKIE");
        } catch (IOException e) {
            log.info("Failed to open config file. Either because it is not found or it is not correctly formatted");
        }
    }

    void openAndSendMessage() {
//        log.info("Sending a message");
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

    void onStop(@Observes @Destroyed(ApplicationScoped.class) ShutdownEvent ev) {
        userInputCommand = "stop";
        connection.sendTextAndAwait(List.of("stdin", String.format("%s\r", userInputCommand)));
        log.info("The application is stopping...");
    }
}