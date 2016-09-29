package com.secqme.domain.dao;

import java.util.Date;
import java.util.List;

import com.secqme.domain.model.event.ShareEventVO;

public interface ShareEventDAO extends BaseDAO<ShareEventVO, Long>{
	public ShareEventVO findByShareToUser(String userid, Long eventid);
	public List<ShareEventVO> findByEventId(Long eventid);
	public List<ShareEventVO> findByShareTimeAndShareToUser(Date shareTime, String userid);
}
