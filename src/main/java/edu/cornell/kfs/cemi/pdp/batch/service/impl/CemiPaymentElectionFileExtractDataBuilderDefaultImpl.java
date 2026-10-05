package edu.cornell.kfs.cemi.pdp.batch.service.impl;

import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.businessobject.ACHBank;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.pdp.batch.businessobject.CemiPaymentElectionFileGroupTwoTabRowBo;
import edu.cornell.kfs.cemi.pdp.batch.service.CemiPaymentElectionFileExtractDataBuilder;
import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionDao;
import edu.cornell.kfs.cemi.pdp.dataaccess.CemiPaymentElectionOrmDao;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;

public class CemiPaymentElectionFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
         implements CemiPaymentElectionFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    protected CemiPaymentElectionOrmDao cemiPaymentElectionOrmDao;
    protected CemiPaymentElectionDao cemiPaymentElectionDao;
    protected AchBankService achBankService;
    protected final boolean maskSensitiveData;

    public CemiPaymentElectionFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final AchBankService achBankService,
            final CemiPaymentElectionOrmDao cemiPaymentElectionOrmDao,
            final CemiPaymentElectionDao cemiPaymentElectionDao, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiPaymentElectionFileGroupTwoTabRowBo.class);
        Validate.notNull(achBankService, "achBankService cannot be null");
        Validate.notNull(cemiPaymentElectionOrmDao, "cemiPaymentElectionOrmDao cannot be null");
        Validate.notNull(cemiPaymentElectionDao, "cemiPaymentElectionDao cannot be null");
        this.achBankService = achBankService;
        this.cemiPaymentElectionOrmDao = cemiPaymentElectionOrmDao;
        this.cemiPaymentElectionDao = cemiPaymentElectionDao;
        this.maskSensitiveData = maskSensitiveData;
    }

    @Override
    public void writePaymentElectionFileGroupTwoTabExtractDataToIntermediateStorage(
            final Iterator<PayeeACHAccount> payeeAchAccounts) {
        int groupTwoTabRowCount = 0;

        for (final PayeeACHAccount payeeAchAccount : IteratorUtils.asIterable(payeeAchAccounts)) {
            groupTwoTabRowCount++;
            if (groupTwoTabRowCount % 1000 == 0) {
                LOG.info("writePaymentElectionFileGroupTwoTabExtractDataToIntermediateStorage, Processed {} "
                        + "PayeeACHAccounts for Payment Election and counting...", groupTwoTabRowCount);
            }
            //Group_TWO Tab
            final String achBankName = determineAchBankName(payeeAchAccount.getBankRoutingNumber());
            //Database table storage of data extract
            createAndStorePaymentElectionFileGroupTwoTabRow(payeeAchAccount, achBankName, jobRunDateString);
        }
        LOG.info("writePaymentElectionFileGroupTwoTabExtractDataToIntermediateStorage, Finished writing {} "
                + "PayeeACHAccounts for Payment Election", groupTwoTabRowCount);
    }

    protected void createAndStorePaymentElectionFileGroupTwoTabRow(final PayeeACHAccount payeeAchAccount,
            final String achBankName, final String jobRunDateString) {

        CemiPaymentElectionFileGroupTwoTabRowBoFactory factoryForBo =
                new CemiPaymentElectionFileGroupTwoTabRowBoFactory(payeeAchAccount, achBankName, jobRunDateString,
                        maskSensitiveData);

        CemiPaymentElectionFileGroupTwoTabRowBo groupTwoTabRow = factoryForBo.createCemiPaymentElectionFileGroupTwoTabRowBo();
        storeSheetRow(groupTwoTabRow);

        //Record identifier associations for Payment Election extract file based upon batch job run date
        recordPaymentElectionIdentifiersInLegacyAssociationTable(groupTwoTabRow.getEmployeeId(),
                groupTwoTabRow.getAchAccountGeneratedIdentifierUsedForDataRow(), groupTwoTabRow.getJobRunDateString());
    }

    protected void recordPaymentElectionIdentifiersInLegacyAssociationTable(final String employeeId,
            final KualiInteger achAccountGeneratedIdentifier, final String jobRunDateString) {
        cemiPaymentElectionDao.storeSpreadsheetRowItemKeyLegacyObjectKeyExtractRunDateMapping(
                employeeId, achAccountGeneratedIdentifier, jobRunDateString);
    }

    protected String determineAchBankName(final String bankRoutingNumber) {
        final ACHBank bankToUseForName = achBankService.getByPrimaryId(bankRoutingNumber);
        return ObjectUtils.isNull(bankToUseForName) ? KFSConstants.EMPTY_STRING : bankToUseForName.getBankName();
    }

}
