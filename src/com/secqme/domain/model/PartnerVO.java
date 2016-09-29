package com.secqme.domain.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author winsontan 20120620
 */
@Entity
@Table(name="partners")
public class PartnerVO {
	
    @Id
    private String code;
    
    @Column(name="name")
    private String name;
    
    
    public PartnerVO() {
        // Empty Constructor
    }
    
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
    
}
