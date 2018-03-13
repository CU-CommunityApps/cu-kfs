package edu.cornell.kfs.fp.batch.xml.fixture;

import edu.cornell.kfs.fp.batch.xml.DefaultKfsAccountForAws;

import java.util.Date;

public enum DefaultKfsAccountForAwsFixture {
    AWS_ABC_KFS_1658328("ABC", "1658328", null),
    AWS_DEF_KFS_165835X("DEF", "165835X", null),
    AWS_GHI_KFS_1658498("GHI", "1658498", null),
    AWS_JKL_KFS_J801000_INVALID("JKL", "CS*J801000**6600***", null),
    AWS_MNO_KFS_INTERNAL("MNO", "Internal", null),
    AWS_PQR_KFS_J801000_INVALID("PQR", "CS*J801000**6600***", null),
    AWS_STU_KFS_1023715_INVALID("STU", "IT*1023715*97601*4020*109**AEH56*BAR", null);

    public final String awsAccount;
    public final String kfsDefaultAccount;
    public final Date updatedAt;

    private DefaultKfsAccountForAwsFixture(String awsAccount, String kfsDefaultAccount, Date updatedAt) {
        this.awsAccount = awsAccount;
        this.kfsDefaultAccount = kfsDefaultAccount;
        this.updatedAt = updatedAt;
    }

    public DefaultKfsAccountForAws toDefaultKfsAccountForAwsPojo() {
        DefaultKfsAccountForAws defaultKfsAccountForAws = new DefaultKfsAccountForAws();
        defaultKfsAccountForAws.setAwsAccount(awsAccount);
        defaultKfsAccountForAws.setKfsDefaultAccount(kfsDefaultAccount);
        defaultKfsAccountForAws.setUpdatedAt(updatedAt);
        return defaultKfsAccountForAws;
    }
}
