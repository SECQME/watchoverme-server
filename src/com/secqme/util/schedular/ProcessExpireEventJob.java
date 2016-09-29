package com.secqme.util.schedular;

import com.secqme.CoreException;
import com.secqme.domain.dao.SecqMeEventDAO;
import com.secqme.domain.model.EventStatusType;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.manager.EventManager;
import com.secqme.util.spring.BeanType;
import com.secqme.util.spring.DefaultSpringUtil;
import org.apache.log4j.Logger;
import org.quartz.*;

import java.util.Date;

/**
 * User: James Khoo
 * Date: 7/25/13
 * Time: 3:28 PM
 */
public class ProcessExpireEventJob implements Job {
    private static final Logger myLog = Logger.getLogger(ProcessExpireEventJob.class);
    private static SecqMeEventDAO secqMeDAO = (SecqMeEventDAO) DefaultSpringUtil.getInstance().getBean(BeanType.SECQME_EVENT_DAO);
    private static EventManager eventManager = (EventManager) DefaultSpringUtil.getInstance().getBean(BeanType.eventManager);

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
        SecqMeEventVO eventVO;
        Long eventId = 0l;
        try {
            JobKey key = context.getJobDetail().getKey();
            JobDataMap dataMap = context.getJobDetail().getJobDataMap();
            eventId = dataMap.getLong(SecqMeEventVO.EVENT_ID_KEY);
            myLog.debug("Processing Expire Event Job, Key=" + key + ", eventId:" + eventId);
            eventVO = secqMeDAO.read(eventId);
            if (eventVO != null && EventStatusType.NEW.equals(eventVO.getStatus())) {
                // Do we need to check: eventVO.getEndTime().getTime() <= new Date().getTime() ???
                // Last time, we check for that condition, but it make intermittent bug in AWS server (the following code was not executed)
                myLog.debug("Trigger Expire Event for->" + eventVO.getUserVO().getUserid());
                eventManager.processExpiredEvent(eventVO);
            }
        } catch (CoreException ex) {
            myLog.error("Problem of executing event of Id:" + eventId, ex);
        }

    }
}
