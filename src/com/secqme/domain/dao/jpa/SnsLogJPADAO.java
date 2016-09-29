package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.SnsLogDAO;
import com.secqme.domain.model.notification.sns.SNSLogVO;

/**
 * Created by edward on 30/07/2015.
 */
public class SnsLogJPADAO extends BaseJPADAO<SNSLogVO, Long> implements SnsLogDAO {

    public SnsLogJPADAO() {
        super(SNSLogVO.class);
    }
}
