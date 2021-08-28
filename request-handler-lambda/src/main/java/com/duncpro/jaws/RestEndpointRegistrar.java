package com.duncpro.jaws;

import com.google.inject.Scopes;
import com.google.inject.spi.ProvisionListener;

public class RestEndpointDiscovery implements ProvisionListener {
    @Override
    public <T> void onProvision(ProvisionInvocation<T> provision) {
        final var isRestApi = provision.provision().getClass().isAnnotationPresent(RestApi.class);
        final var isSingleton = Scopes.isSingleton(provision.getBinding());

        if (isRestApi) {
            if (!isSingleton) {
                throw new RuntimeException("Classes annotated with @RestApi should be eager singletons.");
            }
        }
    }
}
