package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.SubscriptionInfoLogDAO;
import com.secqme.domain.model.payment.UserSubscriptionInfoLogVO;
import org.apache.log4j.Logger;

public class SubscriptionInfoLogJPADAO extends BaseJPADAO<UserSubscriptionInfoLogVO, Long>
	implements SubscriptionInfoLogDAO{

	private static Logger myLog = Logger.getLogger(SubscriptionInfoLogJPADAO.class);
	
	public SubscriptionInfoLogJPADAO() {
		super(UserSubscriptionInfoLogVO.class);
	}

}
