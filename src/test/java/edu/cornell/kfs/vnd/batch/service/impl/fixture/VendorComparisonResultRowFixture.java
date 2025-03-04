package edu.cornell.kfs.vnd.batch.service.impl.fixture;

import org.kuali.kfs.sys.KFSConstants.OptionLabels;
import org.kuali.kfs.vnd.VendorConstants.VendorTypes;

import edu.cornell.kfs.vnd.batch.service.impl.annotation.VendorComparisonResultRow;

public enum VendorComparisonResultRowFixture {

    @VendorComparisonResultRow(
            vendorId = "12345-0", employeeId = "5544332", netId = "jd111", active = OptionLabels.YES,
            hireDate = "2023-01-01", terminationDate = "",  terminationDateGreaterThanProcessingDate = "",
            vendorType = VendorTypes.DISBURSEMENT_VOUCHER
    )
    JOHN_DOE,

    @VendorComparisonResultRow(
            vendorId = "7777-0", employeeId = "9876543", netId = "jqd58", active = OptionLabels.YES,
            hireDate = "2024-04-01", terminationDate = "2023-06-30",  terminationDateGreaterThanProcessingDate = "",
            vendorType = VendorTypes.DISBURSEMENT_VOUCHER
    )
    JANE_DOE,

    @VendorComparisonResultRow(
            vendorId = "23232-0", employeeId = "1357531", netId = "mms223", active = OptionLabels.NO,
            hireDate = "2021-12-15", terminationDate = "2023-10-22",  terminationDateGreaterThanProcessingDate = "",
            vendorType = VendorTypes.REFUND_PAYMENT
    )
    MARY_SMITH,

    @VendorComparisonResultRow(
            vendorId = "8644-0", employeeId = "8648648", netId = "dls99", active = "1",
            hireDate = "2022-09-21", terminationDate = "",  terminationDateGreaterThanProcessingDate = "2024-10-31",
            vendorType = VendorTypes.PURCHASE_ORDER
    )
    DAN_SMITH,

    @VendorComparisonResultRow(
            vendorId = "99911-0", employeeId = "8888888", netId = "jjj33", active = "0",
            hireDate = "2020-02-29", terminationDate = "2024-04-01",  terminationDateGreaterThanProcessingDate = "",
            vendorType = ""
    )
    JACK_JONES,

    @VendorComparisonResultRow(
            vendorId = "", employeeId = "", netId = "", active = "",
            hireDate = "", terminationDate = "",  terminationDateGreaterThanProcessingDate = "",
            vendorType=""
    )
    EMPTY_ROW,

    @VendorComparisonResultRow(
            vendorId = "12345-0", employeeId = "5544332", netId = "jd111", active = "Yeah",
            hireDate = "2023-01-01", terminationDate = "",  terminationDateGreaterThanProcessingDate = "",
            vendorType = VendorTypes.DISBURSEMENT_VOUCHER
    )
    JOHN_DOE_INVALID_ACTIVE_FLAG,

    @VendorComparisonResultRow(
            vendorId = "12345-0", employeeId = "5544332", netId = "jd111", active = OptionLabels.YES,
            hireDate = "2023:01:01", terminationDate = "",  terminationDateGreaterThanProcessingDate = "",
            vendorType = VendorTypes.DISBURSEMENT_VOUCHER
    )
    JOHN_DOE_MALFORMED_HIRE_DATE,

    @VendorComparisonResultRow(
            vendorId = "12345-0", employeeId = "5544332", netId = "jd111", active = OptionLabels.YES,
            hireDate = "2023-01-01", terminationDate = "04042024",  terminationDateGreaterThanProcessingDate = "",
            vendorType = VendorTypes.DISBURSEMENT_VOUCHER
    )
    JOHN_DOE_MALFORMED_TERMINATION_DATE,

    @VendorComparisonResultRow(
            vendorId = "12345-0", employeeId = "5544332", netId = "jd111", active = OptionLabels.YES,
            hireDate = "2023-01-01", terminationDate = "",  terminationDateGreaterThanProcessingDate = "04/04/2024",
            vendorType = VendorTypes.DISBURSEMENT_VOUCHER
    )
    JOHN_DOE_MALFORMED_PENDING_TERMINATION_DATE;

}
