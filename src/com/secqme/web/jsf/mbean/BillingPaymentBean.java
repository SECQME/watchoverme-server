package com.secqme.web.jsf.mbean;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.billing.BillingCycleVO;
import com.secqme.domain.model.payment.PaymentHistoryVO;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.manager.billing.BillingManager;
import com.secqme.manager.payment.PaymentManager;
import com.secqme.util.spring.DefaultSpringUtil;
import com.secqme.web.jsf.util.MessageController;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.apache.commons.lang.time.DateUtils;
import twitter4j.internal.logging.Logger;

/**
 *
 * @author coolboykl
 */
public class BillingPaymentBean implements Serializable {

    private static final Logger myLog = Logger.getLogger(BillingPaymentBean.class);
    private LoginBean loginBean = null;
    private BillingManager billingManager = null;
    private PaymentManager paymentManager = null;
    private String paymentAction = null;
    private String subPkgCode = null;
    private BillingCycleVO billCycleVO = null;
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MMM-yyyy");
    private PaymentStatus paymentStatus = PaymentStatus.INIT_PAYMENT;
    private String paymentStatusMsg = null;
    private List<PricingPackageVO> userPricePackageList = null;
    private List<PaymentHistoryVO> paymentRecordList = null;

    public BillingPaymentBean() {
        // Empty Constructor;
    }

    public BillingManager getBillingManager() {
        return billingManager;
    }

    public void setBillingManager(BillingManager billingManager) {
        this.billingManager = billingManager;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public Boolean getDisplayManageSubscriptionPanel() {
        return loginBean.getLoginUserVO().getUserCurrentSubscription() == null;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getPaymentStatusMsg() {
        return paymentStatusMsg;
    }

    public void setPaymentStatusMsg(String paymentStatusMsg) {
        this.paymentStatusMsg = paymentStatusMsg;
    }

    public LoginBean getLoginBean() {
        return loginBean;
    }

    public void setLoginBean(LoginBean loginBean) {
        this.loginBean = loginBean;
    }

    public PaymentManager getPaymentManager() {
        return paymentManager;
    }

    public void setPaymentManager(PaymentManager paymentManager) {
        this.paymentManager = paymentManager;
    }

    public String getPaymentAction() {
        return paymentAction;
    }

    public String getCurrentPckGWName() {
        return loginBean.getLoginUserVO().getUserCurrentSubscription().getGwName();
    }

    public List<PaymentHistoryVO> getUserPaymentRecords() {
        if (paymentRecordList == null) {
            paymentRecordList = paymentManager.getUserPaymentHistory(loginBean.getLoginUserVO().getUserid());
        }

        return paymentRecordList;
    }

    public boolean isUserPaymentRecordsPanelEnable() {
       return (getUserPaymentRecords() != null && getUserPaymentRecords().size() > 0);
    }

    public String getCurrentPckEffectiveDate() {
        String dateStr = "N/A";
        if (loginBean.getLoginUserVO().getUserCurrentSubscription().getEffectiveDate() != null) {
            dateStr = simpleDateFormat.format(loginBean.getLoginUserVO().getUserCurrentSubscription().getEffectiveDate());
        }
        return dateStr;
    }

    public String getCurrentPckNextPaymentDate() {
        String dateStr = "N/A";
        if (loginBean.getLoginUserVO().getUserCurrentSubscription().getNextPaymentDate() != null) {
            dateStr = simpleDateFormat.format(loginBean.getLoginUserVO().getUserCurrentSubscription().getNextPaymentDate());
        }
        return dateStr;
    }

    public String getCurrentPckName() {
        return loginBean.getLoginUserVO().getUserCurrentSubscription().getPricePkgVO().getPricePkgName();
    }

    public BillingCycleVO getUserBillCycleVO() {
        if (billCycleVO == null) {
            billCycleVO = billingManager.getUserLatestBillCycleVO(loginBean.getLoginUserVO());
        }
        return billCycleVO;
    }

    public String getBillCycleStartDateStr() {
        String startDateStr = "";
        if (getUserBillCycleVO() != null) {
            startDateStr = simpleDateFormat.format(getUserBillCycleVO().getStartDate());
        }

        return startDateStr;
    }

    public String getBillCycleEndDateStr() {
        String endDateStr = "";
        if (getUserBillCycleVO() != null) {
            endDateStr = simpleDateFormat.format(getUserBillCycleVO().getEndDate());
        }

        return endDateStr;
    }

    public void setPaymentAction(String paymentAction) {
        myLog.debug("setting Payment Action->" + paymentAction);
        this.paymentAction = paymentAction;
        if (paymentAction.equalsIgnoreCase("authpayment")) {
        } else if (paymentAction.equalsIgnoreCase("cancelpayment")) {
            MessageController.addInfowithMsgCode(null, "billing.payment.cancel", null);
        }
    }

    public String getSubPkgCode() {
        return subPkgCode;
    }

    public void setSubPkgCode(String subPkgCode) {
        this.subPkgCode = subPkgCode;
    }

    
    public void cancelUserSubscription() {
        UserVO userVO = loginBean.getLoginUserVO();
        myLog.debug("Cancelling user subscription for user->" + loginBean.getLoginUserVO().getUserid());
        String cancelNoteParams[] = new String[2];
        cancelNoteParams[0] = userVO.getUserCurrentSubscription().getPricePkgVO().getPricePkgName();
        cancelNoteParams[1] = getBillCycleStartDateStr();
        String cancelNote = 
                DefaultSpringUtil.getInstance().getMessage("billing.cancel.subscription.note", cancelNoteParams);
        paymentManager.cancelSubscription(userVO, cancelNote);
        MessageController.addInfo(null, "Cancel Subscription", cancelNote);
        updatePaymentStatus();
        
    }
    
    
    public void updatePaymentStatus() {
        myLog.debug("Updating user payment info" + paymentStatus.name()
                + ", paymentStatusMsg:" + paymentStatusMsg);
        if(loginBean.getLoginUserVO() != null){ //khlow20120717 - avoid null exception when users load the page without logging in
        	loginBean.refreshLoginUserVO();
	        refreshUserAvailablePricingPackages();
	        checkSubscriptionExpireSoon();
	        paymentRecordList = null;
	        billCycleVO = null;
	        switch (paymentStatus) {
	            case ERROR_PAYMENT:
	                MessageController.addError(null, "Error Processing Payment", paymentStatusMsg);
	                break;
	
	            case SUCCESS_PAYMENT:
	                // Force the billCycle = null;
	                billCycleVO = null;
	                String thanksParam[] = new String[1];
	                thanksParam[0] = loginBean.getLoginUserVO().getUserCurrentSubscription().getPricePkgVO().getPricePkgName();
	                MessageController.addInfowithMsgCode(null, "billing.thanks.subscription.note", thanksParam);
	                loginBean.refreshLoginUserVO();
	                break;
	
	        }
	        // Reset
	        paymentStatus = PaymentStatus.INIT_PAYMENT;
	        paymentStatusMsg = null;
        }
    }

    public void refreshUserAvailablePricingPackages() {
        UserVO loginUser = loginBean.getLoginUserVO();
        userPricePackageList =
                paymentManager.getAutoRenewPricingPackage(loginUser.getMarketVO().getName());
    }

    public List<PricingPackageVO> getUserPricePackageList() {
        if (userPricePackageList == null) {
            refreshUserAvailablePricingPackages();
        }
        return userPricePackageList;
    }

    public void checkSubscriptionExpireSoon() {
        if (!getUserBillCycleVO().getBillingPkgVO().isRenewable()
                || loginBean.getLoginUserVO().getUserCurrentSubscription() == null) {
            Date today = new Date();
            Date billCycleEndDate = getUserBillCycleVO().getEndDate();
            String[] msgParameters = new String[2];
            msgParameters[0] = getUserBillCycleVO().getBillingPkgVO().getPkgName();
            if (today.getTime() >= billCycleEndDate.getTime()) {
                // Subscription already Expire;
                msgParameters[1] = simpleDateFormat.format(billCycleEndDate);
                MessageController.addErrorwithMsgCode(null, "billing.subscription.expired.msg", msgParameters);
            } else if (getUserBillCycleVO().getBillCycleRemainDays() <= 5) {
                msgParameters[1] = getUserBillCycleVO().getBillCycleRemainDays().toString();
                MessageController.addErrorwithMsgCode(null, "billing.subscription.expiring.soon.msg", msgParameters);
            }
        }
    }
}
