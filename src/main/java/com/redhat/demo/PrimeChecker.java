package com.redhat.demo;

import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/")
@ApplicationScoped
public class PrimeChecker {

    @GET
    @Path("/prime/{number}")
    @Produces(MediaType.TEXT_PLAIN)
    @Counted(name = "checkPrimesCount")
    @Timed(name= "checkPrimesTimer")
    public String checkPrime(@PathParam("number") long number) {
        System.out.println("checkPrime, number: " + number);
        if(number < 1) {
            return "Number is not natural";
        }
        if(number == 1) {
            return "1 is not prime";
        }
        if(number == 2) {
            return "2 is prime";
        }

        if(number %2 == 0) {
            return number + " is not prime, divisible by 2";
        }

        for(int i = 3; i < Math.floor(Math.sqrt(number)) + 1; i = i+2) {
            if(number % i == 0) {
                return number + " is not prime, divisible by " + i;
            }
        }
        return number + " is prime";
    }

}
