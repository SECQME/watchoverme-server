package com.secqme.filter;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.NotFoundException;

import com.secqme.rs.v2.CommonJSONKey;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.json.JSONTokener;

/**
 * Created by edward on 04/05/2015.
 */
public class RollbarFilter implements Filter {

    private final static Logger myLog = Logger.getLogger(RollbarFilter.class);
    private final static String[] NON_SERIALIZED_FIELDS = new String[] {"password", "currentPassword", "newPassword", "passwordResetPin"};

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        try {
            MDC.put("request", servletRequest);

            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
            HttpSession session = httpServletRequest.getSession(false);
            if (session != null) MDC.put("session", session);

            String contentType = httpServletRequest.getHeader("Content-Type");
            if (contentType != null && MediaType.APPLICATION_JSON_TYPE.isCompatible(MediaType.valueOf(contentType))) {
                ResettableStreamHttpServletRequest wrappedRequest = new ResettableStreamHttpServletRequest((HttpServletRequest) servletRequest);
                servletRequest = wrappedRequest;
                MDC.put("request", wrappedRequest);

                try {
                    JSONObject jsonObject = new JSONObject(new JSONTokener(IOUtils.toString(wrappedRequest.getReader())));
                    if (jsonObject.has(CommonJSONKey.AUTH_TOKEN_KEY)) {
                        MDC.put("user", jsonObject.get(CommonJSONKey.AUTH_TOKEN_KEY));
                    }

                    for (String field : NON_SERIALIZED_FIELDS) {
                        if (jsonObject.has(field)) {
                            jsonObject.put(field, "***CONFIDENTIAL***");
                        }
                    }

                    MDC.put("json", jsonObject.toString());
                } catch (JSONException ex) {
                    // myLog.debug("Can't parse request to JSONObject", ex);
                } finally {
                    wrappedRequest.resetInputStream();
                }
            }

            filterChain.doFilter(servletRequest, servletResponse);
        } catch (Exception ex) {
            String simpleName = ex.getClass().getSimpleName();
            boolean reportAsError = true;
            if (simpleName.equals("ClientAbortException")) {
                reportAsError = false;
            } else if (simpleName.equals("ServletException")) {
                if (ex.getCause() instanceof NotFoundException) {
                    reportAsError = false;
                }
            }

            if (reportAsError) {
                myLog.error(simpleName + ": " + ex.getMessage(), ex);
            } else {
                myLog.debug(simpleName + ": " + ex.getMessage(), ex);
            }

            throw ex;
        } finally {
            MDC.remove("json");
            MDC.remove("request");
            MDC.remove("session");
            MDC.remove("user");
        }
    }

    @Override
    public void destroy() {
    }

    private static class ResettableStreamHttpServletRequest extends HttpServletRequestWrapper {

        private byte[] rawData;
        private HttpServletRequest request;
        private ResettableServletInputStream servletStream;

        public ResettableStreamHttpServletRequest(HttpServletRequest request) {
            super(request);
            this.request = request;
            this.servletStream = new ResettableServletInputStream();
        }


        public void resetInputStream() {
            if (rawData != null) {
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (rawData == null) {
                rawData = IOUtils.toByteArray(this.request.getReader());
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
            return servletStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (rawData == null) {
                rawData = IOUtils.toByteArray(this.request.getReader());
                servletStream.stream = new ByteArrayInputStream(rawData);
            }
            return new BufferedReader(new InputStreamReader(servletStream));
        }


        private class ResettableServletInputStream extends ServletInputStream {

            private InputStream stream;

            @Override
            public int read() throws IOException {
                return stream.read();
            }
        }
    }
}