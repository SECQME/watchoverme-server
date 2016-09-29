package com.secqme.domain.dao;

import java.util.List;

import com.secqme.domain.model.payment.UserSubscriptionID;
import com.secqme.domain.model.payment.UserSubscriptionInfoVO;

public interface SubscriptionInfoDAO extends BaseDAO<UserSubscriptionInfoVO, UserSubscriptionID>{
	public List<UserSubscriptionInfoVO> findAllSubscriptionInfo();
}
