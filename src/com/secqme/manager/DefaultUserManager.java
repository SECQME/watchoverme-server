package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.dao.UserDAO;
import com.secqme.domain.model.*;
import com.secqme.domain.model.billing.BillingPkgType;
import com.secqme.domain.model.billing.BillingPkgVO;
import com.secqme.domain.model.notification.MessageType;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.notification.email.EmailLogVO;
import com.secqme.domain.model.notification.email.EmailStatus;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.domain.model.payment.UserPaymentInfoVO;
import com.secqme.domain.model.pushmessage.UserPushMessageToken;
import com.secqme.domain.model.util.ServerParamVO;
import com.secqme.manager.billing.BillingManager;
import com.secqme.manager.payment.PaymentManager;
import com.secqme.manager.referral.ReferralManager;
import com.secqme.manager.worker.ShortURLWorker;
import com.secqme.util.MediaFileType;
import com.secqme.util.ShortURLUpdateService;
import com.secqme.util.marketing.MarketingCampaign;
import com.secqme.util.payment.PaymentHelper;
import com.secqme.util.shorturl.ShortURLException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * @author james
 */
public class DefaultUserManager extends BaseManager implements UserManager, ShortURLUpdateService, Serializable {

    private static final Logger myLog = Logger.getLogger(DefaultUserManager.class);
    private final static String defaultTimeZone = "Etc/GMT-8";
    public static final String MARKET_DEFAULT = "default";
    private final BillingManager billingManager;
    private final QuickEventManager quickEventManager;
    private final ReferralManager referralManager;
    private final ExecutorService threadPool;
    private final PaymentManager paymentManager;
    private final PaymentHelper paymentHelper;
    private static final String iOSUserDevice = "iOS";

    public DefaultUserManager(BillingManager billingMgr, QuickEventManager quickEventManager, ReferralManager refMgr,
    		PaymentManager paymentManager, PaymentHelper paymentHelper) {
        billingManager = billingMgr;
        this.quickEventManager = quickEventManager;
        this.referralManager = refMgr;
        threadPool = Executors.newCachedThreadPool();
        this.paymentManager = paymentManager;
        this.paymentHelper = paymentHelper;
    }

    public UserVO getUserInfoByUserId(String userid) throws CoreException {
        UserVO uVO = getUserDAO().read(userid);
        if (uVO == null) {
            throw new CoreException(ErrorType.USER_BYID_NOT_FOUND_ERROR, null, userid);
        }
        return uVO;
    }

    public void resetPasswordWithRandomString(UserVO userVO) throws CoreException {
        myLog.debug("Reseting user password:" + userVO.getUserid());
        String newPassword = this.getTextUtil().generateRandomString(8, 10);
        userVO.setPassword(getSecurityHelper().encryptPassword(newPassword));
        getUserDAO().update(userVO);
        getNotificationEngine().sendPasswordReset(userVO, newPassword);
    }

    public String forgotPasswordUsingMobilePin(String userMobileCountryISO, String mobileNumber, String emailAddress) throws CoreException {
        myLog.debug("Rest userPassword, mobileCountry->" + userMobileCountryISO +
                ", mobileNumber->" + mobileNumber + ", emailAddress->" + emailAddress);
        String passwordResetPin = null;
        UserVO userVO = null;
        boolean sendSMS = false;
        boolean sendEmail = false;
        if (userMobileCountryISO != null) {
        	CountryVO mobileCountryVO = super.validateUserMobileNumber(userMobileCountryISO, mobileNumber, null);
        	String updatedMobileNo = getMobileNumberUtil().checkAndReturnCorrectMobileNumber(
            		mobileNumber, Integer.parseInt(mobileCountryVO.getCallingCode()));
            userVO = getUserDAO().findByMobileNumberAndMobileCountry(userMobileCountryISO, updatedMobileNo);
            if (userVO == null) {
                throw new CoreException(ErrorType.USER_BYPHONE_NOT_FOUND_ERROR, USER_DEFAULT_LANGUAGE, mobileNumber);
            }
            sendSMS = true;
        }

        if (emailAddress != null) {
            userVO = this.getUserInfoByUserId(emailAddress);
            if (userVO == null) {
                throw new CoreException(ErrorType.USER_BYID_NOT_FOUND_ERROR, USER_DEFAULT_LANGUAGE, emailAddress);
            }
            sendEmail = true;
        }
        if (userVO != null) {
            passwordResetPin = RandomStringUtils.randomNumeric(4);
            userVO.setPasswordResetPin(passwordResetPin);
            userVO.setPasswordResetPinDate(new Date());
            getUserDAO().update(userVO);
            getNotificationEngine().sendPasswordResetPin(userVO, passwordResetPin, sendEmail, sendSMS);
        }

        return passwordResetPin;
    }

    @Override
    public void forgotPasswordUsingToken(UserVO userVO) {
        userVO.setPasswordResetToken(UUID.randomUUID().toString());
        userVO.setPasswordResetTokenDate(new Date());
        getUserDAO().update(userVO);
        getUserDAO().refresh(userVO);

        getNotificationEngine().sendPasswordResetTokenLink(userVO);
    }

    @Override
    public boolean isValidResetPasswordToken(String passwordResetToken) {
        myLog.debug("Validating password using token: " + passwordResetToken);

        UserVO userVO = getUserDAO().findByPasswordResetToken(passwordResetToken);
        if (userVO != null ) {
            Date effectiveDate = DateUtils.addHours(userVO.getPasswordResetTokenDate(), 24);
            if (effectiveDate.getTime() > new Date().getTime()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void resetPasswordUsingToken(String passwordResetToken, String newPassword) throws CoreException {
        myLog.debug("Reset password using token: " + passwordResetToken);

        UserVO userVO = getUserDAO().findByPasswordResetToken(passwordResetToken);
        if (userVO != null ) {
            Date effectiveDate = DateUtils.addHours(userVO.getPasswordResetTokenDate(), 24);
            if (effectiveDate.getTime() > new Date().getTime()) {
                userVO.setPassword(getSecurityHelper().encryptPassword(newPassword));
                userVO.setPasswordResetToken(null);
                userVO.setPasswordResetTokenDate(null);
                getUserDAO().update(userVO);
            } else {
                myLog.debug("Password reset token is already expired: " + passwordResetToken);
                throw new CoreException(ErrorType.USER_BY_PASSWORD_RESET_TOKEN_NOT_FOUND_ERROR, USER_DEFAULT_LANGUAGE, passwordResetToken);
            }
        } else {
            myLog.debug("User not found for password reset token: " + passwordResetToken);
            throw new CoreException(ErrorType.USER_BY_PASSWORD_RESET_TOKEN_NOT_FOUND_ERROR, USER_DEFAULT_LANGUAGE, passwordResetToken);
        }
    }

    @Override
    public UserVO registerTemporaryUser(String name,
                                        String countryISO, String mobileNumber,
                                        String langCode, String deviceCode,
                                        Double latitude, Double longitude) throws CoreException {
        myLog.debug("Creating temporary user->" + name + " country->" + countryISO + ", mobileNumber" + mobileNumber);
        UserVO newUser = prepareNewUserVO(name, null, null, countryISO, mobileNumber, langCode, deviceCode, latitude, longitude);

        ContactVO contactVO = new ContactVO();
        contactVO.setNickName(newUser.getNickName());
        contactVO.setCountryVO(newUser.getMobileCountry());
        contactVO.setRelationship("My Self");
        contactVO.setMobileNo(newUser.getMobileNo());
        contactVO.setUserVO(newUser);
        contactVO.setStatus(ContactInvitationStatus.SELFADDED);

        List<ContactVO> contactVOList = new ArrayList<ContactVO>();
        contactVOList.add(contactVO);
        newUser.setContactList(contactVOList);

        getUserDAO().create(newUser);
        //
        // For WOM version 3, activate LITE package
        //
        postCreateUser(newUser);
        myLog.debug("User:" + newUser.getUserid() + " created");

        return newUser;
    }

    public UserVO activateTemporaryUser(String authToken, String countryISO, String mobileNumber,
                                        String name, String emailAddress, String password) throws CoreException {
        UserVO userVO = getUserByActivationCode(authToken);
        if (userVO.getMobileCountry().getIso().equalsIgnoreCase(countryISO) && userVO.getMobileNo().equalsIgnoreCase(mobileNumber)) {
            userVO.setNickName(name);
            userVO.setEmailAddress(emailAddress);
            userVO.setPassword(getSecurityHelper().encryptPassword(password));
            userVO.setActivated(true);
            userVO.setContactList(null);
            getUserDAO().update(userVO);
//            getNotificationEngine().sendWelcomeEmail(userVO);
        } else {
            throw new CoreException(ErrorType.MOBILE_NUMBER_MISMATCH, userVO.getLangCode());
        }
        return userVO;
    }

    public String generateMobileVerificationPin(String countryISO, String mobileNumber, String langCode) throws
            CoreException {
    	CountryVO mobileCountryVO = super.validateUserMobileNumber(countryISO, mobileNumber, null);
    	String updatedMobileNo = getMobileNumberUtil().checkAndReturnCorrectMobileNumber(
        		mobileNumber, Integer.parseInt(mobileCountryVO.getCallingCode()));
        //first check if existing user exists
        UserVO userVO = getUserDAO().findByMobileNumberAndMobileCountry(countryISO, updatedMobileNo);
        if (userVO != null) {
            throw new CoreException(ErrorType.USER_ID_ALREADY_EXITS_ERROR,
                    userVO.getLangCode(),
                    getUserIdFromMobileNumber(countryISO, updatedMobileNo));
        }

        String randomPin = RandomStringUtils.randomNumeric(4);
        myLog.debug("Mobile verification Code for number:" + countryISO + "-" + updatedMobileNo + " is " + randomPin);
        String finalLangCode = getLocaleManager().verifyLanguageCode(langCode);
        validateUserMobileNumber(countryISO, updatedMobileNo, finalLangCode);
        getNotificationEngine().sendMobileVerificationSMS(MARKET_DEFAULT, finalLangCode, mobileCountryVO, updatedMobileNo, randomPin);

        return randomPin;
    }

    @Override
    public UserVO registerUser(String name, String password, String emailAddress,
                               String mobileCountryIso, String mobileNumber,
                               String langCode, String deviceCode,
                               Double latitude, Double longitude,
                               JSONObject snsConfigObj, double clientVersion, String pictureStream) throws CoreException {
        UserVO newUser = null;
        try {
            myLog.debug("Register new user->" + name + " country->" + mobileCountryIso + ", mobileNumber->" + mobileNumber +
            		", clientVersion->" + clientVersion);
            newUser = prepareNewUserVO(name, password, emailAddress, mobileCountryIso, mobileNumber, langCode, deviceCode, latitude, longitude);
            newUser.setClientVersion(clientVersion);
            if (pictureStream != null) { //set profile pic
            	newUser.setProfilePictureURL(
            			getMediaFileManager().saveUserProfilePicture(newUser, MediaFileType.PICTURE, pictureStream));
            }
            getUserDAO().create(newUser);

            // Generate ShortURL for Referral
            if (snsConfigObj != null) {
                UserSNSConfigVO snsConfigVO = new UserSNSConfigVO(snsConfigObj);
                snsConfigVO.setUserVO(newUser);
                if (snsConfigVO.getSnsName().startsWith("facebook")) {
                    snsConfigVO.setSocialNetworkVO(getFbUtil().getFbSNSVO());
                }
                addReplaceSNSConfig(newUser, snsConfigVO);
            }

            // For WOM version 3
            // Default Bill Package is LITE
            // 
            postCreateUser(newUser);
        } catch (JSONException ex) {
            myLog.error("JSON error", ex);
        }
        return newUser;
    }

    private UserVO prepareNewUserVO(String name, String password, String emailAddress,
                                    String mobileCountryIso, String mobileNumber,
                                    String langCode, String deviceCode,
                                    Double latitude, Double longitude) throws CoreException {
        UserVO newUser = new UserVO();

        if (emailAddress != null) {
            emailAddress = getEmailAddressUtil().validateEmailAddress(emailAddress, langCode);
            ensureUniqueEmailAddress(emailAddress);

            newUser.setUserIdType(UserIdType.EMAIL);
            newUser.setUserid(emailAddress.toLowerCase());
            newUser.setEmailAddress(emailAddress);
        }

        if (mobileCountryIso != null && mobileNumber != null) {
            CountryVO mobileCountryVO = validateUserMobileNumber(mobileCountryIso, mobileNumber, langCode);
            mobileNumber = getMobileNumberUtil().checkAndReturnCorrectMobileNumber(
                    mobileNumber, Integer.valueOf(mobileCountryVO.getCallingCode())); // Remove characters like space ( ) -
            ensureUniqueMobileNumber(mobileCountryIso, mobileNumber);

            newUser.setUserIdType(UserIdType.MOBILE);
            newUser.setUserid(mobileCountryVO.getCallingCode() + "-" + mobileNumber);
            newUser.setMobileNo(mobileNumber);
            newUser.setMobileCountry(mobileCountryVO);
        }

        // Check whether email address or mobile number is not supplied
        if (newUser.getUserid() == null) {
            throw new CoreException(ErrorType.USER_EMAIL_ADDRESS_INVALID, null);
        }

        billingManager.assignFreeMiumBillingPackage(newUser);
        newUser.setActivationCode(UUID.randomUUID().toString());
        newUser.setActivated(true);
        newUser.setCreatedDate(new Date());
        newUser.setUpdatedDate(new Date());
        newUser.setNickName(name);
        newUser.setDevice(deviceCode);
        newUser.setLangCode(getLocaleManager().verifyLanguageCode(langCode));
        newUser.setSubscribeEmailNewsLetter(true);
        newUser.setSubscribeEmailNewsLetterToken(UUID.randomUUID().toString());

        if (password != null) {
            newUser.setPassword(getSecurityHelper().encryptPassword(password));
        } else {
            newUser.setPassword(getSecurityHelper().encryptPassword(UUID.randomUUID().toString()));
        }

        if (latitude != null && longitude != null) {
            newUser.setTimeZone(getLocationUtil().getTimeZone(latitude, longitude));
            String userCountryCode = getLocationUtil().getCountryCodeByLocation(latitude, longitude);
            if (userCountryCode != null) {
                newUser.setCountryVO(getSmsManager().getCountry(userCountryCode));
            }
        }

        return newUser;
    }

    private void ensureUniqueMobileNumber(String mobileCountryCode, String mobileNumber) throws CoreException {
        if (mobileCountryCode == null || mobileNumber == null) return;

        UserVO checkUserVO = getUserDAO().findByMobileNumberAndMobileCountry(mobileCountryCode, mobileNumber);
        if (checkUserVO != null) {
        	throw new CoreException(ErrorType.USER_MOBILE_NO_ALREADY_EXITS_ERROR,
                    null,
                    getUserIdFromMobileNumber(mobileCountryCode, mobileNumber));
        }
    }

    private void ensureUniqueEmailAddress(String emailAddress) throws CoreException {
        if (emailAddress == null) return;

        UserVO checkUserVO = getUserDAO().findByEmail(emailAddress);
        if (checkUserVO != null) {
            throw new CoreException(ErrorType.USER_EMAIL_ALREADY_EXISTS_ERROR,
                    null,
                    emailAddress);
        }
    }

    private void postCreateUser(UserVO newUserVO) throws CoreException {
        myLog.debug("User:" + newUserVO.getUserid() + " created");

        applyGiftPackage(newUserVO);
        updateUserReferralURL(newUserVO);

        if (newUserVO.getDevice() != null) {
            generateAnalyticsId(newUserVO, newUserVO.getDevice());
        } else {
            myLog.error("No device is specified.");
        }

        // For WOM Safety Level version
        // add free sms credit to new user
        if (newUserVO.getClientVersion() == 6) {
            billingManager.addSMSCreditToUser(newUserVO, 7);
        }

        quickEventManager.populateSavedEventListForNewUser(newUserVO);

        getNotificationEngine().sendWelcomeEmail(newUserVO, BillingPkgType.LITE);

        // Only send email verification to user who sign up using email address only
        if (newUserVO.getEmailAddress() != null && newUserVO.getMobileNo() == null) {
            sendEmailVerification(newUserVO);
        }

        // We migrated from marketing funnel (GetResponse) to normal email service,
        // Remove the this block in the future
//        subscribeUserToBillingPackageMarketingCampaign(newUserVO);
    }

    @Override
    public UserVO registerUser(UserSNSConfigVO userSNSConfigVO, String deviceOS) {
        UserVO userVO = getSnsManager().prepareNewUserVO(userSNSConfigVO);

        String name = userVO.getNickName();
        String password = UUID.randomUUID().toString();
        String emailAddress = userVO.getEmailAddress();
        String mobileCountryIso = userVO.getCountryVO() == null ? null : userVO.getCountryVO().getIso();
        String mobileNumber = userVO.getMobileNo();
        String langCode = userVO.getLangCode();
        String deviceCode = deviceOS;
        Double latitude = userVO.getLatitude();
        Double longitude = userVO.getLongitude();

        double clientVersion = 6;
        userVO = registerUser(name, password, emailAddress, mobileCountryIso, mobileNumber, langCode, deviceCode, latitude, longitude, null, clientVersion, null);
        userSNSConfigVO.setUserVO(userVO);
        addReplaceSNSConfig(userVO, userSNSConfigVO);

        return userVO;
    }

    private void applyGiftPackage(UserVO newUser) {
        List<GiftPaymentLogVO> giftPackageList = paymentManager.getGiftPackageForUser(newUser);
        if(giftPackageList != null && giftPackageList.size() > 0) {
            int months = 0;
            BillingPkgVO billPkgVO = null;
            GiftPaymentLogVO mostRecentGiftPaymentLog = null;
            for(GiftPaymentLogVO giftPackage : giftPackageList) {
                mostRecentGiftPaymentLog = giftPackage;
                billPkgVO = giftPackage.getPaymentHistoryVO().getPricingPgkVO().getBillPkgVO();
//            		months += paymentHistoryVO.getPricingPgkVO().getQuantity();
                months = giftPackage.getPaymentHistoryVO().getPricingPgkVO().getQuantity();
                giftPackage.setRedeemed(true);
                paymentManager.updateGiftPaymentLog(giftPackage);
            }
            //TODO : provision multiple months of gift package.
            //Currently only provision one time for user that create account after paying multiple time.
            //This is to synchronize subscriptioninfo table
            billingManager.provisionBillingCycle(billPkgVO, newUser, months, false);
            newUser.setPackageVO(billPkgVO);
            getUserDAO().update(newUser);
            //add subscriptioninfo and userpaymentinfo
            myLog.debug("update subscriptioninfo for user:" + newUser.getUserid());
            paymentHelper.createReplaceSubscriptionRecord(newUser, mostRecentGiftPaymentLog.getPaymentHistoryVO().getPricingPgkVO(),
                    mostRecentGiftPaymentLog.getPaymentHistoryVO().getPaymentGWVO(),
                    mostRecentGiftPaymentLog.getPaymentHistoryVO().getPricingPgkVO().getPrice(),
                    new Date(), null, null, null, null);

            List<UserPaymentInfoVO> paymentInfoList = new ArrayList<UserPaymentInfoVO>();
            newUser.setUserPaymentInfoList(paymentInfoList);
            UserPaymentInfoVO userPaymentInfoVO = new UserPaymentInfoVO();
            userPaymentInfoVO.setUserVO(newUser);
            userPaymentInfoVO.setGatewayVO(mostRecentGiftPaymentLog.getPaymentHistoryVO().getPaymentGWVO());
            userPaymentInfoVO.setPaymentid(mostRecentGiftPaymentLog.getPaymentId());
            userPaymentInfoVO.setAdditionalConfig(mostRecentGiftPaymentLog.getAdditionalConfig());
            newUser.getUserPaymentInfoList().add(userPaymentInfoVO);
            getUserDAO().update(newUser);
        } else {
            billingManager.provisionBillingCycleForNewUser(newUser.getPackageVO(), newUser);
        }
    }

    private void subscribeUserToBillingPackageMarketingCampaign(UserVO newUser) {
        // Add user to the marketing email contact list
        if (newUser.getPackageVO().getPkgType() == BillingPkgType.PREMIUM) {
            addMarketingEmailContact(newUser, MarketingCampaign.PREMIUM_PACKAGE_CAMPAIGN, MarketingCampaign.GENERAL_CAMPAIGN);
        } else {
            addMarketingEmailContact(newUser, MarketingCampaign.LITE_PACKAGE_CAMPAIGN, MarketingCampaign.GENERAL_CAMPAIGN);
        }
    }

    @Override
    public void sendEmailVerification(UserVO userVO) throws CoreException {
        if (userVO.getEmailAddress() != null) {
            if (!userVO.isEmailVerified()) {
                userVO.setEmailVerificationToken(UUID.randomUUID().toString());
                userVO.setEmailVerificationDate(null);
                updateUser(userVO);
                getNotificationEngine().sendEmailVerification(userVO);
            } else {
                throw new CoreException(ErrorType.USER_EMAIL_ALREADY_VERIFIED_ERROR, userVO.getLangCode());
            }
        } else {
            throw new CoreException(ErrorType.USER_NO_EMAIL_ERROR, userVO.getLangCode());
        }
    }

    @Override
    public void sendEmailChangedVerification(UserVO userVO) throws CoreException {
        userVO.setEmailVerificationToken(UUID.randomUUID().toString());
        userVO.setEmailVerificationDate(null);
        updateUser(userVO);
        getNotificationEngine().sendEmailChangedVerification(userVO);
    }

    public UserVO getUserByActivationCode(String activationCode) throws CoreException {
        UserVO userVO = null;
        userVO = getUserDAO().findByActivationCode(activationCode);
        if (userVO == null) {
            throw new CoreException(ErrorType.USER_BY_ACVI_CODE_NOT_FOUND_ERROR, null, activationCode);
        }
        return userVO;
    }

    @Override
    public UserVO getUserByMobileNumber(String mobileCountryISO, String mobileNumber) throws CoreException {
    	String updatedMobileNo = getMobileNumberUtil().checkAndReturnCorrectMobileNumber(
        		mobileNumber, mobileCountryISO);
        return getUserDAO().findByMobileNumberAndMobileCountry(mobileCountryISO, updatedMobileNo);
    }

    @Override
    public UserVO getUserByEmailAddress(String emailAddress) {
        return getUserDAO().findByEmail(emailAddress);
    }

    @Override
    public UserVO getUserByMobileNumberOrEmailAddress(CountryVO mobileCountryVO, String mobileNumber, String emailAddress) {
        UserVO userVO = null;
        if (mobileCountryVO != null && mobileNumber != null) {
            userVO = getUserByMobileNumber(mobileCountryVO.getIso(), mobileNumber);
        }

        if (userVO == null && emailAddress != null) {
            userVO = getUserByEmailAddress(emailAddress);
        }

        return userVO;
    }

    @Override
    public UserVO authenticateUser(String username, String plainPassword) throws CoreException {
        // TODO: Authenticate a user based on email or mobile number
        // Match regex pattern email and then check using authenticateUserByEmailAddress
        // Match regex pattern mobileCountry-mobileNo and then check using  authenticateUserByMobileNumber

        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public UserVO authenticateUserByUserId(String userId, String plainPassword) throws CoreException {
        UserVO userVO = getUserDAO().read(userId);
        if (userVO == null) {
            throw new CoreException(ErrorType.USER_BYID_NOT_FOUND_ERROR,
                    USER_DEFAULT_LANGUAGE,
                    userId);
        }
        return authenticateUserPassword(userVO, plainPassword);
    }

    @Override
    public UserVO authenticateUserByMobileNumber(String userMobileCountryISO, String mobileNumber, String plainPassword) throws CoreException {
    	CountryVO mobileCountryVO = getSmsManager().getCountry(userMobileCountryISO);
    	String updatedMobileNo = getMobileNumberUtil().checkAndReturnCorrectMobileNumber(
        		mobileNumber, Integer.parseInt(mobileCountryVO.getCallingCode()));
        UserVO userVO = getUserDAO().findByMobileNumberAndMobileCountry(userMobileCountryISO, updatedMobileNo);
        if (userVO == null) {
            throw new CoreException(ErrorType.USER_BYPHONE_NOT_FOUND_ERROR, USER_DEFAULT_LANGUAGE, userMobileCountryISO, updatedMobileNo);
        }
        return authenticateUserPassword(userVO, plainPassword);
    }

    @Override
    public UserVO authenticateUserByEmailAddress(String emailAddress, String plainPassword) throws CoreException {
        UserVO userVO = getUserDAO().findByEmail(emailAddress);
        if (userVO == null) {
            throw new CoreException(ErrorType.USER_BYEMAIL_NOT_FOUND_ERROR, USER_DEFAULT_LANGUAGE, emailAddress);
        }
        return authenticateUserPassword(userVO, plainPassword);
    }

    private UserVO authenticateUserPassword(UserVO userVO, String plainPassword) {
        if (!getSecurityHelper().isPasswordMatch(userVO.getPassword(), plainPassword)) {
            throw new CoreException(ErrorType.USER_PASSWORD_MISMATCH_ERROR, userVO.getLangCode());
        }

        // Verify bill before login
        billingManager.verifyUserBillingPackage(userVO);
        userVO.setLastLoginDate(new Date());
        getUserDAO().update(userVO);

        return postLogin(userVO);
    }

    @Override
    public UserVO authenticateUserByActivationCode(String activationCode) throws CoreException {
        UserVO userVO = getUserByActivationCode(activationCode);
        billingManager.verifyUserBillingPackage(userVO);
        return postLogin(userVO);
    }

    @Override
    public UserVO authenticateUserBySNS(String providerId, String providerUserId) throws CoreException {
        UserVO userVO = getUserSnsConfigVOBySnsuidAndSnsName(providerUserId, providerId).getUserVO();
        billingManager.verifyUserBillingPackage(userVO);
        return postLogin(userVO);
    }

    @Override
    public void verifyEmailAddress(String emailVerificationToken) {
        UserVO userVO = getUserDAO().findByEmailVerificationToken(emailVerificationToken);
        if (userVO != null) {
            if (!userVO.isEmailVerified()) {
                userVO.setEmailVerificationDate(new Date());
                updateUser(userVO);
            } else {
                throw new CoreException(ErrorType.USER_EMAIL_ALREADY_VERIFIED_ERROR, userVO.getLangCode());
            }
        } else {
            myLog.debug("User not found for email verification reset token: " + emailVerificationToken);
            throw new CoreException(ErrorType.USER_BY_EMAIL_VERIFICATION_TOKEN_NOT_FOUND_ERROR, USER_DEFAULT_LANGUAGE, emailVerificationToken);
        }
    }


    private UserVO postLogin(UserVO userVO) throws CoreException {
        userVO.setLastLoginDate(new Date());
        // JK Update, check if user have referralURL Set
        if (userVO.getReferralURL() == null) {
            updateUserReferralURL(userVO);
        }
        return userVO;
    }

    private void updateUserReferralURL(UserVO userVO) {
        String userRefURL = referralManager.getReferralURL(userVO);
        ShortURLWorker shorURLWorker = new ShortURLWorker(this, userVO, userRefURL);
        FutureTask ft = new FutureTask(shorURLWorker);
        threadPool.submit(ft);
    }

    @Override
    public void trimNameAndMobileNo(UserVO userVO) throws CoreException {
        if (userVO.getMobileNo() != null) {
            userVO.setMobileNo(userVO.getMobileNo().trim());
        }

        if (userVO.getNickName() != null) {
            userVO.setNickName(userVO.getNickName().trim());
        }

        getUserDAO().update(userVO);
    }

    @Override
    public void checkForClientVersionUpgrade(UserVO userVO, double clientVersion) {
        if (userVO.getClientVersion() < 5) {
            myLog.debug("Login first time for network contact version - " + userVO.getUserid());
            checkContactAddedList(userVO);
        }

        // Login first time for version 6, convert trial user to lite user with sms
        if (userVO.getClientVersion() == 5 && clientVersion == 6) {
            myLog.debug("Login first time for safety level version - " + userVO.getUserid());
            billingManager.checkTrialBillingPackage(userVO);
        }

        if (userVO.getClientVersion() != clientVersion){
            userVO.setClientVersion(clientVersion);
            updateUser(userVO);
        }
    }

    @Override
    public void updateEmailAddress(UserVO userVO, String emailAddress) throws CoreException {
        // TODO: Allow all users to change their email by removing userid

        myLog.debug(String.format("Update user %s email to %s", userVO.getId(), userVO.getEmailAddress()));

        if (userVO.getUserIdType() == UserIdType.MOBILE) {
            emailAddress = getEmailAddressUtil().validateEmailAddress(emailAddress, userVO.getLangCode());

            if (!emailAddress.equals(userVO.getEmailAddress())) {
                ensureUniqueEmailAddress(emailAddress);

                userVO.setEmailAddress(emailAddress);
                updateUser(userVO);

                sendEmailChangedVerification(userVO);
            }
        } else {
            throw new CoreException(ErrorType.UNSUPPORTED_OPERATION_ERROR, USER_DEFAULT_LANGUAGE);
        }
    }


    @Override
    public void updateLocation(UserVO userVO, double latitude, double longitude, boolean resolveCountry) {
        String countryIso = null;
        try {
            // Sometimes, it will fail to resolve country, we just ignore it.
            countryIso = getLocationUtil().getCountryCodeByLocation(latitude, longitude);
        } catch (Exception ex) {
            myLog.debug(ex.getMessage(), ex);
        }

        if (StringUtils.isNotEmpty(countryIso)) {
            userVO.setCountryVO(getSmsManager().getCountry(countryIso));
        }
        userVO.setLatitude(latitude);
        userVO.setLongitude(longitude);
        updateUser(userVO);
    }

    @Override
    public void updateMobileNumber(UserVO userVO, String mobileCountryISO, String mobileNumber) throws CoreException {
        // TODO: Allow all users to change their phone number by removing userid

        myLog.debug(String.format("Update user %s email to %s", userVO.getId(), userVO.getEmailAddress()));

        if (userVO.getUserIdType() == UserIdType.EMAIL) {
            CountryVO mobileCountryVO = validateUserMobileNumber(mobileCountryISO, mobileNumber, userVO.getLangCode());
            mobileNumber = getMobileNumberUtil().checkAndReturnCorrectMobileNumber(
                    mobileNumber, Integer.valueOf(mobileCountryVO.getCallingCode())); // Remove characters like space ( ) -
            ensureUniqueMobileNumber(mobileCountryISO, mobileNumber);

            userVO.setMobileCountry(mobileCountryVO);
            userVO.setMobileNo(mobileNumber);
            updateUser(userVO);
        } else {
            throw new CoreException(ErrorType.UNSUPPORTED_OPERATION_ERROR, USER_DEFAULT_LANGUAGE);
        }
    }

    @Override
    public void updateName(UserVO userVO, String userName) {
        userVO.setNickName(userName);
        updateUser(userVO);
    }

    @Override
    public void updateProfilePicture(UserVO userVO, String encodedPicStr) {
        userVO.setProfilePictureURL(getMediaFileManager().saveUserProfilePicture(userVO, MediaFileType.PICTURE, encodedPicStr));
        updateUser(userVO);
    }

    @Override
    public void markWalkyUser(UserVO userVO) {
        userVO.setWalky(true);
        updateUser(userVO);
    }

    @Override
    public void updateUser(UserVO userVO) throws CoreException {
        userVO.setUpdatedDate(new Date());
        getUserDAO().update(userVO);
    }

    @Override
    public void addReplaceSNSConfig(UserVO userVO, UserSNSConfigVO newSNSConfigVO) throws CoreException {
        // First Determine if user SNSConfigList is null
        List<UserSNSConfigVO> userSNSConfigList = userVO.getSnsConfigList();
        if (newSNSConfigVO.getUpdatedDate() == null) {
            newSNSConfigVO.setUpdatedDate(new Date());
        }
        myLog.debug("Add or Replacing user SNSConfig for user->" + userVO.getUserid()
                + ", snsName:" + newSNSConfigVO.getSnsName()
                + ", notify:" + newSNSConfigVO.isNotify());
        if (userSNSConfigList == null) {
            userSNSConfigList = new ArrayList<UserSNSConfigVO>();
            userSNSConfigList.add(newSNSConfigVO);
            userVO.setSnsConfigList(userSNSConfigList);
        } else {
            // Needs to find out if there is an existing SNSConfig sharing the same SNSVO
            UserSNSConfigVO someSNSConfigVO = null;
            for (UserSNSConfigVO tmpUsrSNSConfigVO : userSNSConfigList) {
                if (StringUtils.equalsIgnoreCase(tmpUsrSNSConfigVO.getSnsName(), newSNSConfigVO.getSnsName())) {
                    // is a replacment
                    someSNSConfigVO = tmpUsrSNSConfigVO;
                    myLog.debug("Existing SNSConfig Found, notify->" + someSNSConfigVO.isNotify());
                    break;
                }
            }
            if (someSNSConfigVO != null) {
                userVO.getSnsConfigList().remove(someSNSConfigVO);
            }
            userVO.getSnsConfigList().add(newSNSConfigVO);
        }
        getUserDAO().update(userVO);
    }

    @Override
    public void removeSNSConfig(UserVO userVO, String providerId) throws CoreException {
        List<UserSNSConfigVO> userSNSConfigVOs = userVO.getSnsConfigList();
        for (UserSNSConfigVO userSNSConfigVO : userSNSConfigVOs) {
            if (StringUtils.equalsIgnoreCase(providerId, userSNSConfigVO.getSnsName())) {
                userSNSConfigVOs.remove(userSNSConfigVO);
                break;
            }
        }
        updateUser(userVO);
    }

    @Override
    public void removeSNSConfig(UserVO userVO, UserSNSConfigVO snsConfigRemove) throws CoreException {
        userVO.getSnsConfigList().remove(snsConfigRemove);
        updateUser(userVO);
    }

    public void updatePassword(UserVO userVO, String currentPassword, String newPassword) throws CoreException {
        if (this.getSecurityHelper().isPasswordMatch(userVO.getPassword(), currentPassword)) {
            userVO.setPassword(this.getSecurityHelper().encryptPassword(newPassword));
            this.getUserDAO().update(userVO);
        } else {
            throw new CoreException(ErrorType.CURRENT_PASSWORD_MISMATCH, userVO.getLangCode());
        }
    }

    public void updatePasswordWithResetPin(UserVO userVO, String resetPin, String newPassword) throws CoreException {
        if (resetPin.equalsIgnoreCase(userVO.getPasswordResetPin()) && userVO.getPasswordResetPinDate() != null) {
            Date effectiveDate = DateUtils.addHours(userVO.getPasswordResetPinDate(), 2);
            if (effectiveDate.getTime() > new Date().getTime()) {
                userVO.setPassword(this.getSecurityHelper().encryptPassword(newPassword));
                userVO.setPasswordResetPinDate(null);
                userVO.setPasswordResetPin(null);
                this.getUserDAO().update(userVO);
            } else {
                userVO.setPasswordResetPinDate(null);
                userVO.setPasswordResetPin(null);
                this.getUserDAO().update(userVO);
                throw new CoreException(ErrorType.RESET_PASSWORD_REQUEST_EXPIRE_ERROR, userVO.getLangCode());
            }
        } else {
            throw new CoreException(ErrorType.USER_WRONG_RESET_PASSWORD_PIN, userVO.getLangCode());
        }
    }

    // JK 21-Feb 
    public UserVO updateUserPushMessageToken(UserVO userVO, String mobilePlatformStr, String pushMessageToken)
            throws CoreException {
        MobilePlatformType platformType = null;
        boolean dataUpdated = false;
        if (mobilePlatformStr.equalsIgnoreCase("Android")) {
            platformType = MobilePlatformType.Android;
        } else if (mobilePlatformStr.equalsIgnoreCase("iOS")
                || mobilePlatformStr.equalsIgnoreCase("iPhone")) {
            platformType = MobilePlatformType.iOS;
        }

        UserPushMessageToken usrPushMsgToken = new UserPushMessageToken();
        usrPushMsgToken.setUserVO(userVO);
        usrPushMsgToken.setPlatformType(platformType);
        usrPushMsgToken.setToken(pushMessageToken);
        usrPushMsgToken.setActivateDate(new Date());

        if (userVO.getPushMessageTokenList() == null
                || userVO.getPushMessageTokenList().isEmpty()) {
            List<UserPushMessageToken> msgTokenList = new ArrayList<UserPushMessageToken>();
            msgTokenList.add(usrPushMsgToken);
            userVO.setPushMessageTokenList(msgTokenList);
            dataUpdated = true;
        } else {
            boolean recordFound = false;
            for (UserPushMessageToken msgToken : userVO.getPushMessageTokenList()) {
                if (msgToken.getPlatformType().equals(platformType)
                        && msgToken.getToken().equals(pushMessageToken)) {
                    recordFound = true;
                }
            }

            if (!recordFound) {
                userVO.getPushMessageTokenList().add(usrPushMsgToken);
                dataUpdated = true;

            }
        }
        getUserDAO().update(userVO);
        return userVO;
    }

    public UserSNSConfigVO getUserSnsConfigVOBySnsuidAndSnsName(String snsuid, String snsName) throws CoreException {
        return getUserSNSConfigDAO().findBySnsNameAndUid(snsuid, snsName);
    }

    @Override
    public String getUserIdFromMobileNumber(String mobileCountry, String mobileNumber) throws CoreException {
        CountryVO country = getSmsManager().getCountry(mobileCountry);
        return country.getCallingCode() + "-" + mobileNumber;
    }

    public void updateShortURL(Object valueObject, String longURL) throws CoreException {
        // Updating user's referral URL
        try {
            UserVO userVO = (UserVO) valueObject;
            myLog.debug("Updating shortURL Service for user->" + userVO.getUserid() + ",url->" + longURL);
            if (longURL.contains("referral")) {
                // Is for referral purpose;
                userVO.setReferralShortURL(getUrlShortenerService().getShortURL(longURL, "referral"));
                userVO.setReferralURL(longURL);
            }
            getUserDAO().update(userVO);
        } catch (ShortURLException ex) {
            myLog.error("Failed to generate short url for: " + longURL, ex);
        }
    }

    public CountryVO validateUserMobileNumber(String mobileCountryISO, String mobileNumber, String langCode) throws CoreException {
    	return super.validateUserMobileNumber(mobileCountryISO, mobileNumber, langCode);
    }
    
    // update current user contact list and get contact list that added current user as contact
    @Override
    public void checkContactAddedList(UserVO userVO) throws CoreException {
    	myLog.debug("checkContactAddedList " + userVO.getUserid());
        
    	List<ContactVO> userContactList = userVO.getContactList();
    	if (userContactList != null) {
    		for (ContactVO contactVO : userContactList) {
                UserVO contactUserVO = getUserByMobileNumberOrEmailAddress(contactVO.getCountryVO(), contactVO.getMobileNo(), contactVO.getEmailAddress());
	    		if (contactUserVO != null) {
	    			// Self added
	    			if (contactUserVO.getId().equals(userVO.getId())) {
	    				contactVO.setStatus(ContactInvitationStatus.SELFADDED);
	    			}
	    			contactVO.setContactUserVO(contactUserVO);
	    		}
                contactVO.setUpdatedAt(new Date());
    		}
    		
    		getUserDAO().update(userVO);
    	}

        List<ContactVO>  contactPendingList = null;
        if (userVO.getMobileCountry() != null && userVO.getMobileNo() != null) {
            contactPendingList = getContactDAO().findByEmailOrMobileNumber(
                    userVO.getEmailAddress(), userVO.getMobileCountry().getIso(), userVO.getMobileNo());
        } else {
            return;
        }

    	if (contactPendingList != null) {
    		myLog.debug("checkContactAddedList contactPendingList" + contactPendingList.size());
    		for (ContactVO pendingContactVO : contactPendingList) {
                pendingContactVO.setContactUserVO(userVO);
                pendingContactVO.setUpdatedAt(new Date());
                getContactDAO().update(pendingContactVO);
    		}
    	}
    }
    
    public String getServerParam(String keyValue) throws CoreException {
    	ServerParamVO serverParamVO = getServerParamDAO().findParameterByKeyValue(keyValue);
    	if(serverParamVO != null) {
    		return serverParamVO.getParameter();
    	}
    	return null;
    }
    
    public void sendNotificationToContacts(UserVO userVO) throws CoreException {
    	List<ContactVO> contactList = userVO.getContactList();
    	if (contactList != null && contactList.size() > 0) {
            List<ContactVO> notifiedContacts = new ArrayList<>();
    		for(ContactVO contactVO : contactList) {
                UserVO contactUserVO = contactVO.getContactUserVO();
                if (contactUserVO != null && contactUserVO.getClientVersion() < 6) {
                    notifiedContacts.add(contactVO);
                }
    		}
            getNotificationEngine().notifyContactToUpdate(userVO, notifiedContacts);
    	}
    }
    
    public List<ContactVO> getContactAddedByOtherUserList(UserVO userVO) throws CoreException {
        if (userVO.getEmailAddress() != null && userVO.getMobileCountry() != null && userVO.getMobileNo() != null) {
            return getContactDAO().findInvitedContactByEmailOrMobileNumber(
                    userVO.getEmailAddress(),
                    userVO.getMobileCountry().getIso(), userVO.getMobileNo());
        } else if (userVO.getEmailAddress() != null) {
            return getContactDAO().findInvitedContactByEmailAddress(userVO.getEmailAddress());
        } else {
            return getContactDAO().findInvitedContactByMobileNumber(userVO.getMobileCountry().getIso(), userVO.getMobileNo());
        }
    }

    public void pushToParse(UserVO userVO, String message, String token) throws CoreException {
        throw new UnsupportedOperationException("Not supported anymore");
    	// getNotificationEngine().sendPushMessage(userVO, message);
    }
    
    public void updateUsersMobileNo() {
    	myLog.debug("updateUsersMobileNo");
    	List<UserVO> userList = getUserDAO().findAllUsers();
    	myLog.debug("updateUsersMobileNo size" + userList.size());
    	int countTotalUpdate = 0;
    	for (UserVO userVO : userList) {
    		String updatedMobileNo = getMobileNumberUtil().checkAndReturnCorrectMobileNumber(
    				userVO.getMobileNo(), userVO.getMobileCountry().getIso());
    		if (userVO.getMobileNo().equalsIgnoreCase(updatedMobileNo)) {
    			//same number after checked by libphonenumber
    			
    		} else {
    			myLog.debug("No." + countTotalUpdate + " id:" + userVO.getUserid() + ", old number:" + userVO.getMobileNo() +
    					", updatedNumber:" + updatedMobileNo);
    			countTotalUpdate++;
    			userVO.setMobileNo(updatedMobileNo);
    			getUserDAO().update(userVO);
    		}
    	}
    	myLog.debug("Total updated user:" + countTotalUpdate);
    }
    
    public void updateContactsMobileNo() {
    	myLog.debug("updateContactsMobileNo");
    	List<ContactVO> contactList = getContactDAO().findAllContacts();
    	myLog.debug("updateContactsMobileNo size " + contactList.size());
    	int countTotalUpdate = 0;
    	for(ContactVO contactVO : contactList) {
    		String updatedMobileNo = getMobileNumberUtil().checkAndReturnCorrectMobileNumber(
    				contactVO.getMobileNo(), contactVO.getCountryVO().getIso());
    		if(contactVO.getMobileNo().equalsIgnoreCase(updatedMobileNo)) {
    			//same number after checked by libphonenumber
    			
    		} else {
    			myLog.debug("No." + countTotalUpdate + " id:" + contactVO.getId() + ", old number:" + contactVO.getMobileNo() +
    					", updatedNumber:" + updatedMobileNo);
    			countTotalUpdate++;
    			contactVO.setMobileNo(updatedMobileNo);
    			getContactDAO().update(contactVO);
    		}
    	}
    	myLog.debug("Total updated user:" + countTotalUpdate);
    }
    
    public void sendTestHtmlEmailSms(String name, String email, String mobileCountry, String mobileNumber) {
        getNotificationEngine().sendTestHtmlEmailSms(name, email, getSmsManager().getCountry(mobileCountry), mobileNumber);
    }
    
    public void resendNotificationToContact(UserVO userVO, List<Long> contactIdList) throws CoreException {
    	if(userVO.getContactList() != null) {
    		for(ContactVO contactVO : userVO.getContactList()) {
    			for(Long contactId : contactIdList) {
    				if(contactVO.getId().equals(contactId)) {
        				getNotificationEngine().resendNotificationToContact(userVO, contactVO);
            			contactVO.setUpdatedAt(new Date());
            			getContactDAO().update(contactVO);
            			break;
        			}
    			}
    		}
    	}
    }

    // GetResponse Contact List
    public void addMarketingEmailContact(UserVO userVO, MarketingCampaign... campaigns) {
    	//temporary on hold for production server
    	getNotificationEngine().addMarketingEmailContact(userVO, campaigns);
    }

    public void deleteMarketingEmailContact(UserVO userVO, MarketingCampaign... campaigns) {
        getNotificationEngine().deleteMarketingEmailContact(userVO, campaigns);
    }

    @Override
    public void unsubscribeMarketingEmailIfNeeded(@NotNull EmailLogVO emailLogVO) {
        if (MessageType.MARKETING_CAMPAIGN.equals(emailLogVO.getMessageType())) {
            EmailStatus status = emailLogVO.getStatus();
            if (EmailStatus.UNSUBSCRIBED.equals(status) || EmailStatus.MARKED_AS_SPAM.equals(status)) {
                unsubscribeMarketingEmail(emailLogVO.getRecipientEmail());
            } else {
                myLog.debug("No need to unsubscribe: " + emailLogVO);
            }
        } else {
            myLog.debug("No need to unsubscribe: " + emailLogVO);
        }
    }

    @Override
    public void unsubscribeMarketingEmail(String email) {
        myLog.debug(String.format("DefaultUserManager.unsubscribeMarketingEmail(%s)", email));

        UserDAO userDAO = getUserDAO();
        UserVO userVO = userDAO.findByEmail(email);
        if (userVO != null) {
            myLog.debug("Unsubscribe marketing email: " + email);
            userVO.setSubscribeEmailNewsLetter(false);
            userDAO.update(userVO);
        }
    }

    @Override
    public void unsubscribeMarketingEmail(String email, String subscribeEmailToken) {
        myLog.debug(String.format("DefaultUserManager.unsubscribeMarketingEmail(%s, %s)", subscribeEmailToken, email));

        UserDAO userDAO = getUserDAO();
        UserVO userVO = userDAO.findByEmail(email);

        if (userVO.getSubscribeEmailNewsLetterToken().equals(subscribeEmailToken)) {
            userVO.setSubscribeEmailNewsLetter(false);
            userDAO.update(userVO);
            myLog.debug("Success to unsubscribe email newsletter for user " + email);
        } else {
            myLog.debug("Failed to unsubscribe email newsletter for user " + email);
            throw new CoreException(ErrorType.USER_EMAIL_AND_TOKEN_MISMATCH_ERROR, UserManager.USER_DEFAULT_LANGUAGE);
        }
    }

    @Override
    public String getAnalyticsId(String authToken, String device) throws CoreException {
        device = StringUtils.lowerCase(device);
        UserVO userVO = getUserByActivationCode(authToken);
        generateAnalyticsId(userVO, device);
        String analyticsId = userVO.getAnalyticsIds().get(device);
        myLog.debug(String.format("Analytics id for user %s with %s device: %s", userVO.getUserid(), device, analyticsId));
        return analyticsId;
    }

    @Override
    public void generateAnalyticsId(UserVO userVO, String os) {
        if (userVO.getAnalyticsIds() == null || !userVO.getAnalyticsIds().containsKey(os)) {
            myLog.debug(String.format("User %s doesn't have analytics id for %s device. Generate a new one.", userVO.getUserid(), os));

            if (userVO.getAnalyticsIds() == null) {
                userVO.setAnalyticsIds(new HashMap<>());
            }
            userVO.setDevice(os);
            userVO.getAnalyticsIds().put(os, String.format("%d-%s", userVO.getId(), os));
            getUserDAO().update(userVO);
        }
    }
}
