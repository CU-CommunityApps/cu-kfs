package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.function.BiConsumer;

import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;

public enum MonthFixture {
    MONTH01(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth1LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth01Amount),
    MONTH02(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth2LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth02Amount),
    MONTH03(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth3LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth03Amount),
    MONTH04(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth4LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth04Amount),
    MONTH05(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth5LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth05Amount),
    MONTH06(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth6LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth06Amount),
    MONTH07(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth7LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth07Amount),
    MONTH08(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth8LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth08Amount),
    MONTH09(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth9LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth09Amount),
    MONTH10(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth10LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth10Amount),
    MONTH11(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth11LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth11Amount),
    MONTH12(BudgetAdjustmentAccountingLine::setFinancialDocumentMonth12LineAmount,
            AccountingXmlDocumentAccountingLine::setMonth12Amount);

    public final BiConsumer<BudgetAdjustmentAccountingLine, KualiDecimal> accountPropertySetter;
    public final BiConsumer<AccountingXmlDocumentAccountingLine, KualiDecimal> xmlPojoPropertySetter;

    private MonthFixture(BiConsumer<BudgetAdjustmentAccountingLine, KualiDecimal> accountPropertySetter,
            BiConsumer<AccountingXmlDocumentAccountingLine, KualiDecimal> xmlPojoPropertySetter) {
        this.accountPropertySetter = accountPropertySetter;
        this.xmlPojoPropertySetter = xmlPojoPropertySetter;
    }

}
