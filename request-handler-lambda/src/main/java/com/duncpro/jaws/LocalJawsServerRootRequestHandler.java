package com.duncpro.jaws;

import com.duncpro.jroute.router.Router;
import com.duncpro.rex.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.function.Function;

public class LocalJawsServerRootRequestHandler implements Function<HttpRequest, SerializedHttpResponse> {
    private final Router<JavaMethodRequestHandler> router;
    private final HttpIntegrator httpIntegrator;

    @Inject
    public LocalJawsServerRootRequestHandler(Router<JavaMethodRequestHandler> router, HttpIntegrator httpIntegrator) {
        this.router = router;
        this.httpIntegrator = httpIntegrator;
    }

    @Override
    public SerializedHttpResponse apply(HttpRequest request) {
        try {
            return Rex.handleRequest(request, router, httpIntegrator);
        } catch (RequestHandlerException e) {
            logger.error("An unexpected error occurred inside of a Rex request handler while servicing" +
                    " this request.", e);
            return new SerializedHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR.getStatusCode());
        }
    }

    private static final Logger logger = LoggerFactory.getLogger(LocalJawsServerRootRequestHandler.class);
}
