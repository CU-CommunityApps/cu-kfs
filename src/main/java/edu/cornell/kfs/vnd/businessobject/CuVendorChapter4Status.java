package edu.cornell.kfs.vnd.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.core.api.mo.common.active.MutableInactivatable;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;


	public class CuVendorChapter4Status extends PersistableBusinessObjectBase implements MutableInactivatable {

		private static final long serialVersionUID = 1L;
		
		private String vendorChapter4StatusCode;
	    private String vendorChapter4StatusDescription;
	    private boolean active;

	    
	    /**
		 * @return the vendorChapter4StatusCode
		 */
		public String getVendorChapter4StatusCode() {
			return vendorChapter4StatusCode;
		}
		/**
		 * @param vendorChapter4StatusCode the vendorChapter4StatusCode to set
		 */
		public void setVendorChapter4StatusCode(String vendorChapter4StatusCode) {
			this.vendorChapter4StatusCode = vendorChapter4StatusCode;
		}
		/**
		 * @return the vendorChapter4StatusDescription
		 */
		public String getVendorChapter4StatusDescription() {
			return vendorChapter4StatusDescription;
		}
		/**
		 * @param vendorChapter4StatusCodeDescription the vendorChapter4StatusCodeDescription to set
		 */
		public void setVendorChapter4StatusDescription(String vendorChapter4StatusDescription) {
			this.vendorChapter4StatusDescription = vendorChapter4StatusDescription;
		}
		/**
		 * @return the active
		 */
		public boolean isActive() {
			return active;
		}
		/**
		 * @param active the active to set
		 */
		public void setActive(boolean active) {
			this.active = active;
		}
	    

		/**
	     * @see org.kuali.rice.kns.bo.BusinessObjectBase#toStringMapper()
	     */
	    protected LinkedHashMap toStringMapper() {
	        LinkedHashMap m = new LinkedHashMap();
	        m.put("vendorChapter4StatusCode", this.vendorChapter4StatusCode);
	        return m;
	    }


	
}
