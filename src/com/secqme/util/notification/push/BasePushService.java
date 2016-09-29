package com.secqme.util.notification.push;

import com.secqme.CoreException;
import com.secqme.domain.model.notification.push.ContentBasedPushMessageVO;
import com.secqme.domain.model.notification.push.PushMessageVO;
import com.secqme.domain.model.notification.push.TemplateBasedPushMessageVO;
import com.secqme.util.ar.ARTemplateEngine;
import com.secqme.util.rest.RestUtil;

import java.util.List;

/**
 * Created by edward on 22/05/2015.
 */
public abstract class BasePushService {
    protected ARTemplateEngine arTemplateEngine;
    protected RestUtil restUtil;

    public BasePushService(ARTemplateEngine arTemplateEngine, RestUtil restUtil) {
        this.restUtil = restUtil;
        this.arTemplateEngine = arTemplateEngine;
    }

    public boolean pushMessage(PushMessageVO pushMessageVO) throws CoreException {
        List<ContentBasedPushMessageVO> contentBasedPushMessageVOs = null;
        if (pushMessageVO instanceof TemplateBasedPushMessageVO) {
            TemplateBasedPushMessageVO templateBasedPushMessageVO = (TemplateBasedPushMessageVO) pushMessageVO;
            contentBasedPushMessageVOs = ContentBasedPushMessageVO.fromTemplateBasedPushMessageVO(templateBasedPushMessageVO, arTemplateEngine);
        } else if (pushMessageVO instanceof ContentBasedPushMessageVO) {
            ContentBasedPushMessageVO contentBasedPushMessageVO = (ContentBasedPushMessageVO) pushMessageVO;
            contentBasedPushMessageVOs = ContentBasedPushMessageVO.separateToListContentBasedPushMessageVO(contentBasedPushMessageVO);
        }

        boolean result = true;
        for (ContentBasedPushMessageVO contentBasedPushMessageVO : contentBasedPushMessageVOs) {
            result &= pushContentBasedPushMessageToSingleRecipient(contentBasedPushMessageVO);
        }

        return result;
    }

    protected abstract boolean pushContentBasedPushMessageToSingleRecipient(ContentBasedPushMessageVO contentBasedPushMessageVO);
}
