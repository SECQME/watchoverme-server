package com.secqme.util.marketing;


import java.util.concurrent.Callable;

import com.secqme.domain.model.ContactVO;
import org.apache.log4j.Logger;

import com.secqme.domain.model.UserVO;

/**
 * Created by edward on 20/07/2015.
 */
public class MarketingEmailUnsubscribeWorker implements Callable {

    private static final Logger myLog = Logger.getLogger(MarketingEmailSubscribeWorker.class);

    UserVO userVO = null;
    ContactVO contactVO = null;
    MarketingCampaign[] campaigns;
    MarketingEmailService marketingEmailService = null;

    public MarketingEmailUnsubscribeWorker(MarketingEmailService marketingEmailService, UserVO userVO, MarketingCampaign... campaigns) {
        this.userVO = userVO;
        this.campaigns = campaigns;
        this.marketingEmailService = marketingEmailService;
    }

    public MarketingEmailUnsubscribeWorker(MarketingEmailService marketingEmailService, ContactVO contactVO, MarketingCampaign... campaigns) {
        this.contactVO = contactVO;
        this.campaigns = campaigns;
        this.marketingEmailService = marketingEmailService;
    }

    public Boolean call() throws Exception {
        try {
            myLog.debug("Marketing Email Worker call, adding email " + userVO.getEmailAddress() + " to campaigns: " + campaigns);
            for (MarketingCampaign campaign : campaigns) {
                if (userVO != null) {
                    marketingEmailService.deleteUserFromCampaign(userVO, campaign);
                } else {
                    marketingEmailService.deleteContactFromCampaign(contactVO, campaign);
                }
            }
            return true;
        } catch (Exception ex) {
            myLog.error(ex.getMessage(), ex);
            return false;
        }
    }

}

