/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.module.ld.businessobject;


import java.util.LinkedHashMap;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.rice.kns.bo.Inactivateable;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.KualiInteger;

/**
 * BO for the Labor Benefit Rate Category Fringe Benefit
 * 
 * @author Allan Sonkin
 */
public class LaborBenefitRateCategory extends PersistableBusinessObjectBase implements Inactivateable {

    private String laborBenefitRateCategoryCode;//the BO code
    
    private Boolean activeIndicator = false;     //indicates active status of this BO
    
    private String codeDesc;                    //description for the BO
    
    
    /**
     * Getter method to get the laborBenefitRateCategoryCode
     * @return laborBenefitRateCategoryCode
     */
	public String getLaborBenefitRateCategoryCode() {
		return laborBenefitRateCategoryCode;
	}
    
    /**
     * 
     * Method to set the code
     * @param code
     */
    public void setLaborBenefitRateCategoryCode(String laborBenefitRateCategoryCode) {
		this.laborBenefitRateCategoryCode = laborBenefitRateCategoryCode;
	}


    /**
     * 
     * Getter method for the active indicator
     * @return activeIndicator
     */
    public Boolean getActiveIndicator() {
        return activeIndicator;
    }

    /**
     * 
     * Sets the activeIndicator
     * @param activeIndicator
     */
    public void setActiveIndicator(Boolean activeIndicator) {
        this.activeIndicator = activeIndicator;
    }

    /**
     * 
     * Getter method for the code's description
     * @return codeDesc
     */
    public String getCodeDesc() {
        return codeDesc;
    }

	/**
	 * 
	 * Sets the codeDesc
	 * @param codeDesc
	 */
    public void setCodeDesc(String codeDesc) {
        this.codeDesc = codeDesc;
    }

    @Override
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        
        m.put("laborBenefitRateCategoryCode", this.laborBenefitRateCategoryCode);
        m.put("codeDesc", this.codeDesc);
        
        return m;
    }

    
    public boolean isActive() {
        return this.activeIndicator;
    }

    
    public void setActive(boolean arg0) {
        this.setActiveIndicator(arg0);
        
    }
    
}
