package com.secqme.domain.dao;

import java.util.List;

import com.secqme.domain.model.event.SavedEventVO;
public interface SavedEventsDAO extends BaseDAO<SavedEventVO, Long> {
	public List<SavedEventVO> findEventsByUserid(String userid);
}
