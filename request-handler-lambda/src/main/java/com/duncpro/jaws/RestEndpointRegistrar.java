package com.duncpro.jaws;

import com.duncpro.jroute.router.Router;
import com.duncpro.rex.JavaMethodRequestHandler;
import com.duncpro.rex.Rex;
import com.google.inject.spi.ProvisionListener;

public class RestEndpointRegistrar implements ProvisionListener {
    private final Router<JavaMethodRequestHandler> router;

    RestEndpointRegistrar(Router<JavaMethodRequestHandler> router) {
        this.router = router;
    }

    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        final var type = provision.getBinding().getKey().getTypeLiteral().getRawType();
        final var isRestApi = type.isAnnotationPresent(RestApi.class);

        if (isRestApi) {
            Rex.addRoutes(provision.provision(), router);
        }
    }
}
