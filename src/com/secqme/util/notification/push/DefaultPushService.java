package com.secqme.util.notification.push;

import com.secqme.domain.model.notification.push.ContentBasedPushMessageVO;
import com.secqme.domain.model.notification.push.PushMessageVO;
import com.secqme.util.ar.ARTemplateEngine;
import org.apache.log4j.Logger;

import com.secqme.CoreException;
import com.secqme.domain.dao.PushMessageLogDAO;
import com.secqme.domain.model.MobilePlatformType;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.pushmessage.PushMessageLogVO;
import com.secqme.domain.model.pushmessage.UserPushMessageToken;
import com.secqme.util.rest.RestUtil;

import java.io.File;
import java.util.Date;

import javapns.Push;
import javapns.communication.exceptions.CommunicationException;
import javapns.communication.exceptions.KeystoreException;
import com.google.android.gcm.server.*;
import java.io.IOException;

@Deprecated
public class DefaultPushService extends BasePushService implements PushService {

    private static final String PRODUCTION_ENVIRONEMENT = "production";
    private static final String SANDBOX_ENVIRONMENT = "sandbox";
    private File iosAPNCertFile = null;
    private String iosCertPassword;
    private Boolean isProductionEnvironment = true;
    private PushMessageLogDAO pushMessageLogDAO = null;
    private Sender googlePushMessageSender = null;
    private static final Logger myLog = Logger.getLogger(DefaultPushService.class);
    private RestUtil restUtil = null;
    
    public DefaultPushService(PushMessageLogDAO pushMsgLogDAO,
            String iosAPNCertFilePath,
            String iosCertPassword,
            String environment,
            String googleGCMAPIKey,
            ARTemplateEngine arTemplateEngine,
            RestUtil restUtil) {
        super(arTemplateEngine, restUtil);

        this.pushMessageLogDAO = pushMsgLogDAO;
        iosAPNCertFile = new File(iosAPNCertFilePath);
        this.iosCertPassword = iosCertPassword;
        if (environment.equalsIgnoreCase(PRODUCTION_ENVIRONEMENT)) {
            isProductionEnvironment = true;
        } else {
            isProductionEnvironment = false;
        }
        googlePushMessageSender = new Sender(googleGCMAPIKey);
        this.restUtil = restUtil;
        myLog.debug("Initated DefaultPushService, filePath" + iosAPNCertFilePath + ", environment:" + environment);
    }

    @Override
    protected boolean pushContentBasedPushMessageToSingleRecipient(ContentBasedPushMessageVO contentBasedPushMessageVO) {
        UserVO userVO = contentBasedPushMessageVO.getRecipients().get(0).getUserVO();
        String message = contentBasedPushMessageVO.getBody();

        if (userVO.getPushMessageTokenList() != null
                && !userVO.getPushMessageTokenList().isEmpty()) {
            for (UserPushMessageToken msgToken : userVO.getPushMessageTokenList()) {
                if (MobilePlatformType.iOS.equals(msgToken.getPlatformType())) {
                    pushIOSMessage(msgToken, message);
                } else if (MobilePlatformType.Android.equals(msgToken.getPlatformType())) {
                    pushAndriodMessage(msgToken, message);
                }
                insertPushMessageLog(msgToken, message);
            }
        }

        return true;
    }

    private boolean pushAndriodMessage(UserPushMessageToken msgToken, String message) {
        boolean sendMessageSuccess = true;
        myLog.debug("Sending push message:" + message + " to " + msgToken.getUserVO() + "'s " + msgToken.getPlatformType().name());

        Message pushMessage =
                new Message.Builder().addData("body", message).build();
        try {
            //message, device id, retry 
            Result result = googlePushMessageSender.send(pushMessage, msgToken.getToken(), 5);
            if (result.getMessageId() != null) {
//                String canonicalRegId = result.getCanonicalRegistrationId();
//                if (canonicalRegId != null) {
//                    // same device has more than on registration ID: update database
//                }
            } else {
//                String error = result.getErrorCodeName();
//                if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
//                    // application has been removed from device - unregister database
//                }
            }
        } catch (IOException e) {
           myLog.error("Error on sending message to Android " + msgToken.getUserVO().getUserid(), e);
        }
        return sendMessageSuccess;
    }

    private boolean pushIOSMessage(UserPushMessageToken msgToken, String message) {
        boolean sendMessageSuccess = true;
        myLog.debug("Sending push message:" + message + " to " + msgToken.getUserVO() + "'s " + msgToken.getPlatformType().name());

        try {
            Push.alert(message, iosAPNCertFile, iosCertPassword, isProductionEnvironment, msgToken.getToken());
        } catch (CommunicationException ex) {
            myLog.error(ex.getMessage(), ex);
            sendMessageSuccess = false;
        } catch (KeystoreException ex) {
            myLog.error(ex.getMessage(), ex);
            sendMessageSuccess = false;
        }

        return sendMessageSuccess;
    }

    private void insertPushMessageLog(UserPushMessageToken msgToken, String message) {
        PushMessageLogVO logVO = new PushMessageLogVO();
        logVO.setUserid(msgToken.getUserVO().getUserid());
        logVO.setToken(msgToken.getToken());
        logVO.setPlatform(msgToken.getPlatformType().name());
        logVO.setMessage(message);
        logVO.setSendTime(new Date());
        pushMessageLogDAO.create(logVO);

    }
}
