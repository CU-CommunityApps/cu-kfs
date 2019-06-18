package edu.cornell.kfs.fp.batch.xml.fixture;

import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;
import edu.cornell.kfs.fp.batch.xml.cloudcheckr.fixture.CloudCheckrFixtureConstants;

import java.util.Date;

public enum DefaultKfsAccountForAwsFixture {
    AWS_ABC_KFS_1658328("ABC", "1658328"),
    AWS_BCD_KFS_R583805_70170("BCD", CloudCheckrFixtureConstants.ACCOUNT_STRING_R583805_70170),
    AWS_BCD_KFS_ACCOUNT_STRING_IT_R589966_NONCA_1000_BAD("CDE", "IT*R589966*NONCA*1000*BAD*EB-PLGIFT*AEH56"),
    AWS_BCD_KFS_ACCOUNT_STRING_IT_R589966_NONCA_1000_10X("CDE", "IT*R589966*NONCA*1000*10X*EB-PLGIFT*AEH56"),
    AWS_DEF_KFS_165835X("DEF", "165835X"),
    AWS_GHI_KFS_1658498("GHI", "1658498"),
    AWS_JKL_KFS_J801000("JKL", "CS*J801000**6600***"),
    AWS_MNO_KFS_INTERNAL("MNO", "Internal"),
    AWS_PQR_KFS_J801000("PQR", "CS*J801000**6600***"),
    AWS_STU_KFS_1023715_INVALID("STU", "IT*1023715*97601*4020*109**AEH56*BAR");

    public final String awsAccount;
    public final String kfsDefaultAccount;
    public final Date updatedAt;

    private DefaultKfsAccountForAwsFixture(String awsAccount, String kfsDefaultAccount, Date updatedAt) {
        this.awsAccount = awsAccount;
        this.kfsDefaultAccount = kfsDefaultAccount;
        this.updatedAt = updatedAt;
    }

    private DefaultKfsAccountForAwsFixture(String awsAccount, String kfsDefaultAccount) {
        this(awsAccount, kfsDefaultAccount, null);
    }

    public DefaultKfsAccountForAws toDefaultKfsAccountForAwsPojo() {
        DefaultKfsAccountForAws defaultKfsAccountForAws = new DefaultKfsAccountForAws();
        defaultKfsAccountForAws.setAwsAccount(awsAccount);
        defaultKfsAccountForAws.setKfsDefaultAccount(kfsDefaultAccount);
        defaultKfsAccountForAws.setUpdatedAt(updatedAt);
        return defaultKfsAccountForAws;
    }
}
