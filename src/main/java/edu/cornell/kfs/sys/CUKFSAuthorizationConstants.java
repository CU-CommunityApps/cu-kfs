package edu.cornell.kfs.sys;

import org.kuali.kfs.sys.KfsAuthorizationConstants.TransactionalEditMode;

public class CUKFSAuthorizationConstants  {

    public static class DisbursementVoucherEditMode extends TransactionalEditMode {
        public static final String TRAVEL_SYSTEM_GENERATED_ENTRY = "travelSystemGeneratedEntry";

    }
    
    public static class AdvanceDepositEditMode extends TransactionalEditMode {
        public static final String EDITABLE_ADVANCE_DEPOSITS = "editableAdvanceDeposits";

    }
}
