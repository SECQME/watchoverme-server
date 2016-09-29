package com.secqme.rs.provider;


import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.manager.UserManager;
import com.secqme.rs.BaseResource;
import org.apache.log4j.Logger;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Created by edward on 09/04/2015.
 */
@Provider
public class ThrowableExceptionMapper extends BaseExceptionMapper<Throwable> {

    private final static Logger LOGGER = Logger.getLogger(ThrowableExceptionMapper.class);

    @Override
    public Response toResponse(Throwable throwable) {
        LOGGER.error("ThrowableExceptionMapper: " + request.getRequestURI(), throwable);

        CoreException ex = new CoreException(ErrorType.HTTP_ERROR_500, UserManager.USER_DEFAULT_LANGUAGE);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(ex.getMessageAsJSON().toString())
                .build();
    }
}
