package edu.cornell.kfs.vnd.businessobject.options;



import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.service.KeyValuesService;

import edu.cornell.kfs.vnd.businessobject.CuVendorChapter4Status;

	/**
	 * Values Finder for <code>CuVendorChapter4Status</code>.
	 * 
	 * @see edu.cornell.kfs.vnd.businessobject.CuVendorChapter4Status
	 */

	public class CuVendorChapter4StatusValuesFinder  extends KeyValuesBase {

	    /**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		/*
	     * @see org.kuali.keyvalues.KeyValuesFinder#getKeyValues()
	     */
	    public List<KeyValue> getKeyValues() {
	        KeyValuesService boService = SpringContext.getBean(KeyValuesService.class);
	        Collection<CuVendorChapter4Status> codes = boService.findAll(CuVendorChapter4Status.class);
	        List<KeyValue> tempLabels = new ArrayList<KeyValue>();
	        tempLabels.add(new ConcreteKeyValue("", ""));
	        for (CuVendorChapter4Status chapter4Status : codes) {
	            if (chapter4Status.isActive()) {
	                tempLabels.add(new ConcreteKeyValue(chapter4Status.getVendorChapter4StatusCode(), chapter4Status.getVendorChapter4StatusCode() + " - " + chapter4Status.getVendorChapter4StatusDescription()));

	            }
	        }

	        return tempLabels;
	    }
	}
