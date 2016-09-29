package com.secqme.util.payment;

import com.secqme.CoreException;
import com.secqme.domain.dao.PayPalIPNDAO;
import com.secqme.domain.dao.PricingPackageDAO;
import com.secqme.domain.dao.UserDAO;
import com.secqme.domain.model.CountryVO;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import com.secqme.domain.model.payment.PayPalIPNLogVO;
import com.secqme.domain.model.payment.PaymentType;
import com.secqme.domain.model.payment.PricingPackageVO;
import com.secqme.domain.model.payment.UserPaymentInfoVO;
import com.secqme.manager.payment.PaymentManager;
import com.secqme.util.ServletUtil;
import com.secqme.util.notification.sms.SMSManager;
import com.secqme.util.spring.BeanType;
import com.secqme.util.spring.DefaultSpringUtil;
import com.secqme.web.jsf.mbean.BillingPaymentBean;
import com.secqme.web.jsf.mbean.GiftBean;
import com.secqme.web.jsf.mbean.LoginBean;
import com.secqme.web.jsf.mbean.PaymentStatus;
import org.apache.log4j.Logger;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;
import java.util.Enumeration;

/**
 *
 * @author coolboykl
 */
public class PaymentServlet extends HttpServlet {

    private static final String PAYPAL_GW = "PAYPAL";
    private static final String INIT_PAYMENT_PATH = "/init";
    private static final String AUTHORIZE_PAYMENT_PATH = "/authorize";
    private static final String PAYMENT_NOTIFICATION_PATH = "/notification";
    private static final String CANCEL_PAYMENT_PATH = "/cancel";
    private static final String USER_PARAM_KEY = "userid";
    private static final String PRICE_PKG_PARAM_KEY = "pricepkg";
    private static final String PROMOTION_PKG_PARAM_KEY = "promotecode";
    private static final String MOBILE_REQUEST_PATH = "/mobile";
    private static final String INIT_GIFT_PAYMENT_PATH = "/gift";
    private final static Logger myLog = Logger.getLogger(PaymentServlet.class);
    private String paypalIPNAckURL = null;
    private PaymentManager paymentManager = null;
    private SMSManager smsManager = null;
    private PayPalIPNDAO paypalIPNLogDAO = null;
    private PricingPackageDAO pricePkgDAO = null;
    private UserDAO userDAO = null;
    
    public PaymentServlet() {
        paymentManager = (PaymentManager) DefaultSpringUtil.getInstance().getBean(BeanType.paymentManager);
        smsManager = (SMSManager) DefaultSpringUtil.getInstance().getBean(BeanType.smsManager);
        paypalIPNLogDAO = (PayPalIPNDAO) DefaultSpringUtil.getInstance().getBean(BeanType.PAYPAL_IPN_LOG_DAO);
        pricePkgDAO = (PricingPackageDAO) DefaultSpringUtil.getInstance().getBean(BeanType.PRICING_PACKAGE_DAO);
        userDAO = (UserDAO) DefaultSpringUtil.getInstance().getBean(BeanType.USER_DAO);

        try {
            paypalIPNAckURL = paymentManager.getPaymentGWConfig(PAYPAL_GW).getString(PaypalGW.IPN_URL_KEY);
        } catch (JSONException ex) {
            myLog.error("Problem onn initiating PaymentServlet->" + ex.getMessage(), ex);
        }
        myLog.debug("PaymentServlet Initialized");
    }

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param req servlet request
     * @param resp servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        ServletUtil.printRequestInfo(req);
        String path = req.getPathInfo();

        if (path != null) {
            if (path.startsWith(INIT_PAYMENT_PATH)) {
                peformInitPayment(req, resp);
            } else if (path.startsWith(AUTHORIZE_PAYMENT_PATH)) {
                performAuthorizePayment(req, resp);
            } else if (path.startsWith(CANCEL_PAYMENT_PATH)) {
                performCancelPayment(req, resp);
            } else if (path.startsWith(PAYMENT_NOTIFICATION_PATH)) {
                processPaymentNotification(req, resp);
            } else if (path.startsWith(INIT_GIFT_PAYMENT_PATH)) {
            	peformInitPaymentForGifting(req, resp);
            }
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "secQme Payment Servlet";
    }

    private void peformInitPayment(HttpServletRequest req, HttpServletResponse resp) {
        String userid = null;
        if (req.getParameterMap().containsKey(USER_PARAM_KEY)) {
            userid = req.getParameter(USER_PARAM_KEY);
        } else {
            // User id is not in the token
            // get it from login Bean;
            LoginBean loginBean = (LoginBean) req.getSession().getAttribute("loginBean");
            if (loginBean != null) {
                UserVO userVO = loginBean.getLoginUserVO();
                userid = userVO.getUserid();
            }
        }
        if (req.getPathInfo().indexOf(PAYPAL_GW) > 0) {
            performPayPalInitPayment(userid, req, resp);
        }
    }
    
    private void peformInitPaymentForGifting(HttpServletRequest req, HttpServletResponse resp) {
    	myLog.debug("peformInitPaymentForGifting ");
        if (req.getPathInfo().indexOf(PAYPAL_GW) > 0) {
            performPayPalInitPaymentForGifting(req, resp);
        }
    }
    
    private void performPayPalInitPayment(String userid, HttpServletRequest req, HttpServletResponse resp) {
        try {
            String productCode = null;
            PaymentType paymentType = null;
            PricingPackageVO pricePkgVO = null;
            Boolean renewPackage = Boolean.FALSE;
            String pkgDesc = null;
            Double price = 0.00;
            UserVO userVO = null;
            
            userVO = userDAO.read(userid);
            
            if(req.getParameter(PRICE_PKG_PARAM_KEY) != null) {
                productCode = req.getParameter(PRICE_PKG_PARAM_KEY);
                paymentType = PaymentType.NEW_SUBSCRIPTION;
                pricePkgVO = pricePkgDAO.read(productCode);
                renewPackage = pricePkgVO.isAutoRenew();
                pkgDesc = getPaypalPkgDesc(pricePkgVO);
                price = pricePkgVO.getPrice();
            }
            myLog.debug("Processing init payment with " + PAYPAL_GW
                    + " for user:" + userVO.getUserid() + ", paymentType" + paymentType.name()
                    + " desc:" + pkgDesc + ", price:" + price);
            JSONObject jobj = paymentManager.processInitPayment(paymentType, renewPackage, pkgDesc,productCode, price, userVO, PAYPAL_GW);

            if (jobj != null) {
                if (!req.getPathInfo().contains(MOBILE_REQUEST_PATH)) {
                    resp.sendRedirect(jobj.getString(PaypalGW.REDIRECT_URL_KEY));
                } else {
                    // Request from the Mobile App
                    // write the json object back to response
                    resp.setContentType("application/json");
                    PrintWriter out = resp.getWriter();
                    out.println(jobj.toString());
                    out.flush();
                    out.close();
                }
            }

        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (CoreException ex) {
            myLog.error(ex.getMessage(), ex);
        }

    }

    private void performPayPalInitPaymentForGifting(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String productCode = null;
            PaymentType paymentType = null;
            PricingPackageVO pricePkgVO = null;
            Boolean renewPackage = Boolean.FALSE;
            String pkgDesc = null;
            Double price = 0.00;
            String recipient = null;
            GiftPaymentLogVO giftPaymentLogVO = new GiftPaymentLogVO();
            if(req.getParameter(PRICE_PKG_PARAM_KEY) != null) {
                productCode = req.getParameter(PRICE_PKG_PARAM_KEY);
                myLog.debug("parameter: "
                		+ req.getParameter(PaypalGW.GIFT_NAME_KEY) + "*"
                		+ req.getParameter(PaypalGW.GIFT_MOBILE_COUNTRY_KEY) + "*"
                		+ req.getParameter(PaypalGW.GIFT_MOBILE_NUMBER_KEY) + "*"
                		+ req.getParameter(PaypalGW.GIFT_EMAIL) + "*"
                		+ req.getParameter(PaypalGW.GIFT_MESSAGE) + "*"
                		+ req.getParameter(PaypalGW.GIFT_RECIPIENT_NAME_KEY) + "*"
                		+ req.getParameter(PaypalGW.GIFT_RECIPIENT_MOBILE_COUNTRY_KEY) + "*"
                		+ req.getParameter(PaypalGW.GIFT_RECIPIENT_MOBILE_NUMBER_KEY) + "*"
                		+ req.getParameter(PaypalGW.GIFT_RECIPIENT_EMAIL_KEY) + "*"
                		+ req.getParameter(PaypalGW.GIFT_SUBSCRIBE) + "*");
                paymentType = PaymentType.NEW_SUBSCRIPTION;
                pricePkgVO = pricePkgDAO.read(productCode);
                renewPackage = pricePkgVO.isAutoRenew();
                pkgDesc = getPaypalPkgDesc(pricePkgVO);
                price = pricePkgVO.getPrice();
                giftPaymentLogVO.setName(req.getParameter(PaypalGW.GIFT_NAME_KEY));
                giftPaymentLogVO.setMobileCountry(smsManager.getCountry(req.getParameter(PaypalGW.GIFT_MOBILE_COUNTRY_KEY)));
                giftPaymentLogVO.setMobileNumber(req.getParameter(PaypalGW.GIFT_MOBILE_NUMBER_KEY));
                giftPaymentLogVO.setEmail(req.getParameter(PaypalGW.GIFT_EMAIL));
                giftPaymentLogVO.setMessage(req.getParameter(PaypalGW.GIFT_MESSAGE));
                giftPaymentLogVO.setRecipientName(req.getParameter(PaypalGW.GIFT_RECIPIENT_NAME_KEY));
                giftPaymentLogVO.setRecipientMobileCountry(smsManager.getCountry(
                		req.getParameter(PaypalGW.GIFT_RECIPIENT_MOBILE_COUNTRY_KEY)));
                giftPaymentLogVO.setRecipientMobileNumber(req.getParameter(PaypalGW.GIFT_RECIPIENT_MOBILE_NUMBER_KEY));
                giftPaymentLogVO.setRecipientEmail(req.getParameter(PaypalGW.GIFT_RECIPIENT_EMAIL_KEY));
                giftPaymentLogVO.setAutoRenew(Boolean.valueOf(req.getParameter(PaypalGW.GIFT_SUBSCRIBE)));
            }
            
            myLog.debug("Processing init payment with " + PAYPAL_GW
                    + " (gifting) for recipient:" 
                    + giftPaymentLogVO.getMobileCountry().getCallingCode()
            		+ giftPaymentLogVO.getMobileNumber() + ", paymentType" + paymentType.name()
                    + " desc:" + pkgDesc + ", price:" + price);
            JSONObject jobj = paymentManager.processInitPaymentForGifting(paymentType, renewPackage, pkgDesc,productCode, price, giftPaymentLogVO, PAYPAL_GW);

            if (jobj != null) {
                if (!req.getPathInfo().contains(MOBILE_REQUEST_PATH)) {
                    resp.sendRedirect(jobj.getString(PaypalGW.REDIRECT_URL_KEY));
                } else {
                    // Request from the Mobile App
                    // write the json object back to response
                    resp.setContentType("application/json");
                    PrintWriter out = resp.getWriter();
                    out.println(jobj.toString());
                    out.flush();
                    out.close();
                }
            }

        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (IOException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (CoreException ex) {
            myLog.error(ex.getMessage(), ex);
        }

    }
    
    private void performAuthorizePayment(HttpServletRequest req, HttpServletResponse resp) {
        myLog.debug("Performing Authorize Payment->" + req.getPathInfo());
        if (req.getPathInfo().indexOf(PAYPAL_GW) > 0) {
            performPayPalAuthorizePayment(req, resp);
        }
    }

    private void performPayPalAuthorizePayment(HttpServletRequest req, HttpServletResponse resp) {
        String redirectURL = null;
        String productCode = null;
        PricingPackageVO pricePackageVO = null;
        UserVO userVO = null;
        String errmsg = null;
        Boolean redirectPromotionTaken = false;
        try {
            ServletUtil.printRequestInfo(req);
            JSONObject jsonObject = new JSONObject();
            PaymentType paymentType = null;
            if (req.getParameter(PaypalGW.PRICE_PKG_CODE_KEY) != null) {
                 productCode = req.getParameter(PaypalGW.PRICE_PKG_CODE_KEY);
                 pricePackageVO = pricePkgDAO.read(productCode);
                 paymentType = PaymentType.NEW_SUBSCRIPTION;
            }
            
            //check if it is normal payment or gifting
            if(req.getParameter(PaypalGW.USER_TOKEN_KEY) != null) {
            	String userToken = req.getParameter(PaypalGW.USER_TOKEN_KEY);
                userVO = userDAO.findByActivationCode(userToken);
                String payPalToken = req.getParameter("token");
                String paypalPayerID = req.getParameter(PaypalGW.PAYPAL_PAYER_ID_KEY);

                myLog.debug("Performing Authorize Payment for User with Token->" + userToken + 
                             ", payPalToken->" + payPalToken );
                jsonObject.put(PaypalGW.NVP_TOKEN_KEY, payPalToken);
                jsonObject.put(PaypalGW.PAYPAL_PAYER_ID_KEY, paypalPayerID);
                paymentManager.updateUserPaymentInfo(userToken, jsonObject, PAYPAL_GW);
                // Continue charging from here
                myLog.debug("Attempt to Charge User Subscription");
                paymentManager.chargeFirstTimeSubscription(paymentType, pricePackageVO, userToken, jsonObject, PAYPAL_GW);
                myLog.debug("User Subscription charged");
            } else if(req.getParameter(PaypalGW.GIFT_NAME_KEY) != null){
            	myLog.debug("paypal authroization:" + "*" + req.getParameter(PaypalGW.GIFT_NAME_KEY)
            			 + "*" + req.getParameter(PaypalGW.GIFT_MOBILE_COUNTRY_KEY)
            			 + "*" + req.getParameter(PaypalGW.GIFT_MOBILE_NUMBER_KEY)
            			 + "*" + req.getParameter(PaypalGW.GIFT_EMAIL)
            			 + "*" + req.getParameter(PaypalGW.GIFT_MESSAGE)
            			 + "*" + req.getParameter(PaypalGW.GIFT_RECIPIENT_NAME_KEY)
            			 + "*" + req.getParameter(PaypalGW.GIFT_RECIPIENT_MOBILE_COUNTRY_KEY)
            			 + "*" + req.getParameter(PaypalGW.GIFT_RECIPIENT_MOBILE_NUMBER_KEY)
            			 + "*" + req.getParameter(PaypalGW.GIFT_RECIPIENT_EMAIL_KEY)
            			 + "*" + req.getParameter(PaypalGW.GIFT_SUBSCRIBE));
            	GiftPaymentLogVO giftDetailVO = new GiftPaymentLogVO();
            	giftDetailVO.setName(req.getParameter(PaypalGW.GIFT_NAME_KEY));
            	giftDetailVO.setMobileCountry(smsManager.getCountry(req.getParameter(PaypalGW.GIFT_MOBILE_COUNTRY_KEY)));
            	giftDetailVO.setMobileNumber(req.getParameter(PaypalGW.GIFT_MOBILE_NUMBER_KEY));
            	giftDetailVO.setEmail(req.getParameter(PaypalGW.GIFT_EMAIL));
            	giftDetailVO.setMessage(req.getParameter(PaypalGW.GIFT_MESSAGE));
            	giftDetailVO.setRecipientName(req.getParameter(PaypalGW.GIFT_RECIPIENT_NAME_KEY));
            	giftDetailVO.setRecipientMobileCountry(smsManager.getCountry(req.getParameter(PaypalGW.GIFT_RECIPIENT_MOBILE_COUNTRY_KEY)));
            	giftDetailVO.setRecipientMobileNumber(req.getParameter(PaypalGW.GIFT_RECIPIENT_MOBILE_NUMBER_KEY));
            	giftDetailVO.setRecipientEmail(req.getParameter(PaypalGW.GIFT_RECIPIENT_EMAIL_KEY));
            	giftDetailVO.setAutoRenew(Boolean.valueOf(req.getParameter(PaypalGW.GIFT_SUBSCRIBE)));
            	giftDetailVO.setCreatedDate(new Date());
            	String payPalToken = req.getParameter("token");
                String paypalPayerID = req.getParameter(PaypalGW.PAYPAL_PAYER_ID_KEY);
                myLog.debug("Performing Authorize Payment recipient -> " + giftDetailVO.getRecipientMobileCountry().getIso()
                		+ giftDetailVO.getRecipientMobileNumber() + 
                        ", payPalToken->" + payPalToken );
				jsonObject.put(PaypalGW.NVP_TOKEN_KEY, payPalToken);
				jsonObject.put(PaypalGW.PAYPAL_PAYER_ID_KEY, paypalPayerID);
				//check if user exist
				userVO = userDAO.findByMobileNumberAndMobileCountry(giftDetailVO.getRecipientMobileCountry().getIso(), 
						giftDetailVO.getRecipientMobileNumber());
				if(userVO != null) {
					paymentManager.updateUserPaymentInfo(userVO.getActivationCode(), jsonObject,
							PAYPAL_GW);
					giftDetailVO.setRedeemed(true);
				} else {
					UserPaymentInfoVO userPaymentInfo = paymentManager.getUserPaymentInfo(jsonObject,
							PAYPAL_GW);
					giftDetailVO.setRedeemed(false);
					if(userPaymentInfo != null){
						giftDetailVO.setPaymentId(userPaymentInfo.getPaymentid());
						giftDetailVO.setAdditionalConfig(userPaymentInfo.getAdditionalConfig());
					}
				}
				// Continue charging from here
				myLog.debug("Attempt to charge user from gift");
				paymentManager.chargeFirstTimeGifting(paymentType,
						pricePackageVO, userVO, jsonObject, PAYPAL_GW, giftDetailVO);
				myLog.debug("User gift charged");
            } else {
            	myLog.debug("Performing Authorize Payment, user token or gifting not found");
            }
            
        } catch (CoreException ex) {
            errmsg = ex.getMessage();
            myLog.error(ex.getMessage(), ex);
        } catch (JSONException ex) {
        	ex.printStackTrace();
            myLog.error(ex.getMessage(), ex);
        } 
        finally {
            try {
                
                BillingPaymentBean billPaymentBean =
                        (BillingPaymentBean) req.getSession().getAttribute("billPaymentBean");
                GiftBean giftBean = (GiftBean) req.getSession().getAttribute("giftBean"); 
                myLog.debug("Refresing BillingPaymentBean:" + billPaymentBean + " GiftBean:" + 
                		giftBean);
                if (billPaymentBean != null) {
                    if (errmsg != null) {
                        billPaymentBean.setPaymentStatus(PaymentStatus.ERROR_PAYMENT);
                        billPaymentBean.setPaymentStatusMsg(errmsg);
                    } else {
                        billPaymentBean.setPaymentStatus(PaymentStatus.SUCCESS_PAYMENT);
                    }
                    billPaymentBean.refreshUserAvailablePricingPackages();
                    redirectURL = ServletUtil.getAuthPaymentRedirectURL(req);
                    resp.sendRedirect(redirectURL);
                } if (giftBean != null) {
                	if (errmsg != null) {
                    } else {
                    }
                    redirectURL = ServletUtil.getAuthGiftPaymentRedirectURL(req);
                    resp.sendRedirect(redirectURL);
                } else {
                    if (errmsg != null) {
                        myLog.debug("Render error on processing the payment");
                        resp.setContentType("text/html");
                        PrintWriter out = resp.getWriter();
                        out.println("<html><head>Error in process your payment!</head>");
                        out.println("<body><br>Error on process your payment, error code:" + errmsg);
                        out.println("</body></html>");
                        out.flush();
                        out.close();

                    } else {
                        if(redirectPromotionTaken) {
                            resp.sendRedirect(ServletUtil.getPromotionCompletedRedirectURL(req, productCode, userVO.getUserid()));
                        } else {
                            resp.sendRedirect("http://secq.me/mobile_thanks.html");
                        }
                    }
//                   

                }
            } catch (IOException ex) {
                myLog.error(ex.getMessage(), ex);
            }
        }


    }

    private void performCancelPayment(HttpServletRequest req, HttpServletResponse resp) {
        try {
        	myLog.debug("performCancelPayment " + req.getParameter(PaypalGW.PAYMENT_TYPE_KEY) 
        			+ req.getParameter(PaypalGW.GIFT_NAME_KEY));
        	
            // jk-21st-Dec-2012
            if(req.getParameter(PaypalGW.PAYMENT_TYPE_KEY) != null) {
                PaymentType paymentType = PaymentType.valueOf(req.getParameter(PaypalGW.PAYMENT_TYPE_KEY));
                if(PaymentType.NEW_SUBSCRIPTION == paymentType) {
                	if(req.getParameter(PaypalGW.GIFT_NAME_KEY) != null) { //cancel gifting
                		resp.sendRedirect(ServletUtil.getGiftPaymentCancelRedirectURL(req));
                	} else {
                		resp.sendRedirect(ServletUtil.getPaymentCancelRedirectURL(req));
                	}
                    
                } else if (PaymentType.PROMOTION == paymentType) {
                    String promoteCode = req.getParameter(PaypalGW.PROMOTION_CODE_KEY);
                    resp.sendRedirect(ServletUtil.getPromotionCancelRedirectURL(req, promoteCode));
                }
            }
        } catch (IOException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }

    private void processPaymentNotification(HttpServletRequest req, HttpServletResponse resp) {
        if (req.getPathInfo().indexOf(PAYPAL_GW) > 0) {
            performPayPalNotification(req, resp);
        }
    }

    private void performPayPalNotification(HttpServletRequest req, HttpServletResponse resp) {
        // Todo
        // 1. Log every payment Notification to db
        // 2. Perform subcription renew
        // 3. Perform subscription cancel

        String payPalReqJSONStr = ServletUtil.getHttpRequstAsJsonString(req);

        myLog.debug("Ack to PayPal IPN, PayPal Request String->" + payPalReqJSONStr);
//        if (ackPaypalIPNNotification(req, resp)) {
        ackPaypalIPNNotification(req, resp);
        try {
            JSONObject notificationObj = new JSONObject(payPalReqJSONStr);
            String userToken = null;
            String giftRecipient = null;
            String pkgCode = null;
            String invoiceid = null;

            invoiceid = req.getParameter("invoice");
            if (invoiceid == null) {
                invoiceid = req.getParameter("rp_invoice_id");
            }
            if (invoiceid != null) {
                String[] params = invoiceid.split("_____");
                if(params[0].indexOf("GIFT_") == 0) {
                	giftRecipient = params[0].substring(5);
                } else {
                	userToken = params[0];
                }
                
                pkgCode = params[1];
            }

            String txnid = req.getParameter("txn_id");
            String payerid = req.getParameter("payer_id");
            String transactionType = req.getParameter("txn_type");
            String paymentStatus = req.getParameter("payment_status");
            String paymentGrossAmtStr = req.getParameter("payment_gross");
            String paymentFeeStr = req.getParameter("payment_fee");
            String paymentDate = req.getParameter("payment_date");
            Double paymentGrossAmt = paymentGrossAmtStr == null ? 0.00 : new Double(paymentGrossAmtStr);
            Double paymentFeeAmt = paymentFeeStr == null ? 0.00 : new Double(paymentFeeStr);
            insertNewPayPalIPNLogRecord(txnid, payerid, transactionType, paymentStatus, paymentGrossAmt, paymentFeeAmt, paymentDate, payPalReqJSONStr);
            if(userToken != null) {
            	paymentManager.handleGWNotification(userToken, pkgCode, notificationObj, PAYPAL_GW);
            } else if(giftRecipient != null) {
            	String[] params = giftRecipient.split("-");
            	CountryVO countryVO = smsManager.getCountry(params[0]);
            	paymentManager.handleGWNotificationGifting(countryVO, params[1], pkgCode, notificationObj, PAYPAL_GW);
            }
            
        } catch (CoreException ex) {
            myLog.error(ex.getMessage(), ex);
        } catch (JSONException ex) {
            myLog.error(ex.getMessage(), ex);
        }
    }

    private void insertNewPayPalIPNLogRecord(String txnid, String payerId, String transactionType,
            String paymntStatus, Double grossAmt, Double paymentFee,
            String paymentDate, String ipnLog) {
        PayPalIPNLogVO logVO = new PayPalIPNLogVO();
        logVO.setReceiveTime(new Date());
        logVO.setTransactionId(txnid);
        logVO.setPayerId(payerId);
        logVO.setTransactionType(transactionType);
        logVO.setPaymentStatus(paymntStatus);
        logVO.setPaymentGrossAmt(grossAmt);
        logVO.setPaymentFee(paymentFee);
        logVO.setPaymentDate(paymentDate);
        logVO.setLogMessage(ipnLog);

        this.paypalIPNLogDAO.create(logVO);
    }

    public boolean ackPaypalIPNNotification(HttpServletRequest req, HttpServletResponse resp) {
        boolean ackSuccess = false;
        try {
            Enumeration en = req.getParameterNames();
            StringBuffer strBuf = new StringBuffer();
            strBuf.append("cmd=_notify-validate");
            while (en.hasMoreElements()) {
                String paramName = (String) en.nextElement();
                String paramValue = req.getParameter(paramName);
                strBuf.append("&");
                strBuf.append(paramName);
                strBuf.append("=");
                strBuf.append(URLEncoder.encode(paramValue));
            }

            String txnid = req.getParameter("txn_id");
            String payerid = req.getParameter("payer_id");

            URL payPalIPNACKURL = new URL(paypalIPNAckURL);
            URLConnection uc = payPalIPNACKURL.openConnection();
            uc.setDoOutput(true);
            uc.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            PrintWriter pw = new PrintWriter(uc.getOutputStream());
            pw.println(strBuf.toString());
            pw.close();

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(uc.getInputStream()));
            String res = in.readLine();
            in.close();

            if (res.equals("VERIFIED")) {
                ackSuccess = true;
                myLog.debug("PayPal IPN for txnid->" + txnid
                        + ", payerid->" + payerid + " VERIFIED");
            } else if (res.equals("INVALID")) {
                myLog.debug("Fail to VERIFIED PayPal IPN for txnid->" + txnid
                        + ", payerid->" + payerid);
            }


        } catch (IOException ex) {
            myLog.error("Problem of posting PayPal IPN Ack->" + ex.getMessage(), ex);
        }

        return ackSuccess;

    }

    private String getPaypalPkgDesc(PricingPackageVO pkgVO) {
        return pkgVO.getPkgDesc() + ", (" + pkgVO.getCurrencyCode() + ") "
                + pkgVO.getPrice();
    }
}
