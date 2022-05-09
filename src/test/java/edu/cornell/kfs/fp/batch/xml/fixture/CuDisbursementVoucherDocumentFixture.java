package edu.cornell.kfs.fp.batch.xml.fixture;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.batch.service.impl.fixture.DocGenVendorFixture;
import edu.cornell.kfs.sys.fixture.XmlDocumentFixtureUtils;
import edu.cornell.kfs.sys.util.fixture.TestUserFixture;
import edu.cornell.kfs.sys.xmladapters.StringToJavaDateAdapter;

public enum CuDisbursementVoucherDocumentFixture {
    EMPTY(),
    JANE_DOE_DV_DETAIL("DISB", "Doe, Jane", "X", "E", 50, "Freeville", "Jane Doe", 25, "7/24/2018", StringUtils.EMPTY, null,
            TestUserFixture.TEST_USER),
    JOHN_DOE_DV_DETAIL("DISB", "Doe, John", "X", "E", 50, "Freeville", "John Doe", 25, StringUtils.EMPTY, "04/15/2020", "12321",
            TestUserFixture.TEST_USER),
    XYZ_INDUSTRIES_DV_DETAIL("DISB", "Wyzee, Eks", "R", "V", -1, null, null, null, "12/28/2020", null, null,
            DocGenVendorFixture.XYZ_INDUSTRIES),
    REE_PHUND_DV_DETAIL("DISB", "Smith, Jack", "F", "V", -1, null, null, null, "12/29/2020", "12/22/2020", "123123",
            DocGenVendorFixture.REE_PHUND);
    
    public final String bankCode;
    public final String contactName;
    public final String paymentReasonCode;
    public final String payeeTypeCode;
    public final KualiDecimal perdiemRate;
    public final String conferenceDestination;
    public final String nonEmployeeTravelerName;
    public final Integer nonEmployeeCarMileage;
    public final DateTime dueDate;
    public final DateTime invoiceDate;
    public final String invoiceNumber;
    public final Enum<?> payee;
    
    private CuDisbursementVoucherDocumentFixture() {
        this.bankCode = StringUtils.EMPTY;
        this.contactName = StringUtils.EMPTY;
        this.paymentReasonCode = StringUtils.EMPTY;
        this.payeeTypeCode = StringUtils.EMPTY;
        this.perdiemRate = KualiDecimal.ZERO;
        this.conferenceDestination = StringUtils.EMPTY;
        this.nonEmployeeTravelerName = StringUtils.EMPTY;
        this.nonEmployeeCarMileage = new Integer(0);
        this.dueDate = calculateDefaultDueDate();
        this.invoiceDate = null;
        this.invoiceNumber = StringUtils.EMPTY;
        this.payee = null;
    }
    
    private CuDisbursementVoucherDocumentFixture(String bankCode, String contactName, String paymentReasonCode, String payeeTypeCode, double perdiemRate, 
            String conferenceDestination,  String nonEmployeeTravelerName, Integer nonEmployeeCarMileage, String dueDateString, String invoiceDateString, 
            String invoiceNumber, Enum<?> payee) {
        this.bankCode = bankCode;
        this.contactName = contactName;
        this.paymentReasonCode = paymentReasonCode;
        this.payeeTypeCode = payeeTypeCode;
        this.perdiemRate = perdiemRate >= 0 ? new KualiDecimal(perdiemRate) : null;
        this.conferenceDestination = conferenceDestination;
        this.nonEmployeeTravelerName = nonEmployeeTravelerName;
        this.nonEmployeeCarMileage = nonEmployeeCarMileage;
        this.payee = payee;
        if (StringUtils.isNotEmpty(dueDateString)) {
            this.dueDate = new DateTime(StringToJavaDateAdapter.parseToDateTime(dueDateString).getMillis());
        } else {
            this.dueDate = calculateDefaultDueDate();
        }
        this.invoiceNumber = invoiceNumber;
        if (StringUtils.isNotEmpty(invoiceDateString)) {
            this.invoiceDate = new DateTime(StringToJavaDateAdapter.parseToDateTime(invoiceDateString).getMillis());
        } else {
            this.invoiceDate = null;
        }
    }
    
    public String getMappedPayeeTypeCode() {
        if (payee != null) {
            if (payee instanceof TestUserFixture) {
                return payeeTypeCode;
            } else if (payee instanceof DocGenVendorFixture) {
                return ((DocGenVendorFixture) payee).payeeTypeCode;
            }
        }
        return null;
    }
    
    private static DateTime calculateDefaultDueDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, 1);
        return new DateTime(cal.getTimeInMillis());
    }
}
