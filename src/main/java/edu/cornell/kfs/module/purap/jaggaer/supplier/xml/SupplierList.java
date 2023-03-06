
package edu.cornell.kfs.module.purap.jaggaer.supplier.xml;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "supplierSQId"
})
@XmlRootElement(name = "SupplierList")
public class SupplierList {

    @XmlElement(name = "SupplierSQId", required = true)
    protected List<SupplierSQId> supplierSQId;

    /**
     * Gets the value of the supplierSQId property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supplierSQId property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupplierSQId().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SupplierSQId }
     * 
     * 
     */
    public List<SupplierSQId> getSupplierSQId() {
        if (supplierSQId == null) {
            supplierSQId = new ArrayList<SupplierSQId>();
        }
        return this.supplierSQId;
    }

}
