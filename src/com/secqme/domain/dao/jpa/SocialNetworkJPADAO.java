package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.SocialNetworkDAO;
import com.secqme.domain.model.notification.SocialNetworkVO;

import java.util.List;


public class SocialNetworkJPADAO extends BaseJPADAO<SocialNetworkVO, String> implements SocialNetworkDAO {
    public SocialNetworkJPADAO() {
        super(SocialNetworkVO.class);
    }

    public List<SocialNetworkVO> findAll() {
        return executeQueryWithResultList(SocialNetworkVO.QUERY_FINDALL);
    }

}
