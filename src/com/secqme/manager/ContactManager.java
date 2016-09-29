package com.secqme.manager;

import com.secqme.CoreException;
import com.secqme.domain.model.ContactInvitationStatus;
import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;
import com.secqme.util.marketing.MarketingCampaign;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Created by edward on 13/05/2015.
 */
public interface ContactManager {
    // There is additional steps require to add the new Contacts, such as sending email
    // and SMS the Contact that user have Nominated them as their emergency contacts
    public void addNewContact(UserVO userVO, ContactVO contactVO) throws CoreException;
    public void changeContact(UserVO userVO, ContactVO newContactVO) throws CoreException;
    public void removeContact(UserVO userVO, ContactVO contactToRemove) throws CoreException;
    public void removeContact(UserVO userVO, long contactId);
    public void removeContacts(UserVO userVO, List<Long> contactIdList) throws CoreException;

    public void acceptContact(@NotNull ContactVO contactVO);
    public void rejectContact(@NotNull ContactVO contactVO, String action);

    // GetResponse Contact List
    public void addMarketingEmailContact(ContactVO contactVO, MarketingCampaign... campaigns);
    public void deleteMarketingEmailContact(ContactVO contactVO, MarketingCampaign... campaigns);

    public void remindContactInvitation();
}
