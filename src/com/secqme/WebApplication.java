package com.secqme;

import org.glassfish.jersey.server.ResourceConfig;

/**
 * Created by edward on 30/09/2015.
 */
public class WebApplication extends ResourceConfig {

    public WebApplication() {
        register(org.glassfish.jersey.media.multipart.MultiPartFeature.class);
        register(org.glassfish.jersey.jettison.JettisonFeature.class);
    }
}
