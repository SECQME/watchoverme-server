package com.secqme.domain.model.location;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * User: James Khoo
 * Date: 2/20/14
 * Time: 1:14 PM
 */
@Entity
@Table(name="googlePlaceCache")
@NamedQueries({
        @NamedQuery(name=GooglePlaceCacheVO.QUERY_FIND_BY_NAME,
        query="SELECT o " +
              "FROM GooglePlaceCacheVO  o " +
              "WHERE o.placeName = :placeName")
})
public class GooglePlaceCacheVO implements Serializable {

    public static final String QUERY_FIND_BY_NAME = "GooglePlaceCacheVO.findByName";

    @Id
    private String placeName;
    private String result;

    @Temporal(TemporalType.DATE)
    private Date updatedDate;

    public GooglePlaceCacheVO() {
        // Empty Constuctor;
    }

    public GooglePlaceCacheVO(String placeName, String result) {
        this.placeName = placeName;
        this.result = result;
        this.updatedDate = new Date();
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Date getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(Date updateDate) {
        this.updatedDate = updateDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GooglePlaceCacheVO that = (GooglePlaceCacheVO) o;

        if (placeName != null ? !placeName.equals(that.placeName) : that.placeName != null) return false;
        if (result != null ? !result.equals(that.result) : that.result != null) return false;
        if (updatedDate != null ? !updatedDate.equals(that.updatedDate) : that.updatedDate != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result1 = placeName != null ? placeName.hashCode() : 0;
        result1 = 31 * result1 + (result != null ? result.hashCode() : 0);
        result1 = 31 * result1 + (updatedDate != null ? updatedDate.hashCode() : 0);
        return result1;
    }
}
