
package com.secqme.util.notification.email;

import com.secqme.domain.model.notification.email.EmailLogVO;
import com.secqme.domain.model.notification.email.EmailRecipientVO;
import com.secqme.domain.model.notification.email.EmailVO;
import org.codehaus.jettison.json.JSONException;

import java.util.List;

/**
 * Interface of Email Notification Service, on providing methods of sending
 * Single, or Bulk SMS..
 *
 * @author james
 */
public interface EmailService {
    public boolean checkEmail(String messageId, String recipientEmail);

    public List<EmailLogVO> sendEmail(EmailVO emailVO, OnBeforeEmailLogSavedListener listener) throws JSONException;

    public interface OnBeforeEmailLogSavedListener {
        public void onBeforeEmailLogSaved(EmailVO emailVO, EmailRecipientVO emailRecipientVO, EmailLogVO log);
    }
}
