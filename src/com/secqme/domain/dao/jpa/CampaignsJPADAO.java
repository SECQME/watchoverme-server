package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.CampaignsDAO;
import com.secqme.domain.model.notification.CampaignsVO;

import java.util.List;

/**
 * Created by Edmund on 4/9/15.
 */
public class CampaignsJPADAO extends BaseJPADAO<CampaignsVO, String> implements CampaignsDAO {

    public CampaignsJPADAO() {
        super(CampaignsVO.class);
    }

    @Override
    public CampaignsVO findCampaignByID(String campaignTag) {
        JPAParameter param = new JPAParameter()
                .setParameter("campaignTag",campaignTag);
        List<CampaignsVO> campaignsVOs = executeQueryWithResultList(CampaignsVO.QUERY_FIND_BY_ID,param);
        if(campaignsVOs.size()>0 && campaignsVOs != null ){
            return campaignsVOs.get(0);
        }else{
            return null;
        }
    }
}
