package com.redhat.demo;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@ApplicationScoped
public class SayStuff {

    @GET
    @Path("/saystuff")
    @Produces(MediaType.TEXT_PLAIN)
    public String sayStuff() {
        return "stuff";
    }
}
