package com.secqme.util.system;

import com.secqme.CoreException;
import org.codehaus.jettison.json.JSONArray;

/**
 *
 * @author coolboykl
 */
public interface SysAdminUtil {
    public void expireAllSystemCache();
    @Deprecated // Not used
    public void testPushMessage(String adminName, String adminPassword, String userid, String message);
    public String getSystemAdminName();
    public boolean isValidClientId(String clientId);
    public boolean isValidClientAccess(String clientId, String clientSecret);
    public void grantClientAccess(String clientId, String clientSecret) throws CoreException;
    public boolean verifyAdminLogin(String adminName, String adminPassword);
    public JSONArray executeSQLQuery(String sqlStatement);
    public String getWomWebSiteURL();
    public String getAppleAppStoreURL();
    public String getAndroidAPPStoreURL();
}
