package com.secqme.domain.model.notification.sms;

import java.io.Serializable;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.OrderBy;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.persistence.annotations.PrivateOwned;

/**
 *
 * @author jameskhoo
 */
@Entity
@Table(name="smsGateWay")
@NamedQueries({
    @NamedQuery(name = SmsGateWayVO.QUERY_FIND_ALL,
    query = "SELECT o FROM SmsGateWayVO o ")
})
public class SmsGateWayVO implements Serializable {

    public final static String QUERY_FIND_ALL = "smsGw.findall";

    @Id
    private String name;
    private String description;
    private String additionalConfig;
    private String implClass;
    @OneToMany(mappedBy = "smsGateWayVO",
               fetch = FetchType.EAGER,
               cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @OrderBy("description")
    @PrivateOwned
    private List<SmsCountryVO> countryVOList;

    public SmsGateWayVO() {
        // Empty Constructor
    }

    public String getAdditionalConfig() {
        return additionalConfig;
    }

    public void setAdditionalConfig(String additionalConfig) {
        this.additionalConfig = additionalConfig;
    }

    public String getDescription() {
        return description;
    }



    public void setDescription(String description) {
        this.description = description;
    }

    public String getImplClass() {
        return implClass;
    }

    public void setImplClass(String implClass) {
        this.implClass = implClass;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<SmsCountryVO> getCountryVOList() {
        return countryVOList;
    }

    public void setCountryVOList(List<SmsCountryVO> countryVOList) {
        this.countryVOList = countryVOList;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final SmsGateWayVO otherVO = (SmsGateWayVO) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)).append(this.getName(), otherVO.getName()).isEquals();

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(11, 11).append(this.getName()).toHashCode();
    }


}
