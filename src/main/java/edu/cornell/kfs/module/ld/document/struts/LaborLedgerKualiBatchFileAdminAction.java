package edu.cornell.kfs.module.ld.document.struts;

import java.io.File;
import java.text.MessageFormat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.web.struts.KualiBatchFileAdminAction;
import org.kuali.kfs.sys.web.struts.KualiBatchFileAdminForm;
import org.kuali.rice.core.util.RiceConstants;
import org.kuali.rice.kns.question.ConfirmationQuestion;
import org.kuali.rice.kns.service.KualiConfigurationService;
import org.kuali.rice.kns.util.KNSConstants;

import edu.cornell.kfs.module.ld.service.LaborLedgerEnterpriseFeedService;

public class LaborLedgerKualiBatchFileAdminAction extends KualiBatchFileAdminAction {

    public ActionForward disencumber(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        KualiBatchFileAdminForm fileAdminForm = (KualiBatchFileAdminForm) form;
        String filePath = BatchFileUtils.resolvePathToAbsolutePath(fileAdminForm.getFilePath());
        File file = new File(filePath).getAbsoluteFile();

        KualiConfigurationService kualiConfigurationService = SpringContext.getBean(KualiConfigurationService.class);

        if (!file.exists() || !file.isFile()) {
            throw new RuntimeException("Error: non-existent file or directory provided");
        }
        File containingDirectory = file.getParentFile();
        if (!BatchFileUtils.isDirectoryAccessible(containingDirectory.getAbsolutePath())) {
            throw new RuntimeException("Error: inaccessible directory provided");
        }

        BatchFile batchFile = new BatchFile();
        batchFile.setFile(file);
        // if (!SpringContext.getBean(BatchFileAdminAuthorizationService.class).canDelete(batchFile,
        // GlobalVariables.getUserSession().getPerson())) {
        // throw new RuntimeException("Error: not authorized to delete file");
        // }

        String displayFileName = BatchFileUtils.pathRelativeToRootDirectory(file.getAbsolutePath());

        Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        if (question == null) {
            String questionText = kualiConfigurationService.getPropertyString(KFSKeyConstants.QUESTION_BATCH_FILE_ADMIN_CREATE_DISENCUMBRANCE_CONFIRM);
            questionText = MessageFormat.format(questionText, displayFileName);
            return performQuestionWithoutInput(mapping, fileAdminForm, request, response, "confirmDelete", questionText, KNSConstants.CONFIRMATION_QUESTION, "delete", fileAdminForm.getFilePath());
        } else {
            Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            if ("confirmDelete".equals(question)) {
                String status = null;
                if (ConfirmationQuestion.YES.equals(buttonClicked)) {
                    try {
                        // call service to create disencumbrance
                        LaborLedgerEnterpriseFeedService ldService = SpringContext.getBean(LaborLedgerEnterpriseFeedService.class);

                        boolean disencCreated = ldService.createDisencumbrance(file);

                        if (disencCreated) {
                            status = kualiConfigurationService.getPropertyString(KFSKeyConstants.MESSAGE_CREATE_DISENCUMBRANCE_SUCCESSFUL);
                            status = MessageFormat.format(status, displayFileName);
                        } else {
                            status = kualiConfigurationService.getPropertyString(KFSKeyConstants.MESSAGE_CREATE_DISENCUMBRANCE_ERROR);
                            status = MessageFormat.format(status, displayFileName);
                        }
                    } catch (SecurityException e) {
                        status = kualiConfigurationService.getPropertyString(KFSKeyConstants.MESSAGE_CREATE_DISENCUMBRANCE_ERROR);
                        status = MessageFormat.format(status, displayFileName);
                    }
                } else if (ConfirmationQuestion.NO.equals(buttonClicked)) {
                    status = kualiConfigurationService.getPropertyString(KFSKeyConstants.MESSAGE_CREATE_DISENCUMBRANCE_CANCELLED);
                    status = MessageFormat.format(status, displayFileName);
                }
                if (status != null) {
                    request.setAttribute("status", status);
                    return mapping.findForward(RiceConstants.MAPPING_BASIC);
                }
            }
            throw new RuntimeException("Unrecognized question: " + question + " or response: " + buttonClicked);
        }
    }

}
