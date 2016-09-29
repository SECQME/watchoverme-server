package com.secqme.util.payment;

import com.secqme.CoreException;
import com.secqme.domain.dao.PaymentHistoryDAO;
import com.secqme.domain.dao.PricingPackageDAO;
import com.secqme.domain.dao.UserDAO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingPkgType;
import com.secqme.domain.model.payment.PaymentHistoryVO;
import com.secqme.domain.model.payment.PaymentType;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.domain.model.payment.UserSubscriptionInfoVO;
import com.secqme.manager.billing.BillingManager;
import com.secqme.util.notification.NotificationEngine;
import com.secqme.util.rest.RestUtil;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 * User: James Khoo
 * Date: 4/15/14
 * Time: 4:07 PM
 */
public abstract class BasePaymentUtil implements PaymentUtil {

    private static Logger myLog = Logger.getLogger(BasePaymentUtil.class);

    protected PaymentHistoryDAO paymentHistoryDAO = null;
    protected RestUtil restUtil = null;
    protected PricingPackageDAO pricePkgDAO;
    protected UserDAO userDAO = null;
    protected BillingManager billingManager = null;
    protected NotificationEngine notificationEngine = null;

    public BasePaymentUtil(
            PaymentHistoryDAO paymntHistDAO,
            PricingPackageDAO pricePkgDAO,
            UserDAO userDAO,
            BillingManager billingManager,
            NotificationEngine notificationEngine) {
        this.paymentHistoryDAO = paymntHistDAO;
        this.pricePkgDAO = pricePkgDAO;
        this.billingManager = billingManager;
        this.notificationEngine = notificationEngine;
        this.userDAO = userDAO;
    }


    protected void activateUserPremiumPackage(String premiumPkgCode, UserVO userVO, Date nextPaymentDate, PaymentGateway paymentGateway, String paymentRemark) throws CoreException {
        PricingPackageVO pricePkgVO = pricePkgDAO.read(premiumPkgCode);
        Date effectiveDate = new Date();
        //APPLE_DATE_FORMAT.parse(latestReceiptInfoObj.getString(APPLE_ORG_PURCHASE_DATE_KEY));

        if (pricePkgVO != null) {
            userVO.setPackageVO(pricePkgVO.getBillPkgVO());
            myLog.debug("Updating billingPkg : " + pricePkgVO.getBillPkgVO().getPkgName()
                    + "for user->" + userVO.getUserid());

            // Let the billing Manager to do the provision first;
            billingManager.provisionBillingCycle(pricePkgVO.getBillPkgVO(), userVO, pricePkgVO.getQuantity(), false);

            // Create userPaymentInfo, and subscriptionInfo
            myLog.debug("Preparing payment History Record for ->" + userVO.getUserid());
            PaymentHistoryVO paymentHistoryVO = new PaymentHistoryVO();
            paymentHistoryVO.setPaymentGWVO(paymentGateway.getPaymentGWVO());
            paymentHistoryVO.setPaymentAmt(pricePkgVO.getPrice());
            paymentHistoryVO.setPaymentFee(0.00);
            paymentHistoryVO.setPaymentDate(effectiveDate);
            paymentHistoryVO.setPaymentType(PaymentType.NEW_SUBSCRIPTION.name());
            paymentHistoryVO.setPricingPgkVO(pricePkgVO);
            paymentHistoryVO.setUserVO(userVO);
            myLog.debug("Payment History->" + paymentHistoryVO);
            paymentHistoryVO.setRemark(paymentRemark);
            paymentHistoryDAO.create(paymentHistoryVO);

            UserSubscriptionInfoVO subscriptionVO = userVO.getUserCurrentSubscription();

            if (subscriptionVO == null) {
                subscriptionVO = new UserSubscriptionInfoVO();
                userVO.setUserCurrentSubscription(subscriptionVO);
            }

            subscriptionVO.setUserVO(userVO);
            subscriptionVO.setEffectiveDate(effectiveDate);
            subscriptionVO.setGatewayVO(paymentGateway.getPaymentGWVO());
            subscriptionVO.setNextPaymentDate(nextPaymentDate);
            subscriptionVO.setPaymentAmt(pricePkgVO.getPrice());
            subscriptionVO.setPricePkgVO(pricePkgVO);
            subscriptionVO.setProfileid(null);
            subscriptionVO.setStatus("Active");
            myLog.debug("Subscription Info->" + subscriptionVO);

            userDAO.update(userVO);

            notificationEngine.sendWelcomeEmail(userVO, BillingPkgType.PREMIUM);
        }
    }



}
