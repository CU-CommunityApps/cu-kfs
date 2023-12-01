package edu.cornell.kfs.kew.actionlist.service.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.kew.actionlist.service.impl.ActionListServiceImpl;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.kew.CuKewKeyConstants;
import edu.cornell.kfs.kew.actionitem.ActionItemExtension;
import edu.cornell.kfs.kew.actionitem.dao.CuActionItemDAO;
import edu.cornell.kfs.kew.actionlist.service.CuActionListService;

public class CuActionListServiceImpl extends ActionListServiceImpl implements CuActionListService {

    private static final Logger LOG = LogManager.getLogger();

    private static final String DEFAULT_NOTE_FAILURE_MESSAGE = "Error: Could not save note";

    private ConfigurationService configurationService;

    @Override
    public ActionItemExtension findActionItemExtensionByActionItemId(final String actionItemId) {
        if (StringUtils.isBlank(actionItemId)) {
            throw new IllegalArgumentException("actionItemId cannot be blank");
        }
        return getCuActionItemDAO().findActionItemExtensionByActionItemId(actionItemId);
    }

    /**
     * Part of MSU action item notes contribution; updated as needed by CU.
     * Allows for blank inputs, to prevent errors when this is invoked
     * by JavaScript on the action list page.
     */
    @Override
    public String saveActionItemNoteForActionItemId(String note, final String actionItemId) {
        String resultStatus = KFSConstants.EMPTY_STRING;
        
        try {
            if (StringUtils.isNotBlank(note) && !validateNoteMaxLength(note)) {
                final int maxLength = Integer.parseInt(configurationService.getPropertyValueAsString(
                        CuKewKeyConstants.ACTION_LIST_RESULTS_NOTES_MAXLENGTH));
                note = StringUtils.substring(note, 0, maxLength);
                resultStatus = configurationService.getPropertyValueAsString(
                        CuKewKeyConstants.ACTION_LIST_RESULTS_SAVED_TRUNCATE);
            }

            if (StringUtils.isNotBlank(actionItemId)) {
                ActionItemExtension extension = findActionItemExtensionByActionItemId(actionItemId);
                
                if (isCreatingNewNonBlankNoteOrChangingExistingNote(extension, note)) {
                    if (ObjectUtils.isNull(extension)) {
                        extension = new ActionItemExtension();
                        extension.setActionItemId(actionItemId);
                    } else if (StringUtils.isBlank(note)) {
                        note = KFSConstants.EMPTY_STRING;
                    }
                    
                    extension.setActionNote(note);
                    getCuActionItemDAO().saveActionItemExtension(extension);
                    
                    if (StringUtils.isBlank(resultStatus)) {
                        resultStatus = configurationService.getPropertyValueAsString(
                                CuKewKeyConstants.ACTION_LIST_RESULTS_SAVED_SUCCESS);
                    }
                }
            } else {
                resultStatus = getNoteSaveFailureStatus();
            }
        } catch (RuntimeException e) {
            LOG.error("saveActionItemNoteForActionItemId, Unexpected exception when saving action item note with ID: "
                    + StringUtils.defaultIfBlank(actionItemId, "(Blank ID)"), e);
            resultStatus = getNoteSaveFailureStatus();
        }
        
        return resultStatus;
    }

    /**
     * Part of MSU action item notes contribution; updated as needed by CU.
     * This method validates user input notes length.
     */
    private boolean validateNoteMaxLength(final String note) {
        final String maxLengthProperty = configurationService.getPropertyValueAsString(
                CuKewKeyConstants.ACTION_LIST_RESULTS_NOTES_MAXLENGTH);

        if (StringUtils.isBlank(maxLengthProperty)) {
            LOG.error("validateNoteMaxLength, Action list note max length not defined and will not be validated " +
                    "for user input");
        } else {
            try {
                final int maxLength = Integer.parseInt(maxLengthProperty);
                return StringUtils.length(note) <= maxLength;
            } catch (NumberFormatException e) {
                LOG.error("validateNoteMaxLength, Action list note max length does not contain a valid integer. " +
                        "No validation will run.");
            }
        }
        return true;
    }

    private boolean isCreatingNewNonBlankNoteOrChangingExistingNote(
            final ActionItemExtension extension, final String newNoteText) {
        if (ObjectUtils.isNull(extension)) {
            return StringUtils.isNotBlank(newNoteText);
        } else if (StringUtils.isBlank(newNoteText)) {
            return StringUtils.isNotBlank(extension.getActionNote());
        } else {
            return !StringUtils.equals(extension.getActionNote(), newNoteText);
        }
    }

    private String getNoteSaveFailureStatus() {
        try {
            return configurationService.getPropertyValueAsString(CuKewKeyConstants.ACTION_LIST_RESULTS_SAVED_FAILURE);
        } catch (RuntimeException e) {
            LOG.error("getNoteSaveFailureStatus, Could not retrieve status from KFS properties", e);
            return DEFAULT_NOTE_FAILURE_MESSAGE;
        }
    }

    private CuActionItemDAO getCuActionItemDAO() {
        return (CuActionItemDAO) getActionItemDAO();
    }

    public void setConfigurationService(final ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

}
