package com.secqme.sns;

import com.secqme.CoreException;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.UserVO;
import com.secqme.manager.UserManager;
import com.secqme.util.ServletUtil;
import com.secqme.util.spring.BeanType;
import com.secqme.util.spring.DefaultSpringUtil;
import com.secqme.web.jsf.mbean.LoginBean;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;

/**
 *
 * @author jameskhoo
 */
public class TwitterServlet extends HttpServlet {

    private static Logger myLog = Logger.getLogger(TwitterServlet.class);
    private UserManager userManager;
    private TwitterUtil twUtil;
    private static final String AUTH_PATH = "/auth";
    private static final String CALLBACK_PATH = "/callback";

    public TwitterServlet() {
        userManager = (UserManager) DefaultSpringUtil.getInstance().getBean(BeanType.userManager);
        twUtil = (TwitterUtil) DefaultSpringUtil.getInstance().getBean(BeanType.twitterUtil);
    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        ServletUtil.printRequestInfo(request);

        String reqPath = request.getPathInfo();
        if (reqPath != null) {
            if (reqPath.startsWith(AUTH_PATH)) {
                processAuthRequest(request, response);
            } else if (reqPath.startsWith(CALLBACK_PATH)) {
                processCallBack(request, response);
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
        return "Twitter Servlet";
    }

    private void processAuthRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Twitter twitter = twUtil.getTwitter();
        myLog.debug("Performing Twitter Auth Request..");
        String userid = request.getParameter("userid");
        try {
            RequestToken reqToken = twitter.getOAuthRequestToken();
            String authURL = reqToken.getAuthorizationURL();
            myLog.debug("AuthURL generated is ->" + authURL);
            if (userid != null) {
                try {
                    UserVO userVO = userManager.getUserInfoByUserId(userid);
                    myLog.debug("Strong user:" + userVO.getUserid() + " into session");
                    request.getSession().setAttribute("userVO", userVO);
                } catch (CoreException ex) {
                    myLog.error(ex.getMessage(), ex);
                }
            }
            request.getSession().setAttribute("twRequestToken", reqToken);
            if (authURL != null) {
                response.sendRedirect(authURL);
            }
        } catch (TwitterException ex) {
            myLog.error("Twitter error", ex);
            response.sendRedirect(ServletUtil.getSNSAuthFinishRedirectURL(request));
        }
    }

    private void processCallBack(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Twitter twitter = twUtil.getTwitter();
        Boolean redirectToMainPortal = false;
        RequestToken requestToken = (RequestToken) request.getSession().getAttribute("twRequestToken");
        String verifier = request.getParameter("oauth_token");
        try {
            LoginBean loginBean = (LoginBean) request.getSession().getAttribute("loginBean");
            UserVO userVO = null;
            if (loginBean != null) {
                userVO = loginBean.getLoginUserVO();
                redirectToMainPortal = true;
                myLog.debug("Getting UserVO from LoginBean, userid->" + userVO.getUserid());

            } else {
                userVO = (UserVO) request.getSession().getAttribute("userVO");
                myLog.debug("Getting UserVO from Session, userid->" + userVO.getUserid());
            }

            if (verifier != null) {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken.getToken(), verifier);
                myLog.debug(accessToken.getScreenName() + "," + accessToken.getToken() + "," + accessToken.getTokenSecret());

                if (userVO != null) {
                    myLog.debug("Adding SNS Config:" + twUtil.getTWSNSVO().getSnsName() + " for user:" + userVO.getUserid());
                    UserSNSConfigVO userSNSConfigVO = new UserSNSConfigVO();
                    userSNSConfigVO.setSocialNetworkVO(twUtil.getTWSNSVO());
                    userSNSConfigVO.setUserVO(userVO);

                    JSONObject additionalConfig = new JSONObject();
                    additionalConfig.put(TwitterUtil.TW_SCREEN_NAME_KEY, accessToken.getScreenName());
                    additionalConfig.put(TwitterUtil.TW_TOKEN_KEY, accessToken.getToken());
                    additionalConfig.put(TwitterUtil.TW_TOKEN_SECRET_KEY, accessToken.getTokenSecret());
                    userSNSConfigVO.setAdditionalConfig(additionalConfig);

                    userSNSConfigVO.setSnsuid("" + accessToken.getUserId());
                    userSNSConfigVO.setNotify(true);
                    userManager.addReplaceSNSConfig(userVO, userSNSConfigVO);

                    if (loginBean != null) {
                        loginBean.refreshLoginUserVO();
                    }
                }
            }
        } catch (TwitterException e) {
            throw new ServletException(e);
        } catch (JSONException ex) {
            myLog.error("JSONException", ex);
        } catch (CoreException ex) {
            myLog.error(ex.getMessage(), ex);
        }
        if (redirectToMainPortal) {
            response.sendRedirect(ServletUtil.getSNSAuthFinishRedirectURL(request));
        } else {
            response.sendRedirect("http://secq.me/mobile_thanks.html");
        }
    }
}
