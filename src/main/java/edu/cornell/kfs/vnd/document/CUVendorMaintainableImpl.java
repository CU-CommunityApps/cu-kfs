package edu.cornell.kfs.vnd.document;

import java.util.Map;

import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.businessobject.VendorHeader;
import org.kuali.kfs.vnd.document.VendorMaintainableImpl;
import org.kuali.rice.kns.document.MaintenanceDocument;

import edu.cornell.kfs.vnd.businessobject.VendorHeaderExtendedAttribute;
import edu.cornell.kfs.vnd.document.validation.impl.VendorHeaderExtensionRule;

public class CUVendorMaintainableImpl extends VendorMaintainableImpl {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3918763793245418356L;
	
	
	protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CUVendorMaintainableImpl.class);
	
	@Override
	public void saveBusinessObject() {
		//super.saveBusinessObject();
		LOG.info("!!!! saveBusinessObject called !!!!");
		VendorDetail vendorDetail = (VendorDetail) super.getBusinessObject();
		VendorHeader vendorHeader = vendorDetail.getVendorHeader();
		VendorHeaderExtendedAttribute vhea = (VendorHeaderExtendedAttribute) vendorHeader.getExtension();
		
//		vhea.setVendorHeaderGeneratedIdentifier(vendorHeader.getVendorHeaderGeneratedIdentifier());
//		vhea.setVersionNumber(vendorHeader.getVersionNumber());
//		vhea.setObjectId(vendorHeader.getObjectId());

		vhea.setVendorHeaderGeneratedIdentifier(vendorDetail.getVendorHeaderGeneratedIdentifier());
		vhea.setVersionNumber(vendorDetail.getVersionNumber());
		vhea.setObjectId(vendorDetail.getObjectId());
		
		super.saveBusinessObject();
	}

    @Override	
	public void refreshBusinessObject() {
    	LOG.error("!!!! refreshBusinessObject called !!!!");
		VendorDetail vd = (VendorDetail) getBusinessObject();
		VendorHeader tempHeader = vd.getVendorHeader();
		VendorHeaderExtendedAttribute tempExtendedAttribute = (VendorHeaderExtendedAttribute)tempHeader.getExtension();
		vd.refreshNonUpdateableReferences();
		tempHeader.setExtension(tempExtendedAttribute);
		vd.setVendorHeader(tempHeader);
		super.refreshBusinessObject();
	}
    
    @Override
    public void refresh(String refreshCaller, Map fieldValues, MaintenanceDocument document)
    {
    	LOG.error("!!!! refresh called !!!!");
    	super.refresh(refreshCaller, fieldValues, document);
    }
}
