package com.secqme.domain.dao;

import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.notification.UserSNSID;

/**
 * User: James Khoo
 * Date: 2/18/14
 * Time: 5:58 PM
 */
public interface UserSNSConfigDAO extends BaseDAO<UserSNSConfigVO, UserSNSID> {
    public UserSNSConfigVO findBySnsNameAndUid(String snsuid, String snsName);
}
