package edu.cornell.kfs.cemi.pdp.batch.service.impl;

import java.util.Iterator;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.pdp.businessobject.ACHBank;
import org.kuali.kfs.pdp.businessobject.PayeeACHAccount;
import org.kuali.kfs.pdp.service.AchBankService;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.cemi.pdp.batch.businessobject.CemiPaymentElectionFileGroupTwoTabRowBo;
import edu.cornell.kfs.cemi.pdp.batch.service.CemiPaymentElectionFileExtractDataBuilder;
import edu.cornell.kfs.cemi.sys.batch.service.impl.CemiOrmDataBuilderBase;

public class CemiPaymentElectionFileExtractDataBuilderDefaultImpl extends CemiOrmDataBuilderBase
        implements CemiPaymentElectionFileExtractDataBuilder {

    private static final Logger LOG = LogManager.getLogger();

    protected AchBankService achBankService;
    protected final boolean maskSensitiveData;

    public CemiPaymentElectionFileExtractDataBuilderDefaultImpl(
            final BusinessObjectService businessObjectService, final String jobRunDateString,
            final AchBankService achBankService, final boolean maskSensitiveData) {
        super(businessObjectService, jobRunDateString, CemiPaymentElectionFileGroupTwoTabRowBo.class);
        Validate.notNull(achBankService, "achBankService cannot be null");
        this.achBankService = achBankService;
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
                        + "PayeeACHAccounts for Payment Election Group TWO tab and counting...", groupTwoTabRowCount);
            }
            createAndStorePaymentElectionFileGroupTwoTabRow(payeeAchAccount, jobRunDateString);
        }
        LOG.info("writePaymentElectionFileGroupTwoTabExtractDataToIntermediateStorage, Finished writing {} "
                + "PayeeACHAccounts for Payment Election Group TWO tab", groupTwoTabRowCount);
    }

    protected void createAndStorePaymentElectionFileGroupTwoTabRow(final PayeeACHAccount payeeAchAccount,
            final String jobRunDateString) {
        final String employeeId = determineEmployeeId(payeeAchAccount);
        final String achBankName = determineAchBankName(payeeAchAccount.getBankRoutingNumber());

        final CemiPaymentElectionFileGroupTwoTabRowBoFactory factoryForBo =
                new CemiPaymentElectionFileGroupTwoTabRowBoFactory(payeeAchAccount, employeeId, achBankName,
                        jobRunDateString, maskSensitiveData);

        final CemiPaymentElectionFileGroupTwoTabRowBo groupTwoTabRow =
                factoryForBo.createCemiPaymentElectionFileGroupTwoTabRowBo();
        storeSheetRow(groupTwoTabRow);
    }

    private static String determineEmployeeId(final PayeeACHAccount payeeAchAccount) {
        return payeeAchAccount.getPayeeIdNumber();
    }

    private String determineAchBankName(final String bankRoutingNumber) {
        final ACHBank bankToUseForName = achBankService.getByPrimaryId(bankRoutingNumber);
        return ObjectUtils.isNull(bankToUseForName) ? KFSConstants.EMPTY_STRING : bankToUseForName.getBankName();
    }

}
