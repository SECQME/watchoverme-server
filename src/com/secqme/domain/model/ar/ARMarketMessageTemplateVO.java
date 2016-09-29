package com.secqme.domain.model.ar;

import com.secqme.domain.model.billing.MarketVO;
import com.secqme.domain.model.pushmessage.UserPushMessageToken;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.eclipse.persistence.annotations.PrivateOwned;

/**
 * Model class for ARMarketMessageTemplateVO
 * @author coolboykl
 */
@Entity
@Table(name="arMarketMsgTemplate")
@NamedQueries({
    @NamedQuery(name = ARMarketMessageTemplateVO.QUERY_FIND_ALL,
    query = "SELECT o FROM ARMarketMessageTemplateVO o ")
})
public class ARMarketMessageTemplateVO implements Serializable {
    
    public static final String QUERY_FIND_ALL = "arMarketMessageTemplateVO.findAll";

    @Id
    private String code;
    
    @ManyToOne
    @JoinColumn(name = "market")
    private MarketVO marketVO;
    
    @OneToMany(mappedBy = "arMarketMessageTemplateVO",
    fetch = FetchType.EAGER,
    cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})

    @PrivateOwned
    private List<ARMessageTemplateVO> messageTemplateList;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public MarketVO getMarketVO() {
        return marketVO;
    }

    public void setMarketVO(MarketVO marketVO) {
        this.marketVO = marketVO;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.code != null ? this.code.hashCode() : 0);
        hash = 37 * hash + (this.marketVO != null ? this.marketVO.hashCode() : 0);
        return hash;
    }

    public List<ARMessageTemplateVO> getMessageTemplateList() {
        return messageTemplateList;
    }

    public void setMessageTemplateList(List<ARMessageTemplateVO> messageTemplateList) {
        this.messageTemplateList = messageTemplateList;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ARMarketMessageTemplateVO other = (ARMarketMessageTemplateVO) obj;
        if ((this.code == null) ? (other.code != null) : !this.code.equals(other.code)) {
            return false;
        }
        if (this.marketVO != other.marketVO && (this.marketVO == null || !this.marketVO.equals(other.marketVO))) {
            return false;
        }
        return true;
    }
}
