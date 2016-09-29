package com.secqme.domain.model.notification;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

@Entity
@Table(name="socialNetworks")
@NamedQueries ({
            @NamedQuery(name = SocialNetworkVO.QUERY_FIND_BY_NAME,
                        query = "SELECT s FROM SocialNetworkVO s WHERE s.snsName LIKE :snsName"),
            @NamedQuery(name=SocialNetworkVO.QUERY_FINDALL,
                        query = "SELECT s from SocialNetworkVO s")
} )
public class SocialNetworkVO implements Serializable {
    public static final String QUERY_FIND_BY_NAME = "SocialNetworkVO.findByName";
    public static final String QUERY_FINDALL = "SocialNetworkVO.findALL";
    public static final String SNS_NAME_KEY = "name";
    public static final String SNS_DESCRIPTION_KEY = "description";
    public static final String SNS_ADDITIONAL_CONFIG_KEY = "additionalconfig";

    @Id
    private String snsName;
    private String snsConfig;

    public SocialNetworkVO() {
        // Empty Constructor
    }

    public String getSnsConfig() {
        return snsConfig;
    }

    public void setSnsConfig(String snsConfig) {
        this.snsConfig = snsConfig;
    }

    public String getSnsName() {
        return snsName;
    }

    public void setSnsName(String snsName) {
        this.snsName = snsName;
    }

    // As each social service may require different set of parameters for
    // user registration, update status url, api key, and etc.
    // all the additional config will store in database as string, arrange as
    // as name value pair, and sperated by ","



  

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        SocialNetworkVO aVO = (SocialNetworkVO) obj;
        return new EqualsBuilder()
                    .append(this.snsName , aVO.snsName)
                    .append(this.snsConfig, aVO.snsConfig)
                    .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(3,21)
                .append(this.snsName)
                .append(this.snsConfig)
                .toHashCode();
    }

}
