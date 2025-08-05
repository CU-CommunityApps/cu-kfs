package edu.cornell.kfs.fp.businessobject;

import java.text.MessageFormat;

import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLineParser;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.sec.document.authorization.SecAccountingLineAuthorizer;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;
import org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizer;
import org.kuali.kfs.sys.document.authorization.AccountingLineAuthorizerBase;
import org.kuali.kfs.sys.document.datadictionary.AccountingLineGroupDefinition;
import org.kuali.kfs.sys.document.datadictionary.FinancialSystemTransactionalDocumentEntry;
import org.kuali.kfs.sys.exception.AccountingLineParserException;

import edu.cornell.kfs.sys.CUKFSKeyConstants;

public class CuBudgetAdjustmentAccountingLineParser extends BudgetAdjustmentAccountingLineParser {

    private DataDictionaryService dataDictionaryService;
    private ConfigurationService configurationService;
    private FinancialSystemTransactionalDocumentEntry documentEntry;

    @Override
    public SourceAccountingLine parseSourceAccountingLine(final AccountingDocument transactionalDocument,
            final String sourceAccountingLineString) {
        final SourceAccountingLine line = super.parseSourceAccountingLine(
                transactionalDocument, sourceAccountingLineString);
        validateBaseAmountIsZeroOrEditable(line, transactionalDocument, sourceAccountingLineString);
        return line;
    }

    @Override
    public TargetAccountingLine parseTargetAccountingLine(final AccountingDocument transactionalDocument,
            final String targetAccountingLineString) {
        final TargetAccountingLine line = super.parseTargetAccountingLine(
                transactionalDocument, targetAccountingLineString);
        validateBaseAmountIsZeroOrEditable(line, transactionalDocument, targetAccountingLineString);
        return line;
    }

    private void validateBaseAmountIsZeroOrEditable(final AccountingLine line,
            final AccountingDocument transactionalDocument, final String accountingLineAsString) {
        final KualiInteger baseAmount = ((BudgetAdjustmentAccountingLine) line).getBaseBudgetAdjustmentAmount();
        if (baseAmount == null || baseAmount.isZero()) {
            return;
        }

        final AccountingLineAuthorizerBase accountingLineAuthorizer = getAccountingLineAuthorizer(
                line, transactionalDocument);
        final String collectionProperty = line.isSourceAccountingLine()
                ? KFSPropertyConstants.SOURCE_ACCOUNTING_LINES : KFSPropertyConstants.TARGET_ACCOUNTING_LINES;
        if (!accountingLineAuthorizer.determineEditPermissionOnField(
                transactionalDocument, line, collectionProperty,
                KFSPropertyConstants.BASE_BUDGET_ADJUSTMENT_AMOUNT, true)) {
            final String principalName = GlobalVariables.getUserSession().getPrincipalName();
            final String baseMessage = getConfigurationService().getPropertyValueAsString(
                    CUKFSKeyConstants.ERROR_ACCOUNTING_LINE_PARSER_INVALID_BASE_AMOUNT);
            final String errorMessage = MessageFormat.format(baseMessage, principalName, accountingLineAsString);
            throw new AccountingLineParserException(errorMessage, KFSKeyConstants.ERROR_CUSTOM, errorMessage);
        }
    }

    private AccountingLineAuthorizerBase getAccountingLineAuthorizer(final AccountingLine line,
            final AccountingDocument document) {
        final String lineGroupName = line.isSourceAccountingLine()
                ? KFSConstants.SOURCE_ACCOUNTING_LINES_GROUP_NAME : KFSConstants.TARGET_ACCOUNTING_LINES_GROUP_NAME;
        final FinancialSystemTransactionalDocumentEntry documentEntry = getDocumentEntry(document);
        final AccountingLineGroupDefinition lineGroup = documentEntry.getAccountingLineGroups().get(lineGroupName);
        AccountingLineAuthorizer accountingLineAuthorizer = lineGroup.getAccountingLineAuthorizer();
        if (accountingLineAuthorizer instanceof SecAccountingLineAuthorizer) {
            accountingLineAuthorizer = ((SecAccountingLineAuthorizer) accountingLineAuthorizer).getLineAuthorizer();
        }
        return (AccountingLineAuthorizerBase) accountingLineAuthorizer;
    }

    private FinancialSystemTransactionalDocumentEntry getDocumentEntry(final AccountingDocument document) {
        if (documentEntry == null) {
            documentEntry = (FinancialSystemTransactionalDocumentEntry)
                    getDataDictionaryService().getDictionaryObjectEntry(document.getClass().getName());
        }
        return documentEntry;
    }

    private DataDictionaryService getDataDictionaryService() {
        if (dataDictionaryService == null) {
            dataDictionaryService = SpringContext.getBean(DataDictionaryService.class);
        }
        return dataDictionaryService;
    }

    private ConfigurationService getConfigurationService() {
        if (configurationService == null) {
            configurationService = SpringContext.getBean(ConfigurationService.class);
        }
        return configurationService;
    }

}
