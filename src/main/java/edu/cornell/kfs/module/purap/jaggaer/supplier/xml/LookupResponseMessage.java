
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.NormalizedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "lookupStatus",
    "organizationId",
    "supplierOrSupplierList"
})
@XmlRootElement(name = "LookupResponseMessage")
public class LookupResponseMessage {

    @XmlAttribute(name = "totalResults", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String totalResults;
    @XmlAttribute(name = "startResult", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String startResult;
    @XmlAttribute(name = "resultSize", required = true)
    @XmlJavaTypeAdapter(NormalizedStringAdapter.class)
    protected String resultSize;
    @XmlElement(name = "LookupStatus", required = true)
    protected LookupStatus lookupStatus;
    @XmlElement(name = "OrganizationId")
    protected String organizationId;
    @XmlElements({
        @XmlElement(name = "Supplier", type = Supplier.class),
        @XmlElement(name = "SupplierList", type = SupplierList.class)
    })
    protected List<Object> supplierOrSupplierList;

    /**
     * Gets the value of the totalResults property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTotalResults() {
        return totalResults;
    }

    /**
     * Sets the value of the totalResults property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTotalResults(String value) {
        this.totalResults = value;
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
     * Gets the value of the resultSize property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getResultSize() {
        return resultSize;
    }

    /**
     * Sets the value of the resultSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setResultSize(String value) {
        this.resultSize = value;
    }

    /**
     * Gets the value of the lookupStatus property.
     * 
     * @return
     *     possible object is
     *     {@link LookupStatus }
     *     
     */
    public LookupStatus getLookupStatus() {
        return lookupStatus;
    }

    /**
     * Sets the value of the lookupStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link LookupStatus }
     *     
     */
    public void setLookupStatus(LookupStatus value) {
        this.lookupStatus = value;
    }

    /**
     * Gets the value of the organizationId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOrganizationId() {
        return organizationId;
    }

    /**
     * Sets the value of the organizationId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOrganizationId(String value) {
        this.organizationId = value;
    }

    /**
     * Gets the value of the supplierOrSupplierList property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supplierOrSupplierList property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupplierOrSupplierList().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Supplier }
     * {@link SupplierList }
     * 
     * 
     */
    public List<Object> getSupplierOrSupplierList() {
        if (supplierOrSupplierList == null) {
            supplierOrSupplierList = new ArrayList<Object>();
        }
        return this.supplierOrSupplierList;
    }

}
