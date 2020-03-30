package edu.cornell.kfs.fp.batch.xml.fixture;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum CuDisbursementVoucherDocumentFixture {
    EMPTY(),
    JANE_DOE_DV_DETAIL("DISB", "Doe, Jane", "X", "E", 50, "Freeville", "Jane Doe", 25, "7/24/2018",
            buildExpenseFixtureArray(CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture.DELTA,
                    CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture.OTHER_LODGING),
            buildExpenseFixtureArray(CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture.PREPAID_AVIS,
                    CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture.PREPAID_OTHER)),
    JOHN_DOE_DV_DETAIL("DISB", "Doe, John", "X", "E", 50, "Freeville", "Jane Doe", 25, StringUtils.EMPTY,
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
    public final Date dueDate;
    public final List<CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture> nonEmployeeTravelerExpense;
    public final List<CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture> nonEmployeeTravelerPrepaidExpense;
    
    private CuDisbursementVoucherDocumentFixture() {
        this.bankCode = StringUtils.EMPTY;
        this.contactName = StringUtils.EMPTY;
        this.paymentReasonCode = StringUtils.EMPTY;
        this.payeeTypeCode = StringUtils.EMPTY;
        this.perdiemRate = KualiDecimal.ZERO;
        this.conferenceDestination = StringUtils.EMPTY;
        this.nonEmployeeTravelerName = StringUtils.EMPTY;
        this.nonEmployeeCarMileage = new Integer(0);
        this.nonEmployeeTravelerExpense = new ArrayList<CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture>();
        this.nonEmployeeTravelerPrepaidExpense = new ArrayList<CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture>();
        this.dueDate = calculateDefaultDueDate();
    }
    
    private CuDisbursementVoucherDocumentFixture(String bankCode, String contactName, String paymentReasonCode, String payeeTypeCode, double perdiemRate, String conferenceDestination, 
            String nonEmployeeTravelerName, Integer nonEmployeeCarMileage, String dueDateString, CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture[] nonEmployeeTravelerExpenseArray,
            CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture[] nonEmployeeTravelerPrepaidExpenseArray) {
        this.bankCode = bankCode;
        this.contactName = contactName;
        this.paymentReasonCode = paymentReasonCode;
        this.payeeTypeCode = payeeTypeCode;
        this.perdiemRate = new KualiDecimal(perdiemRate);
        this.conferenceDestination = conferenceDestination;
        this.nonEmployeeTravelerName = nonEmployeeTravelerName;
        this.nonEmployeeCarMileage = nonEmployeeCarMileage;
        this.nonEmployeeTravelerExpense = XmlDocumentFixtureUtils.toImmutableList(nonEmployeeTravelerExpenseArray);
        this.nonEmployeeTravelerPrepaidExpense = XmlDocumentFixtureUtils.toImmutableList(nonEmployeeTravelerPrepaidExpenseArray);
        if (StringUtils.isNotEmpty(dueDateString)) {
            this.dueDate = new Date(StringToJavaDateAdapter.parseToDateTime(dueDateString).getMillis());
        } else {
            this.dueDate = calculateDefaultDueDate();
        }
    }
    
    private static Date calculateDefaultDueDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);;
        return new Date(cal.getTimeInMillis());
    }
    
    private static CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture[] buildExpenseFixtureArray(CuDisbursementVoucherDocumentNonEmployeeTravelExpenseFixture... fixtures) {
        return fixtures;
    }
}
