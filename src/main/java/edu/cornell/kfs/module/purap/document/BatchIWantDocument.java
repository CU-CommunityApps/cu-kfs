package edu.cornell.kfs.module.purap.document;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.core.web.format.BooleanFormatter;
import org.kuali.kfs.krad.bo.Attachment;

import edu.cornell.kfs.module.purap.businessobject.BatchIWantNote;

public class BatchIWantDocument extends IWantDocument {
	
	protected String initiator;
	protected String sourceNumber;
	protected String businessPurpose;
	
	public BatchIWantDocument() {
	    super();
	    
    }
	
	public String getInitiator() {
		return initiator;
	}

	public String getSourceNumber() {
		return sourceNumber;
	}

	public void setSourceNumber(String sourceNumber) {
		this.sourceNumber = sourceNumber;
	}
	
	public void setInitiator(String initiator) {
		this.initiator = initiator;
	}


	public String getBusinessPurpose() {
		return businessPurpose;
	}

	public void setBusinessPurpose(String businessPurpose) {
		this.businessPurpose = businessPurpose;
	}
	
	public void setGoods(String goods) {
        if (StringUtils.isNotBlank(goods)) {
            Boolean goodsB = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(goods);
            super.setGoods(goodsB);
        }
	}
	
	public void setSameAsInitiator(String sameAsInitiator) {
	    if (StringUtils.isNotBlank(sameAsInitiator)) {
            Boolean sameAsInitiatorB = (Boolean) (new BooleanFormatter()).convertFromPresentationFormat(sameAsInitiator);
	    super.setSameAsInitiator(sameAsInitiatorB);
	    }
	}

}
