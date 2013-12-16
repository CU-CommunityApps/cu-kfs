package edu.cornell.kfs.module.ld.document.struts;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.kuali.kfs.sys.web.struts.KualiBatchInputFileSetForm;

public class DisencumbranceKualiBatchInputFileSetForm extends KualiBatchInputFileSetForm {
    protected String selectedDataFile;
    protected String selectedReconFile;
    
    public DisencumbranceKualiBatchInputFileSetForm() {
        super();
    }

    public String getSelectedDataFile() {
        return selectedDataFile;
    }

    public void setSelectedDataFile(String selectedDataFile) {
        this.selectedDataFile = selectedDataFile;
    }

    public String getSelectedReconFile() {
        return selectedReconFile;
    }

    public void setSelectedReconFile(String selectedReconFile) {
        this.selectedReconFile = selectedReconFile;
    }

}
