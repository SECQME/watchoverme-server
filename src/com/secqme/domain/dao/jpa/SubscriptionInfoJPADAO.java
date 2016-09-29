package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.SubscriptionInfoDAO;
import com.secqme.domain.model.payment.UserSubscriptionID;
import com.secqme.domain.model.payment.UserSubscriptionInfoVO;
import org.apache.log4j.Logger;

import java.util.List;

public class SubscriptionInfoJPADAO extends BaseJPADAO<UserSubscriptionInfoVO, UserSubscriptionID>
	implements SubscriptionInfoDAO{

	private static Logger myLog = Logger.getLogger(SubscriptionInfoJPADAO.class);
	
	public SubscriptionInfoJPADAO() {
		super(UserSubscriptionInfoVO.class);
	}
	
	public List<UserSubscriptionInfoVO> findAllSubscriptionInfo() {
		return executeQueryWithResultList(UserSubscriptionInfoVO.QUERY_FIND_ALL_SUBSCRIPTION_INFO);
	}

}
