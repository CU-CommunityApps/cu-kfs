package edu.cornell.kfs.vnd.util;

import java.io.IOException;
import java.util.Collection;

import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;

@FunctionalInterface
public interface VendorAccountFinder {

    Collection<PayeeACHAccount> findAllAccountsForVendor(final Integer vendorHeaderGeneratedIdentifier,
            final Integer vendorDetailAssignedIdentifier) throws IOException;

}
