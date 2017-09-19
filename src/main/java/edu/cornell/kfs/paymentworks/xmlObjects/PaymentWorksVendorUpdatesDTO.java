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
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "list-item")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksVendorUpdatesDTO {

    private String id;
    private PaymentWorksVendorNumbersDTO vendor_nums;
    private String vendor_name;
    private String group_name;
    private String status;
    private PaymentWorksFieldChangesDTO field_changes;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public PaymentWorksVendorNumbersDTO getVendor_nums() {
        return vendor_nums;
    }

    public void setVendor_nums(PaymentWorksVendorNumbersDTO vendor_nums) {
        this.vendor_nums = vendor_nums;
    }

    public String getVendor_name() {
        return vendor_name;
    }

    public void setVendor_name(String vendor_name) {
        this.vendor_name = vendor_name;
    }

    public String getGroup_name() {
        return group_name;
    }

    public void setGroup_name(String group_name) {
        this.group_name = group_name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public PaymentWorksFieldChangesDTO getField_changes() {
        return field_changes;
    }

    public void setField_changes(PaymentWorksFieldChangesDTO field_changes) {
        this.field_changes = field_changes;
    }

}
