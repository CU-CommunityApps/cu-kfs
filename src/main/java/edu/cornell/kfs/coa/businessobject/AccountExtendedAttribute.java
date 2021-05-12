/*
 * Copyright 2010 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.coa.businessobject;

import java.sql.Date;
import java.util.HashMap;

import org.kuali.kfs.krad.bo.PersistableBusinessObjectBase;
import org.kuali.kfs.krad.bo.PersistableBusinessObjectExtension;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.cg.businessobject.InvoiceFrequency;
import edu.cornell.kfs.module.cg.businessobject.InvoiceType;

public class AccountExtendedAttribute extends PersistableBusinessObjectBase
        implements PersistableBusinessObjectExtension {

    /**
	 * 
	 */
	private static final long serialVersionUID = -5894118108397111352L;
	
	private String chartOfAccountsCode;
    private String accountNumber;
    private String programCode;
    private String appropriationAccountNumber;
    private String subFundGroupCode;
    private String invoiceFrequencyCode;
    private String invoiceTypeCode;
    private boolean everify;
    private String majorReportingCategoryCode;
    private Long costShareForProjectNumber;
    protected Date accountClosedDate;

    private SubFundProgram subFundProgram;
    private AppropriationAccount appropriationAccount;
    private InvoiceFrequency invoiceFrequency;
    private InvoiceType invoiceType;
    private MajorReportingCategory majorReportingCategory;

    
    public AccountExtendedAttribute() {
        
    }
    
    /**
     * Gets the chartOfAccountsCode attribute. 
     * @return Returns the chartOfAccountsCode.
     */
    public String getChartOfAccountsCode() {
        return chartOfAccountsCode;
    }
    /**
     * Sets the chartOfAccountsCode attribute value.
     * @param chartOfAccountsCode The chartOfAccountsCode to set.
     */
    public void setChartOfAccountsCode(String chartOfAccountsCode) {
        this.chartOfAccountsCode = chartOfAccountsCode;
    }
    /**
     * Gets the accountNumber attribute. 
     * @return Returns the accountNumber.
     */
    public String getAccountNumber() {
        return accountNumber;
    }
    /**
     * Sets the accountNumber attribute value.
     * @param accountNumber The accountNumber to set.
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }


	/**
	 * @return the subFundProgram
	 */
	public SubFundProgram getSubFundProgram() {
		return subFundProgram;
	}


	/**
	 * @param subFundProgram the subFundProgram to set
	 */
	public void setSubFundProgram(SubFundProgram subFundProgram) {
		this.subFundProgram = subFundProgram;
	}


	/**
	 * @return the appropriationAccount
	 */
	public AppropriationAccount getAppropriationAccount() {
		return appropriationAccount;
	}


	/**
	 * @param appropriationAccount the appropriationAccount to set
	 */
	public void setAppropriationAccount(AppropriationAccount appropriationAccount) {
		this.appropriationAccount = appropriationAccount;
	}

	/**
	 * @return the invoiceFrequency
	 */
	public InvoiceFrequency getInvoiceFrequency() {
		return invoiceFrequency;
	}

	/**
	 * @param invoiceFrequency the invoiceFrequency to set
	 */
	public void setInvoiceFrequency(InvoiceFrequency invoiceFrequency) {
		this.invoiceFrequency = invoiceFrequency;
	}

	/**
	 * @return the invoiceType
	 */
	public InvoiceType getInvoiceType() {
		return invoiceType;
	}

	/**
	 * @param invoiceType the invoiceType to set
	 */
	public void setInvoiceType(InvoiceType invoiceType) {
		this.invoiceType = invoiceType;
	}

	/**
	 * @return the programCode
	 */
	public String getProgramCode() {
		return programCode;
	}


	/**
	 * @param programCode the programCode to set
	 */
	public void setProgramCode(String programCode) {
		this.programCode = programCode;
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		HashMap<String,String> keys = new HashMap<String,String>();
		keys.put("programCode", programCode);
		keys.put("subFundGroupCode", subFundGroupCode);
		subFundProgram = (SubFundProgram) bos.findByPrimaryKey(SubFundProgram.class, keys );
//		subFundProgram.setProgramCode(programCode);
	}


	/**
	 * @return the appropriationAccountNumber
	 */
	public String getAppropriationAccountNumber() {
		return appropriationAccountNumber;
	}


	/**
	 * @param appropriationAccountNumber the appropriationAccountNumber to set
	 */
	public void setAppropriationAccountNumber(String appropriationAccountNumber) {
		this.appropriationAccountNumber = appropriationAccountNumber;
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		HashMap<String,String> keys = new HashMap<String,String>();
		keys.put("appropriationAccountNumber", appropriationAccountNumber);
		keys.put("subFundGroupCode", subFundGroupCode);
		appropriationAccount = (AppropriationAccount) bos.findByPrimaryKey(AppropriationAccount.class, keys );
//		appropriationAccount.setAppropriationAccountNumber(appropriationAccountNumber);
	}
	/**
	 * @return the subFundGroupCode
	 */
	public String getSubFundGroupCode() {
	
		return subFundGroupCode;
	}
	/**
	 * @param subFundGroupCode the subFundGroupCode to set
	 */
	public void setSubFundGroupCode(String subFundGroupCode) {
		this.subFundGroupCode = subFundGroupCode;
		
	}

	/**
	 * @return the invoiceFrequencyCode
	 */
	public String getInvoiceFrequencyCode() {
		return invoiceFrequencyCode;
	}

	/**
	 * @param invoiceFrequencyCode the invoiceFrequencyCode to set
	 */
	public void setInvoiceFrequencyCode(String invoiceFrequencyCode) {
		this.invoiceFrequencyCode = invoiceFrequencyCode;
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		HashMap<String,String> keys = new HashMap<String,String> ();
		keys.put("invoiceFrequencyCode", invoiceFrequencyCode);
		invoiceFrequency = (InvoiceFrequency) bos.findByPrimaryKey(InvoiceFrequency.class, keys);
//		invoiceFrequency.setInvoiceFrequencyCode(invoiceFrequencyCode);
	}

	/**
	 * @return the invoiceTypeCode
	 */
	public String getInvoiceTypeCode() {
		return invoiceTypeCode;
	}

	/**
	 * @param invoiceTypeCode the invoiceTypeCode to set
	 */
	public void setInvoiceTypeCode(String invoiceTypeCode) {
		this.invoiceTypeCode = invoiceTypeCode;
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		HashMap<String,String> keys = new HashMap<String,String> ();
		keys.put("invoiceTypeCode", invoiceTypeCode);
		invoiceType = (InvoiceType) bos.findByPrimaryKey(InvoiceType.class, keys);
//		invoiceType.setInvoiceTypeCode(invoiceTypeCode);
	}

	/**
	 * @return the everify
	 */
	public boolean isEverify() {
		return everify;
	}

	/**
	 * @param everify the everify to set
	 */
	public void setEverify(boolean everify) {
		this.everify = everify;
	}
	
	/**
	 * @return the majorReportingCategory
	 */
	public MajorReportingCategory getMajorReportingCategory() {
		return majorReportingCategory;
	}


	/**
	 * @param majorReportingCategory the majorReportingCategory to set
	 */
	public void setMajorReportingCategory(MajorReportingCategory majorReportingCategory) {
		this.majorReportingCategory = majorReportingCategory;
	}
    
    
	/**
	 * @return the majorReportingCategoryCode
	 */
	public String getMajorReportingCategoryCode() {
		return majorReportingCategoryCode;
	}

    /**
     * Gets the costShareForProjectNumber.
     * 
     * @return costShareForProjectNumber
     */
    public Long getCostShareForProjectNumber() {
        return costShareForProjectNumber;
    }

    /**
     * Sets the costShareForProjectNumber.
     * 
     * @param costShareForProjectNumber
     */
    public void setCostShareForProjectNumber(Long costShareForProjectNumber) {
        this.costShareForProjectNumber = costShareForProjectNumber;
    }

	/**
	 * @param majorReportingCategoryCode the majorReportingCategoryCode to set
	 */
	public void setMajorReportingCategoryCode(String majorReportingCategoryCode) {
		this.majorReportingCategoryCode = majorReportingCategoryCode;
		BusinessObjectService bos = SpringContext.getBean(BusinessObjectService.class);
		HashMap<String,String> keys = new HashMap<String,String>();
		keys.put("majorReportingCategoryCode", majorReportingCategoryCode);
		majorReportingCategory = (MajorReportingCategory) bos.findByPrimaryKey(MajorReportingCategory.class, keys);
	}

    public Date getAccountClosedDate() {
        return accountClosedDate;
    }

    public void setAccountClosedDate(Date accountClosedDate) {
        this.accountClosedDate = accountClosedDate;
    }

}
