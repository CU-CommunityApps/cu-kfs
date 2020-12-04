package edu.cornell.kfs.sys.service;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySequenceHelper;
import org.kuali.kfs.sys.document.GeneralLedgerPostingDocument;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public interface CuGeneralLedgerPendingEntryService extends GeneralLedgerPendingEntryService {

    GeneralLedgerPendingEntry buildGeneralLedgerPendingEntry(GeneralLedgerPostingDocument document,
            Account account, ObjectCode objectCode, String subAccountNumber, String subObjectCode,
            String organizationReferenceId, String projectCode, String referenceNumber, String referenceTypeCode,
            String referenceOriginCode, String description, boolean isDebit, KualiDecimal amount,
            GeneralLedgerPendingEntrySequenceHelper sequenceHelper);

}
