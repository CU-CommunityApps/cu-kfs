package edu.cornell.kfs.fp.document.service.impl;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.service.NoteService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.PaymentSourceConstants;

import edu.cornell.kfs.fp.CuFPKeyConstants;
import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherCheckStubService;

public class CuDisbursementVoucherCheckStubServiceImpl implements CuDisbursementVoucherCheckStubService {

    private int checkStubMaxLengthForIso20022;
    private ConfigurationService configurationService;
    private NoteService noteService;
    private PersonService personService;

    @Override
    public boolean doesCheckStubNeedTruncatingForIso20022(DisbursementVoucherDocument document) {
        return StringUtils.equalsIgnoreCase(document.getPaymentMethodCode(), PaymentSourceConstants.PAYMENT_METHOD_CHECK)
                && StringUtils.length(document.getDisbVchrCheckStubText()) > checkStubMaxLengthForIso20022;
    }

    @Override
    public void addNoteToDocumentRegardingCheckStubIso20022MaxLength(DisbursementVoucherDocument document) {
        String warningMessage = createWarningMessageForCheckStubIso20022MaxLength();
        Person systemUser = personService.getPersonByPrincipalName(KFSConstants.SYSTEM_USER);

        Note note = noteService.createNote(new Note(), document.getNoteTarget(), systemUser.getPrincipalId());
        note.setNoteText(warningMessage);
        note.setNotePostedTimestampToCurrent();

        note = noteService.save(note);
        document.addNote(note);
    }

    @Override
    public String createWarningMessageForCheckStubIso20022MaxLength() {
        String baseMessage = configurationService.getPropertyValueAsString(
                CuFPKeyConstants.WARNING_DV_CHECK_STUB_ISO_20022_LENGTH);
        return MessageFormat.format(baseMessage, checkStubMaxLengthForIso20022);
    }

    @Override
    public int getCheckStubMaxLengthForIso20022() {
        return checkStubMaxLengthForIso20022;
    }

    public void setCheckStubMaxLengthForIso20022(int checkStubMaxLengthForIso20022) {
        this.checkStubMaxLengthForIso20022 = checkStubMaxLengthForIso20022;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public void setNoteService(NoteService noteService) {
        this.noteService = noteService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

}
