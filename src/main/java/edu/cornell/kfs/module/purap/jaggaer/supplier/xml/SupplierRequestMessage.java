
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
    "supplier"
})
@XmlRootElement(name = "SupplierRequestMessage")
public class SupplierRequestMessage {

    @XmlElement(name = "Supplier", required = true)
    protected List<Supplier> supplier;

    /**
     * Gets the value of the supplier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the supplier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSupplier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Supplier }
     * 
     * 
     */
    public List<Supplier> getSupplier() {
        if (supplier == null) {
            supplier = new ArrayList<Supplier>();
        }
        return this.supplier;
    }

}
