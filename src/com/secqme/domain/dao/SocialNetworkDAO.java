package com.secqme.domain.dao;

import com.secqme.domain.model.notification.SocialNetworkVO;
import java.util.List;

public interface SocialNetworkDAO extends BaseDAO<SocialNetworkVO, String> {
    public List<SocialNetworkVO> findAll();
}
