package com.secqme.util.validator;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.apache.log4j.Logger;

/**
 * User: James Khoo
 * Date: 10/8/13
 * Time: 5:20 PM
 */
public class DefaultMobileNumberUtil implements MobileNumberUtil {
    private static final Logger myLog = Logger.getLogger(DefaultMobileNumberUtil.class);

    private static PhoneNumberUtil phoneNumberUtil = PhoneNumberUtil.getInstance();

    public DefaultMobileNumberUtil() {
        // Empty Constructor
    }


    public Boolean isValidMobileNumber(String mobileNumber, String countryISO) {
        Boolean isValidNumber = false;

        try {
            Phonenumber.PhoneNumber mNumber = phoneNumberUtil.parse(mobileNumber, countryISO);
            if ("MX".equalsIgnoreCase(countryISO)) { // Temporary hardcode for mexico due to libphonenumber issue
            	isValidNumber = phoneNumberUtil.isValidNumber(mNumber);
            } else {
            	isValidNumber = phoneNumberUtil.isValidNumber(mNumber) &&
                        ( PhoneNumberUtil.PhoneNumberType.MOBILE.equals(phoneNumberUtil.getNumberType(mNumber)) ||
                          PhoneNumberUtil.PhoneNumberType.FIXED_LINE_OR_MOBILE.equals(phoneNumberUtil.getNumberType(mNumber))
                        );
            }
            
        } catch (NumberParseException ex) {
            myLog.debug(ex.getMessage(), ex);
        }

        return isValidNumber;
    }
    
    public String checkAndReturnCorrectMobileNumber(String mobileNumber, int countryCallingCode) {
    	String updatedMobileNumber = mobileNumber;
    	try {
    		String countryISO = phoneNumberUtil.getRegionCodeForCountryCode(countryCallingCode);
            Phonenumber.PhoneNumber mNumber = phoneNumberUtil.parse(mobileNumber, countryISO);
            updatedMobileNumber = String.valueOf(mNumber.getNationalNumber());
        } catch (NumberParseException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    	return updatedMobileNumber;
    }
    
    public String checkAndReturnCorrectMobileNumber(String mobileNumber, String countryISO) {
    	String updatedMobileNumber = mobileNumber;
    	try {
            Phonenumber.PhoneNumber mNumber = phoneNumberUtil.parse(mobileNumber, countryISO);
            updatedMobileNumber = String.valueOf(mNumber.getNationalNumber());
        } catch (NumberParseException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    	return updatedMobileNumber;
    }
}
