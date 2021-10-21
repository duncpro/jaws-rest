package com.duncpro.jaws;

import com.duncpro.jroute.HttpMethod;
import com.duncpro.jroute.Path;
import com.duncpro.pets.LocalDeploymentModule;
import com.duncpro.pets.MainModule;
import com.duncpro.rex.*;
import com.google.inject.Guice;
import com.sun.net.httpserver.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

public class LocalJawsServer {
    private static final Logger logger = LoggerFactory.getLogger(LocalJawsServer.class);

    LocalJawsServer(HttpServer httpServer) {
        final var catchAllContext = httpServer.createContext("/");
        catchAllContext.setHandler(this::onRequestWithLogging);
    }

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

    protected static Map<String, String> parseQueryParams(String s) {
        final var params = new HashMap<String, String>();
        if (s == null) return params;

        final var pairs = s.split("&");
        for (String item : pairs) {
            if (item.isBlank()) continue;
            final var pair = item.split("=");
            params.put(pair[0], pair[1]);
        }

        return params;
    }

    private void sendRexResponse(HttpExchange exchange, SerializedHttpResponse response) {
        try {
            exchange.sendResponseHeaders(response.getStatusCode(), 0);
            if (response.getBody().isPresent()) {
                exchange.getResponseBody().write(response.getBody().get());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void onRequest(HttpExchange exchange) {
        final var runtime = new AWSLambdaRuntime(() -> Integer.MAX_VALUE); // No timeout

        final var rexRequest = new HttpRequest(
                exchange.getRequestHeaders(),
                parseQueryParams(exchange.getRequestURI().getQuery()),
                readBody(exchange).orElse(null),
                new Path(exchange.getRequestURI().getPath()),
                HttpMethod.valueOf(exchange.getRequestMethod())
        );

        // New injector on each request to mirror the behavior of the application when it is running on AWS Lambda
        final var requestHandler = Guice.createInjector(new RexIntegrationModule(), new AWSLambdaIntegrationModule(runtime),
                new MainModule(), new LocalDeploymentModule()).getInstance(RootRequestHandler.class);
        sendRexResponse(exchange, requestHandler.apply(rexRequest));
        exchange.close();
        runtime.runShutdownHooks();
    }

    private void onRequestWithLogging(HttpExchange exchange) {
        try {
            onRequest(exchange);
        } catch (RuntimeException e) {
            logger.error("An unexpected error occurred inside of LocalJawsServer. This is typically indicative" +
                    " of a Guice provisioning error. See cause below.", e);
        }
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
