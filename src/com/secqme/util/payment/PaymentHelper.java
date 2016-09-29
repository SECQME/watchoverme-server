
package com.secqme.util.payment;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.payment.PaymentGWVO;
import com.secqme.domain.model.payment.PendingPaymentVO;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.domain.model.payment.UserSubscriptionInfoVO;
import java.util.Date;

/**
 *  Helper Class for payments
 * @author coolboykl
 */
public interface PaymentHelper {
    
    public PendingPaymentVO retrivePendingPayment(String txnid, String paymentGWName);
    public void deletePendingPayment(String txnid, String paymentGWName);
    public void insertNewPendingPaymentVO(String txnid, String paymentGWName, String payerId,
                                          PricingPackageVO pricePkgVO, UserVO userVO,
                                          Double grossAmt, Double paymentFee, String status);
    public UserSubscriptionInfoVO createReplaceSubscriptionRecord(UserVO userVO, PricingPackageVO pricePkgVO,
                                            PaymentGWVO gwVO, Double paymentAmt,
                                            Date effectiveDate, Date nextPaymentDate, String profileId,
                                            String status,
                                            String addtionalConfig);

    public void updateUserSubscriptionInfo(UserVO userVO);

}
