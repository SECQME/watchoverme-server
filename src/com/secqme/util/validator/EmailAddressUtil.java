package com.secqme.util.validator;

import com.secqme.CoreException;

/**
 * Created by edward on 10/08/2015.
 */
public interface EmailAddressUtil {
    public boolean isValidEmailAddress(String emailAddress);
    public String validateEmailAddress(String emailAddress, String langCode) throws CoreException;
}
