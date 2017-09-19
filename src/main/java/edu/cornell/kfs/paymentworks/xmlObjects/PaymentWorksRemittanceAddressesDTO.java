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

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "remittance_addresses")
@XmlAccessorType(XmlAccessType.FIELD)
public class PaymentWorksRemittanceAddressesDTO {

    @XmlElement(name = "list-item")
    private List<PaymentWorksRemittanceAddressDTO> remittance_address;

    public List<PaymentWorksRemittanceAddressDTO> getRemittance_address() {
        return remittance_address;
    }

    public void setRemittance_address(List<PaymentWorksRemittanceAddressDTO> remittance_address) {
        this.remittance_address = remittance_address;
    }

}
