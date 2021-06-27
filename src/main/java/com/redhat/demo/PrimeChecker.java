package com.redhat.demo;

import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.annotation.*;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

@Path("/")
@ApplicationScoped
public class PrimeChecker {

    private CountDownLatch countDownLatch = new CountDownLatch(1);
    ArrayList<String> list = new ArrayList<>();

    @Inject
    @Metric(name = "injectedCounter", absolute = true)
    private Counter injectedCounter;

    @GET
    @Path("/prime/{number}")
    @Produces(MediaType.TEXT_PLAIN)
    @Counted(name = "checkPrimesCount")
    @Timed(name = "checkPrimesTimer", absolute = true, description = "Timing primeCount")
    @Metered(name = "checkPrimesFrequency", absolute = true)
    public String checkPrime(@PathParam("number") long number) {
        System.out.println("checkPrime, number: " + number);
        if (number < 1) {
            return "Number is not natural";
        }
        if (number == 1) {
            return "1 is not prime";
        }
        if (number == 2) {
            return "2 is prime";
        }

        if (number % 2 == 0) {
            return number + " is not prime, divisible by 2";
        }

        for (int i = 3; i < Math.floor(Math.sqrt(number)) + 1; i = i + 2) {
            if (number % i == 0) {
                return number + " is not prime, divisible by " + i;
            }
        }
        return number + " is prime";
    }

    @GET
    @Path("/parallel")
    @ConcurrentGauge(name = "parallelAccess", description = "Parallel accesses")
    public void parallelAccess() throws InterruptedException {
        countDownLatch.await();
        System.out.println("parallelAccess");
    }

    @GET
    @Path("/parallel-finish")
    public void parallelFinish() {
        System.out.println("parallel-finish");
        countDownLatch.countDown();
    }

    @GET
    @Path("/injected-metric")
    public String injectedMetric() {
        injectedCounter.inc();
        System.out.println("injected metric");
        return "injected metric";
    }

    @GET
    @Path("/largememory")
    public String largeMemory() throws InterruptedException {

        for(int i = 0; i < 250000;  i++) {
            list.add("Just some sample text");
        }
        System.out.println("In largeMemory");

        return "list size: " + list.size();
    }
}
