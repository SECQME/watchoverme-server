package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.UserTrackingLogDAO;
import com.secqme.domain.model.UserTrackingLogVO;
import org.apache.log4j.Logger;

public class UserTrackingLogJPADAO extends BaseJPADAO<UserTrackingLogVO, Long> implements UserTrackingLogDAO{
	
	private static Logger myLog = Logger.getLogger(UserTrackingLogJPADAO.class);
			
	public UserTrackingLogJPADAO() {
		super(UserTrackingLogVO.class);
	}

}
