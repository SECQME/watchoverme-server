package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.GiftPaymentLogDAO;
import com.secqme.domain.model.payment.GiftPaymentLogVO;
import org.apache.log4j.Logger;

import java.util.List;

public class GiftPaymentLogJPADAO extends BaseJPADAO<GiftPaymentLogVO, Long>
	implements GiftPaymentLogDAO{

	private static Logger myLog = Logger.getLogger(GiftPaymentLogJPADAO.class);
	
	public GiftPaymentLogJPADAO() {
		super(GiftPaymentLogVO.class);
	}

	public List<GiftPaymentLogVO> findGiftPackageByCountryAndMobileNumber(String mobileCountry, String mobileNumber) {
		JPAParameter parameter = new JPAParameter()
        .setParameter("mobileCountryISO", mobileCountry)
        .setParameter("mobileNumber", mobileNumber)
        .setParameter("redeemed", false);
		return executeQueryWithResultList(GiftPaymentLogVO.QUERY_FIND_BY_COUNTRY_AND_PHONE_NUMBER, parameter);
	}

    public List<GiftPaymentLogVO> findGiftPackageByEmailAddress(String emailAddress) {
        JPAParameter parameter = new JPAParameter()
                .setParameter("emailAddress", emailAddress)
                .setParameter("redeemed", false);
        return executeQueryWithResultList(GiftPaymentLogVO.QUERY_FIND_BY_EMAIL_ADDRESS, parameter);
    }
}
