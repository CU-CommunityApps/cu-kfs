/*
 * Copyright 2009 The Kuali Foundation.
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
package org.kuali.kfs.module.purap.businessobject;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.ojb.broker.PersistenceBroker;
import org.apache.ojb.broker.PersistenceBrokerException;
import org.kuali.kfs.module.purap.document.RequisitionDocument;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;

/**
 * SciQuestPunchoutData
 *  
 * @author Tom Bradford <tbradford@rsmart.com>
 */
public class SciQuestPunchoutData 
    extends PersistableBusinessObjectBase
    implements Serializable {

	private static final long serialVersionUID = 3141592653589793202L;
	
    private Integer punchoutId;
    private Integer requisitionId;
    
    private String requisitionName;
    private String shipToAddressCode;
    
    private RequisitionDocument requisition;    
    private List<SciQuestPunchoutDataItem> items;
    
	private Timestamp lastUpdate;
	
    public Integer getPunchoutId() {
        return punchoutId;
    }

    public void setPunchoutId(Integer punchoutId) {
        this.punchoutId = punchoutId;
    }

    public Integer getRequisitionId() {
        return requisitionId;
    }

    public void setRequisitionId(Integer requisitionId) {
        this.requisitionId = requisitionId;
    }

    public String getRequisitionName() {
        return requisitionName;
    }

    public void setRequisitionName(String requisitionName) {
        this.requisitionName = requisitionName;
    }

	public String getShipToAddressCode() {
		return shipToAddressCode;
	}

	public void setShipToAddressCode(String shipToAddressCode) {
		this.shipToAddressCode = shipToAddressCode;
	}

    public RequisitionDocument getRequisition() {
        return requisition;
    }

    public void setRequisition(RequisitionDocument requisition) {
        this.requisition = requisition;
    }
    
    public List<SciQuestPunchoutDataItem> getItems() {
        return items;
    }

    public void setItems(List<SciQuestPunchoutDataItem> items) {
        this.items = items;
    }

    /**
     * Returns true if any of the child items have a controlled status
     * @return if controlled
     */
    public boolean isControlled() {
        List<SciQuestPunchoutDataItem> items = getItems();
        for ( SciQuestPunchoutDataItem item : items ) {
            if ( item.getControlled().booleanValue() )
                return true;
        }
        return false;
    }

    /**
     * Returns true if any of the child items have a green status
     * @return if green
     */    
    public boolean isGreen() {
        List<SciQuestPunchoutDataItem> items = getItems();
        for ( SciQuestPunchoutDataItem item : items ) {
            if ( item.getGreen().booleanValue() )
                return true;
        }
        return false;
    }

    /**
     * Returns true if any of the child items have a hazardous status
     * @return if hazardous
     */
    public boolean isHazardous() {
        List<SciQuestPunchoutDataItem> items = getItems();
        for ( SciQuestPunchoutDataItem item : items ) {
            if ( item.getHazardous().booleanValue() )
                return true;
        }
        return false;
    }
    
    /**
     * Returns true if any of the child items have a radioactive status
     * @return if radioactive
     */    
    public boolean isRadioactive() {
        List<SciQuestPunchoutDataItem> items = getItems();
        for ( SciQuestPunchoutDataItem item : items ) {
            if ( item.getRadioactive().booleanValue() )
                return true;
        }
        return false;
    }
    
    /**
     * Returns true if any of the child items have a radioactive minor status
     * @return if radioactive minor
     */    
    public boolean isRadioactiveMinor() {
        List<SciQuestPunchoutDataItem> items = getItems();
        for ( SciQuestPunchoutDataItem item : items ) {
            if ( item.getRadioactiveMinor().booleanValue() )
                return true;
        }
        return false;
    }
    
    /**
     * Returns true if any of the child items have a select agent status
     * @return if select agent
     */
    public boolean isSelectAgent() {
        List<SciQuestPunchoutDataItem> items = getItems();
        for ( SciQuestPunchoutDataItem item : items ) {
            if ( item.getSelectAgent().booleanValue() )
                return true;
        }
        return false;
    }
    
    /**
     * Returns true if any of the child items have a toxin status
     * @return if toxin
     */    
    public boolean isToxin() {
        List<SciQuestPunchoutDataItem> items = getItems();
        for ( SciQuestPunchoutDataItem item : items ) {
            if ( item.getToxin().booleanValue() )
                return true;
        }
        return false;
    }

    public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	// handle automatic updating of the timestamp
	
    public void beforeInsert(PersistenceBroker persistenceBroker) throws PersistenceBrokerException {
    	super.beforeInsert( persistenceBroker );
        lastUpdate = new Timestamp(System.currentTimeMillis());
    }

    public void beforeUpdate(PersistenceBroker persistenceBroker) throws PersistenceBrokerException {
    	super.beforeUpdate( persistenceBroker );
        lastUpdate = new Timestamp(System.currentTimeMillis());
    }
	
	/**
     * @see org.kuali.core.bo.BusinessObjectBase#toStringMapper()
     */
    @SuppressWarnings("unchecked")
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        m.put("punchoutId", getPunchoutId());
        m.put("requisitionId", getRequisitionId());
        m.put("requisitionName", getRequisitionName());
        m.put("shipToAddressCode", getShipToAddressCode());
        
        return m;
    }
}
