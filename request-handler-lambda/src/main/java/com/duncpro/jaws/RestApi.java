package com.duncpro.jaws;

import com.duncpro.rex.Rex;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * When a class is bound with {@link com.google.inject.Guice} it is scanned by {@link RestEndpointRegistrar}
 * for this annotation. If present the class will then be scanned for any {@link Rex} request handlers and
 * they will be registered with the application {@link com.duncpro.jroute.router.Router}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface RestApi {
}
