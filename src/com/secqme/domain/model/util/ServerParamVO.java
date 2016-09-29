package com.secqme.domain.model.util;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="serverParam")
@NamedQueries({
        @NamedQuery(name= ServerParamVO.QUERY_FIND_BY_KEY,
        query = "SELECT o " +
                "FROM ServerParamVO o " +
        		"WHERE o.keyValue = :keyValue")
})
public class ServerParamVO implements Serializable {

    public static final String QUERY_FIND_BY_KEY = "ServerParamVO.findByKey";

    @Id
    private String keyValue;
    private String parameter;

    public ServerParamVO() {
        // Empty Constructor
    }

    public String getKeyValue() {
		return keyValue;
	}

	public void setKeyValue(String keyValue) {
		this.keyValue = keyValue;
	}

	public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ServerParamVO that = (ServerParamVO) o;

        if (!keyValue.equals(that.keyValue)) return false;
        if (!parameter.equals(that.parameter)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = keyValue.hashCode();
        result = 31 * result + parameter.hashCode();
        return result;
    }
}