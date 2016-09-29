package com.secqme.util.payment;

import com.secqme.domain.dao.PendingPaymentDAO;
import com.secqme.domain.dao.UserDAO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.payment.PaymentGWVO;
import com.secqme.domain.model.payment.PendingPaymentVO;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.domain.model.payment.UserSubscriptionInfoVO;
import org.apache.log4j.Logger;

import java.util.Date;

/**
 *
 * @author coolboykl
 */
public class DefaultPaymentHelper implements PaymentHelper {

    private static final Logger myLog = Logger.getLogger(DefaultPaymentHelper.class);
    private PendingPaymentDAO pendingPaymentDAO = null;
    private UserDAO userDAO = null;

    public DefaultPaymentHelper(PendingPaymentDAO pendingPmtDAO, UserDAO userDAO) {
        pendingPaymentDAO = pendingPmtDAO;
        this.userDAO = userDAO;
    }

    public PendingPaymentVO retrivePendingPayment(String txnid, String paymentGWName) {
        return pendingPaymentDAO.read(paymentGWName + "_" + txnid);
    }

    public void deletePendingPayment(String txnid, String paymentGWName) {
        PendingPaymentVO vo = pendingPaymentDAO.read(paymentGWName + "_" + txnid);
        pendingPaymentDAO.delete(vo);
    }

    public void insertNewPendingPaymentVO(String txnid, String paymentGWName, String payerid,
            PricingPackageVO pricePkgVO, UserVO userVO,
            Double grossAmt, Double paymentFee, String status) {
        PendingPaymentVO vo = new PendingPaymentVO();
        vo.setTxnid(paymentGWName + "_" + txnid);
        vo.setReceiveTime(new Date());
        vo.setPayerId(payerid);
        vo.setPricePkgVO(pricePkgVO);
        vo.setUserVO(userVO);
        vo.setGrossAmt(grossAmt);
        vo.setPaymentFee(paymentFee);
        vo.setStatus(status);
        pendingPaymentDAO.create(vo);
    }

    public UserSubscriptionInfoVO createReplaceSubscriptionRecord(UserVO userVO, PricingPackageVO pricePkgVO,
            PaymentGWVO gwVO, Double paymentAmt, Date effectiveDate, Date nextPaymentDate,
            String profileId, String status, String addtionalConfig) {
        myLog.debug("Create or replace new Subscription Record for ->" + userVO
                + " pricePkg->" + pricePkgVO.getPkgCode());

        UserSubscriptionInfoVO subscriptionVO = userVO.getUserCurrentSubscription();
        if (subscriptionVO == null) {
            subscriptionVO = new UserSubscriptionInfoVO();
            userVO.setUserCurrentSubscription(subscriptionVO);
        }
        subscriptionVO.setUserVO(userVO);
        subscriptionVO.setPricePkgVO(pricePkgVO);
        subscriptionVO.setGatewayVO(gwVO);
        if (effectiveDate != null) {
            subscriptionVO.setEffectiveDate(effectiveDate);
        }

        if (nextPaymentDate != null) {
            subscriptionVO.setNextPaymentDate(nextPaymentDate);
        }

        if (profileId != null) {
            subscriptionVO.setProfileid(profileId);
        }

        if (addtionalConfig != null) {
            subscriptionVO.setAdditionalConfig(addtionalConfig);
        }

        if (status != null) {
            subscriptionVO.setStatus(status);
        }

        if (paymentAmt != null) {
            subscriptionVO.setPaymentAmt(paymentAmt);
        }

        userDAO.update(userVO);

        return subscriptionVO;

    }

    public void updateUserSubscriptionInfo(UserVO userVO) {
        this.userDAO.update(userVO);
    }
}
