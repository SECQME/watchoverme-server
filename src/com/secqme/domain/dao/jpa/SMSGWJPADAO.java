package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.SMSGWDAO;
import com.secqme.domain.model.notification.sms.SmsGateWayVO;

import java.util.List;

/**
 *
 * @author james
 */
public class SMSGWJPADAO extends BaseJPADAO<SmsGateWayVO, String> implements SMSGWDAO {

    public SMSGWJPADAO() {
        super(SmsGateWayVO.class);
    }

    public List<SmsGateWayVO> findAll() {
        return executeQueryWithResultList(SmsGateWayVO.QUERY_FIND_ALL);
    }
}
