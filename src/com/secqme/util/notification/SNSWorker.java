package com.secqme.util.notification;

import com.secqme.domain.model.notification.UserSNSConfigVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.sns.SNSMessageVO;
import com.secqme.util.notification.sns.SNSManager;
import com.secqme.util.notification.sns.SNSService;

import java.util.HashMap;
import java.util.concurrent.Callable;
import org.apache.log4j.Logger;

/**
 * A Thread working of updating user's status to each
 * respective Social Network
 * @author james
 */
public class SNSWorker implements Callable {

    private final static Logger myLog = Logger.getLogger(SNSWorker.class);
    private SNSMessageVO snsMessageVO;
    private SNSManager snsManager;

    public SNSWorker(SNSManager snsManager, SNSMessageVO snsMessageVO) {
        this.snsManager = snsManager;
        this.snsMessageVO = snsMessageVO;
    }
    
    public Boolean call() throws Exception {
        UserVO userVO = snsMessageVO.getUserVO();

        myLog.debug("Thread:" + Thread.currentThread().getName() + " attempt to update SNS Status to:" + userVO.getUserid());
        snsManager.publishNotification(snsMessageVO);

        return true;
    }

}
