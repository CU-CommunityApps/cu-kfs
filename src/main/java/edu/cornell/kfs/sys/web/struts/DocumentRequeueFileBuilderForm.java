/**
 * 
 */
package edu.cornell.kfs.sys.web.struts;

import org.apache.struts.upload.FormFile;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.kns.web.struts.form.KualiForm;

/**
 * Struts Action Form for Document Requeuer file generation process.
 */
public class DocumentRequeueFileBuilderForm extends KualiForm {

	protected String generateFile;

    public String getGenerateFile() {
        return generateFile;
    }

    public void setGenerateFile(String generateFile) {
        this.generateFile = generateFile;
    }

    /**
     * Constructs a DocumentRequeueFileBuilderForm instance and sets up the appropriately casted document.
     */
    public DocumentRequeueFileBuilderForm() {
        super();
    }
    
}
