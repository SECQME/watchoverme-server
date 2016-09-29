package com.secqme.domain.model;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

@Entity
@Table(name="countries")
public class CountryVO implements Serializable, Comparable {
    
    @Id
    private String iso;
    @Column(name="name")
    private String countryName;
    private String callingCode;

    public CountryVO() {
        // Empty Constructor
    }

    public String getCallingCode() {
        return callingCode;
    }

    public void setCallingCode(String callingCode) {
        this.callingCode = callingCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getIso() {
        return iso;
    }

    public void setIso(String iso) {
        this.iso = iso;
    }

    @Override
    public String toString() {
        return "CountryVO{" + "iso=" + iso + '}';
    }



    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CountryVO otherVO = (CountryVO) obj;

        return new EqualsBuilder().appendSuper(super.equals(obj))
                                  .append(this.iso, otherVO.iso)
                                  .isEquals();

    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(5,11)
                .append(this.iso)
                .toHashCode();
    }

    public int compareTo(Object otherObject) {
        if(otherObject == null) {
            return 1;
        }
        CountryVO countryVO = (CountryVO) otherObject;
        return (this.getCountryName()).compareTo(countryVO.getCountryName());
    }


}
