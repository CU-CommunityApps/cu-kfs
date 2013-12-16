package edu.cornell.kfs.module.ld.document.struts;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.batch.BatchInputFileSetType;
import org.kuali.kfs.sys.batch.service.BatchInputFileSetService;
import org.kuali.kfs.sys.businessobject.BatchUpload;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.exception.FileStorageException;
import org.kuali.kfs.sys.web.struts.KualiBatchInputFileSetAction;
import org.kuali.kfs.sys.web.struts.KualiBatchInputFileSetForm;
import org.kuali.rice.kns.exception.ValidationException;
import org.kuali.rice.kns.util.GlobalVariables;

import edu.cornell.kfs.module.ld.service.LaborLedgerEnterpriseFeedService;
import edu.cornell.kfs.sys.CUKFSConstants;

public class DisencumbranceKualiBatchInputFileSetAction extends KualiBatchInputFileSetAction {

    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(DisencumbranceKualiBatchInputFileSetAction.class);

    /**
     * Override method to create a disencumbrance file for upload
     * 
     * @see org.kuali.kfs.sys.web.struts.KualiBatchInputFileSetAction#save(org.apache.struts.action.ActionMapping,
     *      org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward save(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        DisencumbranceKualiBatchInputFileSetForm batchInputFileSetForm = (DisencumbranceKualiBatchInputFileSetForm) form;
        BatchUpload batchUpload = ((KualiBatchInputFileSetForm) form).getBatchUpload();
        BatchInputFileSetType batchType = retrieveBatchInputFileSetTypeImpl(batchUpload.getBatchInputTypeName());

        boolean requiredValuesForFilesMissing = false;
        boolean errorCreatingDisencFile = false;
        if (StringUtils.isBlank(batchUpload.getFileUserIdentifer())) {
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_NO_FILE_SET_IDENTIFIER_SELECTED, new String[]{});
            requiredValuesForFilesMissing = true;
        }

        BatchInputFileSetService batchInputFileSetService = SpringContext.getBean(BatchInputFileSetService.class);
        if (!batchInputFileSetService.isFileUserIdentifierProperlyFormatted(batchUpload.getFileUserIdentifer())) {
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_FILE_SET_IDENTIFIER_BAD_FORMAT);
            requiredValuesForFilesMissing = true;
        }

        Map<String, InputStream> typeToStreamMap = new HashMap<String, InputStream>();

        String path = CUKFSConstants.STAGING_DIR + System.getProperty("file.separator") + CUKFSConstants.LD_DIR + System.getProperty("file.separator") + CUKFSConstants.ENTERPRISE_FEED_DIR;

        String selectedDataFile = batchInputFileSetForm.getSelectedDataFile();
        if (StringUtils.isNotEmpty(selectedDataFile)) {
            String dataFilePath = BatchFileUtils.resolvePathToAbsolutePath(path + System.getProperty("file.separator") + selectedDataFile);
            File dataFile = new File(dataFilePath).getAbsoluteFile();
            if (dataFile == null) {
                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_NO_FILE_SELECTED_SAVE_FOR_FILE_TYPE, new String[]{batchType.getFileTypeDescription().get(KFSConstants.DATA_FILE_TYPE)});
                requiredValuesForFilesMissing = true;
            } else {

                // call service to create disencumbrance
                LaborLedgerEnterpriseFeedService ldService = SpringContext.getBean(LaborLedgerEnterpriseFeedService.class);
                InputStream disencumFileInputStream = null;
                disencumFileInputStream = ldService.createDisencumbrance(new FileInputStream(dataFile));

                if (disencumFileInputStream == null) {
                    GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.MESSAGE_CREATE_DISENCUMBRANCE_ERROR);

                    errorCreatingDisencFile = true;
                } else {
                    typeToStreamMap.put(KFSConstants.DATA_FILE_TYPE, disencumFileInputStream);
                }

            }
        }

        String selectedReconFile = batchInputFileSetForm.getSelectedReconFile();
        if (StringUtils.isNotEmpty(selectedReconFile)) {
            String reconFilePath = BatchFileUtils.resolvePathToAbsolutePath(path + System.getProperty("file.separator") + selectedReconFile);
            File reconFile = new File(reconFilePath).getAbsoluteFile();
            if (reconFile == null) {
                GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_NO_FILE_SELECTED_SAVE_FOR_FILE_TYPE, new String[]{batchType.getFileTypeDescription().get(KFSConstants.RECON_FILE_TYPE)});
                requiredValuesForFilesMissing = true;
            } else {
                typeToStreamMap.put(KFSConstants.RECON_FILE_TYPE, new FileInputStream(reconFile));
            }

        }

        if (requiredValuesForFilesMissing || errorCreatingDisencFile) {
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        try {
            Map<String, String> typeToSavedFileNames = batchInputFileSetService.save(GlobalVariables.getUserSession().getPerson(), batchType, batchUpload.getFileUserIdentifer(), typeToStreamMap);
        } catch (FileStorageException e) {
            LOG.error("Error occured while trying to save file set (probably tried to save a file that already exists).", e);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_FILE_SAVE_ERROR, new String[]{e.getMessage()});
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        } catch (ValidationException e) {
            LOG.error("Error occured while trying to validate file set.", e);
            GlobalVariables.getMessageMap().putError(KFSConstants.GLOBAL_ERRORS, KFSKeyConstants.ERROR_BATCH_UPLOAD_FILE_VALIDATION_ERROR);
            return mapping.findForward(KFSConstants.MAPPING_BASIC);
        }

        GlobalVariables.getMessageList().add(KFSKeyConstants.MESSAGE_CREATE_DISENCUMBRANCE_SUCCESSFUL);

        return mapping.findForward(KFSConstants.MAPPING_BASIC);

    }

}
