package com.secqme.util.schedular.job;

import com.secqme.domain.dao.UserDAO;
import com.secqme.domain.factory.notification.EmailRecipientVOFactory;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.MessageType;
import com.secqme.domain.model.notification.email.TemplateBasedEmailVO;
import com.secqme.util.notification.DefaultNotificationEngine;
import com.secqme.util.notification.NotificationEngine;
import com.secqme.util.spring.BeanType;
import com.secqme.util.spring.DefaultSpringUtil;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by edward on 09/06/2015.
 */
public class MarketingEmailBlastByDeviceAndTimezoneJob implements Job {
    public static final String DATA_DEVICE = "device";
    public static final String DATA_TIMEZONE = "timezone";
    public static final String DATA_LAST_UPDATE_DAYS = "lastUpdateDays";
    public static final String DATA_TEMPLATE_NAME = "templateName";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private static final Logger myLog = Logger.getLogger(MarketingEmailBlastByDeviceAndTimezoneJob.class);

    private UserDAO userDAO = (UserDAO) DefaultSpringUtil.getInstance().getBean(BeanType.USER_DAO);
    private NotificationEngine notificationEngine = (NotificationEngine) DefaultSpringUtil.getInstance().getBean(BeanType.notificationEngine);

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap jobDataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        String device = jobDataMap.getString(DATA_DEVICE);
        String timezone = jobDataMap.getString(DATA_TIMEZONE);
        String templateName = jobDataMap.getString(DATA_TEMPLATE_NAME);

        long lastUpdateDaysAgo = jobDataMap.getInt(DATA_LAST_UPDATE_DAYS);
        Date updatedDate = new Date(new Date().getTime() - (lastUpdateDaysAgo * 24 * 60 * 60 * 1000));

        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        myLog.debug(String.format("Blasting marketing email to %s - %s - (%d) %s with template name %s", device, timezone, lastUpdateDaysAgo, sdf.format(updatedDate), templateName));

        List<UserVO> users = userDAO.findActiveUserByDeviceAndTimezone(device, timezone, updatedDate);

//        List<UserVO> users = new ArrayList<>();
//        users.add(USER_DAO.findByEmail("raymond_pluto@yahoo.co.id"));
//        users.add(USER_DAO.findByEmail("jinny.wong@gmail.com"));

        int  i = 0;
        for (final UserVO userVO : users) {
            TemplateBasedEmailVO emailVO = new TemplateBasedEmailVO(MessageType.MARKETING_CAMPAIGN, EmailRecipientVOFactory.createToUserVO(userVO), "default", "en_US", templateName);
            emailVO.setSenderEmail(null);
            emailVO.setSenderName(null);
            emailVO.getTags().add(templateName);
            myLog.debug(emailVO);
            notificationEngine.sendEmail(emailVO, new DefaultNotificationEngine.EmailLogFiller(null, userVO, null, null));
        }
    }
}
