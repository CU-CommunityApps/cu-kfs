/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.pdp.businessobject;

import org.kuali.kfs.core.api.mo.common.Coded;
import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.fp.businessobject.DisbursementPayee;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.context.SpringContext;

//Cornell customization: this was overlayed to add the principalName.
public class ACHPayee extends DisbursementPayee implements MutableInactivatable {

    private String entityId;
    //Cornell customization
    private String principalName;

    public ACHPayee() {
        super();
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(final String entityId) {
        this.entityId = entityId;
    }

    @Override
    public String getPayeeTypeDescription() {
        final Coded payeeType = SpringContext.getBean(BusinessObjectService.class).findBySinglePrimaryKey(
                PayeeType.class, getPayeeTypeCode());
        return payeeType.getName();
    }
    
    //Cornell customization
    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(final String principalName) {
        this.principalName = principalName;
    }

    /**
     * Getter for ACH person that always returns null; it is only intended to aid with
     * generating a lookup icon for the "principalName" property.
     * 
     * @return null.
     */
    public Person getAchPerson() {
        return null;
    }

    /**
     * No-op setter for ACH person; it is only intended to aid with
     * generating a lookup icon for the "principalName" property.
     * 
     * @param achPerson The ACH person to set; not actually used.
     */
    public void setAchPerson(Person achPerson) {
        // Do nothing.
    }
    // end Cornell customization

}
