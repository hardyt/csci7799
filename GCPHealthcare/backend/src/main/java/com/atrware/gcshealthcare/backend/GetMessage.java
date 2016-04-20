package com.atrware.gcshealthcare.backend;

/**
 * Created by Tom on 4/20/2016.
 */
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

import java.io.IOException;
import java.util.logging.Logger;

import javax.inject.Named;

/** An endpoint class we are exposing */
@Api(
        name = "getfilename",
        version = "v1",
        namespace = @ApiNamespace(
                ownerDomain = "backend.gcshealthcare.atrware.com",
                ownerName = "backend.gcshealthcare.atrware.com",
                packagePath=""))
public class GetMessage {

    private static final Logger log = Logger.getLogger(GetMessage.class.getName());

    /** A simple endpoint method that takes a name and says Hi back */
    @ApiMethod(name = "sayHi")
    public GetEndpointObjectModel sayHi(@Named("name") String name) {
        GetEndpointObjectModel response = new GetEndpointObjectModel();
        response.setData("Hi, " + name);

        //send gcm
        MessagingEndpoint messageSender = new MessagingEndpoint();
        try {
            messageSender.sendMessage(name);
        } catch (IOException e) {
            log.severe("IOExcpetion with getting name: " + e.toString());
        }



        return response;
    }

}
