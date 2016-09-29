package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.domain.model.ContactInvitationStatus;
import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.email.EmailLogVO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.util.marketing.MarketingCampaign;
import org.codehaus.jettison.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author james
 */
public interface UserManager {

    public final static String USER_DEFAULT_LANGUAGE = "en_US";

    @Deprecated
    public String getUserIdFromMobileNumber(String mobileCountry, String mobileNumber) throws CoreException;

    public UserVO getUserInfoByUserId(String userid) throws CoreException;
    public UserVO getUserByActivationCode(String activationCode) throws CoreException;
    public UserVO getUserByMobileNumber(String mobileCountryISO, String mobileNumber);
    public UserVO getUserByEmailAddress(String emailAddress);
    public UserVO getUserByMobileNumberOrEmailAddress(CountryVO mobileCountryVO, String mobileNumber, String emailAddress);

    @Deprecated
    public UserVO registerTemporaryUser(String name,
                                        String countryISO, String mobileNumber,
                                        String langCode, String deviceCode,
                                        Double latitude, Double longitude) throws CoreException;
    public UserVO activateTemporaryUser(String authToken, String countryISO, String mobileNumber,
                                      String name, String emailAddress, String password) throws CoreException;
    public UserVO registerUser(String name, String password, String emailAddress,
                               String mobileCountryIso, String mobileNumber,
                               String langCode, String deviceCode,
                               Double latitude, Double longitude,
                               JSONObject snsConfigObj, double clientVersion, String pictureStream) throws CoreException;
    public UserVO registerUser(UserSNSConfigVO userSNSConfigVO, String deviceOS);

    public String generateMobileVerificationPin(String countryISO, String mobileNumber, String langCode) throws CoreException;
    public void sendEmailVerification(UserVO newUserVO) throws CoreException;
    public void sendEmailChangedVerification(UserVO userVO) throws CoreException;

    public UserVO authenticateUser(String username, String plainPassword) throws CoreException;
    public UserVO authenticateUserByMobileNumber(String userMobileCountryISO, String mobileNumber, String plainPassword) throws CoreException;
    public UserVO authenticateUserByEmailAddress(String emailAddress, String plainPassword) throws CoreException;
    public UserVO authenticateUserByActivationCode(String activationCode) throws CoreException;
    public UserVO authenticateUserBySNS(String providerId, String providerUserId) throws CoreException;

    @Deprecated
    public UserVO authenticateUserByUserId(String userId, String plainPassword) throws CoreException;

    public void verifyEmailAddress(String emailVerificationToken);

    public void trimNameAndMobileNo(UserVO userVO) throws CoreException;

    // SNS Related
    public void addReplaceSNSConfig(UserVO userVO, UserSNSConfigVO userSNSConfigVO) throws CoreException;
    public void removeSNSConfig(UserVO userVO, String providerId);
    public void removeSNSConfig(UserVO userVO, UserSNSConfigVO snsConfigRemove) throws CoreException;

    public void updatePasswordWithResetPin(UserVO userVO, String resetPin, String newPassword) throws CoreException;
    public void updatePassword(UserVO userVO, String currentPassword, String newPassword) throws CoreException;

    // for version 1 API
    public void resetPasswordWithRandomString(UserVO userVO) throws CoreException;

    // for version 2 API
    public String forgotPasswordUsingMobilePin(String userMobileCountryISO, String mobileNumber, String emailAddress) throws CoreException;

    public void forgotPasswordUsingToken(UserVO userVO);
    public boolean isValidResetPasswordToken(String passwordResetToken);
    public void resetPasswordUsingToken(String passwordResetToken, String newPassword) throws CoreException;

    // Mobile Native Push Message Related
    public UserVO updateUserPushMessageToken(UserVO userVO, String mobilePlatformStr, String pushMessageToken) throws CoreException;
    
    // Get user sns record by snsuid and snsname
    public UserSNSConfigVO getUserSnsConfigVOBySnsuidAndSnsName(String snsuid, String snsName) throws CoreException;

    public void checkForClientVersionUpgrade(UserVO userVO, double clientVersion);

    public void updateEmailAddress(UserVO userVO, String emailAddress) throws CoreException;
    public void updateLocation(UserVO userVO, double latitude, double longitude, boolean resolveCountry);
    public void updateMobileNumber(UserVO userVO, String mobileCountryISO, String mobileNumber) throws CoreException;
    public void updateName(UserVO userVO, String userName);
    public void updateProfilePicture(UserVO userVO, String encodedPicStr);
    public void markWalkyUser(UserVO userVO);
    
    public CountryVO validateUserMobileNumber(String mobileCountryISO, String mobileNumber, String langCode) throws CoreException;

    // network contact first time login, check if this user was being added by any other users
    public void checkContactAddedList(UserVO userVO) throws CoreException;
    
    public String getServerParam(String keyValue) throws CoreException;

    @Deprecated
    public void sendNotificationToContacts(UserVO userVO) throws CoreException;

    public void updateUser(UserVO userVO) throws CoreException;
    
    // get contact added by other user list
    public List<ContactVO> getContactAddedByOtherUserList(UserVO userVO) throws CoreException;
    
    // push message to parse
    @Deprecated
    public void pushToParse(UserVO userVO, String message, String token) throws CoreException;
    
    // update all users mobileNo to standard format from database
    public void updateUsersMobileNo();
    
    // update all users mobileNo to standard format from database
    public void updateContactsMobileNo();
    
    // test html email template, sms template
    public void sendTestHtmlEmailSms(String name, String email, String mobileCountry, String mobileNumber);
    
    // resend invitation to contact that don have the app installed
    public void resendNotificationToContact(UserVO userVO, List<Long> contactIdList) throws CoreException;
    
    // add user to marketing email
    public void addMarketingEmailContact(UserVO userVO, MarketingCampaign... campaigns);
    public void deleteMarketingEmailContact(UserVO userVO, MarketingCampaign... campaigns);

    public void unsubscribeMarketingEmailIfNeeded(@NotNull EmailLogVO emailLogVO);
    public void unsubscribeMarketingEmail(String email, String subscribeToken);
    public void unsubscribeMarketingEmail(String email);

    public String getAnalyticsId(String authToken, String device);
    public void generateAnalyticsId(UserVO userVO, String os);

}
