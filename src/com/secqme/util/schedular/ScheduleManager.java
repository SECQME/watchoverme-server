package com.secqme.util.schedular;

import com.secqme.domain.model.event.SecqMeEventVO;

/**
 * User: James Khoo
 * Date: 7/25/13
 * Time: 1:21 PM
 */
public interface ScheduleManager {
    public void initScheduleManager();
    public void shutdown();
    public void scheduleCheckExpireEventJob(SecqMeEventVO eventVO);
    public void removeExpireEventJob(SecqMeEventVO eventVO);
    public void rescheduleExpireEventJob(SecqMeEventVO eventVO);
    public void scheduleSubscriptionInfoJob();

}
