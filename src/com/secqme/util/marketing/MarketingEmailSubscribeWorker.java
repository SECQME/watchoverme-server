package com.secqme.util.marketing;

import java.util.concurrent.Callable;

import com.secqme.domain.model.ContactVO;
import org.apache.log4j.Logger;

import com.secqme.domain.model.UserVO;

public class MarketingEmailSubscribeWorker implements Callable {

    private static final Logger myLog = Logger.getLogger(MarketingEmailSubscribeWorker.class);

    UserVO userVO = null;
    ContactVO contactVO = null;
    MarketingCampaign[] campaigns;
    MarketingEmailService marketingEmailService = null;

    public MarketingEmailSubscribeWorker(MarketingEmailService marketingEmailService, UserVO userVO, MarketingCampaign... campaigns) {
    	this.userVO = userVO;
    	this.campaigns = campaigns;
    	this.marketingEmailService = marketingEmailService;
    }

    public MarketingEmailSubscribeWorker(MarketingEmailService marketingEmailService, ContactVO contactVO, MarketingCampaign... campaigns) {
        this.contactVO = contactVO;
        this.campaigns = campaigns;
        this.marketingEmailService = marketingEmailService;
    }

    public Boolean call() throws Exception {
        try {
            for (MarketingCampaign campaign : campaigns) {
                if (userVO != null) {
                    myLog.debug("Marketing Email Worker call, adding email " + userVO.getEmailAddress() + " to campaigns: " + campaign);
                    marketingEmailService.addUserToCampaign(userVO, campaign);
                } else {
                    myLog.debug("Marketing Email Worker call, adding email " + contactVO.getEmailAddress() + " to campaigns: " + campaign);
                    marketingEmailService.addContactToCampaign(contactVO, campaign);
                }
            }
            return true;
        } catch (Exception ex) {
            myLog.error(ex.getMessage(), ex);
            return false;
        }
    }

}

