package com.secqme.util.payment;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import org.codehaus.jettison.json.JSONObject;

/**
 * User: James Khoo
 * Date: 4/15/14
 * Time: 4:05 PM
 */
public interface PaymentUtil {
    public void processPayment(String receiptData, UserVO userVO) throws CoreException;
    public void processPaymentWithVerifiedReceipt(JSONObject verifiedReceipt, UserVO userVO) throws CoreException;

}
