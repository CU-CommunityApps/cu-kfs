package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.kuali.kfs.fp.businessobject.BudgetAdjustmentAccountingLine;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.core.api.util.type.KualiInteger;

import edu.cornell.kfs.fp.batch.xml.AccountingXmlDocumentAccountingLine;

public enum BudgetAdjustmentAccountDataFixture {
    BASE_0_NO_MONTHS(0),
    NO_BASE_MONTH03_40(monthAmount(MonthFixture.MONTH03, 40.00)),
    BASE_0_MONTH03_40(0, monthAmount(MonthFixture.MONTH03, 40.00)),
    BASE_10_MONTH03_40(10, monthAmount(MonthFixture.MONTH03, 40.00)),
    BASE_0_MONTH01_37_MONTH08_15(0, monthAmount(MonthFixture.MONTH01, 37.00), monthAmount(MonthFixture.MONTH08, 15.00)),
    BASE_0_ALL_MONTHS_4_OR_5(0,
            monthAmount(MonthFixture.MONTH01, 4.00), monthAmount(MonthFixture.MONTH02, 5.00), monthAmount(MonthFixture.MONTH03, 4.00),
            monthAmount(MonthFixture.MONTH04, 4.00), monthAmount(MonthFixture.MONTH05, 5.00), monthAmount(MonthFixture.MONTH06, 4.00),
            monthAmount(MonthFixture.MONTH07, 4.00), monthAmount(MonthFixture.MONTH08, 4.00), monthAmount(MonthFixture.MONTH09, 4.00),
            monthAmount(MonthFixture.MONTH10, 5.00), monthAmount(MonthFixture.MONTH11, 5.00), monthAmount(MonthFixture.MONTH12, 4.00));

    public final KualiInteger baseAmount;
    public final Map<MonthFixture, KualiDecimal> monthAmounts;

    @SafeVarargs
    private BudgetAdjustmentAccountDataFixture(Pair<MonthFixture, KualiDecimal>... monthAmounts) {
        this.baseAmount = null;
        this.monthAmounts = buildMonthAmountsMap(monthAmounts);
    }

    @SafeVarargs
    private BudgetAdjustmentAccountDataFixture(long baseAmount, Pair<MonthFixture, KualiDecimal>... monthAmounts) {
        this.baseAmount = new KualiInteger(baseAmount);
        this.monthAmounts = buildMonthAmountsMap(monthAmounts);
    }

    @SafeVarargs
    private final Map<MonthFixture, KualiDecimal> buildMonthAmountsMap(Pair<MonthFixture, KualiDecimal>... amountPairs) {
        Map<MonthFixture, KualiDecimal> amountsMap = Arrays.stream(amountPairs)
                .collect(Collectors.toMap(
                        Pair::getKey, Pair::getValue, this::doNotAllowMergingOfValuesWithSameKey,
                        () -> new EnumMap<MonthFixture, KualiDecimal>(MonthFixture.class)));
        return Collections.unmodifiableMap(amountsMap);
    }

    private KualiDecimal doNotAllowMergingOfValuesWithSameKey(KualiDecimal value1, KualiDecimal value2) {
        throw new IllegalArgumentException("Month amounts must have distinct keys; merging is not permitted");
    }

    public void configureAccountingLineXmlPojo(AccountingXmlDocumentAccountingLine linePojo) {
        if (baseAmount != null) {
            linePojo.setBaseAmount(baseAmount);
        }
        monthAmounts.forEach((monthFixture, amount) -> {
            monthFixture.xmlPojoPropertySetter.accept(linePojo, amount);
        });
    }

    public void configureBudgetAdjustmentAccountingLine(BudgetAdjustmentAccountingLine accountingLine) {
        if (baseAmount != null) {
            accountingLine.setBaseBudgetAdjustmentAmount(baseAmount);
        }
        monthAmounts.forEach((monthFixture, amount) -> {
            monthFixture.accountPropertySetter.accept(accountingLine, amount);
        });
    }

    private static Pair<MonthFixture, KualiDecimal> monthAmount(MonthFixture month, double amount) {
        return Pair.of(month, new KualiDecimal(amount));
    }

}
