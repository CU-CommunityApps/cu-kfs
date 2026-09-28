package edu.cornell.kfs.cemi.pdp.dataaccess;

import java.util.stream.Stream;

import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

public interface CemiPaymentElectionOrmDao {

    Stream<PayeeACHAccount> getPayeeAchAccountIdsForCemiPaymentElectionExtractAsCloseableStream();

}
