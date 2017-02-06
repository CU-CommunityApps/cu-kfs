package edu.cornell.kfs.module.ld.businessobject;
import java.sql.Date;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtensionBase;


public class PositionDataExtentedAttribute extends PersistableBusinessObjectExtensionBase {
	private static final long serialVersionUID = 1L;
	
	  private String positionNumber;
	  private String orgCode;
	  private Date effectiveDate;
	  
	  public PositionDataExtentedAttribute(){
		 
	  }

	  

	public String getPositionNumber() {
		return positionNumber;
	}

	public void setPositionNumber(String positionNumber) {
		this.positionNumber = positionNumber;
	}

	public String getOrgCode() {
		return orgCode;
	}

	public void setOrgCode(String orgCode) {
		this.orgCode = orgCode;
	}

	public Date getEffectiveDate() {
		return effectiveDate;
	}

	public void setEffectiveDate(Date effectiveDate) {
		this.effectiveDate = effectiveDate;
	}

	  


	  
	  
}