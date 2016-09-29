package com.secqme.util.schedular;

import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.util.schedular.job.ContactInvitationReminderJob;
import com.secqme.util.schedular.job.MarketingEmailBlastByDeviceAndTimezoneJob;
import com.secqme.util.schedular.job.ProcessSubscriptionInfoJob;
import org.apache.log4j.Logger;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

/**
 * User: James Khoo
 * Date: 7/25/13
 * Time: 1:26 PM
 */
public class DefaultScheduleManager implements ScheduleManager {
    public static final Logger myLog = Logger.getLogger(DefaultScheduleManager.class);
    private static final String EXPIRE_EVENT_JOB_KEY = "expireEvent:";
    private static final String EXPIRE_EVENT_JOB_GROUP_NAME = "eventGroup";
    private static final String EXPIRE_EVENT_TRIGGER_KEY = "expireEventTrigger:";
    
    private static final String SUBSCRIPTION_INFO_JOB_KEY = "subscriptionInfo";
    private static final String SUBSCRIPTION_INFO_JOB_GROUP_NAME = "subscriptionInfoGroup";
    private static final String SUBSCRIPTION_INFO_TRIGGER_KEY = "subscriptionInfoTrigger";

    private static final String EC_REMINDER_JOB_KEY = "reminderEmail";
    private static final String EC_REMINDER_JOB_GROUP_NAME = "reminderEmailGroup";
    private static final String EC_REMINDER_TRIGGER_KEY = "reminderEmailTrigger";

    private static final String MARKETING_EMAIL_JOB_KEY = "marketingEmail";
    private static final String MARKETING_EMAIL_JOB_GROUP_NAME = "marketingEmaiGroupl";
    private static final String MARKETING_EMAIL_TRIGGER_KEY = "marketingEmailTrigger";
    
    private String SUBSCRIPTION_INFO_TRIGGER_SCHEDULE;
    private String EC_REMINDER_TRIGGER_SCHEDULE;
    private boolean enabled;
    private static Scheduler scheduler;

    public DefaultScheduleManager(String SUBSCRIPTION_INFO_TRIGGER_SCHEDULE, String EC_REMINDER_TRIGGER_SCHEDULE, boolean enabled) {
        this.SUBSCRIPTION_INFO_TRIGGER_SCHEDULE = SUBSCRIPTION_INFO_TRIGGER_SCHEDULE;
        this.EC_REMINDER_TRIGGER_SCHEDULE = EC_REMINDER_TRIGGER_SCHEDULE;
        this.enabled = enabled;
    }

    @Override
    public void initScheduleManager() {
        if (enabled) {
            try {
                myLog.debug("Preparing to start Quartz Scheduler");
                scheduler = StdSchedulerFactory.getDefaultScheduler();
                scheduler.start();
                myLog.debug(scheduler.getSchedulerName() + " scheduler started");
                scheduleSubscriptionInfoJob();
                processECReminderEmail();

//                try {
//                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                    scheduleMarketingEmail("XinchInLASorry", sdf.parse("2015-06-09 23:00:00"), "", "America/Los_Angeles", 180, "XinchInLASorry");
//
//
//                } catch (ParseException e) {
//                    myLog.debug("Parse date failed");
//                }
//
//                JobKey jobKey = new JobKey(MARKETING_EMAIL_JOB_KEY + "XinchInLA", MARKETING_EMAIL_JOB_GROUP_NAME);
//                if (scheduler.checkExists(jobKey)) {
//                    scheduler.deleteJob(jobKey);
//                }
//                scheduleMarketingEmail("XinchInLASorry", "0 15 12 10 6 ? *", "", "America/Los_Angeles", 180, "XinchInLASorry");
            } catch (SchedulerException se) {
                myLog.error("Error on starting the Quartz Scheduler..", se);
            }
        } else {
            myLog.debug("DefaultScheduleManager is disabled.");
        }
    }

    @Override
    public void shutdown() {
        if (enabled) {
            try {
                myLog.debug("Preparing to shutdown the  Quartz Scheduler");
                scheduler.shutdown(true);
                myLog.debug(scheduler.getSchedulerName() + " is down");
            } catch (SchedulerException se) {
                myLog.error("Error on shutting down the Quartz Scheduler..", se);
            }
        } else {
            myLog.debug("DefaultScheduleManager is disabled.");
        }
    }

    @Override
    public void scheduleCheckExpireEventJob(SecqMeEventVO eventVO) {
        myLog.debug("Schedule the new Job for event:" + eventVO.getId() + ", user:" + eventVO.getUserVO().getUserid() +
                " endTime at " + eventVO.getEndTime());
        try {
            JobDetail myJob = newJob(ProcessExpireEventJob.class)
                    .withIdentity(EXPIRE_EVENT_JOB_KEY + eventVO.getId(), EXPIRE_EVENT_JOB_GROUP_NAME)
                    .usingJobData(SecqMeEventVO.EVENT_ID_KEY, eventVO.getId())
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(EXPIRE_EVENT_TRIGGER_KEY + eventVO.getId(), EXPIRE_EVENT_JOB_GROUP_NAME)
                    .startAt(eventVO.getEndTime())
                    .forJob(myJob)
                    .build();

            myLog.debug("Trigger Detail, startTime->" + trigger.getStartTime() + ", finalFireTime->" + trigger.getFinalFireTime());
            scheduler.scheduleJob(myJob, trigger);
        } catch (SchedulerException se) {
            myLog.error("Error on schedule the Job for event from:" + eventVO.getUserVO().getUserid(), se);
        }
    }

    @Override
    public void removeExpireEventJob(SecqMeEventVO eventVO) {
        myLog.debug("Removing expireEventJob, eventId:" + eventVO.getId() + ", user:" + eventVO.getUserVO().getUserid() +
                " endTime at " + eventVO.getEndTime());
        try {
            JobKey jobKey = new JobKey(EXPIRE_EVENT_JOB_KEY + eventVO.getId(), EXPIRE_EVENT_JOB_GROUP_NAME);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
        } catch (SchedulerException se) {
            myLog.error("Error on removing the expire Event Job for" + eventVO.getUserVO().getUserid(), se);
        }
    }

    @Override
    public void rescheduleExpireEventJob(SecqMeEventVO eventVO) {
        myLog.debug("Rescheduling EventJob, eventId:" + eventVO.getId() + ", user:" + eventVO.getUserVO().getUserid() +
                " new endTime at " + eventVO.getEndTime());
        TriggerKey triggerKey;
        try {
            triggerKey = triggerKey(EXPIRE_EVENT_TRIGGER_KEY + eventVO.getId(), EXPIRE_EVENT_JOB_GROUP_NAME);
            if(scheduler.getTrigger(triggerKey) != null) {
                Trigger trigger = newTrigger()
                        .withIdentity(EXPIRE_EVENT_TRIGGER_KEY + eventVO.getId(), EXPIRE_EVENT_JOB_GROUP_NAME)
                        .startAt(eventVO.getEndTime())
                        .build();
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        } catch (SchedulerException se) {
            myLog.error("Error on reschedule expireEvent Job for:" + eventVO.getUserVO().getUserid(), se);
        }

    }
    
    @Override
    public void scheduleSubscriptionInfoJob() {
        myLog.debug("scheduleSubscriptionInfoJob ");
        try {
        	Date startDate = new Date(new Date().getTime() + (10000 * 1));
        	JobKey jobKey = new JobKey(SUBSCRIPTION_INFO_JOB_KEY, SUBSCRIPTION_INFO_JOB_GROUP_NAME);
			if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
			}
            JobDetail myJob = newJob(ProcessSubscriptionInfoJob.class)
                    .withIdentity(SUBSCRIPTION_INFO_JOB_KEY, SUBSCRIPTION_INFO_JOB_GROUP_NAME)
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(SUBSCRIPTION_INFO_TRIGGER_KEY, SUBSCRIPTION_INFO_JOB_GROUP_NAME)
                    .withSchedule(cronSchedule(SUBSCRIPTION_INFO_TRIGGER_SCHEDULE))
//            	    .startAt(startDate)
//            	    .withSchedule(simpleSchedule()
//                            .withIntervalInMinutes(5)
//                            .repeatForever())
                    .forJob(myJob)
                    .build();
            scheduler.scheduleJob(myJob, trigger);
        } catch (SchedulerException se) {
            myLog.error("Error on scheduleSubscriptionInfoJob ", se);
        }
    }

    public void scheduleMarketingEmail(String key, String cronSchedule, String device, String timezone, int lastUpdateDaysAgo, String templateName) {
        myLog.debug("scheduleMarketingEmail");
        try {
            JobKey jobKey = new JobKey(MARKETING_EMAIL_JOB_KEY + key, MARKETING_EMAIL_JOB_GROUP_NAME);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }

            JobDetail myJob = newJob(MarketingEmailBlastByDeviceAndTimezoneJob.class)
                    .withIdentity(MARKETING_EMAIL_JOB_KEY + key, MARKETING_EMAIL_JOB_GROUP_NAME)
                    .usingJobData(MarketingEmailBlastByDeviceAndTimezoneJob.DATA_DEVICE, device)
                    .usingJobData(MarketingEmailBlastByDeviceAndTimezoneJob.DATA_TIMEZONE, timezone)
                    .usingJobData(MarketingEmailBlastByDeviceAndTimezoneJob.DATA_LAST_UPDATE_DAYS, lastUpdateDaysAgo)
                    .usingJobData(MarketingEmailBlastByDeviceAndTimezoneJob.DATA_TEMPLATE_NAME, templateName)
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(MARKETING_EMAIL_TRIGGER_KEY + key, MARKETING_EMAIL_JOB_GROUP_NAME)
                    .withSchedule(cronSchedule(cronSchedule))
                    .forJob(myJob)
                    .build();
            scheduler.scheduleJob(myJob, trigger);

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            myLog.debug(String.format("Schedule marketing email to %s - %s - %d days ago with template name %s. Next start at: %s.", device, timezone, lastUpdateDaysAgo, templateName, sdf.format(trigger.getNextFireTime())));
        } catch (SchedulerException se) {
            myLog.error("Error on scheduleMarketingEmail ", se);
        }
    }

    // EC = Emergency Contact
    public void processECReminderEmail() {
        myLog.debug("processEmergencyContactReminderEmail");
        try{
            JobKey jobKey = new JobKey(EC_REMINDER_JOB_KEY, EC_REMINDER_JOB_GROUP_NAME);
            if (scheduler.checkExists(jobKey)) {
                scheduler.deleteJob(jobKey);
            }
            JobDetail myJob = newJob(ContactInvitationReminderJob.class)
                    .withIdentity(EC_REMINDER_JOB_KEY,EC_REMINDER_JOB_GROUP_NAME)
                    .build();

            Trigger trigger = newTrigger()
                    .withIdentity(EC_REMINDER_TRIGGER_KEY,EC_REMINDER_JOB_GROUP_NAME)
                    .withSchedule(cronSchedule(EC_REMINDER_TRIGGER_SCHEDULE))
                    .forJob(myJob)
                    .build();
            scheduler.scheduleJob(myJob,trigger);
        }catch (SchedulerException se) {
            myLog.error("Error on scheduleEmergencyContactReminderJob ", se);
        }
    }
}
