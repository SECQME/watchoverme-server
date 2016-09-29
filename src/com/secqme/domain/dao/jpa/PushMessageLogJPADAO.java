package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.PushMessageLogDAO;
import com.secqme.domain.model.pushmessage.PushMessageLogVO;

/**
 *
 * @author khlow20120626
 */
public class PushMessageLogJPADAO extends BaseJPADAO<PushMessageLogVO, Long> implements PushMessageLogDAO {

    public PushMessageLogJPADAO() {
        super(PushMessageLogVO.class);
    }
}
