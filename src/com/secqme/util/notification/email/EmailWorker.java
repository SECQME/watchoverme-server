package com.secqme.util.notification.email;

import com.secqme.domain.model.notification.email.EmailLogVO;
import com.secqme.domain.model.notification.email.EmailVO;

import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

/**
 * A Thread working of sending email in background thread
 * @author james
 */
public class EmailWorker implements Callable<List<EmailLogVO>> {

    private final static Logger myLog = Logger.getLogger(EmailWorker.class);

    private EmailService emailService = null;
    private EmailVO emailVO = null;
    private EmailService.OnBeforeEmailLogSavedListener onBeforeEmailLogSavedListener = null;

    public EmailWorker(EmailService emailService, EmailVO emailVO, EmailService.OnBeforeEmailLogSavedListener onBeforeEmailLogSavedListener) {
        this.emailService = emailService;
        this.emailVO = emailVO;
        this.onBeforeEmailLogSavedListener = onBeforeEmailLogSavedListener;
    }

    public List<EmailLogVO> call() throws Exception {
        myLog.debug("EmailWorker.call(): " + emailVO);
        try {
            return emailService.sendEmail(emailVO, onBeforeEmailLogSavedListener);
        } catch (Throwable t) {
            myLog.error("Failed to send email.", t);
            return null;
        }
    }
}
