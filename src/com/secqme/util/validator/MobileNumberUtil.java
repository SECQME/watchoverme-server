package com.secqme.util.validator;

/**
 * User: James Khoo
 * Date: 10/8/13
 * Time: 5:19 PM
 */
public interface MobileNumberUtil {
    public Boolean isValidMobileNumber(String mobileNumber, String countryISO);
    public String checkAndReturnCorrectMobileNumber(String mobileNumber, int countryCallingCode);
    public String checkAndReturnCorrectMobileNumber(String mobileNumber, String countryISO);
}
