
package com.secqme.util.marketing;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;

public interface MarketingEmailService {
    public void addUserToCampaign(UserVO userVO, MarketingCampaign campaign);
    public void addContactToCampaign(ContactVO contactVO, MarketingCampaign campaign);
    public void deleteUserFromCampaign(UserVO userVO, MarketingCampaign campaign);
    public void deleteContactFromCampaign(ContactVO contactVO, MarketingCampaign campaign);
}
