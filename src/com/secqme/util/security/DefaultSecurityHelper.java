package com.secqme.util.security;

import org.jasypt.util.password.BasicPasswordEncryptor;

/**
 *
 * @author james
 */
public class DefaultSecurityHelper implements SecurityHelper {

    BasicPasswordEncryptor passwordEncryptor = null;

    public DefaultSecurityHelper() {
        passwordEncryptor = new BasicPasswordEncryptor();
    }

    public String encryptPassword(String plainPassword) {
        return passwordEncryptor.encryptPassword(plainPassword);
    }

    public Boolean isPasswordMatch(String encryptedPassword, String plainPassword) {
        return passwordEncryptor.checkPassword(plainPassword, encryptedPassword);
    }

}
