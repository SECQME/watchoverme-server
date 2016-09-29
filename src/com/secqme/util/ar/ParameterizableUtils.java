package com.secqme.util.ar;

import com.secqme.domain.model.ContactVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.ar.Parameterizable;
import com.secqme.domain.model.event.SecqMeEventVO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.util.HtmlUtils;
import com.secqme.util.SystemProperties;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by edward on 20/05/2015.
 */
public class ParameterizableUtils {
    public static final String PARAM_BASE_URL = "BASE_URL";
    public static final String PARAM_USER_EMAIL = "USER_EMAIL";
    public static final String PARAM_USER_NAME = "USER_NAME";
    public static final String PARAM_USER_EMAIL_TOKEN = "USER_EMAIL_TOKEN";
    public static final String PARAM_USER_PASSWORD_RESET_TOKEN = "RESET_PASSWORD_TOKEN";
    public static final String PARAM_USER_EMAIL_ACTIVATION_TOKEN = "EMAIL_ACTIVATION_TOKEN";
    public static final String PARAM_CONTACT_CODE = "CONTACT_CODE";
    public static final String PARAM_CONTACT_EMAIL = "CONTACT_EMAIL";
    public static final String PARAM_CONTACT_NAME = "CONTACT_NAME";
    public static final String PARAM_CONTACT_PHONE = "CONTACT_PHONE_NUMBER";
    public static final String PARAM_GIFT_BUYER_NAME = "GIFT_BUYER_NAME";
    public static final String PARAM_GIFT_BUYER_EMAIL = "GIFT_BUYER_EMAIL";
    public static final String PARAM_GIFT_BUYER_PHONE = "GIFT_BUYER_PHONE";
    public static final String PARAM_GIFT_RECEIVER_NAME = "GIFT_RECEIVER_NAME";
    public static final String PARAM_GIFT_RECEIVER_EMAIL = "GIFT_RECEIVER_EMAIL";
    public static final String PARAM_GIFT_RECEIVER_PHONE = "GIFT_RECEIVER_PHONE";
    public static final String PARAM_GIFT_MESSAGE = "GIFT_MESSAGE";
    public static final String PARAM_PIN = "PIN";

    private static final String SHORT_DATE_FORMAT_STRING = "MMMdd HH:mm";
    private static final String DEFAULT_DATE_FORMAT_STRING = "MMM-dd hh:mm aaa";
    private static final String DEFAULT_TIMEZONE_STRING = "Etc/GMT+0";

    public static <K, V> Map<K, V> mergeParams(Parameterizable<K, V>... params) {
        Map<K, V> result = new HashMap<>();
        for (Parameterizable<K, V> param : params) {
            result.putAll(param.getParams());
        }
        return result;
    }

    public static void fillWithBaseUrl(Parameterizable<String, Object> params) {
        params.putParam(PARAM_BASE_URL, SystemProperties.instance.getString(SystemProperties.PROP_EMAIL_BASE_URL));
    }

    public static void fillWithContactPublicData(Parameterizable<String, Object> params, ContactVO contactVO) {
        params.putParam(PARAM_CONTACT_EMAIL, contactVO.getEmailAddress());
        params.putParam(PARAM_CONTACT_PHONE, contactVO.getMobileNo());
        params.putParam(PARAM_CONTACT_NAME, contactVO.getNickName());

        fillWithUserPublicData(params, contactVO.getUserVO());
    }

    public static void fillWithContactPrivateData(Parameterizable<String, Object> params, ContactVO contactVO) {
        fillWithContactPublicData(params, contactVO);
        params.putParam(PARAM_CONTACT_CODE,contactVO.getContactToken());
    }

    public static void fillWithUserPublicData(Parameterizable<String, Object> params, UserVO userVO) {
        params.putParam(PARAM_USER_NAME, userVO.getNickName());
        params.putParam(PARAM_USER_EMAIL, userVO.getEmailAddress());
    }

    public static void fillWithUserPrivateData(Parameterizable<String, Object> params, UserVO userVO) {
        fillWithUserPublicData(params, userVO);
        params.putParam(PARAM_USER_EMAIL_TOKEN, userVO.getSubscribeEmailNewsLetterToken());
        params.putParam(PARAM_USER_PASSWORD_RESET_TOKEN, userVO.getPasswordResetToken());
        params.putParam(PARAM_USER_EMAIL_ACTIVATION_TOKEN, userVO.getEmailVerificationToken());
    }

    public static void fillParamsWithPin(Parameterizable<String, Object> params, String pin) {
        params.putParam(PARAM_PIN, pin);
    }

    public static void fillWithGiftData(Parameterizable<String, Object> params, GiftPaymentLogVO giftPaymentLogVO) {
        params.putParam(PARAM_GIFT_BUYER_NAME, giftPaymentLogVO.getName());
        params.putParam(PARAM_GIFT_BUYER_EMAIL, giftPaymentLogVO.getEmail());
        params.putParam(PARAM_GIFT_BUYER_PHONE, giftPaymentLogVO.getMobileCountry().getCallingCode() + giftPaymentLogVO.getMobileNumber());
        params.putParam(PARAM_GIFT_RECEIVER_NAME, giftPaymentLogVO.getRecipientName());
        params.putParam(PARAM_GIFT_RECEIVER_EMAIL, giftPaymentLogVO.getRecipientEmail());
        params.putParam(PARAM_GIFT_RECEIVER_PHONE, giftPaymentLogVO.getRecipientMobileCountry().getCallingCode() + giftPaymentLogVO.getRecipientMobileNumber());
        params.putParam(PARAM_GIFT_MESSAGE, giftPaymentLogVO.getMessage());
    }

    public static void fillEventParams(Parameterizable<String, Object> params, SecqMeEventVO event) {
        fillWithUserPublicData(params, event.getUserVO());

        params.putParam("TRACK_URL", event.getTrackingURL());

        String timezoneStr = DEFAULT_TIMEZONE_STRING;
        if (StringUtils.isNotEmpty(event.getEventTimeZone())) {
            timezoneStr = event.getEventTimeZone();
        } else if (StringUtils.isNotEmpty(event.getUserVO().getTimeZone())) {
            timezoneStr = event.getUserVO().getTimeZone();
        }

        SimpleDateFormat smsDateFormat = new SimpleDateFormat(SHORT_DATE_FORMAT_STRING);
        smsDateFormat.setTimeZone(TimeZone.getTimeZone(timezoneStr));

        SimpleDateFormat defaultDateFormat = new SimpleDateFormat(DEFAULT_DATE_FORMAT_STRING);
        defaultDateFormat.setTimeZone(TimeZone.getTimeZone(timezoneStr));

        String timezoneMsg = " (Time Zone:" + timezoneStr + ")";

        params.putParam("CURRENT_TIME", defaultDateFormat.format(new Date()) + timezoneMsg);
        params.putParam("EVENT_START_TIME", defaultDateFormat.format(event.getStartTime()) + timezoneMsg);

        String eventTimeStr = smsDateFormat.format(event.getStartTime());
        params.putParam("EVENT_RECEIVED_TIME", eventTimeStr);

        if (event.getEventDurationInMinutes() != null) {
            params.putParam("EVENT_DURATION", event.getEventDurationInMinutes());
        }

        if (event.getEndTime() == null) {
            params.putParam("EVENT_END_TIME", "NA");
        } else {
            params.putParam("EVENT_END_TIME", defaultDateFormat.format(event.getEndTime()) + timezoneMsg);
        }

        if (StringUtils.isNotEmpty(event.getMessage())) {
            String eventMessage = HtmlUtils.escape(event.getMessage());
            params.putParam("EVENT_MESSAGE", eventMessage);
        } else {
            params.putParam("EVENT_MESSAGE", " ");
        }
    }
}
