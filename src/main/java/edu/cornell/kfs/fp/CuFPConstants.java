package edu.cornell.kfs.fp;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;

/**
 Copyright Cornell University
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 Portions Modified 04/2016 and Copyright Indiana University
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
public class CuFPConstants {
    public static final String ACTIVE = "active";
    public static final String YES = "Y";
    public static final String NO = "N";
    public static final String OPEN = "OPEN";
    public static final String INTERDEPARTMENTAL_PAYMENT = "interdepartmentalPayment";
    public static final String DISPLAY_ON_DV_DOCUMENT = "displayOnDisbursementVoucherDocument";

    public static final String CURRENCY_CODE_U = "U";
    public static final String CURRENCY_CODE_C = "C";
    public static final String CURRENCY_CODE_F = "F";

    public static final String CURRENCY_US_DOLLAR = "U.S. Dollars";
    public static final String CURRENCY_US_DOLLAR_TO_FOREIGN = "Amount is stated in U.S. dollars; convert to foreign currency";
    public static final String CURRENCY_FOREIGN = "Amount is stated in foreign currency";

    public static final String BATCH_DETAIL_TYPE = "Batch";
    public static final String ENCUMBRANCE_AUTOMATIC_PARTIAL_DIS_ENCUMBRANCES_TITLE = "Encumbrance Automatic Partial Dis-Encumbrances";
    public static final String GENERAL_LEDGER_PENDING_ENTRIES_TITLE = "General Ledger Pending Entries";
    
    public static final String CORPORATE_BILLED_CORPORATE_PAID_DOCUMENT_TYPE_CODE = "CBCP";
    public static final String CORPORATE_BILLED_CORPORATE_PAID_FLAT_INPUT_FILE_TYPE = "corporateBilledCorporatePaidFlatInputFileType";
    
    public static final String XML_FILE_EXTENSION = ".xml";
    public static final String ACCOUNTING_DOCUMENT_GENERATOR_BEAN_PREFIX = "AccountingDocumentGenerator_";
    public static final String ACCOUNTING_DOCUMENT_XML_ROUTE_ANNOTATION = "Generated by Create Accounting Document batch job";
    public static final String ALTERNATE_BASE_VALIDATION_ERROR_MESSAGE = "Validation Failure";

    public static class ScheduledSourceAccountingLineConstants {
        public static final String SCHEDULE_TYPE = "scheduleType";
        public static final String END_DATE = "endDate";
        public static final String START_DATE = "startDate";
        public static final String PARTIAL_TRANSACTION_COUNT = "partialTransactionCount";
        public static final String PARTIAL_AMOUNT = "partialAmount";
    	
    	public enum ScheduleTypes {
    		DAILY("Daily", Calendar.DATE, 1),
    		BIWEEKLY("Bi-Weekly", Calendar.DATE, 14),
    		MONTHLY("Monthly", Calendar.MONTH, 1),
    		YEARLY("Yearly", Calendar.YEAR, 1);
    		
        	public final String name;
        	public final int calendarIncrementorType;
        	public final int calendarIncrementorMutliplier;
        	private ScheduleTypes(String name, int calendarIncrementorType, int calendarIncrementorMutliplier) {
                this.name = name;
                this.calendarIncrementorType = calendarIncrementorType;
                this.calendarIncrementorMutliplier = calendarIncrementorMutliplier;
            }
        	
        	public static ScheduleTypes fromName(String name) {
        		for (ScheduleTypes st : ScheduleTypes.values()) {
        			if (StringUtils.equals(name, st.name)) {
        				return st;
        			}
        		}
        		throw new IllegalArgumentException("invalid schedule type: "  + name);
        	}
    	}
    }

    public static class RecurringDisbursementVoucherDocumentConstants {
        public static final String RECURRING_DV_COMPONENT_NAME = "RecurringDisbursementVoucher";
        public static final String RECURRING_DV_PAYMENT_METHOD_FILTER_PARAMETER_NAME = "RECURRING_DV_PAYMENT_METHOD_FILTER";
        public static final String RECURRING_DV_MAX_FUTURE_DATE = "RECURRING_DV_MAX_FUTURE_DATE";
        public static final String RECURRING_DV_DOCUMENT_TYPE_NAME = "RCDV";
        public static final String RECURRING_DV_EXPLANATION_TO_DV_NOTE_STARTER = "DV created by recurring DV: ";
        public static final String RECURRING_DETAILS_TAB_NAME = "RecurringDetails";
        public static final String RECURRING_DV_CANCEL_PAYMENTS_PERMISSION_NAME = "CANCEL RECURRING DISBURSEMENT VOUCHER PAYMENTS";
        public static final String PDP_PRE_EXTRACTION_STATUS = "Pre-Extraction";
    }
    
    public static final String CREDENTIAL_BASE_URL = "CREDENTIAL_BASE_URL";

    public static class CreateAccountingDocumentValidatedDataElements {
        public static final String ACCOUNT_NBR = "account_nbr";
        public static final String AMOUNT = "amount";
        public static final String COA_CD = "coa_cd";
        public static final String DESCRIPTION = "Description";
        public static final String DOCUMENT_TYPE = "DocumentType";
        public static final String EXPLANATION = "Explanation";
        public static final String INDEX = "Index";
        public static final String OVERVIEW = "Overview";
        public static final String REPORT_EMAIL = "ReportEmail";
        public static final String PAYEE_NAME = "payee_name";
    }

    public static final class CreateAccountingDocumentConstants {
        public static final class FileEntryFieldLengths {
            public static final int FILE_NAME = 250;
            public static final int FILE_OVERVIEW = 250;
            public static final int REPORT_EMAIL_ADDRESS = 200;
        }
    }

    public static final String IS_NOT_TRIP_DOC = "0";
    public static final String IS_TRIP_DOC = "1";

    public static final String DV_CHECK_STUB_FIELD_LABEL = "Check Stub Text";

    public static final String ACCOUNTING_XML_DOCUMENT_FILE_TYPE_IDENTIFIER = "accountingXmlDocumentInputFileType";
    public static final String ACCOUNTING_XML_DOCUMENT_XSD_LOCATION
            = "classpath:edu/cornell/kfs/fp/batch/accountingXmlDocument.xsd";
}
