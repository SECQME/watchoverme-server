package com.secqme.rs.provider;

import com.secqme.CoreException;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Created by edward on 22/04/2015.
 */
@Provider
public class CoreExceptionMapper extends BaseExceptionMapper<CoreException> {

    private final static Logger myLog = Logger.getLogger(CoreExceptionMapper.class);

    @Override
    public Response toResponse(CoreException e) {
        myLog.debug("CoreExceptionMapper: " + request.getRequestURI(), e);

        if (request.getRequestURI().contains("/rs/v2.1/")) {
            return Response.status(e.getErrorType().getHttpStatusCode())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(e.getMessageAsJSON().toString())
                    .build();
        } else {
            return Response.status(e.getErrorType().getHttpStatusCodeLegacy())
                    .type(MediaType.APPLICATION_JSON)
                    .entity(e.getMessageAsJSON().toString())
                    .build();
        }
    }
}
