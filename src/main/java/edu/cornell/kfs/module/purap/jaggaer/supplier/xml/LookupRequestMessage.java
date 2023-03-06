
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "resultCount",
    "startResult",
    "supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber",
    "relationship",
    "includeDeleted",
    "internalERPName"
})
@XmlRootElement(name = "LookupRequestMessage")
public class LookupRequestMessage {

    @XmlElement(name = "ResultCount")
    protected String resultCount;
    @XmlElement(name = "StartResult")
    protected String startResult;
    @XmlElements({
        @XmlElement(name = "SupplierSQId", required = true, type = SupplierSQId.class),
        @XmlElement(name = "SupplierNumber", required = true, type = SupplierNumber.class),
        @XmlElement(name = "TaxIdentificationNumber", required = true, type = TaxIdentificationNumber.class),
        @XmlElement(name = "DUNS", required = true, type = DUNS.class),
        @XmlElement(name = "ThirdPartyRefNumber", required = true, type = ThirdPartyRefNumber.class)
    })
    protected List<Object> supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber;
    @XmlElement(name = "Relationship")
    protected String relationship;
    @XmlElement(name = "IncludeDeleted")
    protected String includeDeleted;
    @XmlElement(name = "InternalERPName")
    protected String internalERPName;

    /**
     * Gets the value of the resultCount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResultCount() {
        return resultCount;
    }

    /**
     * Sets the value of the resultCount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResultCount(String value) {
        this.resultCount = value;
    }

    /**
     * Gets the value of the startResult property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartResult() {
        return startResult;
    }

    /**
     * Sets the value of the startResult property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartResult(String value) {
        this.startResult = value;
    }

    /**
     * Gets the value of the supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SupplierSQId }
     * {@link SupplierNumber }
     * {@link TaxIdentificationNumber }
     * {@link DUNS }
     * {@link ThirdPartyRefNumber }
     * 
     * 
     */
    public List<Object> getSupplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber() {
        if (supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber == null) {
            supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber = new ArrayList<Object>();
        }
        return this.supplierSQIdOrSupplierNumberOrTaxIdentificationNumberOrDUNSOrThirdPartyRefNumber;
    }

    /**
     * Gets the value of the relationship property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRelationship() {
        return relationship;
    }

    /**
     * Sets the value of the relationship property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRelationship(String value) {
        this.relationship = value;
    }

    /**
     * Gets the value of the includeDeleted property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIncludeDeleted() {
        return includeDeleted;
    }

    /**
     * Sets the value of the includeDeleted property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIncludeDeleted(String value) {
        this.includeDeleted = value;
    }

    /**
     * Gets the value of the internalERPName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInternalERPName() {
        return internalERPName;
    }

    /**
     * Sets the value of the internalERPName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInternalERPName(String value) {
        this.internalERPName = value;
    }

}
