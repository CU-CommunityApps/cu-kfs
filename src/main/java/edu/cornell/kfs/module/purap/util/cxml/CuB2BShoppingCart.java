package edu.cornell.kfs.module.purap.util.cxml;

import org.kuali.kfs.module.purap.util.cxml.B2BShoppingCart;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import jakarta.xml.bind.annotation.XmlType;

import org.kuali.kfs.core.api.impex.xml.XmlConstants;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "cXML", namespace = XmlConstants.B2B_PUNCH_OUT_ORDER_NAMESPACE)
public class CuB2BShoppingCart extends B2BShoppingCart {

    @XmlTransient
	private String businessPurpose;

	/**
	 * Gets the businessPurpose.
	 * 
	 * @return businessPurpose
	 */
	public String getBusinessPurpose() {
		return businessPurpose;
	}

	/**
	 * Sets the businessPurpose.
	 * 
	 * @param businessPurpose
	 */
	public void setBusinessPurpose(String businessPurpose) {
		this.businessPurpose = businessPurpose;
	}

}
