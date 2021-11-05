/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
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
package org.kuali.kfs.module.cg.businessobject;

import org.kuali.kfs.core.api.mo.common.active.MutableInactivatable;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.sys.businessobject.Country;
import org.kuali.kfs.sys.businessobject.State;

/**
 * Subcontractors are vendors involved with an awarded {@link Proposal}.
 */
/* Cornell Customization: backport redis */
public class SubContractor extends PersistableBusinessObjectBase implements MutableInactivatable {

    protected String subcontractorNumber;
    protected String subcontractorName;
    protected String subcontractorAddressLine1;
    protected String subcontractorAddressLine2;
    protected String subcontractorCity;
    protected String subcontractorStateCode;
    protected String subcontractorZipCode;
    protected String subcontractorCountryCode;
    protected boolean active;

    protected Country subcontractorCountry;
    protected State subcontractorState;

    public String getSubcontractorNumber() {
        return subcontractorNumber;
    }

    public void setSubcontractorNumber(String subcontractorNumber) {
        this.subcontractorNumber = subcontractorNumber;
    }

    public String getSubcontractorName() {
        return subcontractorName;
    }

    public void setSubcontractorName(String subcontractorName) {
        this.subcontractorName = subcontractorName;
    }

    public String getSubcontractorAddressLine1() {
        return subcontractorAddressLine1;
    }

    public void setSubcontractorAddressLine1(String subcontractorAddressLine1) {
        this.subcontractorAddressLine1 = subcontractorAddressLine1;
    }

    public String getSubcontractorAddressLine2() {
        return subcontractorAddressLine2;
    }

    public void setSubcontractorAddressLine2(String subcontractorAddressLine2) {
        this.subcontractorAddressLine2 = subcontractorAddressLine2;
    }

    public String getSubcontractorCity() {
        return subcontractorCity;
    }

    public void setSubcontractorCity(String subcontractorCity) {
        this.subcontractorCity = subcontractorCity;
    }

    public String getSubcontractorStateCode() {
        return subcontractorStateCode;
    }

    public void setSubcontractorStateCode(String subcontractorStateCode) {
        this.subcontractorStateCode = subcontractorStateCode;
    }

    public String getSubcontractorZipCode() {
        return subcontractorZipCode;
    }

    public void setSubcontractorZipCode(String subcontractorZipCode) {
        this.subcontractorZipCode = subcontractorZipCode;
    }

    public String getSubcontractorCountryCode() {
        return subcontractorCountryCode;
    }

    public void setSubcontractorCountryCode(String subcontractorCountryCode) {
        this.subcontractorCountryCode = subcontractorCountryCode;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    @Override
    public void setActive(boolean active) {
        this.active = active;
    }

    public Country getSubcontractorCountry() {
        return subcontractorCountry;
    }

    public void setSubcontractorCountry(Country country) {
        this.subcontractorCountry = country;
    }

    public State getSubcontractorState() {
        return subcontractorState;
    }

    public void setSubcontractorState(State state) {
        this.subcontractorState = state;
    }

}
