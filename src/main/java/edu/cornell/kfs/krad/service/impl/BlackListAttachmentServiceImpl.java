package edu.cornell.kfs.krad.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.service.impl.KfsParameterConstants;

import edu.cornell.kfs.krad.service.BlackListAttachmentService;
import edu.cornell.kfs.sys.CUKFSParameterKeyConstants;

public class BlackListAttachmentServiceImpl implements BlackListAttachmentService {
    private static final Logger LOG = LogManager.getLogger();
    
    private static final String FILE_EXTENSION_DELIMITER = ".";
    
    private ParameterService parameterService;
    
    public boolean attachmentFileExtensionIsDisallowed(String uploadedFileName) {
        String fileExtension = obtainFileExtension(uploadedFileName);
        
        if (StringUtils.isNotBlank(fileExtension)) {
            List<String> disallowedAttachmentFileExtensions = getDisallowedFileExtensions();
            return disallowedAttachmentFileExtensions.contains(fileExtension);
        }
        return false;
    }
    
    private String obtainFileExtension(String uploadedFileName) {
        String fileExtension = KFSConstants.EMPTY_STRING;
        
        if (StringUtils.isNotBlank(uploadedFileName)) {
            int lastIndexOfFileDelimiter = uploadedFileName.lastIndexOf(FILE_EXTENSION_DELIMITER);
            
            if (lastIndexOfFileDelimiter != -1) {
                fileExtension = uploadedFileName.substring((lastIndexOfFileDelimiter + 1), uploadedFileName.length());
            }
        }
        return fileExtension;
    }
    
    private ArrayList<String> getDisallowedFileExtensions() {
        ArrayList<String> disallowedAttachmentFileExtensions = new ArrayList<>();
        if (getParameterService().parameterExists(
                KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                CUKFSParameterKeyConstants.SysParameterConstants.DISALLOWED_ATTACHMENT_FILE_EXTENSIONS)) {
            
            disallowedAttachmentFileExtensions = new ArrayList<String>(getParameterService().getParameterValuesAsString(
                    KfsParameterConstants.FINANCIAL_SYSTEM_ALL.class,
                    CUKFSParameterKeyConstants.SysParameterConstants.DISALLOWED_ATTACHMENT_FILE_EXTENSIONS));
        } else {
            LOG.error("disallowFileExtensionOnAttachment: Financials Parameter does not exist "
                    + "Namespace={} Component={} ParameterName={}", 
                    KfsParameterConstants.FINANCIAL_PROCESSING_NAMESPACE, KfsParameterConstants.ALL_COMPONENT,
                    CUKFSParameterKeyConstants.SysParameterConstants.DISALLOWED_ATTACHMENT_FILE_EXTENSIONS);
        }
        return disallowedAttachmentFileExtensions;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }
}
