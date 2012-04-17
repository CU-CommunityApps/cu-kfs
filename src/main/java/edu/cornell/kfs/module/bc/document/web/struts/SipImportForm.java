package edu.cornell.kfs.module.bc.document.web.struts;

import org.apache.struts.upload.FormFile;
import org.kuali.kfs.module.bc.document.web.struts.BudgetExpansionForm;

public class SipImportForm extends BudgetExpansionForm {
    
    private String fileName;
    private int importCount;
    private FormFile file;
    private boolean allowExecutivesToBeImported;
    
    public FormFile getFile() {
        return file;
    }

    public void setFile(FormFile file) {
        this.file = file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getImportCount() {
        return importCount;
    }

    public void setImportCount(int importCount) {
        this.importCount = importCount;
    }

	/**
	 * @return the allowExecutivesToBeImported
	 */
	public boolean isAllowExecutivesToBeImported() {
		return allowExecutivesToBeImported;
	}

	/**
	 * @param allowExecutivesToBeImported the allowExecutivesToBeImported to set
	 */
	public void setAllowExecutivesToBeImported(boolean allowExecutivesToBeImported) {
		this.allowExecutivesToBeImported = allowExecutivesToBeImported;
	}
}
