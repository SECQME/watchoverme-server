package com.secqme.domain.model.billing;

import org.eclipse.persistence.annotations.PrivateOwned;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author coolboykl
 */
@Entity
@Table(name="market")
@NamedQueries({
    @NamedQuery(name = MarketVO.QUERY_FIND_ALL,
    query = "SELECT o "
    + "FROM MarketVO o ")
})
public class MarketVO implements Serializable {
    
    public static final String QUERY_FIND_ALL = "marketVO.findAll";
    
    @Id
    private String name;
    private String remark;
    
    @OneToMany(mappedBy = "marketVO",
              fetch = FetchType.EAGER,
              cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    @PrivateOwned
    private List<MarketBillingPkgVO> billingPkgList;

    public MarketVO() {
        // empty Constuctor
    }

    public List<MarketBillingPkgVO> getBillingPkgList() {
        return billingPkgList;
    }

    public void setBillingPkgList(List<MarketBillingPkgVO> billingPkgList) {
        this.billingPkgList = billingPkgList;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	@Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MarketVO other = (MarketVO) obj;
        if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
            return false;
        }
        if ((this.remark == null) ? (other.remark != null) : !this.remark.equals(other.remark)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + (this.remark != null ? this.remark.hashCode() : 0);
        return hash;
    }

  
    
    
}
