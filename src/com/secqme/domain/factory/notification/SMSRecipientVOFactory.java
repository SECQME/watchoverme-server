package com.secqme.domain.factory.notification;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.sms.SMSRecipientVO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.util.ar.ParameterizableUtils;

/**
 * Created by edward on 15/05/2015.
 */
public class SMSRecipientVOFactory {

    public static SMSRecipientVO createToContactVO(ContactVO contactVO) {
        SMSRecipientVO smsRecipientVO = new SMSRecipientVO(contactVO.getCountryVO(), contactVO.getMobileNo());
        ParameterizableUtils.fillWithContactPrivateData(smsRecipientVO, contactVO);
        return smsRecipientVO;
    }

    public static SMSRecipientVO createToUserVO(ContactVO contactVO) {
        UserVO userVO = contactVO.getUserVO();
        SMSRecipientVO smsRecipientVO = new SMSRecipientVO(userVO.getMobileCountry(), userVO.getMobileNo());
        ParameterizableUtils.fillWithContactPublicData(smsRecipientVO, contactVO);
        return smsRecipientVO;
    }

    public static SMSRecipientVO createToUserVO(UserVO userVO) {
        SMSRecipientVO smsRecipientVO = new SMSRecipientVO(userVO.getMobileCountry(), userVO.getMobileNo());
        ParameterizableUtils.fillWithUserPrivateData(smsRecipientVO, userVO);
        return smsRecipientVO;
    }

    public static SMSRecipientVO createToGiftBuyer(GiftPaymentLogVO giftPaymentLogVO) {
        SMSRecipientVO smsRecipientVO = new SMSRecipientVO(giftPaymentLogVO.getMobileCountry(), giftPaymentLogVO.getMobileNumber());
        ParameterizableUtils.fillWithGiftData(smsRecipientVO, giftPaymentLogVO);
        return smsRecipientVO;
    }

    public static SMSRecipientVO createToGiftReceiver(GiftPaymentLogVO giftPaymentLogVO) {
        SMSRecipientVO smsRecipientVO = new SMSRecipientVO(giftPaymentLogVO.getRecipientMobileCountry(), giftPaymentLogVO.getRecipientMobileNumber());
        ParameterizableUtils.fillWithGiftData(smsRecipientVO, giftPaymentLogVO);
        return smsRecipientVO;
    }
}
