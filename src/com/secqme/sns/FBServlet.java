package com.secqme.sns;

import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.UserVO;
import com.secqme.manager.UserManager;
import com.secqme.util.ServletUtil;
import com.secqme.util.rest.RestUtil;
import com.secqme.util.spring.BeanType;
import com.secqme.util.spring.DefaultSpringUtil;
import com.secqme.web.jsf.mbean.LoginBean;
import com.visural.common.IOUtil;
import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 *
 * @author jameskhoo
 */
public class FBServlet extends HttpServlet {

    private RestUtil restUtil = null;
    private FacebookUtil fbUtil = null;
    private UserManager userManager = null;
    private static final Logger myLog = Logger.getLogger(FBServlet.class);
    private static final String FB_AUTH_CODE_PARAM = "code";
    private static final String AUTH_PATH = "/auth";
    private static final String REMOVE_PATH = "/remove";
    private static final String CANVAS_PATH = "/canvas";

    public FBServlet() {
        restUtil = (RestUtil) DefaultSpringUtil.getInstance().getBean(BeanType.restUtil);
        fbUtil = (FacebookUtil) DefaultSpringUtil.getInstance().getBean(BeanType.faceBookUtil);
        userManager = (UserManager) DefaultSpringUtil.getInstance().getBean(BeanType.userManager);
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletUtil.printRequestInfo(req);

        String reqPath = req.getPathInfo();
        if (reqPath != null) {
            if (reqPath.startsWith(AUTH_PATH)) {
                processFBAuth(req, resp);
            } else if (reqPath.startsWith(REMOVE_PATH)) {
                processRemoveFB(req, resp);
            } else if (reqPath.startsWith(CANVAS_PATH)) {
                processFBCanvas(req, resp);
            }
        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

    private void processFBAuth(HttpServletRequest req, HttpServletResponse resp) {

        String authCode = req.getParameter(FB_AUTH_CODE_PARAM);
        String encodedAuthCode = URLEncoder.encode(authCode);
        myLog.debug("AuthCode->" + authCode + ", encoded AuthCode->" + encodedAuthCode);
        try {
            String fbAuthURLStr = fbUtil.getAuthURL(encodedAuthCode);
            myLog.debug("AuthUrl=" + fbAuthURLStr);
            String result = restUtil.executeGet(fbAuthURLStr, null);
            myLog.debug("result->" + result);
            String accessToken = null;
            String[] KeyValuePair = result.split("&");
            if (KeyValuePair.length != 2) {
                throw new RuntimeException("Unexpected auth response");
            } else {
                for (String kv : KeyValuePair) {
                    String[] tokens = kv.split("=");
                    if (tokens[0].equalsIgnoreCase("access_token")) {
                        accessToken = tokens[1];
                    }
                }
                //140865702623190|a30ab3eb8c354e5da45acb2c-100000976508107|_UkWXLYBeR-gfJvdun7Pnwj7opQ
            }
            if (accessToken != null) {
                myLog.debug("Access Token is ->" + accessToken);
                JSONObject userFBJSONObj = new JSONObject(IOUtil.urlToString(new URL("https://graph.facebook.com/me?access_token=" + accessToken)));

                LoginBean loginBean = (LoginBean) req.getSession().getAttribute("loginBean");
                if (loginBean != null) {
                    UserVO userVO = loginBean.getLoginUserVO();
                    myLog.debug("Adding SNS Config:" + fbUtil.getFbSNSVO().getSnsName() + " for user:" + userVO.getUserid());
                    UserSNSConfigVO userSNSConfigVO = new UserSNSConfigVO();
                    userSNSConfigVO.setSocialNetworkVO(fbUtil.getFbSNSVO());
                    userSNSConfigVO.setUserVO(userVO);
                    JSONObject additionalConfig = new JSONObject();
                    additionalConfig.put(fbUtil.FB_ACCESS_TOKEN_KEY, accessToken);
                    userSNSConfigVO.setAdditionalConfig(additionalConfig);
                    userSNSConfigVO.setSnsuid(userFBJSONObj.getString("id"));
                    userSNSConfigVO.setNotify(true);
                    userManager.addReplaceSNSConfig(userVO, userSNSConfigVO);
                    loginBean.refreshLoginUserVO();
                }

                resp.sendRedirect(ServletUtil.getSNSAuthFinishRedirectURL(req));

            } else {
                throw new RuntimeException("Access token and expires not found");
            }
        } catch (JSONException ex) {
            myLog.error("JSONException", ex);
        } catch (IOException ex) {
            myLog.error("IOException", ex);
        }
    }

    private void processRemoveFB(HttpServletRequest req, HttpServletResponse resp) {
        myLog.debug("Process Remove Facebook Acct");
        //   key is "fb_sig_user"
    }

    private void processFBCanvas(HttpServletRequest req, HttpServletResponse resp) {
        myLog.debug("Process FB Canvas");
    }

}
