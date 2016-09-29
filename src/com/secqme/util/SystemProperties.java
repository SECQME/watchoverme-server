
package com.secqme.util;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.log4j.Logger;

/**
 *
 * @author james
 */
public final class SystemProperties {
    public static final String PROP_EMAIL_BASE_URL = "email.base.url";
    public static final String PROP_FROM_EMAIL = "emailsvc.default.from.email";
    public static final String PROP_FROM_NAME = "emailsvc.default.from.name";
    public static final String PROP_REPLY_TO_EMAIL = "emailsvc.default.replyTo.email";

    private Logger myLog  = Logger.getLogger(SystemProperties.class);

    private static final String SYSYTEM_CONFIG_FILE = "SystemConfig.properties";
    private Configuration config = null;
    public static final SystemProperties instance = new SystemProperties();

    private SystemProperties() {
        try {
            config = new PropertiesConfiguration(SYSYTEM_CONFIG_FILE);
        } catch (ConfigurationException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }

    public String getString(String key) {
        return config.getString(key);
    }

    public Integer getInteger(String key) {
        return config.getInt(key);
    }


}
