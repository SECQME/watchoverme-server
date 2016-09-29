package com.secqme.tasks;

import com.secqme.CoreException;
import com.secqme.domain.dao.SecqMeEventDAO;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.manager.EventManager;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author jameskhoo
 */
public class ProcessEvent {

    private SecqMeEventDAO secqMeDAO = null;
    private static final Logger myLog = Logger.getLogger(ProcessEvent.class);
    private EventManager eventManager = null;

    public ProcessEvent(EventManager evtManager, SecqMeEventDAO secqMeDAO) {
        this.secqMeDAO = secqMeDAO;
        eventManager = evtManager;
    }

    public void checkEvent() {
        Date expireEndTime = DateUtils.round(DateUtils.addSeconds(new Date(), 30), Calendar.MINUTE);
        try {
            processExpireEvents(expireEndTime);
        } catch (CoreException ce) {
            myLog.error(ce.getMessage(), ce);
        }
    }

    private void processExpireEvents(Date endTime) throws CoreException {
        myLog.debug("Searching expired SecqMe Event, EndTime:" + endTime);
        List<SecqMeEventVO> eventList =
                secqMeDAO.findNewEventsLessThenEndTime(endTime);
        if (eventList != null && eventList.size() > 0) {
            myLog.debug("Extracted total of: " + eventList.size() + " expired Events");
            eventManager.processExpireEventList(eventList);
        } else {
            myLog.debug("No Expire events founds with end time less then:" + endTime);
        }
    }


}
