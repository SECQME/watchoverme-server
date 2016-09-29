package com.secqme.filter;

import com.secqme.rs.v2.CommonJSONKey;
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by edward on 15/09/2015.
 */
public class AuthorizationFilter implements Filter {

    private final static Logger myLog = Logger.getLogger(RollbarFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

        String authorization = httpServletRequest.getHeader("Authorization");
        if (authorization != null) {
            myLog.debug("Got Authorization header: " + authorization);
            String[] params = authorization.split(" ", 2);
            if (params.length == 2 && "Bearer".equals(params[0])) {
                MDC.put("user", params[1]);
                httpServletRequest.setAttribute(CommonJSONKey.AUTH_TOKEN_KEY, params[1]);
            }
        }

        String clientVersion = httpServletRequest.getHeader("X-WOM-Client-Version");
        if (clientVersion != null) {
            myLog.debug("Got X-WOM-Client-Version header: " + clientVersion);
            MDC.put(CommonJSONKey.CLIENT_VERSION_KEY, clientVersion);
            httpServletRequest.setAttribute(CommonJSONKey.CLIENT_VERSION_KEY, Math.floor(Double.parseDouble(clientVersion)));
        }

        String clientOS = httpServletRequest.getHeader("X-WOM-Client-OS");
        if (clientVersion != null) {
            myLog.debug("Got X-WOM-Client-OS header: " + clientOS);
            MDC.put(CommonJSONKey.CLIENT_OS_KEY, clientOS);
            httpServletRequest.setAttribute(CommonJSONKey.CLIENT_OS_KEY, clientOS);
        }

        filterChain.doFilter(httpServletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
