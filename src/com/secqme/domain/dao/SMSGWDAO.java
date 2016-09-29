package com.secqme.domain.dao;

import com.secqme.domain.model.notification.sms.SmsGateWayVO;
import java.util.List;

/**
 *
 * @author james
 */
public interface SMSGWDAO extends BaseDAO<SmsGateWayVO, String> {
    public List<SmsGateWayVO> findAll();
}
