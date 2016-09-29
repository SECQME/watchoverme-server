package com.secqme.rs.provider;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.ExceptionMapper;

/**
 * Created by edward on 23/04/2015.
 */
public abstract class BaseExceptionMapper<T extends Throwable> implements ExceptionMapper<T> {
    @Context
    protected HttpServletRequest request;

    @Context
    protected ServletContext servletContext;
}
