package com.secqme.manager.worker;

import com.secqme.CoreException;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.manager.EventManager;
import java.util.concurrent.Callable;

/**
 *
 * @author jameskhoo
 */
public class NotifyExpiredEventWorker implements Callable {

    private EventManager eventManager = null;
    private SecqMeEventVO eventVO = null;

    public NotifyExpiredEventWorker(EventManager emanager, SecqMeEventVO eVO) {
        this.eventManager = emanager;
        this.eventVO = eVO;
    }

    public Object call() throws CoreException {
        eventManager.processExpiredEvent(eventVO);
        return null;
    }

}
