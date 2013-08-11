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

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

/**
 * SciQuestPunchoutDataItem
 *  
 * @author Tom Bradford <tbradford@rsmart.com>
 */
public class SciQuestPunchoutDataItem 
    extends PersistableBusinessObjectBase
    implements Serializable {
	
	private static final long serialVersionUID = 3141592653589793201L;
	
	private Integer punchoutItemId;
    private Integer punchoutId;
    
    private String spsc;
    private String unspsc;
    private String commodityCode;
    private String productSource;
    
    private Boolean controlled;
    private Boolean green;
    private Boolean hazardous;
    private Boolean radioactive;
    private Boolean radioactiveMinor;
    private Boolean selectAgent;
    private Boolean toxin;
        
	private Timestamp lastUpdate;

    private SciQuestPunchoutData punchoutData;
    
    public Integer getPunchoutItemId() {
        return punchoutItemId;
    }

    public void setPunchoutItemId(Integer punchoutItemId) {
        this.punchoutItemId = punchoutItemId;
    }

    public Integer getPunchoutId() {
        return punchoutId;
    }

    public void setPunchoutId(Integer punchoutId) {
        this.punchoutId = punchoutId;
    }

    public String getSpsc() {
        return spsc;
    }

    public void setSpsc(String spsc) {
        this.spsc = spsc;
    }

    public String getUnspsc() {
        return unspsc;
    }

    public void setUnspsc(String unspsc) {
        this.unspsc = unspsc;
    }

    public String getCommodityCode() {
        return commodityCode;
    }

    public void setCommodityCode(String commodityCode) {
        this.commodityCode = commodityCode;
    }

    public String getProductSource() {
        return productSource;
    }

    public void setProductSource(String productSource) {
        this.productSource = productSource;
    }

    public Boolean getControlled() {
        return controlled;
    }

    public void setControlled(Boolean controlled) {
        this.controlled = controlled;
    }

    public Boolean getGreen() {
        return green;
    }

    public void setGreen(Boolean green) {
        this.green = green;
    }

    public Boolean getHazardous() {
        return hazardous;
    }

    public void setHazardous(Boolean hazardous) {
        this.hazardous = hazardous;
    }

    public Boolean getRadioactive() {
        return radioactive;
    }

    public void setRadioactive(Boolean radioactive) {
        this.radioactive = radioactive;
    }

    public Boolean getRadioactiveMinor() {
        return radioactiveMinor;
    }

    public void setRadioactiveMinor(Boolean radioactiveMinor) {
        this.radioactiveMinor = radioactiveMinor;
    }

    public Boolean getSelectAgent() {
        return selectAgent;
    }

    public void setSelectAgent(Boolean selectAgent) {
        this.selectAgent = selectAgent;
    }

    public Boolean getToxin() {
        return toxin;
    }

    public void setToxin(Boolean toxin) {
        this.toxin = toxin;
    }

    public SciQuestPunchoutData getPunchoutData() {
        return punchoutData;
    }

    public void setPunchoutData(SciQuestPunchoutData punchoutData) {
        this.punchoutData = punchoutData;
    }

    public Timestamp getLastUpdate() {
		return lastUpdate;
	}

	public void setLastUpdate(Timestamp lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	
  	// handle automatic updating of the timestamp
    @Override
    protected void prePersist() {
      super.prePersist();
      lastUpdate = new Timestamp(System.currentTimeMillis());
    }
    
    @Override
    protected void preUpdate() {
      super.preUpdate();
      lastUpdate = new Timestamp(System.currentTimeMillis());
    }
    
    /**
     * @see org.kuali.core.bo.BusinessObjectBase#toStringMapper()
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();

        m.put("punchoutItemId", getPunchoutItemId());
        m.put("punchoutId", getPunchoutId());
        
        m.put("spsc", getSpsc());
        m.put("unspsc", getUnspsc());
        m.put("commodityCode", getCommodityCode());
        m.put("productSource", getProductSource());

        m.put("controlled", getControlled());
        m.put("green", getGreen());
        m.put("hazardous", getHazardous());
        m.put("radioactive", getRadioactive());
        m.put("radioactiveMinor", getRadioactiveMinor());
        m.put("selectAgent", getSelectAgent());
        m.put("toxin", getToxin());
        
        return m;
    }
}
