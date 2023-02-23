/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import org.kuali.kfs.core.api.impex.xml.XmlConstants;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorDetail;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;simpleContent&gt;
 *     &lt;extension base="&lt;http://www.kuali.org/kfs/sys/types&gt;oneToTwentyFiveCharType"&gt;
 *       &lt;attribute name="id_type" use="required" type="{http://www.kuali.org/kfs/sys/types}oneAlphaType" /&gt;
 *     &lt;/extension&gt;
 *   &lt;/simpleContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = "value")
@XmlRootElement(name = "payee_id", namespace = XmlConstants.PAYMENT_NAMESPACE)
public class PayeeId implements Serializable {
    private static final long serialVersionUID = -4519718925159242810L;

    @XmlAttribute(name = "id_type", required = true)
    protected String idType;

    @XmlValue
    protected String value;

    /**
     * Gets the value of the idType property.
     *
     * @return possible object is {@link String }
     */
    public String getIdType() {
        return idType;
    }

    /**
     * Sets the value of the idType property.
     *
     * @param idType allowed object is {@link String }
     */
    public void setIdType(final String idType) {
        this.idType = idType;
    }

    /**
     * Gets the value of the value property.
     *
     * @return possible object is {@link String }
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     *
     * @param value allowed object is {@link String }
     */
    public void setValue(final String value) {
        this.value = value;
    }

}
