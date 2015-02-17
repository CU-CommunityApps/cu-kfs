package edu.cornell.kfs.vnd.businessobject;

import java.sql.Date;
import java.util.HashMap;

import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.bo.PersistableBusinessObjectExtensionBase;
import org.kuali.rice.krad.service.BusinessObjectService;


public class CuVendorHeaderExtension extends PersistableBusinessObjectExtensionBase {

	private static final long serialVersionUID = 1L;
	private Integer vendorHeaderGeneratedIdentifier;
    private Date vendorW9ReceivedDate;
    private String vendorChapter4StatusCode;
    private Date vendorW8BenReceivedDate; 
    private String vendorForeignTaxNumber; 
    private String vendorGIIN;
    private Date vendorForeignRecipientBirthDate;
    private CuVendorChapter4Status vendorChapter4Status;
    private String vendorLocale;
   
    public Date getVendorW9ReceivedDate() {
        return vendorW9ReceivedDate;
    }

    public void setVendorW9ReceivedDate(Date vendorW9ReceivedDate) {
        this.vendorW9ReceivedDate = vendorW9ReceivedDate;
    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(
            Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

	public String getVendorChapter4StatusCode() {
		return vendorChapter4StatusCode;
	}

	public void setVendorChapter4StatusCode(String vendorChapter4StatusCode) {
		this.vendorChapter4StatusCode = vendorChapter4StatusCode;
	    BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
	    HashMap<String,String> keys = new HashMap<String,String>();
	 	keys.put("vendorChapter4StatusCode", vendorChapter4StatusCode);
	 	vendorChapter4Status = (CuVendorChapter4Status) bos.findByPrimaryKey(CuVendorChapter4Status.class, keys);
	}

	public CuVendorChapter4Status getVendorChapter4Status() {
		return vendorChapter4Status;
	}

	public void setVendorChapter4Status(CuVendorChapter4Status vendorChapter4Status) {
		this.vendorChapter4Status = vendorChapter4Status;
	}

	public Date getVendorW8BenReceivedDate() {
		return vendorW8BenReceivedDate;
	}

	public void setVendorW8BenReceivedDate(Date vendorW8BenReceivedDate) {
		this.vendorW8BenReceivedDate = vendorW8BenReceivedDate;
	}

	public String getVendorForeignTaxNumber() {
		return vendorForeignTaxNumber;
	}

	public void setVendorForeignTaxNumber(
			String vendorForeignTaxNumber) {
		this.vendorForeignTaxNumber = vendorForeignTaxNumber;
	}

	public String getVendorGIIN() {
		return vendorGIIN;
	}

	public void setVendorGIIN(String vendorGIIN) {
		this.vendorGIIN = vendorGIIN;
	}

	public Date getVendorForeignRecipientBirthDate() {
		return vendorForeignRecipientBirthDate;
	}

	public void setVendorForeignRecipientBirthDate(
			Date vendorForeignRecipientBirthDate) {
		this.vendorForeignRecipientBirthDate = vendorForeignRecipientBirthDate;
	}

	public String getVendorLocale() {
		return vendorLocale;
	}

	public void setVendorLocale(String vendorLocale) {
		this.vendorLocale = vendorLocale;
	}

}
