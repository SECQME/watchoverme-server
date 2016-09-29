package com.secqme.web.jsf.mbean;

import com.secqme.domain.dao.ContactDAO;
import com.secqme.domain.model.ContactInvitationStatus;
import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;
import com.secqme.manager.ContactManager;
import com.secqme.manager.UserManager;
import com.secqme.util.marketing.MarketingCampaign;
import com.secqme.web.jsf.util.NavigationRules;
import org.apache.log4j.Logger;

import javax.faces.context.FacesContext;
import javax.ws.rs.NotFoundException;
import java.io.Serializable;

/**
 * Created by Edmund on 4/28/15.
 */
public class ManageEmergencyContactBean implements Serializable {

    private static final Logger myLog = Logger.getLogger(ManageEmergencyContactBean.class);

    private String userName = null;
    private String emailAddress = null;
    private String contactToken = null;
    private String action = null;

    private ContactDAO contactDAO = null;
    private UserManager userManager = null;
    private ContactManager contactManager = null;

    public void initEmergencyContactDetails() {
        if (contactToken != null) {
            myLog.debug("ManageEmergencyContactBean.initEmergencyContactDetails(): " + contactToken);
            ContactVO contactVO = contactDAO.findByContactToken(contactToken);
            if (contactVO != null) {
                myLog.debug(contactVO);

                UserVO userVO = contactVO.getUserVO();
                this.userName = userVO.getNickName();
                this.emailAddress = userVO.getEmailAddress();

                if (contactVO.getStatus() == ContactInvitationStatus.ACCEPTED) {
                    this.action = "leave";
                }
            } else {
                myLog.debug("Invalid contactToken");
                redirectToInvalidToken();
            }
        } else {
            myLog.debug("contactToken is null");
            redirectToErrorPage();
        }
    }

    public void acceptEmergencyContact() {
        initEmergencyContactDetails();

        myLog.debug("acceptEmergencyContact - " + contactToken);
        ContactVO contactVO = contactDAO.findByContactToken(contactToken);
        if (contactVO != null) {
            contactManager.acceptContact(contactVO);

            // Add to emergency contact funnel if not a user
            if (contactVO.getContactUserVO() == null) {
                contactManager.addMarketingEmailContact(contactVO, MarketingCampaign.EMERGENCY_CONTACT_FUNNEL);
            }
        } else {
            myLog.debug("Invalid contactToken");
            redirectToInvalidToken();
        }
    }

    public String rejectEmergencyContact() {
        try {
            myLog.debug("changeContactInvitationStatus - " + contactToken);
            ContactVO contactVO = contactDAO.findByContactToken(contactToken);
            if (contactVO != null) {
                contactManager.rejectContact(contactVO, action);

                // Delete from emergency contact funnel if not a user
                if (contactVO.getContactUserVO() == null) {
                    contactManager.addMarketingEmailContact(contactVO, MarketingCampaign.EMERGENCY_CONTACT_FUNNEL);
                }
            } else {
                throw new NotFoundException(String.format("Contact with token %s is not found.", contactToken));
            }
            return NavigationRules.REJECT_EMERGENCY_CONTACT_SUCCESS.getNaviPath();
        } catch (Exception ex) {
            myLog.error("ManageEmergencyContactBean.changeContactInvitationStatus()", ex);
            return NavigationRules.ERROR.getNaviPath();
        }
    }

    private void redirectToErrorPage() {
        FacesContext fc = FacesContext.getCurrentInstance();
        fc.getApplication().getNavigationHandler().handleNavigation(fc, null, NavigationRules.ERROR.getNaviPath());
    }

    public void redirectToInvalidToken() {
        FacesContext fc = FacesContext.getCurrentInstance();
        fc.getApplication().getNavigationHandler().handleNavigation(fc, null, NavigationRules.EMERGENCY_CONTACT_INVALID.getNaviPath());
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getContactToken() {
        return contactToken;
    }

    public void setContactToken(String contactToken) {
        this.contactToken = contactToken;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public ContactDAO getContactDAO() {
        return contactDAO;
    }

    public void setContactDAO(ContactDAO contactDAO) {
        this.contactDAO = contactDAO;
    }

    public UserManager getUserManager() {
        return userManager;
    }

    public void setUserManager(UserManager userManager) {
        this.userManager = userManager;
    }

    public ContactManager getContactManager() {
        return contactManager;
    }

    public void setContactManager(ContactManager contactManager) {
        this.contactManager = contactManager;
    }
}
