package com.duncpro.jaws;

import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;
import com.duncpro.jroute.router.Router;
import com.duncpro.pets.MainModule;
import com.duncpro.rex.HttpIntegrator;
import com.duncpro.rex.HttpRequest;
import com.duncpro.rex.JavaMethodRequestHandler;
import com.duncpro.rex.Rex;
import com.google.inject.Guice;
import com.sun.net.httpserver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

public class LocalJawsServer {
    private static final Logger logger = LoggerFactory.getLogger(LocalJawsServer.class);

    LocalJawsServer(HttpServer httpServer) {
        final var catchAllContext = httpServer.createContext("/");
        catchAllContext.setHandler(this::onRequest);
    }

    @Inject
    private Router<JavaMethodRequestHandler> router;

    @Inject
    private HttpIntegrator httpIntegrator;

    private Optional<byte[]> readBody(HttpExchange exchange) {
        byte[] body = null;
        try {
            body = exchange.getRequestBody().readAllBytes();
            exchange.getRequestBody().close();
            if (body.length == 0) body = null;
        } catch (IOException e) {
            logger.error("Unexpected error while request retrieving body", e);
        } finally {
            try {
                exchange.getRequestBody().close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return ofNullable(body);
    }

    private Map<String, String> parseQueryParams(String s) {
        final var params = new HashMap<String, String>();
        if (s == null) return params;

        final var pairs = s.split("&");
        String key = null;
        String value = null;
        for (String item : pairs) {
            final var pair = item.split("=");
            if (key == null) key = pair[0];
            if (value == null) value = pair[1];
            if (key != null & value != null) {
                params.put(key, value);
                key = null;
                value = null;
            }
        }

        return params;
    }

    private void onRequest(HttpExchange exchange) {
        final var runtime = new AWSLambdaRuntime(() -> Integer.MAX_VALUE); // No timeout

        // Inject on each request to mirror the behavior of the application when it is running on AWS Lambda
        Guice.createInjector(new MainModule(), new RexIntegrationModule(),
                new AWSLambdaIntegrationModule(runtime))
                .injectMembers(this);

        final var rexRequest = new HttpRequest(
                exchange.getRequestHeaders(),
                parseQueryParams(exchange.getRequestURI().getQuery()),
                readBody(exchange).orElse(null),
                new Path(exchange.getRequestURI().getPath()),
                HttpMethod.valueOf(exchange.getRequestMethod())
        );

        final var response = Rex.handleRequest(rexRequest, router, httpIntegrator);

        try {
            exchange.sendResponseHeaders(response.getStatusCode(),
                    0);
            if (response.getBody().isPresent()) {
                exchange.getResponseBody().write(response.getBody().get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            exchange.close();
        }

        exchange.close();

        runtime.runShutdownHooks();
    }


    public static void main(String[] args) throws IOException {
        final var httpServer = HttpServer.create(new InetSocketAddress(8000), 0);
        httpServer.setExecutor(null);
        httpServer.start();
        new LocalJawsServer(httpServer);
        logger.info("Started local development server... Press return to stop");
        System.in.read();
        httpServer.stop(0);
    }
}
