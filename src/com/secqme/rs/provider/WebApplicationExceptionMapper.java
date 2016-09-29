package com.secqme.rs.provider;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.manager.UserManager;
import com.secqme.rs.BaseResource;
import org.apache.log4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

/**
 * Created by edward on 22/04/2015.
 */
@Provider
public class WebApplicationExceptionMapper extends BaseExceptionMapper<WebApplicationException> {

    private final static Logger myLog = Logger.getLogger(CoreExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException e) {
        Object entity = e.getResponse().getEntity();
        String type = MediaType.APPLICATION_JSON;
        int status = e.getResponse().getStatus();

        switch (status) {
        case 400:
            entity = new CoreException(ErrorType.HTTP_ERROR_400, UserManager.USER_DEFAULT_LANGUAGE).getMessageAsJSON().toString();
            break;
        case 404:
            entity = new CoreException(ErrorType.HTTP_ERROR_404, UserManager.USER_DEFAULT_LANGUAGE).getMessageAsJSON().toString();
            break;
        case 405:
            entity = new CoreException(ErrorType.HTTP_ERROR_405, UserManager.USER_DEFAULT_LANGUAGE).getMessageAsJSON().toString();
            break;
        default:
            type = MediaType.TEXT_HTML;
        }

        switch (status) {
            case 400:
            case 404:
            case 405:
                myLog.debug("4xx WebApplicationExceptionMapper: " + request.getPathInfo() + " (" + status + ")", e);
                break;
            default:
                myLog.error("WebApplicationExceptionMapper: " + request.getPathInfo() + " (" + status + ")", e);
                break;
        }


        return Response.status(status)
                .type(type)
                .entity(entity)
                .build();
    }
}
