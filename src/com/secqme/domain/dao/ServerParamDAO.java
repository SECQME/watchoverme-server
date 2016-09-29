package com.secqme.domain.dao;

import com.secqme.domain.model.util.ServerParamVO;

public interface ServerParamDAO  extends BaseDAO<ServerParamVO, String>{
	public ServerParamVO findParameterByKeyValue(String keyValue);
}
