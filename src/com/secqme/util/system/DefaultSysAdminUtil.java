package com.secqme.util.system;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.manager.UserManager;
import com.secqme.util.DatabaseUtil;
import com.secqme.util.ar.ARTemplateEngine;
import com.secqme.util.cache.CacheUtil;
import com.secqme.util.notification.push.PushService;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONArray;

/**
 *
 * @author coolboykl
 */
public class DefaultSysAdminUtil implements SysAdminUtil {
    
    private static final Logger myLog = Logger.getLogger(DefaultSysAdminUtil.class);
    
    private CacheUtil cacheUtil = null;
    private static String adminUserName;
    private static String adminUserPassword;
    private UserManager userManager = null;
    private PushService pushService = null;
    private DatabaseUtil dbUtil = null;
    private ARTemplateEngine templateEngine = null;
    private String commonAppId = null;
    private String commonAppSecret = null;
    private String androidAPPStoreURL = null;
    private String appleAppStoreURL = null;
    private String womWebSiteURL = null;

    public DefaultSysAdminUtil(String adminName, String password,
                               String commonAppId, String commonAppSecret,
                               CacheUtil cacheUtil,
                               UserManager usrManager,
                               PushService pushService,
                               DatabaseUtil dbUtil,
                               ARTemplateEngine arTemplateEngine,
                               String androidAppURL,
                               String appleAppStoreURL,
                               String womWebSiteURL){
        adminUserName = adminName;
        adminUserPassword = password;
        this.commonAppId = commonAppId;
        this.commonAppSecret = commonAppSecret;
        this.cacheUtil = cacheUtil;
        this.userManager = usrManager;
        this.pushService = pushService;
        this.dbUtil = dbUtil;
        this.templateEngine = arTemplateEngine;
        this.androidAPPStoreURL = androidAppURL;
        this.appleAppStoreURL = appleAppStoreURL;
        this.womWebSiteURL = womWebSiteURL;
    }
    
     public void expireAllSystemCache() {
          myLog.debug("Expiring All System Cache");
          cacheUtil.expireAllCacheObject();
          templateEngine.refresh();
      }

    
    private boolean isValidSystemAdminUser(String adminName, String password) {
        return (adminUserName.equals(adminName) && adminUserPassword.equals(password));
    }

    public boolean isValidClientId(String clientId) {
        return clientId.equalsIgnoreCase(commonAppId);
    }

    public boolean isValidClientAccess(String clientId, String clientSecret) {
        return clientId.equalsIgnoreCase(commonAppId) && clientSecret.equalsIgnoreCase(commonAppSecret);
    }

    public void grantClientAccess(String clientId, String clientSecret) throws CoreException {
        if(!isValidClientAccess(clientId, clientSecret)) {
            throw new CoreException(ErrorType.CLIENT_APPLICATION_AUTHENTICATION_ERROR, clientId, clientSecret);
        }
    }


    public void testPushMessage(String adminName, String adminPassword, String userid, String message) {
        throw new UnsupportedOperationException("");

//        myLog.debug("Test Push Message for:" + userid + ",message:" + message);
//        if(isValidSystemAdminUser(adminName, adminPassword)) {
//            try {
//                UserVO userVO = userManager.getUserInfoByUserId(userid);
//                // PARSE_PUSH_SERVICE.pushMessage(userVO, message);
//            } catch (CoreException ex) {
//                ex.printStackTrace();
//            }
//        }
    }
    
    public String getSystemAdminName() {
        return this.adminUserName;
    }
    
    public boolean verifyAdminLogin(String adminName, String adminPassword) {
        return this.isValidSystemAdminUser(adminName, adminPassword);
    }
    
    public JSONArray executeSQLQuery(String sqlStatement) {
        return dbUtil.executeStatement(sqlStatement);
    }

    public String getWomWebSiteURL() {
        return womWebSiteURL;
    }

    public void setWomWebSiteURL(String womWebSiteURL) {
        this.womWebSiteURL = womWebSiteURL;
    }

    public String getAppleAppStoreURL() {
        return appleAppStoreURL;
    }

    public void setAppleAppStoreURL(String appleAppStoreURL) {
        this.appleAppStoreURL = appleAppStoreURL;
    }

    public String getAndroidAPPStoreURL() {
        return androidAPPStoreURL;
    }

    public void setAndroidAPPStoreURL(String androidAPPStoreURL) {
        this.androidAPPStoreURL = androidAPPStoreURL;
    }
}
