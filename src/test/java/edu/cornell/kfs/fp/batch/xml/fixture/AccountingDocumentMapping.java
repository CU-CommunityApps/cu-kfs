package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.kuali.kfs.fp.document.InternalBillingDocument;
import org.kuali.kfs.krad.bo.AdHocRoutePerson;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.businessobject.TargetAccountingLine;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.fp.batch.service.impl.AccountingDocumentGeneratorBase;
import edu.cornell.kfs.fp.batch.service.impl.CuDistributionOfIncomeAndExpenseDocumentGenerator;
import edu.cornell.kfs.fp.batch.service.impl.InternalBillingDocumentGenerator;
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
            InternalBillingDocumentGenerator::new);

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

    public static Optional<AccountingDocumentMapping> getMappingByDocumentType(String documentTypeName) {
        try {
            return Optional.of(AccountingDocumentMapping.valueOf(documentTypeName + MAPPING_ENUM_CONST_SUFFIX));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static Optional<AccountingDocumentMapping> getMappingByDocumentClass(Class<? extends Document> documentClass) {
        return Arrays.stream(AccountingDocumentMapping.values())
                .filter((mapping) -> mapping.documentClass.isAssignableFrom(documentClass))
                .findFirst();
    }

    public static Stream<BiFunction<Supplier<Note>, Supplier<AdHocRoutePerson>, AccountingDocumentGeneratorBase<?>>>
            getGeneratorConstructorsAsStream() {
        return Arrays.stream(AccountingDocumentMapping.values())
                .map((mapping) -> mapping.generatorConstructor);
    }

}
