package com.secqme.domain.model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name="timeZones")
@NamedQueries({
    @NamedQuery(name = TimeZoneVO.QUERY_FIND_ALL,
    query = "SELECT o "
    + "FROM TimeZoneVO o ")
})
public class TimeZoneVO implements Serializable{
    
    public static final String QUERY_FIND_ALL = "timeZoneVO.findAll";
    
    @Id
    private String latlng;
    
    private String timezoneid;

    public TimeZoneVO() {
        // Empty Constructor
    }

    public String getLatlng() {
        return latlng;
    }

    public void setLatlng(String latlng) {
        this.latlng = latlng;
    }

    public String getTimezoneid() {
        return timezoneid;
    }

    public void setTimezoneid(String timezoneid) {
        this.timezoneid = timezoneid;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + (this.latlng != null ? this.latlng.hashCode() : 0);
        hash = 41 * hash + (this.timezoneid != null ? this.timezoneid.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TimeZoneVO other = (TimeZoneVO) obj;
        if ((this.latlng == null) ? (other.latlng != null) : !this.latlng.equals(other.latlng)) {
            return false;
        }
        if ((this.timezoneid == null) ? (other.timezoneid != null) : !this.timezoneid.equals(other.timezoneid)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TimeZoneVO{" + "latlng=" + latlng + ", timezoneid=" + timezoneid + '}';
    }
    
    
    
    
}
