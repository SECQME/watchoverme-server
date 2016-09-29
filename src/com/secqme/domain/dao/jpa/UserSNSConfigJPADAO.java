package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.UserSNSConfigDAO;
import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.notification.UserSNSID;
import org.apache.log4j.Logger;

import java.util.List;

public class UserSNSConfigJPADAO extends BaseJPADAO<UserSNSConfigVO, UserSNSID> implements UserSNSConfigDAO{
	
	private static Logger myLog = Logger.getLogger(UserSNSConfigJPADAO.class);
			
	public UserSNSConfigJPADAO() {
		super(UserSNSConfigVO.class);
	}

	public UserSNSConfigVO findBySnsNameAndUid(String snsuid, String snsName) {
        UserSNSConfigVO userSNSConfigVO = null;
		JPAParameter parameter = new JPAParameter().setParameter("snsuid", snsuid)
				.setParameter("snsName", snsName);
        List<UserSNSConfigVO> userSNSConfigVOList = executeQueryWithResultList(UserSNSConfigVO.QUERY_FIND_BY_SNSUID_SNSNAME, parameter);
        if(userSNSConfigVOList != null && userSNSConfigVOList.size() > 0) {
            userSNSConfigVO = userSNSConfigVOList.get(0);
        }
		return userSNSConfigVO;
	}
}
