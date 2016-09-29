package com.secqme.domain.dao.jpa;

import com.secqme.domain.dao.ServerParamDAO;
import com.secqme.domain.model.util.ServerParamVO;

public class ServerParamJPADAO extends BaseJPADAO<ServerParamVO, String> implements ServerParamDAO{
	public ServerParamJPADAO() {
		super(ServerParamVO.class);
	}

	@Override
	public ServerParamVO findParameterByKeyValue(String keyValue) {
		JPAParameter parameter = new JPAParameter()
    	.setParameter("keyValue", keyValue);
		return executeQueryWithSingleResult(ServerParamVO.QUERY_FIND_BY_KEY, parameter);
	}
}
