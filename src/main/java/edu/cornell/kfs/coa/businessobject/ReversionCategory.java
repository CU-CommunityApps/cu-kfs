/*
 * Copyright 2006 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package edu.cornell.kfs.coa.businessobject;

import java.util.LinkedHashMap;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;

/**
 * 
 */
public class ReversionCategory extends PersistableBusinessObjectBase implements MutableInactivatable {

    private String reversionCategoryCode;
    private String reversionCategoryName;
    private String reversionSortCode;
    private boolean active;

    /**
     * Default constructor.
     */
    public ReversionCategory() {

    }

    
    /**
     * Gets the reversionCategoryCode attribute. 
     * @return Returns the reversionCategoryCode.
     */
    public String getReversionCategoryCode() {
        return reversionCategoryCode;
    }


    /**
     * Sets the reversionCategoryCode attribute value.
     * @param reversionCategoryCode The reversionCategoryCode to set.
     */
    public void setReversionCategoryCode(String reversionCategoryCode) {
        this.reversionCategoryCode = reversionCategoryCode;
    }


    /**
     * Gets the reversionCategoryName attribute. 
     * @return Returns the reversionCategoryName.
     */
    public String getReversionCategoryName() {
        return reversionCategoryName;
    }


    /**
     * Sets the reversionCategoryName attribute value.
     * @param reversionCategoryName The reversionCategoryName to set.
     */
    public void setReversionCategoryName(String reversionCategoryName) {
        this.reversionCategoryName = reversionCategoryName;
    }


    /**
     * Gets the reversionSortCode attribute. 
     * @return Returns the reversionSortCode.
     */
    public String getReversionSortCode() {
        return reversionSortCode;
    }


    /**
     * Sets the reversionSortCode attribute value.
     * @param reversionSortCode The reversionSortCode to set.
     */
    public void setReversionSortCode(String reversionSortCode) {
        this.reversionSortCode = reversionSortCode;
    }


    /**
     * Gets the active attribute.
     * 
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets the active attribute value.
     * 
     * @param active The active to set.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @see org.kuali.kfs.kns.bo.BusinessObjectBase#toStringMapper()
     */
    protected LinkedHashMap toStringMapper() {
        LinkedHashMap m = new LinkedHashMap();
        return m;
    }

}
