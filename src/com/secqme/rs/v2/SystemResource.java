package com.secqme.rs.v2;

import com.secqme.CoreException;
import com.secqme.domain.model.notification.sms.SmsCountryVO;
import com.secqme.rs.BaseResource;
import com.sun.jersey.api.ConflictException;
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

@Path("/v2/systemadmin")
public class SystemResource extends BaseResource {

    private final static Logger myLog = Logger.getLogger(SystemResource.class);

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
            if (smsCountryVOList != null && smsCountryVOList.size() > 0) {
                JSONArray smsArray = new JSONArray();
                for (SmsCountryVO smsCountry : smsCountryVOList) {
                    smsArray.put(smsCountry.toJSONObject());
                }

                JSONObject jobj = new JSONObject();
                jobj.put(CommonJSONKey.COUNTRY_LIST_KET, smsArray);
                result = jobj.toString();
            }
        } catch (JSONException ex) {
            myLog.error("JSONException", ex);
        }
        return result;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String TestResources(@Context HttpServletRequest httpReq, @Context HttpServletResponse httpResp) {
        String result = "";


        try {
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
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result;
    }

    /*
     * 
     * {
     * 	"status": 0 //0 for user, 1 for contact
     */
    @POST
    @Path("/upatemobilenumber/{appid}/{appsecret}")
    @Produces(MediaType.APPLICATION_JSON)
    public String updateDatabaseMobileNo(@PathParam("appid") String applicationId,
                                              @PathParam("appsecret") String applicationSecret,
                                              String reqBody) {

        try {
            sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
            JSONObject jobj = new JSONObject(reqBody);
            if(jobj.has(CommonJSONKey.STATUS)) {
            	int status = jobj.getInt(CommonJSONKey.STATUS); 
            	if(status == 0) { //update user mobileNo
            		userManager.updateUsersMobileNo();
            	} else if (status == 1) { //update contact mobileNo
            		userManager.updateContactsMobileNo();
            	}
            } else {
            	return NOT_OK_RESULT;
            }

        } catch (CoreException e) {
            return e.getMessageAsJSON().toString();
        } catch (JSONException je) {
            throw new ConflictException(je.getMessage());
        }
        return OK_STATUS;
    }
    
    /*
     * 
     * {
     * 	"emailAddr":"kithau.low@secq.me", 
     * "mobileCountry":"MY",
     * "mobileNumber":"123456789"
     * }
     */
    @POST
    @Path("/sendtestemail/{appid}/{appsecret}")
    @Produces(MediaType.APPLICATION_JSON)
    public String sendTestEmail(@PathParam("appid") String applicationId,
                                              @PathParam("appsecret") String applicationSecret,
                                              String reqBody) {

        try {
            sysAdminUtil.grantClientAccess(applicationId, applicationSecret);
            JSONObject jobj = new JSONObject(reqBody);
            String name = null;
            String email = null;
            String mobileCountry = null;
            String mobileNumber = null;
            if (jobj.has(CommonJSONKey.USER_NAME_KEY)) {
                name = jobj.getString(CommonJSONKey.USER_NAME_KEY);
            }
            if (jobj.has(CommonJSONKey.EMAIL_ADDR_KEY)) {
            	email = jobj.getString(CommonJSONKey.EMAIL_ADDR_KEY);
            }
            if (jobj.has(CommonJSONKey.MOBILE_COUNTRY_KEY)) {
            	mobileCountry = jobj.getString(CommonJSONKey.MOBILE_COUNTRY_KEY); 
            }
            if (jobj.has(CommonJSONKey.MOBILE_NO_KEY)) {
            	mobileNumber = jobj.getString(CommonJSONKey.MOBILE_NO_KEY); 
            }
            
            if (email == null && mobileCountry == null) {
            	return NOT_OK_RESULT;
            } else {
            	userManager.sendTestHtmlEmailSms(name, email, mobileCountry, mobileNumber);
            }

        } catch (CoreException e) {
            return e.getMessageAsJSON().toString();
        } catch (JSONException je) {
            throw new ConflictException(je.getMessage());
        }
        return OK_STATUS;
    }
}
