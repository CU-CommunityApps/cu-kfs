package edu.cornell.kfs.tax.batch.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.dto.RouteHeaderLite;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;

public interface TransactionDetailBuilderHelperService {

    void insertTransactionDetails(
            final List<TransactionDetail> transactionDetails, final TaxBatchConfig config);

    List<RouteHeaderLite> getBasicRouteHeaderData(final List<String> documentIds);

    String getOrGeneratePlaceholderTaxIdForPayee(final String payeeId);

    Account getAccountByPrimaryId(final String chartOfAccountsCode, final String accountNumber);

    Organization getOrganizationByPrimaryId(final String chartOfAccountsCode, final String organizationCode);

    Person getPerson(final String principalId);

    Map<String, String> getSubParameters(final String componentCode, final String parameterName);

    Set<String> getParameterValues(final String componentCode, final String parameterName);

    String getParameterValue(final String componentCode, final String parameterName);

}
