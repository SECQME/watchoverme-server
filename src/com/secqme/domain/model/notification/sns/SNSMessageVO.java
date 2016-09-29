package com.secqme.domain.model.notification.sns;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.MessageType;

/**
 * Created by edward on 22/05/2015.
 */
public abstract class SNSMessageVO {

    private MessageType messageType;
    private UserVO userVO;

    public SNSMessageVO(MessageType messageType, UserVO userVO) {
        this.messageType = messageType;
        this.userVO = userVO;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }
}
