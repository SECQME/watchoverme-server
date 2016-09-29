package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.ShareEventDAO;
import com.secqme.domain.model.event.ShareEventVO;

import java.util.Date;
import java.util.List;

public class ShareEventJPADAO extends BaseJPADAO<ShareEventVO, Long> implements ShareEventDAO{
	public ShareEventJPADAO() {
		super(ShareEventVO.class);
	}

	@Override
	public ShareEventVO findByShareToUser(String userid, Long eventid) {
		JPAParameter parameter = new JPAParameter()
    	.setParameter("userid", userid)
    	.setParameter("eventid", eventid);
		return executeQueryWithSingleResult(ShareEventVO.QUERY_FIND_BY_SHARE_TO_USER, parameter);
	}
	
	@Override
	public List<ShareEventVO> findByEventId(Long eventid) {
		JPAParameter parameter = new JPAParameter()
    	.setParameter("eventid", eventid);
		return executeQueryWithResultList(ShareEventVO.QUERY_FIND_BY_EVENT_ID, parameter);
	}
	
	@Override
	public List<ShareEventVO> findByShareTimeAndShareToUser(Date shareTime, String userid) {
		JPAParameter parameter = new JPAParameter()
		.setParameter("sharetime", shareTime)
    	.setParameter("userid", userid);
		return executeQueryWithResultList(ShareEventVO.QUERY_FIND_BY_SHARE_TIME_AND_SHARE_TO_USER, parameter);
	}
}