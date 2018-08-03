package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public enum CuDisbursementVoucherDocumentFixture {
    EMPTY(),
    JANE_DOE_DV_DETAIL("DISB", "Doe, Jane", "X", "E", 50, "Freeville", "Jane Doe", 25,
            buildExpenseFixtureArray(CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture.DELTA,
                    CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture.OTHER_LODGING),
            buildExpenseFixtureArray(CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture.PREPAID_AVIS,
                    CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture.PREPAID_OTHER));
    
    public final String bankCode;
    public final String contactName;
    public final String paymentReasonCode;
    public final String payeeTypeCode;
    public final KualiDecimal perdiemRate;
    public final String conferenceDestination;
    public final String nonEmployeeTravelerName;
    public final Integer nonEmployeeCarMileage;
    public final List<CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture> nonEmployeeTrevelerExpense;
    public final List<CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture> nonEmployeeTrevelerPrepaidExpense;
    
    private CuDisbursementVoucherDocumentFixture() {
        this.bankCode = StringUtils.EMPTY;
        this.contactName = StringUtils.EMPTY;
        this.paymentReasonCode = StringUtils.EMPTY;
        this.payeeTypeCode = StringUtils.EMPTY;
        this.perdiemRate = KualiDecimal.ZERO;
        this.conferenceDestination = StringUtils.EMPTY;
        this.nonEmployeeTravelerName = StringUtils.EMPTY;
        this.nonEmployeeCarMileage = new Integer(0);
        this.nonEmployeeTrevelerExpense = new ArrayList<CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture>();
        this.nonEmployeeTrevelerPrepaidExpense = new ArrayList<CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture>();
    }
    
    private CuDisbursementVoucherDocumentFixture(String bankCode, String contactName, String paymentReasonCode, String payeeTypeCode, double perdiemRate, String conferenceDestination, 
            String nonEmployeeTravelerName, Integer nonEmployeeCarMileage, CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture[] nonEmployeeTrevelerExpenseArray,
            CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture[] nonEmployeeTrevelerPrepaidExpenseArray) {
        this.bankCode = bankCode;
        this.contactName = contactName;
        this.paymentReasonCode = paymentReasonCode;
        this.payeeTypeCode = payeeTypeCode;
        this.perdiemRate = new KualiDecimal(perdiemRate);
        this.conferenceDestination = conferenceDestination;
        this.nonEmployeeTravelerName = nonEmployeeTravelerName;
        this.nonEmployeeCarMileage = nonEmployeeCarMileage;
        this.nonEmployeeTrevelerExpense = AccountingXmlDocumentFixtureUtils.toImmutableList(nonEmployeeTrevelerExpenseArray);
        this.nonEmployeeTrevelerPrepaidExpense = AccountingXmlDocumentFixtureUtils.toImmutableList(nonEmployeeTrevelerPrepaidExpenseArray);
    }
    
    private static CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture[] buildExpenseFixtureArray(CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture... fixtures) {
        return fixtures;
    }
}
