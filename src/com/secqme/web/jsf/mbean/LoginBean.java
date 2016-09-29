package com.secqme.web.jsf.mbean;

import com.secqme.CoreException;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingCycleVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.manager.EventManager;
import com.secqme.manager.UserManager;
import com.secqme.manager.billing.BillingManager;
import com.secqme.util.system.SysAdminUtil;
import com.secqme.web.jsf.util.MessageController;
import com.secqme.web.jsf.util.NavigationRules;
import com.secqme.web.jsf.util.SessionKey;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.text.SimpleDateFormat;

/**
 *
 * @author james
 */
// @ManagedBean(name="loginBean")
// @SessionScoped
public class LoginBean implements Serializable {

    private final static Logger myLog = Logger.getLogger(LoginBean.class);
    private final static String COOKIE_USER_NAME_KEY = "cookie_UserName";
    private final static String COOKIE_USER_REMEMBER_ME = "cookie_rememberMe";
    private final static String SNS_FB_NAME = "facebook";
    private final static String SNS_TW_NAME = "twitter";
    private String userName;
    private String password;
    private UserVO loginUserVO;
    private BillingCycleVO billingCycleVO;
    private String eventCreditBalanceStr = "0";
    private String smsCreditBalanceStr = "0";
    private UserManager userManager;
    private BillingManager billingManager;
    private EventManager eventManager;
    private SysAdminUtil sysAdminUtil = null;
    private SecqMeEventVO latestEvent = null;
    private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm aaa");
    private UserSNSConfigVO fbConfigVO = null;
    private UserSNSConfigVO twConfigVO = null;
    private Boolean rememberMe = false;

//    public static final String LOGIN_SESSION_KEY="secq.me.session.key";
    public LoginBean() {
        checkCookies();
    }

    public String getLastLoginDate() {
        String lastLoginStr = null;
        if (loginUserVO != null && loginUserVO.getLastLoginDate() != null) {
            lastLoginStr = dateFormat.format(loginUserVO.getLastLoginDate());
        }

        return lastLoginStr;
    }

    public SysAdminUtil getSysAdminUtil() {
        return sysAdminUtil;
    }

    public void setSysAdminUtil(SysAdminUtil sysAdminUtil) {
        this.sysAdminUtil = sysAdminUtil;
    }
    

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Boolean getRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(Boolean rememberMe) {
        this.rememberMe = rememberMe;
    }

    

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setUserManager(UserManager usrMgr) {
        this.userManager = usrMgr;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    public BillingManager getBillingManager() {
        return billingManager;
    }

    public void setBillingManager(BillingManager billingManager) {
        this.billingManager = billingManager;
    }
    

    public void confirmSafety(ActionEvent actionEvent) {
        try {
            eventManager.confirmSafetyForUserLatestEvent(loginUserVO, true);
            refreshLoginUserVO();
        } catch (CoreException ex) {
        }
    }

    public SecqMeEventVO getLatestEvent() {
        return latestEvent;
    }

    public void setLatestEvent(SecqMeEventVO latestEvent) {
        this.latestEvent = latestEvent;
    }
    
    public Boolean getUserHasRunningEvent() {
        Boolean runningEvent = false;
        if(latestEvent != null) {
            runningEvent = latestEvent.isEventRunning();
        }
        return runningEvent;
    }
    

    private void retriveUserLatestEvent() {
         latestEvent =
                    eventManager.getUserLatestEvent(loginUserVO.getUserid());
    }

    public UserVO getLoginUserVO() {
        return loginUserVO;
    }

    public void refreshLoginUserVO() {
        try {
            loginUserVO = userManager.getUserInfoByUserId(loginUserVO.getUserid());
            populateUserSNSConfig();
            refreshBillingCycleInfo();
            retriveUserLatestEvent();
        } catch (CoreException ex) {
        }
    }

    public Boolean getNotifyToFB() {
        return (fbConfigVO == null || fbConfigVO.isNotify() == null) ? false : fbConfigVO.isNotify();
    }

    public void setNotifyToFB(Boolean notifyToFB) {
        if (fbConfigVO != null) {
            fbConfigVO.setNotify(notifyToFB);
        }
    }

    public Boolean getNotifyToTW() {
        return (twConfigVO == null || twConfigVO.isNotify() == null) ? false : twConfigVO.isNotify();
    }

    public void setNotifyToTW(Boolean notifyToTW) {
        if (twConfigVO != null) {
            twConfigVO.setNotify(notifyToTW);
        }
    }

    private void populateUserSNSConfig() {
        fbConfigVO = null;
        twConfigVO = null;
        for (UserSNSConfigVO configVO : loginUserVO.getSnsConfigList()) {
            if (configVO.getSnsName().startsWith(SNS_FB_NAME)) {
                fbConfigVO = configVO;
            } else if (configVO.getSnsName().startsWith(SNS_TW_NAME)) {
                twConfigVO = configVO;
            }
        }
    }

    public boolean isConnectWithFB() {
        return fbConfigVO == null ? false : true;
    }

    public void removeFacebook(ActionEvent avt) {
        try {
            userManager.removeSNSConfig(getLoginUserVO(), getFbConfigVO());
            refreshLoginUserVO();
        } catch (CoreException ex) {
            MessageController.addCoreExceptionError(null, ex);
        }

    }

    public void removeTwitter(ActionEvent avt) {
        try {
            userManager.removeSNSConfig(getLoginUserVO(), getTwConfigVO());
            refreshLoginUserVO();
        } catch (CoreException ex) {
            MessageController.addCoreExceptionError(null, ex);
        }

    }

    public boolean isUserSNSConfigEmpty() {
        boolean isEmpty = true;

        if (loginUserVO.getSnsConfigList() != null && loginUserVO.getSnsConfigList().size() > 0) {
            isEmpty = false;
        }

        return isEmpty;
    }

    public boolean isConnectWithTW() {
        return twConfigVO == null ? false : true;
    }


    public String logout() {
        myLog.debug("Logging out for user:" + loginUserVO.getUserid());
        ((HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false)).invalidate();
        return NavigationRules.LOGOUT.getNaviPath();
    }

    public UserSNSConfigVO getFbConfigVO() {
        return fbConfigVO;
    }

    public void setFbConfigVO(UserSNSConfigVO fbConfigVO) {
        this.fbConfigVO = fbConfigVO;
    }

    public UserSNSConfigVO getTwConfigVO() {
        return twConfigVO;
    }

    public void setTwConfigVO(UserSNSConfigVO twConfigVO) {
        this.twConfigVO = twConfigVO;
    }

    public void updateUserSNSConfig(ActionEvent event) {
        try {
            if (fbConfigVO != null) {
                userManager.addReplaceSNSConfig(loginUserVO, fbConfigVO);
            } else if (twConfigVO != null) {
                userManager.addReplaceSNSConfig(loginUserVO, twConfigVO);
            }
            MessageController.addInfo(null, "SNS Seting updated!", "User's SNS Setting Updated.");
        } catch (CoreException ce) {
            MessageController.addCoreExceptionError(null, ce);
        }
        refreshLoginUserVO();
    }

    public String getEventCreditBalanceStr() {
        return eventCreditBalanceStr;
    }


    public String getSmsCreditBalanceStr() {
        return smsCreditBalanceStr;
    }


    public String loginUser() {
        loginUserVO = null;
        myLog.debug("Attempt to login " + this.getUserName() + " into system");
        try {
            // JK check if user is login with system admin
             HttpSession httpSession =
                    (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);
            if(this.getUserName().equals(sysAdminUtil.getSystemAdminName()) &&
               sysAdminUtil.verifyAdminLogin(this.getUserName(), this.getPassword())) {
                
                httpSession.setAttribute(SessionKey.SYSTEM_ADMIN_USER_NAME.name(), this.getUserName());
                return NavigationRules.LOGIN_SYSTEM_ADMIN.getNaviPath();
            }
            
            loginUserVO = userManager.authenticateUserByUserId(this.getUserName(), this.getPassword());

            httpSession.setAttribute(SessionKey.LOGIN_USER.name(), loginUserVO);
            populateUserSNSConfig();
            refreshBillingCycleInfo();
            retriveUserLatestEvent();

            // If rememberMe is check,
            // store userName, password, and remember Me into Cookies
            if (rememberMe) {
                myLog.debug("Creating remember Me cookies for " + this.getUserName() + ", rememberMe->" + rememberMe);
                String rememberMeStr = rememberMe.toString();
                Cookie userNameCookie = new Cookie(COOKIE_USER_NAME_KEY, this.getUserName());
                Cookie rememberMeCookie = new Cookie(COOKIE_USER_REMEMBER_ME, rememberMeStr);

                userNameCookie.setMaxAge(3600);
                HttpServletResponse resp = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
                resp.addCookie(userNameCookie);
                resp.addCookie(rememberMeCookie);

            }

            // ((HttpServletResponse)FacesContext.getCurrentInstance().getExternalContext().getResponse()).addCookie(new Cookie(LOGIN_SESSION_KEY, loginUserVO.getActivationCode()));
        } catch (CoreException ex) {
            myLog.error("Login Fail, Exception is ->" + ex.getMessage());
            MessageController.addCoreExceptionError(null, ex);
            this.password = null;
            this.loginUserVO = null;
            return NavigationRules.LOGIN_FAILURE.getNaviPath();
        }

        return NavigationRules.LOGIN_SUCCESS.getNaviPath();
    }

    private UserSNSConfigVO getUserSNSConfig(String snsName) {
        UserSNSConfigVO configVO = null;
        if (loginUserVO.getSnsConfigList() != null) {
            for (UserSNSConfigVO tmpSNSConfigVO : loginUserVO.getSnsConfigList()) {
                if (tmpSNSConfigVO.getSnsName().startsWith(snsName)) {
                    configVO = tmpSNSConfigVO;
                    System.out.println("ConfigVO->" + configVO.getSnsName() + configVO.isNotify());
                    break;
                }
            }
        }
        return configVO;
    }

    private void checkCookies() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        String cookieName = null;
        Cookie cookie[] = ((HttpServletRequest) facesContext.getExternalContext().
                getRequest()).getCookies();
        if (cookie != null && cookie.length > 0) {
            for (int i = 0; i < cookie.length; i++) {
                cookieName = cookie[i].getName();
                if (cookieName.equals(COOKIE_USER_NAME_KEY)) {
                    myLog.debug("Cookie user name found->" + cookie[i].getValue());
                    this.userName = cookie[i].getValue();
                } else if (cookieName.equals(COOKIE_USER_REMEMBER_ME)) {
                    rememberMe = Boolean.valueOf(cookie[i].getValue());
                }
            }
        }
    }

    private void refreshBillingCycleInfo() {
        billingCycleVO = billingManager.getUserLatestBillCycleVO(loginUserVO);
        eventCreditBalanceStr = billingCycleVO.getEventCreditBalanceText();
        smsCreditBalanceStr = billingCycleVO.getSMSCreditBalanceText();
    }
}
