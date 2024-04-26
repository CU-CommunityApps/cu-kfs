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
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;
import org.apache.commons.collections4.CollectionUtils;
import org.kuali.kfs.core.api.impex.xml.XmlConstants;
import org.kuali.kfs.module.purap.businessobject.ElectronicInvoiceDetailRequestHeader;
import org.kuali.kfs.module.purap.util.cxml.CxmlExtrinsic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

//CU Customization: Fix for missing Description tag in data submitted by eInvoice vendors, KFSPTS-31486/KFSPTS-31489
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
 *         &lt;element name="InvoiceDetailHeaderIndicator"&gt;
 *           &lt;complexType&gt;
 *             &lt;simpleContent&gt;
 *               &lt;extension base="&lt;http://www.kuali.org/kfs/purap/types&gt;emptyType"&gt;
 *                 &lt;attribute name="isHeaderInvoice"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;enumeration value="yes"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *                 &lt;attribute name="isVatRecoverable"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;enumeration value="yes"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *               &lt;/extension&gt;
 *             &lt;/simpleContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="InvoiceDetailLineIndicator"&gt;
 *           &lt;complexType&gt;
 *             &lt;simpleContent&gt;
 *               &lt;extension base="&lt;http://www.kuali.org/kfs/purap/types&gt;emptyType"&gt;
 *                 &lt;attribute name="isTaxInLine"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;enumeration value="yes"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *                 &lt;attribute name="isSpecialHandlingInLine"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;enumeration value="yes"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *                 &lt;attribute name="isShippingInLine"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;enumeration value="yes"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *                 &lt;attribute name="isDiscountInLine"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;enumeration value="yes"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *                 &lt;attribute name="isAccountingInLine"&gt;
 *                   &lt;simpleType&gt;
 *                     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                       &lt;enumeration value="yes"/&gt;
 *                     &lt;/restriction&gt;
 *                   &lt;/simpleType&gt;
 *                 &lt;/attribute&gt;
 *               &lt;/extension&gt;
 *             &lt;/simpleContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="InvoicePartner" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Contact" maxOccurs="unbounded"/&gt;
 *                   &lt;element name="IdReference" minOccurs="0"&gt;
 *                     &lt;complexType&gt;
 *                       &lt;complexContent&gt;
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                           &lt;sequence&gt;
 *                             &lt;element name="Creator" type="{http://www.kuali.org/kfs/sys/types}zeroToTwentyCharType" minOccurs="0"/&gt;
 *                             &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Description" minOccurs="0"/&gt;
 *                           &lt;/sequence&gt;
 *                           &lt;attribute name="domain" use="required" type="{http://www.kuali.org/kfs/sys/types}zeroToTwentyCharType" /&gt;
 *                           &lt;attribute name="identifier" use="required" type="{http://www.kuali.org/kfs/sys/types}zeroToTwentyCharType" /&gt;
 *                         &lt;/restriction&gt;
 *                       &lt;/complexContent&gt;
 *                     &lt;/complexType&gt;
 *                   &lt;/element&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}InvoiceDetailShipping" minOccurs="0"/&gt;
 *         &lt;choice maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;element name="InvoiceDetailPaymentTerm"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="payInNumberOfDays" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *                   &lt;attribute name="percentageRate" use="required" type="{http://www.w3.org/2001/XMLSchema}decimal" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *           &lt;element name="PaymentTerm"&gt;
 *             &lt;complexType&gt;
 *               &lt;complexContent&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                   &lt;attribute name="payInNumberOfDays" use="required" type="{http://www.w3.org/2001/XMLSchema}unsignedInt" /&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/complexContent&gt;
 *             &lt;/complexType&gt;
 *           &lt;/element&gt;
 *         &lt;/choice&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Comments" minOccurs="0"/&gt;
 *         &lt;element ref="{http://www.kuali.org/kfs/purap/electronicInvoice}Extrinsic" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="purpose" default="standard"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="standard"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="operation" default="new"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="new"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *       &lt;attribute name="invoiceDate" use="required" type="{http://www.kuali.org/kfs/purap/types}dateStringType" /&gt;
 *       &lt;attribute name="invoiceID" use="required" type="{http://www.kuali.org/kfs/purap/types}idType" /&gt;
 *       &lt;attribute name="isInformationOnly"&gt;
 *         &lt;simpleType&gt;
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *             &lt;enumeration value="yes"/&gt;
 *           &lt;/restriction&gt;
 *         &lt;/simpleType&gt;
 *       &lt;/attribute&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {"invoiceDetailHeaderIndicator", "invoiceDetailLineIndicator", "invoicePartner",
    "invoiceDetailShipping", "invoiceDetailPaymentTermOrPaymentTerm", "comments", "extrinsic"})
public class InvoiceDetailRequestHeader {

    @XmlElement(name = "InvoiceDetailHeaderIndicator", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE, required =
        true)
    protected InvoiceDetailHeaderIndicator invoiceDetailHeaderIndicator;
    @XmlElement(name = "InvoiceDetailLineIndicator", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE, required = true)
    protected InvoiceDetailLineIndicator invoiceDetailLineIndicator;
    @XmlElement(name = "InvoicePartner", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected List<InvoicePartner> invoicePartner;
    @XmlElement(name = "InvoiceDetailShipping", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected InvoiceDetailShipping invoiceDetailShipping;
    @XmlElements({
        @XmlElement(name = "InvoiceDetailPaymentTerm", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE, type =
            InvoiceDetailPaymentTerm.class),
        @XmlElement(name = "PaymentTerm", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE, type =
            PaymentTerm.class)})
    protected List<Object> invoiceDetailPaymentTermOrPaymentTerm;
    @XmlElement(name = "Comments", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected Comments comments;
    @XmlElement(name = "Extrinsic", namespace = XmlConstants.ELECTRONIC_INV_NAMESPACE)
    protected List<Extrinsic> extrinsic;
    @XmlAttribute(name = "purpose")
    protected String purpose;
    @XmlAttribute(name = "operation")
    protected String operation;
    @XmlAttribute(name = "invoiceDate", required = true)
    protected String invoiceDate;
    @XmlAttribute(name = "invoiceID", required = true)
    protected String invoiceID;
    @XmlAttribute(name = "isInformationOnly")
    protected String isInformationOnly;

    /**
     * Gets the value of the invoiceDetailHeaderIndicator property.
     *
     * @return possible object is {@link InvoiceDetailHeaderIndicator }
     */
    public InvoiceDetailHeaderIndicator getInvoiceDetailHeaderIndicator() {
        return invoiceDetailHeaderIndicator;
    }

    /**
     * Sets the value of the invoiceDetailHeaderIndicator property.
     *
     * @param invoiceDetailHeaderIndicator allowed object is {@link InvoiceDetailHeaderIndicator }
     */
    public void setInvoiceDetailHeaderIndicator(final InvoiceDetailHeaderIndicator invoiceDetailHeaderIndicator) {
        this.invoiceDetailHeaderIndicator = invoiceDetailHeaderIndicator;
    }

    /**
     * Gets the value of the invoiceDetailLineIndicator property.
     *
     * @return possible object is {@link InvoiceDetailLineIndicator }
     */
    public InvoiceDetailLineIndicator getInvoiceDetailLineIndicator() {
        return invoiceDetailLineIndicator;
    }

    /**
     * Sets the value of the invoiceDetailLineIndicator property.
     *
     * @param invoiceDetailLineIndicator allowed object is {@link InvoiceDetailLineIndicator }
     */
    public void setInvoiceDetailLineIndicator(final InvoiceDetailLineIndicator invoiceDetailLineIndicator) {
        this.invoiceDetailLineIndicator = invoiceDetailLineIndicator;
    }

    /**
     * Gets the value of the invoicePartner property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the Jakarta XML Binding object. This is why there is not a
     * <CODE>set</CODE> method for the invoicePartner property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvoicePartner().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link InvoicePartner }
     */
    public List<InvoicePartner> getInvoicePartner() {
        if (invoicePartner == null) {
            invoicePartner = new ArrayList<>();
        }
        return invoicePartner;
    }

    /**
     * Gets the value of the invoiceDetailShipping property.
     *
     * @return possible object is {@link InvoiceDetailShipping }
     */
    public InvoiceDetailShipping getInvoiceDetailShipping() {
        return invoiceDetailShipping;
    }

    /**
     * Sets the value of the invoiceDetailShipping property.
     *
     * @param invoiceDetailShipping allowed object is {@link InvoiceDetailShipping }
     */
    public void setInvoiceDetailShipping(final InvoiceDetailShipping invoiceDetailShipping) {
        this.invoiceDetailShipping = invoiceDetailShipping;
    }

    /**
     * Gets the value of the invoiceDetailPaymentTermOrPaymentTerm property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
     * the returned list will be present inside the Jakarta XML Binding object. This is why there is not a
     * <CODE>set</CODE> method for the invoiceDetailPaymentTermOrPaymentTerm property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInvoiceDetailPaymentTermOrPaymentTerm().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list {@link InvoiceDetailPaymentTerm } {@link PaymentTerm }
     */
    public List<Object> getInvoiceDetailPaymentTermOrPaymentTerm() {
        if (invoiceDetailPaymentTermOrPaymentTerm == null) {
            invoiceDetailPaymentTermOrPaymentTerm = new ArrayList<>();
        }
        return invoiceDetailPaymentTermOrPaymentTerm;
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
     * Gets the value of the purpose property.
     *
     * @return possible object is {@link String }
     */
    public String getPurpose() {
        if (purpose == null) {
            return "standard";
        } else {
            return purpose;
        }
    }

    /**
     * Sets the value of the purpose property.
     *
     * @param purpose allowed object is {@link String }
     */
    public void setPurpose(final String purpose) {
        this.purpose = purpose;
    }

    /**
     * Gets the value of the operation property.
     *
     * @return possible object is {@link String }
     */
    public String getOperation() {
        if (operation == null) {
            return "new";
        } else {
            return operation;
        }
    }

    /**
     * Sets the value of the operation property.
     *
     * @param operation allowed object is {@link String }
     */
    public void setOperation(final String operation) {
        this.operation = operation;
    }

    /**
     * Gets the value of the invoiceDate property.
     *
     * @return possible object is {@link String }
     */
    public String getInvoiceDate() {
        return invoiceDate;
    }

    /**
     * Sets the value of the invoiceDate property.
     *
     * @param invoiceDate allowed object is {@link String }
     */
    public void setInvoiceDate(final String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    /**
     * Gets the value of the invoiceID property.
     *
     * @return possible object is {@link String }
     */
    public String getInvoiceID() {
        return invoiceID;
    }

    /**
     * Sets the value of the invoiceID property.
     *
     * @param invoiceID allowed object is {@link String }
     */
    public void setInvoiceID(final String invoiceID) {
        this.invoiceID = invoiceID;
    }

    /**
     * Gets the value of the isInformationOnly property.
     *
     * @return possible object is {@link String }
     */
    public String getIsInformationOnly() {
        return isInformationOnly;
    }

    /**
     * Sets the value of the isInformationOnly property.
     *
     * @param isInformationOnly allowed object is {@link String }
     */
    public void setIsInformationOnly(final String isInformationOnly) {
        this.isInformationOnly = isInformationOnly;
    }

    public ElectronicInvoiceDetailRequestHeader toElectronicInvoiceDetailRequestHeader() {
        final ElectronicInvoiceDetailRequestHeader header = new ElectronicInvoiceDetailRequestHeader();
        header.setInvoiceDateString(invoiceDate);
        if (invoiceDetailShipping != null) {
            header.setShippingDateString(invoiceDetailShipping.getShippingDate());
            header.setInvoiceShippingContacts(invoiceDetailShipping.getContact()
                    .stream()
                    .map(Contact::toElectronicInvoiceContact)
                    .collect(Collectors.toList()));
        }
        if (CollectionUtils.isNotEmpty(extrinsic)) {
            header.setExtrinsics(extrinsic.stream()
                    .map(ex -> new CxmlExtrinsic(ex.getName(), ex.getValue()))
                    .collect(Collectors.toList()));
        }
        header.setInvoiceId(invoiceID);
        header.setDiscountInfoProvidedIndicator(invoiceDetailLineIndicator.getIsDiscountInLine());
        header.setHeaderInvoiceInd(invoiceDetailHeaderIndicator.getIsHeaderInvoice());
        header.setbuyerInformationOnlyIndicator(isInformationOnly);
        header.setShippingInfoProvidedIndicator(invoiceDetailLineIndicator.getIsShippingInLine());
        header.setSpecialHandlingInfoProvidedIndicator(invoiceDetailLineIndicator.getIsSpecialHandlingInLine());
        header.setTaxInfoProvidedIndicator(invoiceDetailLineIndicator.getIsTaxInLine());
        header.setOperation(operation);
        header.setPurpose(purpose);
        header.setAccountingInfoProvidedIndicator(invoiceDetailLineIndicator.getIsAccountingInLine());
        if (CollectionUtils.isNotEmpty(invoicePartner)) {
            header.setInvoicePartnerContacts(invoicePartner.stream()
                    .map(InvoicePartner::getContact)
                    .flatMap(Collection::stream)
                    .map(Contact::toElectronicInvoiceContact)
                    .collect(Collectors.toList()));
            // The xml digester just took the last item it found to fill these.
            final InvoicePartner lastPartner = invoicePartner.get(invoicePartner.size() - 1);
            if (lastPartner.getIdReference() != null) {
                header.setIdReferenceCreator(lastPartner.getIdReference().getCreator());
                
                //CU Customization: Fix for missing Description tag in data submitted by eInvoice vendors, KFSPTS-31486/KFSPTS-31489
                //header.setIdReferenceDescription(lastPartner.getIdReference().getDescription().getValue();
                if (lastPartner.getIdReference().getDescription() != null
                        && lastPartner.getIdReference().getDescription().getValue() != null) {
                    header.setIdReferenceDescription(lastPartner.getIdReference().getDescription().getValue());
                }
                
                header.setIdReferenceDomain(lastPartner.getIdReference().getDomain());
                header.setIdReferenceIdentifier(lastPartner.getIdReference().getIdentifier());
            }
        }
        if (CollectionUtils.isNotEmpty(invoiceDetailPaymentTermOrPaymentTerm)) {
            final Object lastTerm =
                    invoiceDetailPaymentTermOrPaymentTerm.get(invoiceDetailPaymentTermOrPaymentTerm.size() - 1);
            if (lastTerm instanceof InvoiceDetailPaymentTerm) {
                header.setPayInNumberOfDays((int) ((InvoiceDetailPaymentTerm) lastTerm).getPayInNumberOfDays());
                header.setPercentageRate(((InvoiceDetailPaymentTerm) lastTerm).getPercentageRate().toPlainString());
            } else if (lastTerm instanceof PaymentTerm) {
                header.setPayInNumberOfDays((int) ((PaymentTerm) lastTerm).getPayInNumberOfDays());
            }
        }
        return header;
    }

}
