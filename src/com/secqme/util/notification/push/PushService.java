package com.secqme.util.notification.push;

import com.secqme.CoreException;
import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.notification.push.PushMessageVO;

public interface PushService {
	public boolean pushMessage(PushMessageVO pushMessageVO) throws CoreException;
}
