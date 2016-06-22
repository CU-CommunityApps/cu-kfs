package edu.cornell.kfs.fp;

import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

import edu.cornell.kfs.sys.CUKFSConstants.PreEncumbranceSourceAccountingLineConstants;

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
    public static final String INTERDEPARTMENTAL_PAYMENT = "interdepartmentalPayment";
    public static final String DISPLAY_ON_DV_DOCUMENT = "displayOnDisbursementVoucherDocument";
    public static final String DISPLAY_ON_VENDOR_DOCUMENT = "displayOnVendorDocument";

    public static final String CURRENCY_CODE_U = "U";
    public static final String CURRENCY_CODE_C = "C";
    public static final String CURRENCY_CODE_F = "F";

    public static final String CURRENCY_US_DOLLAR = "U.S. Dollars";
    public static final String CURRENCY_US_DOLLAR_TO_FOREIGN = "Amount is stated in U.S. dollars; convert to foreign currency";
    public static final String CURRENCY_FOREIGN = "Amount is stated in foreign currency";

    public static final String PAYMENT_REASON = "payment reason";
    public static final String NON_EMPLOYEE_TRAVEL = "Non-Employee Travel";

    public static final String ACH_INCOME_FILE_DATE_FORMAT = "yyMMddHHmm";
    public static final String ADVANCE_DEPOSIT_DEFAULT_CAMPUS_CODE = "IT";
    public static final String ADVANCE_DEPOSIT_NOTE_FILE_PREFIX = "advancedeposit_note_";
    public static final String BATCH_DETAIL_TYPE = "Batch";

    public static final String USE_OVERSIZED_CHECK_STUB_TEXT_QUESTION_ID = "UseOversizedCheckStubTextQuestion";

    public static class AchIncomeFileGroup {
        public static final String GROUP_FUNCTIONAL_IDENTIFIER_CD_RA = "RA";
    }

    public static class AchIncomeFileTransaction {
        public static final String TRANS_PAYMENT_METHOD_ACH = "ACH";
        public static final String TRANS_PAYMENT_METHOD_FWT = "FWT";
        public static final String PAYER_NOT_IDENTIFIED = "PAYER NOT IDENTIFIED";
    }

    public static class AchIncomeFileTransactionOpenItemReference {
        public static final String RMR_REFERENCE_TYPE_CR = "CR";
        public static final String RMR_REFERENCE_TYPE_IV = "IV";
        public static final String RMR_REFERENCE_TYPE_OI = "OI";
        public static final int RMR_NOTE_LINE_COLUMN_WIDTH = 8;
        public static final int RMR_NOTE_INVOICE_NUMBER_COLUMN_WIDTH = 22;
        public static final int RMR_NOTE_NET_AMOUNT_COLUMN_WIDTH = 20;
        public static final int RMR_NOTE_INVOICE_AMOUNT_COLUMN_WIDTH = 20;
    }

    public static class AchIncomeFileTransactionReference {
        public static final String REF_REFERENCE_TYPE_TN = "TN";
        public static final String REF_REFERENCE_TYPE_CT = "CT";
        public static final String REF_REFERENCE_TYPE_VV = "VV";
    }

    public static class AchIncomeFileTransactionDateTime {
        public static final String DTM_DATE_TYPE_097 = "097";
    }

    public static class AchIncomeFileTransactionPayerOrPayeeName {
        public static final int ACH_TRN_PAYER_NM_DB_SIZE = 40;
        public static final String PAYER_TYPE_PE = "PE";
        public static final String PAYER_TYPE_PR = "PR";
    }
    
    public static class ScheduledSourceAccountingLineConstants extends PreEncumbranceSourceAccountingLineConstants {
    	public static final String SCHEDULE_TYPE = "scheduleType";
    	
    	public enum ScheduleTypes {
    		DAILY("Daily", Calendar.DATE, 1),
    		WEEKLY("Weekly", Calendar.DATE, 7),
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
    	public static final String RECURRING_DV_PAYMENT_REASON_FILTER_PARAMETER_NAME = "recurringDVPaynmentReasonFilter";
    }
    
    public enum PermissionNames {
    	RECURRING_DV_PERMISSION(KFSConstants.CoreModuleNamespaces.FINANCIAL, "USE RECURRING DV");
    	public final String name;
        public final String namespace;
    	private PermissionNames(String namespace, String name) {
            this.namespace = namespace;
            this.name = name;
        }
    }

}
