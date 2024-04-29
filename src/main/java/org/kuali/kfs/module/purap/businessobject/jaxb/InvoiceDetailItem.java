/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.module.purap.businessobject.jaxb;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlSchemaType;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.core.api.impex.xml.XmlConstants;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceItem;
import org.kuali.kfs.module.purap.util.cxml.CxmlExtrinsic;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

// CU Customization: Fix for missing Description tag in data submitted by eInvoice vendors, KFSPTS-31486/KFSPTS-31489

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
 *         &lt;element name="UnitOfMeasure" type="{http://www.kuali.org/kfs/purap/types}uomType"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}UnitPrice"/&gt;
 *         &lt;element name="InvoiceDetailItemReference"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="ItemID" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="SupplierPartID" type="{http://www.kuali.org/kfs/purap/types}catalogNumberType"/&gt;
 *                             &lt;element name="SupplierPartAuxiliaryID" type="{http://www.kuali.org/kfs/purap/types}auxiliaryIDType" minOccurs="0"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Description" minOccurs="0"/&gt;
 *                   &lt;element name="Classification" maxOccurs="unbounded" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://www.w3.org/2001/XMLSchema&gt;string"&gt;
 *                           &lt;attribute name="domain" use="required" type="{http://www.kuali.org/kfs/sys/types}oneToTenCharType" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;choice minOccurs="0"&gt;
 *                     &lt;sequence&gt;
 *                       &lt;element name="ManufacturerPartID" type="{http://www.kuali.org/kfs/purap/types}hundredCharsType"/&gt;
 *                       &lt;element name="ManufacturerName"&gt;
 *                         &lt;complexType&gt;
 *                           &lt;simpleContent&gt;
 *                             &lt;extension base="&lt;http://www.kuali.org/kfs/purap/types&gt;hundredCharsType"&gt;
 *                               &lt;attribute ref="{http://www.w3.org/XML/1998/namespace}lang"/&gt;
 *                             &lt;/extension&gt;
 *                           &lt;/simpleContent&gt;
 *                         &lt;/complexType&gt;
 *                       &lt;/element&gt;
 *                     &lt;/sequence&gt;
 *                   &lt;/choice&gt;
 *                   &lt;element name="Country" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;simpleContent&gt;
 *                         &lt;extension base="&lt;http://www.kuali.org/kfs/purap/types&gt;addressType"&gt;
 *                           &lt;attribute name="isoCountryCode" use="required" type="{http://www.kuali.org/kfs/sys/types}oneToFourCharType" /&gt;
 *                         &lt;/extension&gt;
 *                       &lt;/simpleContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="SerialNumbers" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/&gt;
 *                 &lt;/sequence&gt;
 *                 &lt;attribute name="lineNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *                 &lt;attribute name="serialNumber" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="SubtotalAmount" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Money"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Tax" minOccurs="0"/&gt;
 *         &lt;element name="InvoiceDetailLineSpecialHandling" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Money"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="InvoiceDetailLineShipping" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}InvoiceDetailShipping" minOccurs="0"/&gt;
 *                   &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Money"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}GrossAmount" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}InvoiceDetailDiscount" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}NetAmount" minOccurs="0"/&gt;
 *         &lt;element name="Distribution" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="Accounting"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="AccountingSegment" maxOccurs="unbounded"&gt;
 *                               &lt;complexType&gt;
 *                                 &lt;complexContent&gt;
 *                                   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                                     &lt;sequence&gt;
 *                                       &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Name"/&gt;
 *                                       &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Description"/&gt;
 *                                     &lt;/sequence&gt;
 *                                     &lt;attribute name="id" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                                   &lt;/restriction&gt;
 *                                 &lt;/complexContent&gt;
 *                               &lt;/complexType&gt;
 *                             &lt;/element&gt;
 *                           &lt;/sequence&gt;
 *                           &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                   &lt;element name="Charge"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Money"/&gt;
 *                           &lt;/sequence&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Comments" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Extrinsic" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="invoiceLineNumber" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedByte" /&gt;
 *       &lt;attribute name="quantity" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"unitOfMeasure", "unitPrice", "invoiceDetailItemReference", "subtotalAmount", "tax",
    "invoiceDetailLineSpecialHandling", "invoiceDetailLineShipping", "grossAmount", "invoiceDetailDiscount",
    "netAmount", "distribution", "comments", "extrinsic"})
@XmlRootElement(name = "InvoiceDetailItem", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
public class InvoiceDetailItem {

    @XmlElement(name = "UnitOfMeasure", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE, required = true)
    protected String unitOfMeasure;
    @XmlElement(name = "UnitPrice", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE, required = true)
    protected UnitPrice unitPrice;
    @XmlElement(name = "InvoiceDetailItemReference", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE, required = true)
    protected InvoiceDetailItemReference invoiceDetailItemReference;
    @XmlElement(name = "SubtotalAmount", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected SubtotalAmount subtotalAmount;
    @XmlElement(name = "Tax", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected Tax tax;
    @XmlElement(name = "InvoiceDetailLineSpecialHandling", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected InvoiceDetailLineSpecialHandling invoiceDetailLineSpecialHandling;
    @XmlElement(name = "InvoiceDetailLineShipping", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected InvoiceDetailLineShipping invoiceDetailLineShipping;
    @XmlElement(name = "GrossAmount", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected GrossAmount grossAmount;
    @XmlElement(name = "InvoiceDetailDiscount", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected InvoiceDetailDiscount invoiceDetailDiscount;
    @XmlElement(name = "NetAmount", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected NetAmount netAmount;
    @XmlElement(name = "Distribution", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected List<Distribution> distribution;
    @XmlElement(name = "Comments", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected Comments comments;
    @XmlElement(name = "Extrinsic", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected List<Extrinsic> extrinsic;
    @XmlAttribute(name = "invoiceLineNumber", required = true)
    @XmlSchemaType(name = "unsignedByte")
    protected short invoiceLineNumber;
    @XmlAttribute(name = "quantity", required = true)
    protected BigDecimal quantity;

    /**
     * Gets the value of the unitOfMeasure property.
     *
     * @return possible object is {@link String }
     */
    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    /**
     * Sets the value of the unitOfMeasure property.
     *
     * @param unitOfMeasure allowed object is {@link String }
     */
    public void setUnitOfMeasure(final String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    /**
     * Gets the value of the unitPrice property.
     *
     * @return possible object is {@link UnitPrice }
     */
    public UnitPrice getUnitPrice() {
        return unitPrice;
    }

    /**
     * Sets the value of the unitPrice property.
     *
     * @param unitPrice allowed object is {@link UnitPrice }
     */
    public void setUnitPrice(final UnitPrice unitPrice) {
        this.unitPrice = unitPrice;
    }

    /**
     * Gets the value of the invoiceDetailItemReference property.
     *
     * @return possible object is {@link InvoiceDetailItemReference }
     */
    public InvoiceDetailItemReference getInvoiceDetailItemReference() {
        return invoiceDetailItemReference;
    }

    /**
     * Sets the value of the invoiceDetailItemReference property.
     *
     * @param invoiceDetailItemReference allowed object is {@link InvoiceDetailItemReference }
     */
    public void setInvoiceDetailItemReference(final InvoiceDetailItemReference invoiceDetailItemReference) {
        this.invoiceDetailItemReference = invoiceDetailItemReference;
    }

    /**
     * Gets the value of the subtotalAmount property.
     *
     * @return possible object is {@link SubtotalAmount }
     */
    public SubtotalAmount getSubtotalAmount() {
        return subtotalAmount;
    }

    /**
     * Sets the value of the subtotalAmount property.
     *
     * @param subtotalAmount allowed object is {@link SubtotalAmount }
     */
    public void setSubtotalAmount(final SubtotalAmount subtotalAmount) {
        this.subtotalAmount = subtotalAmount;
    }

    /**
     * Gets the value of the tax property.
     *
     * @return possible object is {@link Tax }
     */
    public Tax getTax() {
        return tax;
    }

    /**
     * Sets the value of the tax property.
     *
     * @param tax allowed object is {@link Tax }
     */
    public void setTax(final Tax tax) {
        this.tax = tax;
    }

    /**
     * Gets the value of the invoiceDetailLineSpecialHandling property.
     *
     * @return possible object is {@link InvoiceDetailLineSpecialHandling }
     */
    public InvoiceDetailLineSpecialHandling getInvoiceDetailLineSpecialHandling() {
        return invoiceDetailLineSpecialHandling;
    }

    /**
     * Sets the value of the invoiceDetailLineSpecialHandling property.
     *
     * @param invoiceDetailLineSpecialHandling allowed object is {@link InvoiceDetailLineSpecialHandling }
     */
    public void setInvoiceDetailLineSpecialHandling(final InvoiceDetailLineSpecialHandling invoiceDetailLineSpecialHandling) {
        this.invoiceDetailLineSpecialHandling = invoiceDetailLineSpecialHandling;
    }

    /**
     * Gets the value of the invoiceDetailLineShipping property.
     *
     * @return possible object is {@link InvoiceDetailLineShipping }
     */
    public InvoiceDetailLineShipping getInvoiceDetailLineShipping() {
        return invoiceDetailLineShipping;
    }

    /**
     * Sets the value of the invoiceDetailLineShipping property.
     *
     * @param invoiceDetailLineShipping allowed object is {@link InvoiceDetailLineShipping }
     */
    public void setInvoiceDetailLineShipping(final InvoiceDetailLineShipping invoiceDetailLineShipping) {
        this.invoiceDetailLineShipping = invoiceDetailLineShipping;
    }

    /**
     * Gets the value of the grossAmount property.
     *
     * @return possible object is {@link GrossAmount }
     */
    public GrossAmount getGrossAmount() {
        return grossAmount;
    }

    /**
     * Sets the value of the grossAmount property.
     *
     * @param grossAmount allowed object is {@link GrossAmount }
     */
    public void setGrossAmount(final GrossAmount grossAmount) {
        this.grossAmount = grossAmount;
    }

    /**
     * Gets the value of the invoiceDetailDiscount property.
     *
     * @return possible object is {@link InvoiceDetailDiscount }
     */
    public InvoiceDetailDiscount getInvoiceDetailDiscount() {
        return invoiceDetailDiscount;
    }

    /**
     * Sets the value of the invoiceDetailDiscount property.
     *
     * @param invoiceDetailDiscount allowed object is {@link InvoiceDetailDiscount }
     */
    public void setInvoiceDetailDiscount(final InvoiceDetailDiscount invoiceDetailDiscount) {
        this.invoiceDetailDiscount = invoiceDetailDiscount;
    }

    /**
     * Gets the value of the netAmount property.
     *
     * @return possible object is {@link NetAmount }
     */
    public NetAmount getNetAmount() {
        return netAmount;
    }

    /**
     * Sets the value of the netAmount property.
     *
     * @param netAmount allowed object is {@link NetAmount }
     */
    public void setNetAmount(final NetAmount netAmount) {
        this.netAmount = netAmount;
    }

    /**
     * Gets the value of the distribution property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the Jakarta XML Binding object. This is why there is not a
     * <CODE>set</CODE> method for the distribution property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDistribution().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Distribution }
     */
    public List<Distribution> getDistribution() {
        if (distribution == null) {
            distribution = new ArrayList<>();
        }
        return distribution;
    }

    /**
     * Gets the value of the comments property.
     *
     * @return possible object is {@link Comments }
     */
    public Comments getComments() {
        return comments;
    }

    /**
     * Sets the value of the comments property.
     *
     * @param comments allowed object is {@link Comments }
     */
    public void setComments(final Comments comments) {
        this.comments = comments;
    }

    /**
     * Gets the value of the extrinsic property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the Jakarta XML Binding object. This is why there is not a
     * <CODE>set</CODE> method for the extrinsic property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getExtrinsic().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link Extrinsic }
     */
    public List<Extrinsic> getExtrinsic() {
        if (extrinsic == null) {
            extrinsic = new ArrayList<>();
        }
        return extrinsic;
    }

    /**
     * Gets the value of the invoiceLineNumber property.
     */
    public short getInvoiceLineNumber() {
        return invoiceLineNumber;
    }

    /**
     * Sets the value of the invoiceLineNumber property.
     */
    public void setInvoiceLineNumber(final short invoiceLineNumber) {
        this.invoiceLineNumber = invoiceLineNumber;
    }

    /**
     * Gets the value of the quantity property.
     *
     * @return possible object is {@link BigDecimal }
     */
    public BigDecimal getQuantity() {
        return quantity;
    }

    /**
     * Sets the value of the quantity property.
     *
     * @param quantity allowed object is {@link BigDecimal }
     */
    public void setQuantity(final BigDecimal quantity) {
        this.quantity = quantity;
    }

    ElectronicInvoiceItem toElectronicInvoiceItem() {
        final ElectronicInvoiceItem item = new ElectronicInvoiceItem();

        item.setInvoiceLineNumber(String.valueOf(invoiceLineNumber));
        item.setQuantity(quantity.toPlainString());
        item.setUnitOfMeasure(unitOfMeasure);
        item.setUnitPrice(unitPrice.getMoney().getValue());
        item.setUnitPriceCurrency(unitPrice.getMoney().getCurrency());

        if (invoiceDetailDiscount != null) {
            item.setInvoiceLineDiscountAmount(invoiceDetailDiscount.getMoney().getValue());
            item.setInvoiceLineDiscountAmountCurrency(invoiceDetailDiscount.getMoney().getCurrency());
            item.setInvoiceLineDiscountPercentageRate(invoiceDetailDiscount.getPercentageRate().toPlainString());
        }

        if (invoiceDetailLineShipping != null) {
            item.setInvoiceLineShippingAmount(invoiceDetailLineShipping.getMoney().getValue());
            item.setInvoiceLineShippingAmountCurrency(invoiceDetailLineShipping.getMoney().getCurrency());

            if (invoiceDetailLineShipping.getInvoiceDetailShipping() != null) {
                item.setShippingDateString(invoiceDetailLineShipping.getInvoiceDetailShipping().getShippingDate());
                if (CollectionUtils.isNotEmpty(invoiceDetailLineShipping.getInvoiceDetailShipping().getContact())) {
                    item.setInvoiceShippingContacts(invoiceDetailLineShipping.getInvoiceDetailShipping()
                            .getContact()
                            .stream()
                            .map(Contact::toElectronicInvoiceContact)
                            .collect(Collectors.toList()));
                }
            }
        }

        if (grossAmount != null) {
            item.setInvoiceLineGrossAmount(grossAmount.getMoney().getValue());
            item.setInvoiceLineGrossAmountCurrency(grossAmount.getMoney().getCurrency());
        }

        if (netAmount != null) {
            item.setInvoiceLineNetAmount(netAmount.getMoney().getValue());
            item.setInvoiceLineNetAmountCurrency(netAmount.getMoney().getCurrency());
        }

        if (invoiceDetailLineSpecialHandling != null) {
            item.setInvoiceLineSpecialHandlingAmount(invoiceDetailLineSpecialHandling.getMoney().getValue());
            item.setInvoiceLineSpecialHandlingAmountCurrency(invoiceDetailLineSpecialHandling.getMoney().getCurrency());
        }

        if (subtotalAmount != null) {
            item.setSubTotalAmount(subtotalAmount.getMoney().getValue());
            item.setSubTotalAmountCurrency(subtotalAmount.getMoney().getCurrency());
        }

        if (tax != null) {
            item.setTaxAmount(tax.getMoney().getValue());
            item.setTaxAmountCurrency(tax.getMoney().getCurrency());
            item.setTaxDescription(tax.getDescription().getValue());
        }

        if (invoiceDetailItemReference != null) {
            if (invoiceDetailItemReference.getCountry() != null) {
                item.setReferenceCountryName(invoiceDetailItemReference.getCountry().getValue());
                item.setReferenceCountryCode(invoiceDetailItemReference.getCountry().getIsoCountryCode());
            }
            
            //CU Customization: Fix for missing Description tag in data submitted by eInvoice vendors, KFSPTS-31486/KFSPTS-31489
            if (invoiceDetailItemReference.getDescription() != null
                    && invoiceDetailItemReference.getDescription().getValue() != null) {
                item.setReferenceDescription(invoiceDetailItemReference.getDescription().getValue());
            }
            
            item.setReferenceItemIDSupplierPartAuxID(invoiceDetailItemReference.getItemID()
                    .getSupplierPartAuxiliaryID());
            item.setReferenceItemIDSupplierPartID(invoiceDetailItemReference.getItemID().getSupplierPartID());
            item.setReferenceLineNumber(String.valueOf(invoiceDetailItemReference.getLineNumber()));
            if (invoiceDetailItemReference.getManufacturerName() != null) {
                item.setReferenceManufacturerName(invoiceDetailItemReference.getManufacturerName().getValue());
            }
            item.setReferenceManufacturerPartID(invoiceDetailItemReference.getManufacturerPartID());
            item.setReferenceSerialNumber(invoiceDetailItemReference.getSerialNumber());
            item.setReferenceSerialNumbers(invoiceDetailItemReference.getSerialNumbers());
        }

        if (comments != null) {
            item.addComments(comments.getValue());
        }

        if (CollectionUtils.isNotEmpty(extrinsic)) {
            item.setExtrinsic(extrinsic.stream()
                    .map(ex -> new CxmlExtrinsic(ex.getName(), ex.getValue()))
                    .collect(Collectors.toList()));
        }

        return item;
    }

}
