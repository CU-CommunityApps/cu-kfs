package edu.cornell.kfs.tax.batch.service.impl;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.Organization;
import org.kuali.kfs.coa.service.AccountService;
import org.kuali.kfs.coa.service.OrganizationService;
import org.kuali.kfs.kim.api.identity.PersonService;
import org.kuali.kfs.kim.impl.identity.Person;

import edu.cornell.kfs.tax.CUTaxConstants;
import edu.cornell.kfs.tax.CUTaxConstants.TaxCommonParameterNames;
import edu.cornell.kfs.tax.batch.TaxBatchConfig;
import edu.cornell.kfs.tax.batch.dataaccess.TransactionDetailBuilderDao;
import edu.cornell.kfs.tax.batch.dto.RouteHeaderLite;
import edu.cornell.kfs.tax.batch.service.TransactionDetailBuilderHelperService;
import edu.cornell.kfs.tax.businessobject.TransactionDetail;
import edu.cornell.kfs.tax.service.TaxParameterService;

/**
 * NOTE: Due to how this implementation handles the generation of placeholder tax ID numbers,
 *       the related Spring bean should be declared as a prototype to prevent interference
 *       between subsequent batch job runs.
 */
public class TransactionDetailBuilderHelperServiceImpl implements TransactionDetailBuilderHelperService {

    private static final Logger LOG = LogManager.getLogger();

    private static final int MAX_AUTO_TAXNUM_DIGITS = 8;
    private static final int DEFAULT_BATCH_INSERTION_SIZE = 100;

    private final DecimalFormat taxIdFormat;
    private final Map<String, String> taxIdsByPayeeId;
    private final AtomicInteger taxIdCount;

    private TransactionDetailBuilderDao transactionDetailBuilderDao;
    private AccountService accountService;
    private OrganizationService organizationService;
    private PersonService personService;
    private TaxParameterService taxParameterService;

    public TransactionDetailBuilderHelperServiceImpl() {
        this.taxIdFormat = createDecimalFormatForPlaceholderTaxIds();
        this.taxIdsByPayeeId = new ConcurrentHashMap<>();
        this.taxIdCount = new AtomicInteger(0);
    }

    private static DecimalFormat createDecimalFormatForPlaceholderTaxIds() {
        final DecimalFormat taxIdFormat = new DecimalFormat("~00000000", new DecimalFormatSymbols(Locale.US));
        taxIdFormat.setMaximumIntegerDigits(MAX_AUTO_TAXNUM_DIGITS);
        return taxIdFormat;
    }

    @Override
    public void insertTransactionDetails(final List<TransactionDetail> transactionDetails, final TaxBatchConfig config) {
        transactionDetailBuilderDao.insertTransactionDetails(transactionDetails, config);
    }

    @Override
    public List<RouteHeaderLite> getBasicRouteHeaderData(final List<String> documentIds) {
        return transactionDetailBuilderDao.getBasicRouteHeaderData(documentIds);
    }

    @Override
    public String getOrGeneratePlaceholderTaxIdForPayee(final String payeeId) {
        return taxIdsByPayeeId.computeIfAbsent(payeeId, this::generatePlaceholderTaxId);
    }

    private String generatePlaceholderTaxId(final String payeeId) {
        return taxIdFormat.format(taxIdCount.incrementAndGet());
    }

    @Override
    public Account getAccountByPrimaryId(final String chartOfAccountsCode, final String accountNumber) {
        return accountService.getByPrimaryIdWithCaching(chartOfAccountsCode, accountNumber);
    }

    @Override
    public Organization getOrganizationByPrimaryId(final String chartOfAccountsCode, final String organizationCode) {
        return organizationService.getByPrimaryIdWithCaching(chartOfAccountsCode, organizationCode);
    }

    @Override
    public Person getPerson(final String principalId) {
        return personService.getPerson(principalId);
    }

    @Override
    public int getBatchInsertionSize() {
        int batchInsertionSize = -1;
        final String insertionSizeParamValue = getParameterValue(CUTaxConstants.TAX_PARM_DETAIL,
                TaxCommonParameterNames.TRANSACTION_DETAIL_BATCH_INSERTION_SIZE);
        if (StringUtils.isNotBlank(insertionSizeParamValue)) {
            try {
                batchInsertionSize = Integer.parseInt(insertionSizeParamValue);
            } catch (final NumberFormatException e) {
                batchInsertionSize = -1;
            }
        }

        if (batchInsertionSize <= 0) {
            LOG.error("getBatchInsertionSize, parameter {} has a missing or invalid value; using default value of {}",
                    TaxCommonParameterNames.TRANSACTION_DETAIL_BATCH_INSERTION_SIZE, DEFAULT_BATCH_INSERTION_SIZE);
            batchInsertionSize = DEFAULT_BATCH_INSERTION_SIZE;
        }
        return batchInsertionSize;
    }

    @Override
    public Map<String, String> getSubParameters(final String componentCode, final String parameterName) {
        return taxParameterService.getSubParameters(componentCode, parameterName);
    }

    @Override
    public Set<String> getParameterValues(final String componentCode, final String parameterName) {
        return taxParameterService.getParameterValuesSetAsString(componentCode, parameterName);
    }

    @Override
    public String getParameterValue(final String componentCode, final String parameterName) {
        return taxParameterService.getParameterValueAsString(componentCode, parameterName);
    }

    public void setTransactionDetailBuilderDao(final TransactionDetailBuilderDao transactionDetailBuilderDao) {
        this.transactionDetailBuilderDao = transactionDetailBuilderDao;
    }

    public void setAccountService(final AccountService accountService) {
        this.accountService = accountService;
    }

    public void setOrganizationService(final OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    public void setPersonService(final PersonService personService) {
        this.personService = personService;
    }

    public void setTaxParameterService(final TaxParameterService taxParameterService) {
        this.taxParameterService = taxParameterService;
    }

}
