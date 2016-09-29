package com.secqme.domain.model.notification.email;

import java.util.ArrayList;
import java.util.List;

import com.secqme.domain.model.notification.MessageType;
import com.secqme.domain.model.notification.email.EmailRecipientVO;
import com.secqme.domain.model.notification.email.EmailVO;

import javax.validation.constraints.NotNull;

/**
 *
 * @author james
 */
public class ContentBasedEmailVO extends EmailVO {
    private String subject;
    private String body;

    public ContentBasedEmailVO(@NotNull MessageType messageType, @NotNull EmailRecipientVO emailRecipientVO, String subject, String body) {
        super(messageType, new ArrayList<EmailRecipientVO>());
        this.recipients.add(emailRecipientVO);
        this.subject = subject;
        this.body = body;
    }

    public ContentBasedEmailVO(@NotNull MessageType messageType, @NotNull List<EmailRecipientVO> emailRecipientVOs, String subject, String body) {
        super(messageType, emailRecipientVOs);
        this.subject = subject;
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Override
    public String toString() {
        return "{\"recipients\":" + recipients + "," +
                 "\"subject\":\""  + this.subject + "\"," +
                 "\"body\":\"" + this.body + "\"}";
    }
}
