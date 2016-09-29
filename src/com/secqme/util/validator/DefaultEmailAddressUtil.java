package com.secqme.util.validator;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.web.jsf.validator.EmailValidator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by edward on 10/08/2015.
 */
public class DefaultEmailAddressUtil implements EmailAddressUtil {

    private Pattern pattern;
    private Matcher matcher;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public DefaultEmailAddressUtil() {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    @Override
    public boolean isValidEmailAddress(String emailAddress) {
        matcher = pattern.matcher(emailAddress);
        return matcher.matches();
    }

    @Override
    public String validateEmailAddress(String emailAddress, String langCode) throws CoreException {
        if (isValidEmailAddress(emailAddress)) {
            return emailAddress;
        }
        throw new CoreException(ErrorType.USER_EMAIL_ADDRESS_INVALID, "", langCode, emailAddress);
    }
}
