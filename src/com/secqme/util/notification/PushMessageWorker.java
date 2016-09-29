package com.secqme.util.notification;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.push.PushMessageVO;
import com.secqme.util.notification.push.PushService;
import java.util.concurrent.Callable;

/**
 *
 * @author coolboykl
 */
public class PushMessageWorker implements Callable {

    private PushService pushService;
    private PushMessageVO pushMessageVO;

    public PushMessageWorker(PushService pushService, PushMessageVO pushMessageVO) {
        this.pushService = pushService;
        this.pushMessageVO = pushMessageVO;
    }

    public Boolean call() throws Exception {
        return pushService.pushMessage(pushMessageVO);
    }
    
}
