package edu.cornell.kfs.cemi.pdp.batch.service;

import java.io.Closeable;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Iterator;

import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

public interface CemiPaymentElectionDataBuilder extends Closeable {

    void writePaymentElectionDataToIntermediateStorage(final Iterator<PayeeACHAccount> payeeAchAccount,
            final LocalDateTime jobRunDate) throws IOException;

}
