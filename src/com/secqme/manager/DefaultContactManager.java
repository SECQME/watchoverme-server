package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.ErrorType;
import com.secqme.domain.dao.ContactDAO;
import com.secqme.domain.model.ContactInvitationStatus;
import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.MessageType;
import com.secqme.util.marketing.MarketingCampaign;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by edward on 13/05/2015.
 */
public class DefaultContactManager extends BaseManager implements ContactManager {

    private static final Logger myLog = Logger.getLogger(DefaultContactManager.class);

    private UserManager userManager;

    public DefaultContactManager(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public void addNewContact(UserVO userVO, ContactVO contactVO) throws CoreException {
        boolean validContactMobileNumber = false;
        String i18nMobileNumber = null;

        myLog.debug(String.format("Adding new contact %s (%s / %s-%s) for %s ",
                contactVO.getNickName(), contactVO.getEmailAddress(), contactVO.getCountryVO(), contactVO.getMobileNo(), userVO.getUserid()));

        List<ContactVO> userContactList = userVO.getContactList();
        if (userContactList == null) {
            userContactList = new ArrayList<>();
            userVO.setContactList(userContactList);
        }

        ensureNoDuplicateContact(userVO, contactVO);
        ensureMaximumContactsAllowed(userVO);

        if (StringUtils.isNotEmpty(contactVO.getEmailAddress())) {
            contactVO.setNotifyEmail(true);
            contactVO.setSafetyNotifyEmail(true);
        }

        if (StringUtils.isEmpty(contactVO.getMobileNo())) {
            contactVO.setCountryVO(null);
            contactVO.setNotifySMS(false);
            contactVO.setSafetyNotifySMS(false);
        } else {
            i18nMobileNumber = "+" + contactVO.getCountryVO().getCallingCode() + contactVO.getMobileNo();
            validContactMobileNumber = getMobileNumberUtil().isValidMobileNumber(i18nMobileNumber, contactVO.getCountryVO().getIso());

            if (validContactMobileNumber) {
                contactVO.setSafetyNotifySMS(true);
                contactVO.setNotifySMS(true);
            } else {
                throw new CoreException(ErrorType.USER_CONTACT_MOBILE_NUMBER_INVALID, userVO.getLangCode(), i18nMobileNumber);
            }
        }

        myLog.debug("Adding Contact->" + contactVO + " for user:" + userVO.getUserid());

        setupContactStatus(userVO, contactVO);
        contactVO.setUserVO(userVO);
        contactVO.setCreatedAt(new Date());
        contactVO.setUpdatedAt(new Date());

        userContactList.add(contactVO);
        getUserDAO().update(userVO);

        getNotificationEngine().sendContactInvitation(contactVO,
                StringUtils.isNotEmpty(contactVO.getEmailAddress()), validContactMobileNumber);
    }

    private void ensureNoDuplicateContact(UserVO userVO, ContactVO contactVO) {
        myLog.debug("Check for duplicate contact.");
        for (ContactVO existingContactVO : userVO.getContactList()) {
            if (contactVO.getCountryVO() != null && existingContactVO.getCountryVO() != null
                    && !existingContactVO.getId().equals(contactVO.getId())
                    && StringUtils.equals(contactVO.getMobileNo(), existingContactVO.getMobileNo())
                    && StringUtils.equals(contactVO.getCountryVO().getIso(), existingContactVO.getCountryVO().getIso())) {
                throw new CoreException(ErrorType.USER_CONTACT_EXIST_ERROR, null);
            }

            if (contactVO.getEmailAddress() != null && existingContactVO.getEmailAddress() != null
                    && !existingContactVO.getId().equals(contactVO.getId())
                    && StringUtils.equals(contactVO.getEmailAddress(), existingContactVO.getEmailAddress())) {
                throw new CoreException(ErrorType.USER_CONTACT_EXIST_ERROR, null);
            }
        }
    }

    private void ensureMaximumContactsAllowed(UserVO userVO) {
        List<ContactVO> userContactList = userVO.getContactList();
        int maxSMSContactsAllows = userVO.getPackageVO().getMaxSMSContactAllow();

        myLog.debug(String.format("Check for maximum contact allowed %d/%d", userContactList.size(), maxSMSContactsAllows));

        if (maxSMSContactsAllows > 0 && userContactList.size() >= maxSMSContactsAllows) {
            throw new CoreException(ErrorType.MAX_CONTACT_ALLOWS_ERROR, null, "" + maxSMSContactsAllows);
        }
    }

    private void setupContactStatus(UserVO userVO, ContactVO contactVO) {
        UserVO contactUserVO = userManager.getUserByMobileNumberOrEmailAddress(contactVO.getCountryVO(), contactVO.getMobileNo(), contactVO.getEmailAddress());
        contactVO.setContactUserVO(contactUserVO);

        contactVO.setStatus(ContactInvitationStatus.INVITED);
        if (contactUserVO != null) {
            contactVO.setContactUserVO(contactUserVO);

            // Self-added
            if (contactUserVO.getId().equals(userVO.getId())) {
                contactVO.setStatus(ContactInvitationStatus.SELFADDED);
            }
        }
    }

    @Override
    public void changeContact(UserVO userVO, ContactVO newContactVO) throws CoreException {
        myLog.debug("Change Contact for user->" + userVO + " contact->" + newContactVO);

        boolean validContactMobileNumber = false;
        boolean emailChanged = false;
        boolean mobileNumberChange = false;

        if (userVO.getContactList() != null) {
            for (ContactVO contactVO : userVO.getContactList()) {
                if (contactVO.getId().equals(newContactVO.getId())) {
                    checkPermission(userVO, contactVO);

                    if (StringUtils.isNotEmpty(newContactVO.getNickName())) {
                        contactVO.setNickName(newContactVO.getNickName());
                    }

                    if (!StringUtils.equalsIgnoreCase(contactVO.getEmailAddress(), newContactVO.getEmailAddress())) {
                        emailChanged = true;

                        if (StringUtils.isEmpty(newContactVO.getEmailAddress())) {
                            contactVO.setEmailAddress(null);
                            contactVO.setNotifyEmail(false);
                            contactVO.setSafetyNotifyEmail(false);
                        } else {
                            contactVO.setEmailAddress(newContactVO.getEmailAddress());
                            contactVO.setNotifyEmail(newContactVO.getNotifyEmail());
                            contactVO.setSafetyNotifyEmail(newContactVO.getSafetyNotifyEmail());
                        }
                    }

                    if (!StringUtils.equals(contactVO.getMobileNo(), newContactVO.getMobileNo())) {
                        mobileNumberChange = true;
                        String i18nMobileNumber = "+" + newContactVO.getCountryVO().getCallingCode() + newContactVO.getMobileNo();
                        validContactMobileNumber = getMobileNumberUtil().isValidMobileNumber(i18nMobileNumber, newContactVO.getCountryVO().getIso());
                        if (!validContactMobileNumber) {
                            throw new CoreException(ErrorType.USER_CONTACT_MOBILE_NUMBER_INVALID, userVO.getLangCode(), i18nMobileNumber);
                        }

                        if (StringUtils.isEmpty(newContactVO.getEmailAddress())) {
                            contactVO.setCountryVO(null);
                            contactVO.setMobileNo(null);
                            contactVO.setNotifySMS(false);
                            contactVO.setSafetyNotifySMS(false);
                        } else {
                            contactVO.setCountryVO(newContactVO.getCountryVO());
                            contactVO.setMobileNo(newContactVO.getMobileNo());
                            contactVO.setNotifySMS(newContactVO.getNotifySMS());
                            contactVO.setSafetyNotifySMS(newContactVO.getSafetyNotifySMS());
                        }
                    }

                    // TODO: Needs to change if previous contact is SMS disable
                    // and now SMS enable
                    // and user is assing to billing Pkg with limited SMS Contacts allows

                    ensureNoDuplicateContact(userVO, contactVO);
                    setupContactStatus(userVO, contactVO);

                    if (emailChanged || mobileNumberChange) {
                        getNotificationEngine().sendContactInvitation(newContactVO, emailChanged, validContactMobileNumber);
                    }

                    getUserDAO().refresh(userVO);
                    return;
                }
            }
        }

        throw new CoreException(ErrorType.CONTACT_NOT_FOUND, userVO.getLangCode());
    }

    private void checkPermission(UserVO userVO, ContactVO contactVO) {
        if (!contactVO.getUserVO().getId().equals(userVO.getId())) {
            throw new CoreException(ErrorType.CONTACT_MODIFY_PERMISSION_DENIED, userVO.getLangCode());
        }
    }

    @Override
    public void removeContact(UserVO userVO, ContactVO contactVO) throws CoreException {
        checkPermission(userVO, contactVO);
        userVO.getContactList().removeIf(c -> c.getId().equals(contactVO.getId()));
        getUserDAO().refresh(userVO);
    }

    @Override
    public void removeContact(UserVO userVO, long contactId) {
        ContactVO contactVO = getContactDAO().read(contactId);
        removeContact(userVO, contactVO);
    }

    @Override
    public void removeContacts(UserVO userVO, List<Long> contactIdList) throws CoreException {
        for (long contactId : contactIdList) {
            removeContact(userVO, contactId);
        }
    }

    @Override
    public void acceptContact(@NotNull ContactVO contactVO) {
        myLog.debug(String.format("DefaultUserManager.changeContactInvitationStatus(%s)", contactVO));

        ContactDAO contactDAO = getContactDAO();

        if (contactVO.getStatus() != ContactInvitationStatus.SELFADDED) {
            contactVO.setStatus(ContactInvitationStatus.ACCEPTED);
        }
        contactVO.setUpdatedAt(new Date());
        contactDAO.update(contactVO);
    }

    @Override
    public void rejectContact(@NotNull ContactVO contactVO, String action) {
        ContactDAO contactDAO = getContactDAO();
        contactDAO.delete(contactVO);

        MessageType messageType = MessageType.REJECTED_EMERGENCY_CONTACT;
        if ("leave".equals(action)) {
            messageType = MessageType.LEFT_EMERGENCY_CONTACT;
        }

        getNotificationEngine().sendContactRejectedNotification(contactVO, messageType);
    }

    @Override
    public void addMarketingEmailContact(ContactVO contactVO, MarketingCampaign... campaigns) {
        getNotificationEngine().addMarketingEmailContact(contactVO, campaigns);
    }

    @Override
    public void deleteMarketingEmailContact(ContactVO contactVO, MarketingCampaign... campaigns) {
        getNotificationEngine().deleteMarketingEmailContact(contactVO, campaigns);
    }

    @Override
    public void remindContactInvitation() {
        ContactDAO contactDAO = getContactDAO();

        Date beforeDate = DateUtils.addDays(new Date(), -3);
        List<ContactVO> contactVOs = contactDAO.findContactsNeedToBeReminded(beforeDate);
        myLog.debug(String.format("Send reminder to %d users", contactVOs != null ? contactVOs.size() : 0));

        if (contactVOs != null) {
            for (ContactVO contactVO : contactVOs) {
                getNotificationEngine().sendContactInvitationReminder(contactVO, true);
                contactVO.setReminderSent(true);
                contactDAO.update(contactVO);
            }
        } else {
            myLog.debug("No contacts need to be reminded.");
        }
    }
}
