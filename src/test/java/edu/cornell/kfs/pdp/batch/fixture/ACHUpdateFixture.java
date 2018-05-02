package edu.cornell.kfs.pdp.batch.fixture;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;

public enum ACHUpdateFixture {
    NO_UPDATE,
    CREATE_JOHN_DOE_EMPLOYEE_ACCOUNT(PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_EMPLOYEE_NEW),
    CREATE_JOHN_DOE_ENTITY_ACCOUNT(PayeeACHAccountFixture.JOHN_DOE_CHECKING_ACCOUNT_ENTITY_NEW),
    UPDATE_JANE_DOE_EMPLOYEE_ACCOUNT(
            PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_OLD, PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_EMPLOYEE_NEW),
    CREATE_JANE_DOE_ENTITY_ACCOUNT(PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_NEW),
    CREATE_JANE_DOE_ENTITY_ALT_ACCOUNT(PayeeACHAccountFixture.JANE_DOE_SAVINGS_ACCOUNT_ENTITY_ALT_NEW),
    CREATE_ROBERT_SMITH_EMPLOYEE_ACCOUNT(PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_EMPLOYEE_NEW),
    CREATE_ROBERT_SMITH_EMPLOYEE_ALT_ACCOUNT(PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_EMPLOYEE_ALT_NEW),
    UPDATE_ROBERT_SMITH_ENTITY_ACCOUNT(
            PayeeACHAccountFixture.ROBERT_SMITH_CHECKING_ACCOUNT_ENTITY_OLD, PayeeACHAccountFixture.ROBERT_SMITH_SAVINGS_ACCOUNT_ENTITY_NEW),
    UPDATE_MARY_SMITH_EMPLOYEE_ACCOUNT(
            PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_OLD, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_EMPLOYEE_NEW),
    UPDATE_MARY_SMITH_ENTITY_ACCOUNT(
            PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_OLD, PayeeACHAccountFixture.MARY_SMITH_CHECKING_ACCOUNT_ENTITY_NEW);

    public final PayeeACHAccountFixture oldAccount;
    public final PayeeACHAccountFixture newAccount;

    private ACHUpdateFixture() {
        this.oldAccount = null;
        this.newAccount = null;
    }

    private ACHUpdateFixture(PayeeACHAccountFixture newAccount) {
        this(null, newAccount);
    }

    private ACHUpdateFixture(PayeeACHAccountFixture oldAccount, PayeeACHAccountFixture newAccount) {
        this.oldAccount = oldAccount;
        this.newAccount = newAccount;
    }

    public String getExpectedMaintenanceAction() {
        if (newAccount == null) {
            return StringUtils.EMPTY;
        } else if (oldAccount == null) {
            return KFSConstants.MAINTENANCE_NEW_ACTION;
        } else {
            return KFSConstants.MAINTENANCE_EDIT_ACTION;
        }
    }

}
