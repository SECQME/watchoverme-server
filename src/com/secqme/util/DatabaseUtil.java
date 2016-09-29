package com.secqme.util;

import org.codehaus.jettison.json.JSONArray;

/**
 *
 * @author coolboykl
 */
public interface DatabaseUtil {
    
    public JSONArray executeStatement(String sqlStatement);
    
}
