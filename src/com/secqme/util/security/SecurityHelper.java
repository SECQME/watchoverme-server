package com.secqme.util.security;

/**
 *
 * @author james
 */
public interface SecurityHelper {

    public String encryptPassword(String plainPassword);
    public Boolean isPasswordMatch(String encryptedPassword, String plainPassword);
    
}
