package com.secqme.rs;

import com.secqme.domain.model.notification.sms.SmsCountryVO;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Enumeration;
import java.util.List;

@Path("/systemadmin")
public class SystemResource extends BaseResource {

    private final static Logger myLog = Logger.getLogger(SystemResource.class);
    private final static String ADMIN_NAME_KEY = "adminName";
    private final static String ADMIN_PASSWORD_KEY = "adminPassword";
    private final static String COUNTRY_LIST_KET = "countryList";

    public SystemResource() {
    }

    @GET
    @Path("/smscountries/{appid}/{appsecret}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSupportedCountriesForSMS(@PathParam("appid") String applicationId,
                                              @PathParam("appsecret") String applicationSecret) {
        String result = "";

        try {
            sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
            List<SmsCountryVO> smsCountryVOList = smsManager.getSupportedSMSCountryList();
            if(smsCountryVOList != null && smsCountryVOList.size() >  0) {
                JSONArray smsArray = new JSONArray();
                for(SmsCountryVO smsCountry : smsCountryVOList) {
                    smsArray.put(smsCountry.toJSONObject());
                }
                JSONObject jobj = new JSONObject();
                jobj.put(COUNTRY_LIST_KET, smsArray);
                result = jobj.toString();
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return result;
    }

    @POST
    @Path("/cache/expire")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String expireCache(String reqBody) {
        /**

         * {
         * "adminName":"secqmeadmin", "adminPassword":"zaq12wsx#" }
         */
        try {
            JSONObject jobj = new JSONObject(reqBody);
            if (jobj.has(ADMIN_NAME_KEY) && jobj.has(ADMIN_PASSWORD_KEY)) {
                myLog.debug("Expiring all system param Cached");
                if (sysAdminUtil.verifyAdminLogin(jobj.getString(ADMIN_NAME_KEY),
                        jobj.getString(ADMIN_PASSWORD_KEY))) {
                    sysAdminUtil.expireAllSystemCache();

                }
            }
        } catch (JSONException ex) {
            throw new BadRequestException("Invalid JSON request body.", ex);
        }

        return "{\"status\":\"ok\"}";
    }


    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String TestResources(@Context HttpServletRequest httpReq, @Context HttpServletResponse httpResp) {
        String result = "";

        try {

            String ipAddress = httpReq.getHeader("X-FORWARDED-FOR");
            myLog.debug("Capture iPAddress->" + ipAddress);
            Enumeration<String> hearderNameEnum = httpReq.getHeaderNames();
            while(hearderNameEnum.hasMoreElements()) {
                String headerName = hearderNameEnum.nextElement();
                myLog.debug("H->" + headerName + ":" + httpReq.getHeader(headerName));
            }

            Enumeration<String> parameterNames = httpReq.getParameterNames() ;
            while(parameterNames.hasMoreElements()) {
                String paraName = parameterNames.nextElement();
                myLog.debug("P->" + paraName + ":" + httpReq.getParameter(paraName));
            }
            myLog.debug( ", 2.->" + httpReq.getContextPath() +
                    ",4->" + httpReq.getRemoteAddr() + ",5.->" + httpReq.getRequestedSessionId());
            myLog.debug("-RemoteUser->" + httpReq.getRemoteUser());
            myLog.debug("-CharacterEncoding->" + httpReq.getCharacterEncoding());

            httpResp.sendRedirect("http://google.com");
        } catch (IOException ex) {
            myLog.error("Redirect error", ex);
        }

        return result;
    }
}
