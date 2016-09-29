package com.secqme.util.schedular.job;

import com.secqme.manager.payment.UserSubscriptionInfoManager;
import com.secqme.util.spring.BeanType;
import com.secqme.util.spring.DefaultSpringUtil;
import org.apache.log4j.Logger;
import org.quartz.*;

/**
 * User: James Khoo
 * Date: 7/25/13
 * Time: 3:28 PM
 */
public class ProcessSubscriptionInfoJob implements Job {
    private static final Logger myLog = Logger.getLogger(ProcessSubscriptionInfoJob.class);
    private static UserSubscriptionInfoManager userSubscriptionInfoManager = 
    		(UserSubscriptionInfoManager) DefaultSpringUtil.getInstance().getBean(BeanType.userSubscriptionInfoManager);

    public void execute(JobExecutionContext context)
            throws JobExecutionException {
    	userSubscriptionInfoManager.processSubscriptionInfo();
    }
}
