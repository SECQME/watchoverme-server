package com.secqme.domain.factory.notification;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.push.PushMessageRecipientVO;
import com.secqme.util.ar.ParameterizableUtils;

/**
 * Created by edward on 15/05/2015.
 */
public class PushMessageRecipientVOFactory {

    public static PushMessageRecipientVO createToContactAsUser(UserVO fromUserVO, UserVO toUserVO) {
        PushMessageRecipientVO recipientVO = new PushMessageRecipientVO(toUserVO);
        ParameterizableUtils.fillWithUserPublicData(recipientVO, fromUserVO);

        recipientVO.putParam(ParameterizableUtils.PARAM_CONTACT_EMAIL, toUserVO.getEmailAddress());
        recipientVO.putParam(ParameterizableUtils.PARAM_CONTACT_PHONE, toUserVO.getMobileNo());
        recipientVO.putParam(ParameterizableUtils.PARAM_CONTACT_NAME, toUserVO.getAliasName());

        return recipientVO;
    }

    public static PushMessageRecipientVO createFromContactToUser(ContactVO fromContactVO, UserVO toUserVO) {
        PushMessageRecipientVO recipientVO = new PushMessageRecipientVO(toUserVO);
        ParameterizableUtils.fillWithContactPublicData(recipientVO, fromContactVO);
        ParameterizableUtils.fillWithUserPrivateData(recipientVO, toUserVO);
        return recipientVO;
    }
}
