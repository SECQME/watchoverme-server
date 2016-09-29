package com.secqme.util.schedular.job;

import com.secqme.manager.ContactManager;
import com.secqme.util.spring.BeanType;
import com.secqme.util.spring.DefaultSpringUtil;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Created by Edmund on 5/5/15.
 */
public class ContactInvitationReminderJob implements Job {

    private static final Logger myLog = Logger.getLogger(ContactInvitationReminderJob.class);

    private ContactManager contactManager = (ContactManager) DefaultSpringUtil.getInstance().getBean(BeanType.CONTACT_MANAGER);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        myLog.debug("ContactInvitationReminderJob starts");
        contactManager.remindContactInvitation();
    }
}
