package com.secqme.manager.worker;

import com.secqme.domain.model.EventStatusType;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.util.notification.NotificationEngine;
import java.util.concurrent.Callable;

/**
 *
 * @author jameskhoo
 */
public class SendEventEndConfirmWorker implements Callable {

    private NotificationEngine notifiEngine = null;
    private SecqMeEventVO eventVO = null;

    public SendEventEndConfirmWorker(NotificationEngine engine, SecqMeEventVO event) {
        this.eventVO = event;
        this.notifiEngine = engine;
    }

    public SecqMeEventVO call() {
//        notifiEngine.sendEventEndConfirmation(eventVO);
        eventVO.setStatus(EventStatusType.CONFIRM);
        return eventVO;
    }
}
