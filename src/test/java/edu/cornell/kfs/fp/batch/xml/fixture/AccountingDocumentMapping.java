package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.kuali.kfs.fp.document.AuxiliaryVoucherDocument;
import org.kuali.kfs.fp.document.InternalBillingDocument;
import org.kuali.kfs.fp.document.ServiceBillingDocument;
import org.kuali.kfs.fp.document.TransferOfFundsDocument;
import org.kuali.kfs.fp.document.YearEndBudgetAdjustmentDocument;
import org.kuali.kfs.fp.document.YearEndDistributionOfIncomeAndExpenseDocument;
import org.kuali.kfs.fp.document.YearEndTransferOfFundsDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.fp.CuFPTestConstants;
import edu.cornell.kfs.fp.batch.service.impl.AccountingDocumentGeneratorBase;
import edu.cornell.kfs.fp.batch.service.impl.AuxiliaryVoucherDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.impl.CuBudgetAdjustmentDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.impl.CuDisbursementVoucherDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.impl.CuDistributionOfIncomeAndExpenseDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.impl.CuYearEndBudgetAdjustmentDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.impl.CuYearEndDistributionOfIncomeAndExpenseDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.impl.CuYearEndTransferOfFundsDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.impl.InternalBillingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.impl.ServiceBillingDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.impl.TransferOfFundsDocumentGenerator;
import edu.cornell.kfs.fp.businessobject.TestBudgetAdjustmentSourceAccountingLine;
import edu.cornell.kfs.fp.businessobject.TestBudgetAdjustmentTargetAccountingLine;
import edu.cornell.kfs.fp.document.CuBudgetAdjustmentDocument;
import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.document.CuDistributionOfIncomeAndExpenseDocument;
import edu.cornell.kfs.sys.businessobject.TestSourceAccountingLine;
import edu.cornell.kfs.sys.businessobject.TestTargetAccountingLine;

@SuppressWarnings("deprecation")
public enum AccountingDocumentMapping {
    DI_DOCUMENT(KFSConstants.FinancialDocumentTypeCodes.DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            CuDistributionOfIncomeAndExpenseDocument.class, TestSourceAccountingLine.class, TestTargetAccountingLine.class,
            CuDistributionOfIncomeAndExpenseDocumentGenerator::new),
    IB_DOCUMENT(KFSConstants.FinancialDocumentTypeCodes.INTERNAL_BILLING,
            InternalBillingDocument.class, TestSourceAccountingLine.class, TestTargetAccountingLine.class,
            InternalBillingDocumentGenerator::new),
    TF_DOCUMENT(KFSConstants.TRANSFER_FUNDS,
            TransferOfFundsDocument.class, TestSourceAccountingLine.class, TestTargetAccountingLine.class,
            TransferOfFundsDocumentGenerator::new),
    BA_DOCUMENT(CuFPTestConstants.BUDGET_ADJUSTMENT_DOC_TYPE,
            CuBudgetAdjustmentDocument.class, TestBudgetAdjustmentSourceAccountingLine.class, TestBudgetAdjustmentTargetAccountingLine.class,
            CuBudgetAdjustmentDocumentGenerator::new),
    SB_DOCUMENT(KFSConstants.FinancialDocumentTypeCodes.SERVICE_BILLING,
            ServiceBillingDocument.class, TestSourceAccountingLine.class, TestTargetAccountingLine.class,
            ServiceBillingDocumentGenerator::new),
    DV_DOCUMENT(CuFPTestConstants.DISBURSEMENT_VOUCHER_DOC_TYPE,
            CuDisbursementVoucherDocument.class, TestSourceAccountingLine.class, TestTargetAccountingLine.class,
            CuDisbursementVoucherDocumentGenerator::new),
    YEDI_DOCUMENT(KFSConstants.FinancialDocumentTypeCodes.YEAR_END_DISTRIBUTION_OF_INCOME_AND_EXPENSE,
            YearEndDistributionOfIncomeAndExpenseDocument.class, TestSourceAccountingLine.class, TestTargetAccountingLine.class,
            CuYearEndDistributionOfIncomeAndExpenseDocumentGenerator::new),
    YEBA_DOCUMENT(CuFPTestConstants.YEAR_END_BUDGET_ADJUSTMENT_DOC_TYPE,
            YearEndBudgetAdjustmentDocument.class, TestBudgetAdjustmentSourceAccountingLine.class, TestBudgetAdjustmentTargetAccountingLine.class,
            CuYearEndBudgetAdjustmentDocumentGenerator::new),
    YETF_DOCUMENT(CuFPTestConstants.YEAR_END_TRANSFER_OF_FUNDS_DOC_TYPE,
            YearEndTransferOfFundsDocument.class, TestSourceAccountingLine.class, TestTargetAccountingLine.class,
            CuYearEndTransferOfFundsDocumentGenerator::new),
    AV_DOCUMENT(CuFPTestConstants.AUXILIARY_VOUCHER_DOC_TYPE,
            AuxiliaryVoucherDocument.class, TestSourceAccountingLine.class, TestTargetAccountingLine.class,
            AuxiliaryVoucherDocumentGenerator::new);

    public static final String MAPPING_ENUM_CONST_SUFFIX = "_DOCUMENT";

    public final String documentTypeName;
    public final Class<? extends AccountingDocument> documentClass;
    public final Class<? extends SourceAccountingLine> sourceAccountingLineClass;
    public final Class<? extends TargetAccountingLine> targetAccountingLineClass;
    public final BiFunction<Supplier<Note>, Supplier<AdHocRoutePerson>, AccountingDocumentGeneratorBase<?>> generatorConstructor;

    private AccountingDocumentMapping(String documentTypeName, Class<? extends AccountingDocument> documentClass,
            Class<? extends SourceAccountingLine> sourceAccountingLineClass, Class<? extends TargetAccountingLine> targetAccountingLineClass,
            BiFunction<Supplier<Note>, Supplier<AdHocRoutePerson>, AccountingDocumentGeneratorBase<?>> generatorConstructor) {
        this.documentTypeName = documentTypeName;
        this.documentClass = documentClass;
        this.sourceAccountingLineClass = sourceAccountingLineClass;
        this.targetAccountingLineClass = targetAccountingLineClass;
        this.generatorConstructor = generatorConstructor;
    }

    public BiFunction<Supplier<Note>, Supplier<AdHocRoutePerson>, AccountingDocumentGeneratorBase<?>> getGeneratorConstructor() {
        return generatorConstructor;
    }

    public static Optional<AccountingDocumentMapping> getMappingByDocumentType(String documentTypeName) {
        try {
            return Optional.of(AccountingDocumentMapping.valueOf(documentTypeName + MAPPING_ENUM_CONST_SUFFIX));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static Optional<AccountingDocumentMapping> getMappingByDocumentClass(Class<? extends Document> documentClass) {
        return Arrays.stream(AccountingDocumentMapping.values())
                .filter((mapping) -> mapping.documentClass.equals(documentClass))
                .findFirst();
    }

}
