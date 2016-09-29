package com.secqme.rs.v2;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.referral.ReferralClickType;
import com.secqme.domain.model.referral.ReferralLogVO;
import com.secqme.rs.BaseResource;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.Enumeration;

/**
 * User: James Khoo
 * Date: 2/10/14
 * Time: 11:43 AM
 */
@Path("/v2/referral")
public class ReferralResource extends BaseResource {
    private final static Logger myLog = Logger.getLogger(ReferralResource.class);
    private static String USER_AGENT_PARAM_KEY = "user-agent";
    private static String ACCEPT_LANGAGUE_KEY = "accept-language";
    private static String ACCEPT_PARAM_KEY = "accept";
    private static String SESSION_ID_PARAM_KEY = "sessionID";
    private static String REQUEST_IP_KEY = "requestIP";
    private static String USER_REFERRAL_CODE_PARAM_KEY = "code";
    private static String STAGE_PARAM_KEY = "stage";
    private static String REFERRAL_USER_TOKEN_KEY = "reftoken";
    private static String USER_TOKEN_KEY = "usertoken";
    private static String IPHONE_AGENT_PREFIX = "iPhone";
    private static String IPAD_AGENT_PREFIX = "iPad";
    private static String IPOD_AGENT_PREFIX = "iPod";
    private static String ANDROID_AGENT_PREFIX = "Android";
    private static String NO_REF_CODE = "NONE";

    public ReferralResource() {
        // Empty Constructor
    }

    @Path("/{userAuthToken}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserReferralInfo(@PathParam("userAuthToken") String userToken) {
        String resultStr;
        try {
            UserVO userVO = userManager.getUserByActivationCode(userToken);
            resultStr =  referralManager.getReferralInfoVO(userVO).toJSONObject().toString();
        } catch (CoreException ce) {
            return ce.getMessageAsJSON().toString();
        }
        return resultStr;
    }

    @GET
    public String referralClick(@Context HttpServletRequest httpReq, @Context HttpServletResponse httpResp) {
        String result = OK_STATUS;

        // Three Stages
        // 1. {{ServerBaseURL}}/v2/referral?code=USER_AUTO_TOKEN, means user's friend click on referral URL, try to capture the FingerPrint.
        // 2. {{ServerBaseURL}}/v2/referral?stage=INSTALL&timestamp=234234324, means user's have click on install, map the referralURL
        //
        ReferralClickType referralClickType = ReferralClickType.CLICK;
        String userAgentStr;
        String sessionID;
        String requestIP;
        String acceptLanguageStr;
        String userAuthToken;
        String stageStr;
        String finalRedirectURL = sysAdminUtil.getWomWebSiteURL();
        StringBuilder fullURLStr = new StringBuilder();
        UserVO refUserVO = null;

        try {
            userAgentStr = httpReq.getHeader(USER_AGENT_PARAM_KEY);
            acceptLanguageStr = httpReq.getHeader(ACCEPT_LANGAGUE_KEY);
            fullURLStr.append(httpReq.getRequestURL().toString());
            sessionID = httpReq.getRequestedSessionId();
            requestIP = httpReq.getRemoteAddr();
            JSONObject fingerPrintObj = new JSONObject();
            fingerPrintObj.put(REQUEST_IP_KEY, requestIP);
            if (sessionID != null) {
                fingerPrintObj.put(SESSION_ID_PARAM_KEY, sessionID);
            }


            Enumeration<String> hearderNameEnum = httpReq.getHeaderNames();
            while (hearderNameEnum.hasMoreElements()) {
                String headerName = hearderNameEnum.nextElement();
                fingerPrintObj.put(headerName, httpReq.getHeader(headerName));
            }
            myLog.debug("FingerPrint->" + fingerPrintObj.toString());


            Enumeration<String> parameterNames = httpReq.getParameterNames();
            if (parameterNames != null && parameterNames.hasMoreElements()) {
                fullURLStr.append("?");
                while (parameterNames.hasMoreElements()) {
                    String paraName = parameterNames.nextElement();
                    fullURLStr.append(paraName + "=" + httpReq.getParameter(paraName) + "&");
                }
                fullURLStr.deleteCharAt(fullURLStr.length() - 1);
            }
            stageStr = httpReq.getParameter(STAGE_PARAM_KEY);
            if (stageStr != null) {
                referralClickType = ReferralClickType.valueOf(stageStr);
            }

            String refCode = httpReq.getParameter(USER_REFERRAL_CODE_PARAM_KEY);
            if (refCode != null) {
                refUserVO = userManager.getUserByActivationCode(refCode);
            }
            // Record down the

            switch (referralClickType) {
                case CLICK:
                    referralManager.insertReferralLogs(fullURLStr.toString(), referralClickType, requestIP,
                            refUserVO, userAgentStr, acceptLanguageStr, sessionID, fingerPrintObj.toString());
                    myLog.debug("Handling Click Transaction, fullURL->" + fullURLStr);

                    if (userAgentStr.contains(IPHONE_AGENT_PREFIX) || userAgentStr.contains(IPAD_AGENT_PREFIX) || userAgentStr.contains(IPOD_AGENT_PREFIX)) {
                        finalRedirectURL = sysAdminUtil.getAppleAppStoreURL();
                    } else if (userAgentStr.contains(ANDROID_AGENT_PREFIX)) {
                        finalRedirectURL = sysAdminUtil.getAndroidAPPStoreURL();
                    }
                    httpResp.sendRedirect(finalRedirectURL);
                    break;
                case INSTALL:
                    // 2. {{ServerBaseURL}}/v2/referral?stage=INSTALL&timestamp=234234324, means user's have click on install, map the referralURL
                    myLog.debug("Handling Install Transaction, fullURL->" + fullURLStr);
                    ReferralLogVO refLogVO = referralManager.findReferralLogMatchWithFingerPrint(ReferralClickType.CLICK,
                            requestIP, fingerPrintObj.toString(), 60);
                    JSONObject resultObj = new JSONObject();
                    if (refLogVO != null) {
                        resultObj.put(REFERRAL_USER_TOKEN_KEY, refLogVO.getRefUserVO().getActivationCode());
                        referralManager.insertReferralLogs(fullURLStr.toString(), referralClickType, requestIP,
                                refLogVO.getRefUserVO(), userAgentStr, acceptLanguageStr, sessionID, fingerPrintObj.toString());
                    } else {
                        resultObj.put(REFERRAL_USER_TOKEN_KEY, NO_REF_CODE);
                    }
                    result = resultObj.toString();
                    break;
                case REGISTER:
//                    3. {{ServerBaseURL}}/v2/referral?stage=REGISTER&reftoken=4234234&usertoken=34234234_II
                    myLog.debug("Handling Register Transaction, fullURL->" + fullURLStr);
                    String refToken = httpReq.getParameter(REFERRAL_USER_TOKEN_KEY);
                    String userToken = httpReq.getParameter(USER_TOKEN_KEY);
                    UserVO orgUser = userManager.getUserByActivationCode(refToken);
                    UserVO refUser = userManager.getUserByActivationCode(userToken);
                    referralManager.insertReferralLogs(fullURLStr.toString(), referralClickType, requestIP,
                            orgUser, userAgentStr, acceptLanguageStr, sessionID, fingerPrintObj.toString());
                    referralManager.rewardUser(orgUser, refUser);
                    // Reward the users accordingly;
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (JSONException je) {
            je.printStackTrace();
        } catch (CoreException ce) {
            ce.printStackTrace();
            return ce.getMessageAsJSON().toString();
        }

        return result;
    }

    @Path("/report/{userToken}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getUserReferralReport(@PathParam("authToken") String userToken) {
        String resultStr = null;
        myLog.debug("Attempt to get user's Referral Report" + userToken);
        try {
            JSONObject resultObject = new JSONObject();

            UserVO userVO = userManager.getUserByActivationCode(userToken);
            if (userVO.getSnsConfigList() != null && userVO.getSnsConfigList().size() > 0) {
            }

        } catch (CoreException ce) {
            return ce.getMessageAsJSON().toString();
        }

        return resultStr;

    }


}
