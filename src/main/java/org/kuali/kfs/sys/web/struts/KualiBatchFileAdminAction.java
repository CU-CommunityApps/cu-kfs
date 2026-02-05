/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.web.struts;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.KeyValue;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.kns.datadictionary.control.ControlDefinition;
import org.kuali.kfs.kns.question.ConfirmationQuestion;
import org.kuali.kfs.kns.service.KNSServiceLocator;
import org.kuali.kfs.kns.web.struts.action.KualiAction;
import org.kuali.kfs.krad.exception.AuthorizationException;
import org.kuali.kfs.krad.keyvalues.KeyValuesFinder;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.batch.BatchFile;
import org.kuali.kfs.sys.batch.BatchFileUtils;
import org.kuali.kfs.sys.batch.service.BatchFileAdminAuthorizationService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.FileStorageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

// CU customization to increase method visibility
public class KualiBatchFileAdminAction extends KualiAction {

    public ActionForward download(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) throws Exception {
        final KualiBatchFileAdminForm fileAdminForm = (KualiBatchFileAdminForm) form;
        final String filePath = BatchFileUtils.resolvePathToAbsolutePath(fileAdminForm.getFilePath());
        final FileStorageService fileStorageService = SpringContext.getBean(FileStorageService.class);
        final String directoryName = StringUtils.substringBeforeLast(filePath, fileStorageService.separator());
        final String fileName = StringUtils.substringAfterLast(filePath, fileStorageService.separator());

        if (!fileStorageService.fileExists(filePath)) {
            throw new RuntimeException("Error: non-existent file or directory provided");
        }
        if (!isDirectoryAccessible(directoryName)) {
            throw new RuntimeException("Error: inaccessible directory provided");
        }

        // TODO: Eliminate use of File class altogether, once BatchFile class is refactored.
        final File file = new File(filePath).getAbsoluteFile();
        final BatchFile batchFile = new BatchFile(file);
        if (!SpringContext.getBean(BatchFileAdminAuthorizationService.class).canDownload(batchFile,
                GlobalVariables.getUserSession().getPerson())) {
            logFileAction(filePath, "DOWNLOAD", false);
            throw new RuntimeException("Error: not authorized to download file");
        }

        response.setContentType("application/octet-stream");
        response.setHeader("Content-disposition", "attachment; filename=" + fileName);
        response.setHeader("Expires", "0");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setContentLength((int) fileStorageService.getFileLength(filePath));

        logFileAction(filePath, "DOWNLOAD", true);
        try (InputStream fis = fileStorageService.getFileStream(filePath)) {
            IOUtils.copy(fis, response.getOutputStream());
        }
        response.getOutputStream().flush();
        return null;
    }

    public ActionForward delete(
            final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
            final HttpServletResponse response) {
        final KualiBatchFileAdminForm fileAdminForm = (KualiBatchFileAdminForm) form;
        final String filePath = BatchFileUtils.resolvePathToAbsolutePath(fileAdminForm.getFilePath());
        final FileStorageService fileStorageService = SpringContext.getBean(FileStorageService.class);
        final String directoryName = StringUtils.substringBeforeLast(filePath, fileStorageService.separator());

        final ConfigurationService kualiConfigurationService = SpringContext.getBean(ConfigurationService.class);

        if (!fileStorageService.fileExists(filePath)) {
            throw new RuntimeException("Error: non-existent file or directory provided");
        }
        if (!isDirectoryAccessible(directoryName)) {
            throw new RuntimeException("Error: inaccessible directory provided");
        }

        // TODO: Eliminate use of File class altogether, once BatchFile class is refactored.
        final File file = new File(filePath).getAbsoluteFile();
        final BatchFile batchFile = new BatchFile(file);
        if (!SpringContext.getBean(BatchFileAdminAuthorizationService.class).canDelete(batchFile,
                GlobalVariables.getUserSession().getPerson())) {
            logFileAction(filePath, "DELETE", false);
            throw new RuntimeException("Error: not authorized to delete file");
        }

        final String displayFileName = BatchFileUtils.pathRelativeToRootDirectory(filePath);

        final Object question = request.getParameter(KFSConstants.QUESTION_INST_ATTRIBUTE_NAME);
        if (question == null) {
            String questionText = kualiConfigurationService.getPropertyValueAsString(
                    KFSKeyConstants.QUESTION_BATCH_FILE_ADMIN_DELETE_CONFIRM);
            questionText = MessageFormat.format(questionText, displayFileName);
            return performQuestionWithoutInput(mapping, fileAdminForm, request, response, "confirmDelete",
                    questionText,
                KRADConstants.CONFIRMATION_QUESTION, "delete", fileAdminForm.getFilePath());
        } else {
            final Object buttonClicked = request.getParameter(KFSConstants.QUESTION_CLICKED_BUTTON);
            if ("confirmDelete".equals(question)) {
                String status = null;
                if (ConfirmationQuestion.YES.equals(buttonClicked)) {
                    try {
                        logFileAction(filePath, "DELETE", true);
                        fileStorageService.delete(filePath);
                        status = kualiConfigurationService.getPropertyValueAsString(
                                KFSKeyConstants.MESSAGE_BATCH_FILE_ADMIN_DELETE_SUCCESSFUL);
                        status = MessageFormat.format(status, displayFileName);
                    } catch (final Exception e) {
                        status = kualiConfigurationService.getPropertyValueAsString(
                                KFSKeyConstants.MESSAGE_BATCH_FILE_ADMIN_DELETE_ERROR);
                        status = MessageFormat.format(status, displayFileName);
                    }
                } else if (ConfirmationQuestion.NO.equals(buttonClicked)) {
                    status = kualiConfigurationService.getPropertyValueAsString(
                            KFSKeyConstants.MESSAGE_BATCH_FILE_ADMIN_DELETE_CANCELLED);
                    status = MessageFormat.format(status, displayFileName);
                }
                if (status != null) {
                    request.setAttribute("status", status);
                    return mapping.findForward(KFSConstants.MAPPING_BASIC);
                }
            }
            throw new RuntimeException("Unrecognized question: " + question + " or response: " + buttonClicked);
        }
    }

    @Override
    protected void checkAuthorization(final ActionForm form, final String methodToCall) throws AuthorizationException {
        // do nothing... authorization is integrated into action handler
    }

    protected void logFileAction(final String filePath, final String action, final boolean granted) {
        final StringBuilder buf = new StringBuilder(300);
        buf.append(action).append(",").append(granted ? "SUCCESS" : "DENY").append(",").append(filePath);
        KNSServiceLocator.getSecurityLoggingService().logCustomString(buf.toString());
    }

    // CU customization to increase method visibility
    public static boolean isDirectoryAccessible(final String directory) {
        List<String> pathNames = null;

        final ControlDefinition controlDefinition = SpringContext.getBean(DataDictionaryService.class)
                .getAttributeControlDefinition("BatchFile", "path");
        final KeyValuesFinder keyValuesFinder = controlDefinition.getValuesFinder();
        if (keyValuesFinder != null) {
            pathNames = new ArrayList<>();

            final List<KeyValue> keyValues = keyValuesFinder.getKeyValues();
            for (final KeyValue keyValue : keyValues) {
                pathNames.add(new File(BatchFileUtils.resolvePathToAbsolutePath(keyValue.getKey())).getAbsolutePath());
            }
        }

        final File directoryAbsolute = new File(directory).getAbsoluteFile();
        final String directoryAbsolutePath = directoryAbsolute.getAbsolutePath();
        if (pathNames != null) {
            if (!pathNames.contains(directoryAbsolutePath)) {
                return false;
            }
        }

        final List<Path> rootDirectories = BatchFileUtils.retrieveBatchFileLookupRootDirectories();
        for (final Path rootDirectory : rootDirectories) {
            if (BatchFileUtils.isSuperDirectoryOf(rootDirectory, directoryAbsolute.toPath())) {
                return true;
            }
        }
        return false;
    }

}
