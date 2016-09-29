package com.secqme.domain.dao;

import java.util.List;

import com.secqme.domain.model.payment.GiftPaymentLogVO;

public interface GiftPaymentLogDAO extends BaseDAO<GiftPaymentLogVO, Long>{

	public List<GiftPaymentLogVO> findGiftPackageByCountryAndMobileNumber(String mobileCountry, String mobileNumber);
    public List<GiftPaymentLogVO> findGiftPackageByEmailAddress(String emailAddress);
}
