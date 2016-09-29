package com.secqme.domain.dao;


import com.secqme.domain.model.notification.CampaignsVO;

/**
 * Created by Edmund on 4/9/15.
 */
public interface CampaignsDAO extends BaseDAO<CampaignsVO, String> {

    public CampaignsVO findCampaignByID(String campaignTag);
}
