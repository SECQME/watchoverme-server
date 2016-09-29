package com.secqme.domain.model.notification;

import org.eclipse.persistence.annotations.PrivateOwned;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Created by Edmund on 4/9/15.
 */
@Entity
@Table(name="campaigns")
@NamedQueries({
        @NamedQuery(name=CampaignsVO.QUERY_FIND_BY_ID,
                query="SELECT o "+
                      "FROM CampaignsVO o "+
                      "WHERE o.campaignTag = :campaignTag")
})
public class CampaignsVO implements Serializable {

    public static final String QUERY_FIND_BY_ID = "campaignsVO.findById";

    @Id
    private String campaignTag;
    private String campaignSub;
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdDate;

    public CampaignsVO(){}

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public String getCampaignSub() {
        return campaignSub;
    }

    public void setCampaignSub(String campaignSub) {
        this.campaignSub = campaignSub;
    }

    public String getCampaignTag() {
        return campaignTag;
    }

    public void setCampaignTag(String campaignTag) {
        this.campaignTag = campaignTag;
    }
}
