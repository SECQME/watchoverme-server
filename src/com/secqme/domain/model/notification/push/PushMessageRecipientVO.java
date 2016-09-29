package com.secqme.domain.model.notification.push;

import com.secqme.domain.model.UserVO;
import com.secqme.domain.model.ar.Parameterizable;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by edward on 22/05/2015.
 */
public class PushMessageRecipientVO implements Parameterizable<String, Object> {

    private UserVO userVO;
    private Map<String, Object> params;

    public PushMessageRecipientVO(UserVO userVO) {
        this(userVO, new HashMap<String, Object>());
    }

    public PushMessageRecipientVO(UserVO userVO, @NotNull Map<String, Object> params) {
        this.userVO = userVO;
        this.params = params;
    }

    public UserVO getUserVO() {
        return userVO;
    }

    public void setUserVO(UserVO userVO) {
        this.userVO = userVO;
    }

    @Override
    public Map<String, Object> getParams() {
        return this.params;
    }

    @Override
    public void clearParam() {
        params.clear();
    }

    @Override
    public void putParam(String key, Object value) {
        params.put(key, value);
    }

    @Override
    public Object removeParam(String key) {
        return params.remove(key);
    }
}
