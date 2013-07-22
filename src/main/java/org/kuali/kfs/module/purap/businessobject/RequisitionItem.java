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

package org.kuali.kfs.module.purap.businessobject;

import org.kuali.kfs.module.purap.document.RequisitionDocument;

/**
 * Requisition Item Business Object.
 */
public class RequisitionItem extends PurchasingItemBase {

    private boolean itemRestrictedIndicator;
    /**
     * Default constructor.
     */
    public RequisitionItem() {
    }
    
    public boolean isItemRestrictedIndicator() {
        return itemRestrictedIndicator;
    }

    public void setItemRestrictedIndicator(boolean itemRestrictedIndicator) {
        this.itemRestrictedIndicator = itemRestrictedIndicator;
    }   

    public RequisitionDocument getRequisition() {
        return super.getPurapDocument();
    }

    public void setRequisition(RequisitionDocument requisition) {
        setPurapDocument(requisition);
    }



    /**
     * @see org.kuali.kfs.module.purap.businessobject.PurchasingItemBase#getAccountingLineClass()
     */
    @Override
    public Class getAccountingLineClass() {
        return RequisitionAccount.class;
    }

    @Override
    public Class getUseTaxClass() {
        return PurchaseRequisitionItemUseTax.class;
    }

    // KFSPTS-2257
	public String getEshopFlags() {
		StringBuilder sb = new StringBuilder();
		if (isControlled()) {
			sb.append("Controlled,");
		}
		if (isEnergyStar()) {
			sb.append("Energy Star,");
		}
		if (isSelectAgent()) {
			sb.append("Select Agent,");
		}
		if (isRadioactive()) {
			sb.append("Radioactive,");
		}
		if (isRadioactiveMinor()) {
			sb.append("Radioactive Minor,");
		}
		if (isRecycled()) {
			sb.append("Recycled,");
		}
		if (isGreen()) {
			sb.append("Green,");
		}
		if (isToxin()) {
			sb.append("Toxin,");
		}
		if (isHazardous()) {
			sb.append("Hazardous,");
		}
        if (sb.length() == 0) {
        	return "No e-Shop flag is set to true";
        }
		return sb.substring(0, sb.length()-1);
	}
	

}
