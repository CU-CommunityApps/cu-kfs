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
package org.kuali.kfs.module.purap.util.cxml;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import org.kuali.kfs.core.api.impex.xml.XmlConstants;

import java.math.BigDecimal;

/**
 * <p>Java class for anonymous complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="Total"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.kuali.org/kfs/purap/b2bPunchOutOrder}Money"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="operationAllowed" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *       &lt;attribute name="quoteStatus" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = "total")
public class PunchOutOrderMessageHeader {

    @XmlAttribute(name = "operationAllowed")
    private String operationAllowed;

    // CU customization
    @XmlAttribute(name = "businessPurpose")
    private String quoteStatus;

    @XmlElement(name = "Total", namespace = XmlConstants.B2B_PUNCH_OUT_ORDER_NAMESPACE, required = true)
    private Total total;

    /**
     * Gets the value of the operationAllowed property.
     *
     * @return possible object is {@link String }
     */
    public String getOperationAllowed() {
        return operationAllowed;
    }

    /**
     * Sets the value of the operationAllowed property.
     *
     * @param operationAllowed allowed object is {@link String }
     */
    public void setOperationAllowed(final String operationAllowed) {
        this.operationAllowed = operationAllowed;
    }

    /**
     * Gets the value of the quoteStatus property.
     *
     * @return possible object is {@link String }
     */
    public String getQuoteStatus() {
        return quoteStatus;
    }

    /**
     * Sets the value of the quoteStatus property.
     *
     * @param quoteStatus allowed object is {@link String }
     */
    public void setQuoteStatus(final String quoteStatus) {
        this.quoteStatus = quoteStatus;
    }

    /**
     * Gets the value of the total property.
     *
     * @return possible object is {@link Total }
     */
    public Total getTotal() {
        return total;
    }

    /**
     * Sets the value of the total property.
     *
     * @param total allowed object is {@link Total }
     */
    public void setTotal(final Total total) {
        this.total = total;
    }

    /**
     * <p>Java class for anonymous complex type.
     *
     * <p>The following schema fragment specifies the expected content contained within this class.
     *
     * <pre>
     * &lt;complexType&gt;
     *   &lt;complexContent&gt;
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
     *       &lt;sequence&gt;
     *         &lt;element ref="{http://www.kuali.org/kfs/purap/b2bPunchOutOrder}Money"/&gt;
     *       &lt;/sequence&gt;
     *     &lt;/restriction&gt;
     *   &lt;/complexContent&gt;
     * &lt;/complexType&gt;
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = "money")
    public static class Total {

        @XmlElement(name = "Money", namespace = XmlConstants.B2B_PUNCH_OUT_ORDER_NAMESPACE, required = true,
                defaultValue = "0")
        private Money money;

        public Total() {
        }

        public Total(final String currency, final BigDecimal value) {
            money = new Money(currency, value);
        }

        /**
         * Gets the value of the money property.
         *
         * @return possible object is {@link Money }
         */
        public Money getMoney() {
            return money;
        }

        /**
         * Sets the value of the money property.
         *
         * @param value allowed object is {@link Money }
         */
        public void setMoney(final Money value) {
            money = value;
        }

    }

}
