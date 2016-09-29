package com.secqme.util.marketing;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;

/**
 * Created by edward on 10/06/2016.
 */
public class NullMarketingEmailService implements MarketingEmailService {

    @Override
    public void addUserToCampaign(UserVO userVO, MarketingCampaign campaign) {
        // Do-nothing
    }

    @Override
    public void addContactToCampaign(ContactVO contactVO, MarketingCampaign campaign) {
        // Do-nothing
    }

    @Override
    public void deleteUserFromCampaign(UserVO userVO, MarketingCampaign campaign) {
        // Do-nothing
    }

    @Override
    public void deleteContactFromCampaign(ContactVO contactVO, MarketingCampaign campaign) {
        // Do-nothing
    }
}
