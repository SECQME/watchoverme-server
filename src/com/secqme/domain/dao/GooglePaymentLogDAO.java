package com.secqme.domain.dao;

import com.secqme.domain.model.payment.GooglePaymentLogVO;

/**
 *
 * @author james
 */
public interface GooglePaymentLogDAO extends BaseDAO<GooglePaymentLogVO, Long> {
	public GooglePaymentLogVO getUserLatestLogVO(String userid);
}
