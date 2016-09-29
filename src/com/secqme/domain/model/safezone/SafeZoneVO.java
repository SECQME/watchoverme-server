package com.secqme.domain.model.safezone;

import com.secqme.domain.model.UserVO;
import com.secqme.rs.v2.CommonJSONKey;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * User: James Khoo
 * Date: 2/10/14
 * Time: 10:30 AM
 */

@Entity
@Table(name = "safeZones")
public class SafeZoneVO implements Serializable {
	public static final String SAFE_ZONE_HOME = "Home";
	public static final String SAFE_ZONE_WORK = "Work";
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "createdBy")
    private UserVO createdByUserVO;
    private String zoneName;
    private String zoneInternalName;
    private String address;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;
    private Double latitude;
    private Double longitude;
    private Double radius;
    private Boolean autoConfirmSafety;

    public SafeZoneVO() {
        // Empty Constructor
    }

    public SafeZoneVO(UserVO createdByUserVO, String zoneName,
                      Double latitude, Double longitude,
                      Double radius, Boolean autoConfirmSafety) {
        this.createdByUserVO = createdByUserVO;
        this.zoneName = zoneName;
        this.zoneInternalName = zoneName + "_" + createdByUserVO.getUserid();
        this.createdDate = new Date();
        this.latitude = latitude;
        this.longitude = longitude;
        this.radius = radius;
        this.autoConfirmSafety = autoConfirmSafety;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserVO getCreatedByUserVO() {
        return createdByUserVO;
    }

    public void setCreatedByUserVO(UserVO cretedByUserVO) {
        this.createdByUserVO = cretedByUserVO;
    }

    public String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getZoneInternalName() {
        return zoneInternalName;
    }

    public void setZoneInternalName(String zoneInternalName) {
        this.zoneInternalName = zoneInternalName;
    }

    public Boolean getAutoConfirmSafety() {
        return autoConfirmSafety;
    }

    public void setAutoConfirmSafety(Boolean autoConfirmSafety) {
        this.autoConfirmSafety = autoConfirmSafety;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double getRadius() {
        return radius;
    }

    public void setRadius(Double radius) {
        this.radius = radius;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SafeZoneVO that = (SafeZoneVO) o;

        if (!id.equals(that.id)) return false;
        if (!latitude.equals(that.latitude)) return false;
        if (!longitude.equals(that.longitude)) return false;
        if (zoneInternalName != null ? !zoneInternalName.equals(that.zoneInternalName) : that.zoneInternalName != null)
            return false;
        if (!zoneName.equals(that.zoneName)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(7, 17).append(this.id).toHashCode();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put(CommonJSONKey.ZONE_ID_KEY, this.getId())
                .put(CommonJSONKey.ZONE_NAME_KEY, this.getZoneName())
                .put(CommonJSONKey.CREATED_BY_USER_KEY, this.getCreatedByUserVO().getUserid())
                .put(CommonJSONKey.LATITUDE_KEY, this.getLatitude())
                .put(CommonJSONKey.LONGITUDE_KEY, this.getLongitude())
                .put(CommonJSONKey.LOCATION_ACCURACY_KEY, this.getRadius())
                .put(CommonJSONKey.ZONE_ADDRESS_KEY, this.getAddress())
                .put(CommonJSONKey.AUTO_CONFIRM_SAFETY_KEY, this.getAutoConfirmSafety());


        return jobj;
    }


}
