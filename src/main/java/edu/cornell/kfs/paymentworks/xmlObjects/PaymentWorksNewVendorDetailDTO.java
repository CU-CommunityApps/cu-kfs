/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2014 The Kuali Foundation
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package edu.cornell.kfs.paymentworks.xmlObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "root")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksNewVendorDetailDTO {

    private String id;
    private String request_status;
    @XmlElement(name = "custom_fields")
    private PaymentWorksCustomFieldsDTO custom_fields;
    private PaymentWorksRequestingCompanyDTO requesting_company;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRequest_status() {
        return request_status;
    }

    public void setRequest_status(String request_status) {
        this.request_status = request_status;
    }

    public PaymentWorksRequestingCompanyDTO getRequesting_company() {
        return requesting_company;
    }

    public void setRequesting_company(PaymentWorksRequestingCompanyDTO requesting_company) {
        this.requesting_company = requesting_company;
    }

    public PaymentWorksCustomFieldsDTO getCustom_fields() {
        return custom_fields;
    }

    public void setCustom_fields(PaymentWorksCustomFieldsDTO custom_fields) {
        this.custom_fields = custom_fields;
    }

}
