package edu.cornell.kfs.coa.document;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coa.businessobject.Account;
import org.kuali.kfs.coa.businessobject.AccountGlobalDetail;
import org.kuali.kfs.coa.document.AccountGlobalMaintainableImpl;
import org.kuali.kfs.coa.service.SubAccountTrickleDownInactivationService;
import org.kuali.kfs.coa.service.SubObjectTrickleDownInactivationService;
import org.kuali.kfs.krad.bo.GlobalBusinessObject;
import org.kuali.kfs.krad.bo.PersistableBusinessObject;
import org.kuali.kfs.krad.maintenance.MaintenanceLock;
import org.kuali.kfs.krad.maintenance.MaintenanceUtils;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.DocumentHeader;
import org.kuali.kfs.sys.context.SpringContext;
import org.springframework.cache.Cache;

import edu.cornell.kfs.coa.businessobject.CuAccountGlobal;
import edu.cornell.kfs.coa.service.AccountReversionTrickleDownInactivationService;

@SuppressWarnings("deprecation")
public class CuAccountGlobalMaintainableImpl extends AccountGlobalMaintainableImpl {
    private static final Logger LOG = LogManager.getLogger();

    private transient SubAccountTrickleDownInactivationService subAccountTrickleDownInactivationService;
    private transient SubObjectTrickleDownInactivationService subObjectTrickleDownInactivationService;
    private transient AccountReversionTrickleDownInactivationService accountReversionTrickleDownInactivationService;

    @Override
    public List<MaintenanceLock> generateMaintenanceLocks() {
        final List<MaintenanceLock> accountLocks = super.generateMaintenanceLocks();
        final CuAccountGlobal accountGlobal = (CuAccountGlobal) getBusinessObject();
        final Map<String, Boolean> oldAccountClosedStatuses = getExistingAccountClosedStatuses(accountGlobal);
        final boolean closedStatusForLocks = getAccountClosedStatusForMaintenanceLocks(accountGlobal);
        
        Stream<MaintenanceLock> trickleDownLocks = accountGlobal.getAccountGlobalDetails().stream()
                .map(accountDetail -> buildTemporaryAccountForMaintenanceLocks(accountDetail, closedStatusForLocks))
                .filter(account -> isClosingAccount(account, oldAccountClosedStatuses))
                .flatMap(this::generateTrickleDownMaintenanceLocks);
        
        return Stream.concat(accountLocks.stream(), trickleDownLocks)
                .collect(Collectors.toUnmodifiableList());
    }
    
    @Override
    public void doRouteStatusChange(final DocumentHeader documentHeader) {
        super.doRouteStatusChange(documentHeader);
        if (MaintenanceUtils.shouldClearCacheOnStatusChange(documentHeader)) {
            clearBlockingCacheForCurrentDocument();
        }
    }

    public void clearBlockingCacheForCurrentDocument() {
        final Cache cache = MaintenanceUtils.getBlockingCache();
        final String cacheKey = MaintenanceUtils.buildLockingDocumentCacheKey(getDocumentNumber());
        if (LOG.isDebugEnabled()) {
            LOG.debug("clearBlockingCacheForCurrentDocument, clear cache id: " + cacheKey);
        }
        cache.evictIfPresent(cacheKey);
    }
    
    @Override
    public void prepareForSave() {
        clearBlockingCacheForCurrentDocument();
        super.prepareForSave();
    }

    private boolean getAccountClosedStatusForMaintenanceLocks(final CuAccountGlobal accountGlobal) {
        Boolean closed = accountGlobal.getClosed();
        return closed != null && closed.booleanValue();
    }

    private Account buildTemporaryAccountForMaintenanceLocks(final AccountGlobalDetail accountGlobalDetail, final boolean closed) {
        final Account account = new Account();
        account.setChartOfAccountsCode(accountGlobalDetail.getChartOfAccountsCode());
        account.setAccountNumber(accountGlobalDetail.getAccountNumber());
        account.setClosed(closed);
        return account;
    }

    private Stream<MaintenanceLock> generateTrickleDownMaintenanceLocks(final Account inactivatedAccount) {
        final List<MaintenanceLock> subAccountLocks = getSubAccountTrickleDownInactivationService()
                .generateTrickleDownMaintenanceLocks(inactivatedAccount, getDocumentNumber());
        final List<MaintenanceLock> subObjectLocks = getSubObjectTrickleDownInactivationService()
                .generateTrickleDownMaintenanceLocks(inactivatedAccount, getDocumentNumber());
        final List<MaintenanceLock> accountReversionLocks = getAccountReversionTrickleDownInactivationService()
                .generateTrickleDownMaintenanceLocks(inactivatedAccount, getDocumentNumber());
        return Stream.of(subAccountLocks, subObjectLocks, accountReversionLocks)
                .flatMap(List::stream);
    }

    /*
     * Copied and overrode this method to also inactivate downstream objects.
     * Some of the updated logic is similar to that from the ObjectCodeGlobalMaintainableImpl class.
     */
    @Override
    public void saveBusinessObject() {
        final BusinessObjectService boService = getBusinessObjectService();
        final GlobalBusinessObject gbo = (GlobalBusinessObject) getBusinessObject();

        final Map<String, Boolean> oldAccountClosedStatuses = getExistingAccountClosedStatuses((CuAccountGlobal) gbo);

        // delete any indicated BOs
        final List<PersistableBusinessObject> bosToDeactivate = gbo.generateDeactivationsToPersist();
        if (CollectionUtils.isNotEmpty(bosToDeactivate)) {
            boService.save(bosToDeactivate);
        }

        // persist any indicated BOs
        final List<PersistableBusinessObject> bosToPersist = gbo.generateGlobalChangesToPersist();
        if (CollectionUtils.isNotEmpty(bosToPersist)) {
            for (final PersistableBusinessObject boToPersist : bosToPersist) {
                final Account account = (Account) boToPersist;
                boService.save(account);
                if (isClosingAccount(account, oldAccountClosedStatuses)) {
                    trickleDownInactivateBusinessObjectsForClosedAccount(account);
                }
            }
        }

    }

    private void trickleDownInactivateBusinessObjectsForClosedAccount(final Account account) {
        getSubAccountTrickleDownInactivationService().trickleDownInactivateSubAccounts(account, getDocumentNumber());
        getSubObjectTrickleDownInactivationService().trickleDownInactivateSubObjects(account, getDocumentNumber());
        getAccountReversionTrickleDownInactivationService().trickleDownInactivateAccountReversions(
                account, getDocumentNumber());
    }

    private Map<String, Boolean> getExistingAccountClosedStatuses(final CuAccountGlobal accountGlobal) {
        final Map<String, List<String>> accountsByChart = accountGlobal.getAccountGlobalDetails().stream()
                .collect(Collectors.groupingBy(AccountGlobalDetail::getChartOfAccountsCode, Collectors.mapping(
                        AccountGlobalDetail::getAccountNumber, Collectors.toUnmodifiableList())));
        
        return accountsByChart.entrySet().stream()
                .flatMap(this::getExistingMatchingAccounts)
                .collect(Collectors.toUnmodifiableMap(this::buildAccountClosedStatusKey, Account::isClosed));
    }

    private Stream<Account> getExistingMatchingAccounts(final Map.Entry<String, List<String>> chartAndAccountsListPair) {
        final Map<String, Object> criteria = Map.ofEntries(
                Map.entry(KFSPropertyConstants.CHART_OF_ACCOUNTS_CODE, chartAndAccountsListPair.getKey()),
                Map.entry(KFSPropertyConstants.ACCOUNT_NUMBER, chartAndAccountsListPair.getValue())
        );
        final Collection<Account> accounts = getBusinessObjectService().findMatching(Account.class, criteria);
        return accounts.stream();
    }

    private boolean isClosingAccount(final Account account, final Map<String, Boolean> oldAccountClosedStatuses) {
        if (!account.isClosed()) {
            return false;
        }
        final String accountKey = buildAccountClosedStatusKey(account);
        final Boolean oldAccountClosedFlag = oldAccountClosedStatuses.get(accountKey);
        return oldAccountClosedFlag != null && !oldAccountClosedFlag.booleanValue();
    }

    private String buildAccountClosedStatusKey(final Account account) {
        return StringUtils.join(account.getChartOfAccountsCode(),
                KRADConstants.Maintenance.LOCK_AFTER_VALUE_DELIM, account.getAccountNumber());
    }

    protected SubAccountTrickleDownInactivationService getSubAccountTrickleDownInactivationService() {
        if (subAccountTrickleDownInactivationService == null) {
            subAccountTrickleDownInactivationService = SpringContext.getBean(
                    SubAccountTrickleDownInactivationService.class);
        }
        return subAccountTrickleDownInactivationService;
    }

    public void setSubAccountTrickleDownInactivationService(
            final SubAccountTrickleDownInactivationService subAccountTrickleDownInactivationService) {
        this.subAccountTrickleDownInactivationService = subAccountTrickleDownInactivationService;
    }

    protected SubObjectTrickleDownInactivationService getSubObjectTrickleDownInactivationService() {
        if (subObjectTrickleDownInactivationService == null) {
            subObjectTrickleDownInactivationService = SpringContext.getBean(
                    SubObjectTrickleDownInactivationService.class);
        }
        return subObjectTrickleDownInactivationService;
    }

    public void setSubObjectTrickleDownInactivationService(
            final SubObjectTrickleDownInactivationService subObjectTrickleDownInactivationService) {
        this.subObjectTrickleDownInactivationService = subObjectTrickleDownInactivationService;
    }

    protected AccountReversionTrickleDownInactivationService getAccountReversionTrickleDownInactivationService() {
        if (accountReversionTrickleDownInactivationService == null) {
            accountReversionTrickleDownInactivationService = SpringContext.getBean(
                    AccountReversionTrickleDownInactivationService.class);
        }
        return accountReversionTrickleDownInactivationService;
    }

    public void setAccountReversionTrickleDownInactivationService(
            final AccountReversionTrickleDownInactivationService accountReversionTrickleDownInactivationService) {
        this.accountReversionTrickleDownInactivationService = accountReversionTrickleDownInactivationService;
    }

}
