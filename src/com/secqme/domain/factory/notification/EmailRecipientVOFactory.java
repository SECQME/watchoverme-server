package com.secqme.domain.factory.notification;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.email.EmailRecipientVO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.util.ar.ParameterizableUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by edward on 28/04/2015.
 */
public class EmailRecipientVOFactory {

    public static EmailRecipientVO createToContactVO(ContactVO contactVO) {
        EmailRecipientVO emailRecipientVO = new EmailRecipientVO(contactVO.getNickName(), contactVO.getEmailAddress());
        ParameterizableUtils.fillWithContactPrivateData(emailRecipientVO, contactVO);
        return emailRecipientVO;
    }

    public static EmailRecipientVO createToUserVO(ContactVO contactVO) {
        UserVO userVO = contactVO.getUserVO();
        EmailRecipientVO emailRecipientVO = new EmailRecipientVO(userVO.getNickName(), userVO.getEmailAddress());
        ParameterizableUtils.fillWithContactPublicData(emailRecipientVO, contactVO);
        return emailRecipientVO;
    }

    public static EmailRecipientVO createToUserVO(UserVO userVO) {
        EmailRecipientVO emailRecipientVO = new EmailRecipientVO(userVO.getNickName(), userVO.getEmailAddress());
        ParameterizableUtils.fillWithUserPrivateData(emailRecipientVO, userVO);
        return emailRecipientVO;
    }

    public static List<EmailRecipientVO> createToUserVO(List<UserVO> userVOs) {
        List<EmailRecipientVO> result = new ArrayList<>(userVOs.size());
        for (UserVO userVO : userVOs) {
            EmailRecipientVO emailRecipientVO = new EmailRecipientVO(userVO.getNickName(), userVO.getEmailAddress());
            ParameterizableUtils.fillWithUserPrivateData(emailRecipientVO, userVO);
            result.add(emailRecipientVO);
        }

        return result;
    }

    public static EmailRecipientVO createToGiftBuyer(GiftPaymentLogVO giftPaymentLogVO) {
        EmailRecipientVO emailRecipientVO = new EmailRecipientVO(giftPaymentLogVO.getName(), giftPaymentLogVO.getEmail());
        ParameterizableUtils.fillWithGiftData(emailRecipientVO, giftPaymentLogVO);
        return emailRecipientVO;
    }

    public static EmailRecipientVO createToGiftReceiver(GiftPaymentLogVO giftPaymentLogVO) {
        EmailRecipientVO emailRecipientVO = new EmailRecipientVO(giftPaymentLogVO.getRecipientName(), giftPaymentLogVO.getRecipientEmail());
        ParameterizableUtils.fillWithGiftData(emailRecipientVO, giftPaymentLogVO);
        return emailRecipientVO;
    }
}
