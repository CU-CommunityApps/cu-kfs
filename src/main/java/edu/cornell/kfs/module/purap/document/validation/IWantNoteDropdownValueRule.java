package edu.cornell.kfs.module.purap.document.validation;

import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.maintenance.rules.MaintenanceDocumentRuleBase;
import org.kuali.kfs.krad.service.SequenceAccessorService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.module.purap.CUPurapConstants;
import edu.cornell.kfs.module.purap.businessobject.IWantNoteDropdownValue;

public class IWantNoteDropdownValueRule extends MaintenanceDocumentRuleBase {

    @Override
    protected boolean processCustomRouteDocumentBusinessRules(MaintenanceDocument document) {
        boolean isValid = super.processCustomRouteDocumentBusinessRules(document);
        if (isValid) {
            final IWantNoteDropdownValue iWantNote = (IWantNoteDropdownValue) document.getNewMaintainableObject().getBusinessObject();
            if (ObjectUtils.isNull(iWantNote.getNoteIdentifier())) {
                final SequenceAccessorService sequenceAccessorService = SpringContext
                        .getBean(SequenceAccessorService.class);
                final Integer noteId = Integer.valueOf(String.valueOf(sequenceAccessorService
                        .getNextAvailableSequenceNumber(CUPurapConstants.CU_PUR_IWNT_NTE_DRPDN_VAL_ID_SEQ)));
                iWantNote.setNoteIdentifier(noteId);
            }
        }
        return isValid;
    }

}
