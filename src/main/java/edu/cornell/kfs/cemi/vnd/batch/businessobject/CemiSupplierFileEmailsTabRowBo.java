package edu.cornell.kfs.cemi.vnd.batch.businessobject;

import edu.cornell.kfs.cemi.sys.batch.businessobject.CemiIndexedBusinessObjectBase;

/*
 * NOTE: Whenever this class gets updated to add or remove more email addresses, make sure
 *       the email-related processing of the Remit To Supplier extract also gets updated.
 *       See the related comments in the CemiRemitToSupplierDataBuilderDefaultImpl class.
 */
public class CemiSupplierFileEmailsTabRowBo extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = -1311681792505926049L;

    private String supplierId;
    private String emailId1;
    private String emailAddress1;
    private String emailPrimary1;
    private String emailUseFor1_1;
    private String emailUseFor1_2;
    private String emailUseFor1_3;
    private String emailUseFor1_4;
    private String useForTenanted1_1;
    private String useForTenanted1_2;
    private String useForTenanted1_3;
    private String useForTenanted1_4;
    private String emailId2;
    private String emailAddress2;
    private String emailPrimary2;
    private String emailUseFor2_1;
    private String emailUseFor2_2;
    private String emailUseFor2_3;
    private String emailUseFor2_4;
    private String useForTenanted2_1;
    private String useForTenanted2_2;
    private String useForTenanted2_3;
    private String useForTenanted2_4;
    private String emailId3;
    private String emailAddress3;
    private String emailPrimary3;
    private String emailUseFor3_1;
    private String emailUseFor3_2;
    private String emailUseFor3_3;
    private String emailUseFor3_4;
    private String useForTenanted3_1;
    private String useForTenanted3_2;
    private String useForTenanted3_3;
    private String useForTenanted3_4;
    private String emailId4;
    private String emailAddress4;
    private String emailPrimary4;
    private String emailUseFor4_1;
    private String emailUseFor4_2;
    private String emailUseFor4_3;
    private String emailUseFor4_4;
    private String useForTenanted4_1;
    private String useForTenanted4_2;
    private String useForTenanted4_3;
    private String useForTenanted4_4;
    private String emailId5;
    private String emailAddress5;
    private String emailPrimary5;
    private String emailUseFor5_1;
    private String emailUseFor5_2;
    private String emailUseFor5_3;
    private String emailUseFor5_4;
    private String useForTenanted5_1;
    private String useForTenanted5_2;
    private String useForTenanted5_3;
    private String useForTenanted5_4;
    private String emailId6;
    private String emailAddress6;
    private String emailPrimary6;
    private String emailUseFor6_1;
    private String emailUseFor6_2;
    private String emailUseFor6_3;
    private String emailUseFor6_4;
    private String useForTenanted6_1;
    private String useForTenanted6_2;
    private String useForTenanted6_3;
    private String useForTenanted6_4;
    private String emailId7;
    private String emailAddress7;
    private String emailPrimary7;
    private String emailUseFor7_1;
    private String emailUseFor7_2;
    private String emailUseFor7_3;
    private String emailUseFor7_4;
    private String useForTenanted7_1;
    private String useForTenanted7_2;
    private String useForTenanted7_3;
    private String useForTenanted7_4;

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(final String supplierId) {
        this.supplierId = supplierId;
    }

    public String getEmailId1() {
        return emailId1;
    }

    public void setEmailId1(final String emailId1) {
        this.emailId1 = emailId1;
    }

    public String getEmailAddress1() {
        return emailAddress1;
    }

    public void setEmailAddress1(final String emailAddress1) {
        this.emailAddress1 = emailAddress1;
    }

    public String getEmailPrimary1() {
        return emailPrimary1;
    }

    public void setEmailPrimary1(final String emailPrimary1) {
        this.emailPrimary1 = emailPrimary1;
    }

    public String getEmailUseFor1_1() {
        return emailUseFor1_1;
    }

    public void setEmailUseFor1_1(final String emailUseFor1_1) {
        this.emailUseFor1_1 = emailUseFor1_1;
    }

    public String getEmailUseFor1_2() {
        return emailUseFor1_2;
    }

    public void setEmailUseFor1_2(final String emailUseFor1_2) {
        this.emailUseFor1_2 = emailUseFor1_2;
    }

    public String getEmailUseFor1_3() {
        return emailUseFor1_3;
    }

    public void setEmailUseFor1_3(final String emailUseFor1_3) {
        this.emailUseFor1_3 = emailUseFor1_3;
    }

    public String getEmailUseFor1_4() {
        return emailUseFor1_4;
    }

    public void setEmailUseFor1_4(final String emailUseFor1_4) {
        this.emailUseFor1_4 = emailUseFor1_4;
    }

    public String getUseForTenanted1_1() {
        return useForTenanted1_1;
    }

    public void setUseForTenanted1_1(final String useForTenanted1_1) {
        this.useForTenanted1_1 = useForTenanted1_1;
    }

    public String getUseForTenanted1_2() {
        return useForTenanted1_2;
    }

    public void setUseForTenanted1_2(final String useForTenanted1_2) {
        this.useForTenanted1_2 = useForTenanted1_2;
    }

    public String getUseForTenanted1_3() {
        return useForTenanted1_3;
    }

    public void setUseForTenanted1_3(final String useForTenanted1_3) {
        this.useForTenanted1_3 = useForTenanted1_3;
    }

    public String getUseForTenanted1_4() {
        return useForTenanted1_4;
    }

    public void setUseForTenanted1_4(final String useForTenanted1_4) {
        this.useForTenanted1_4 = useForTenanted1_4;
    }

    public String getEmailId2() {
        return emailId2;
    }

    public void setEmailId2(final String emailId2) {
        this.emailId2 = emailId2;
    }

    public String getEmailAddress2() {
        return emailAddress2;
    }

    public void setEmailAddress2(final String emailAddress2) {
        this.emailAddress2 = emailAddress2;
    }

    public String getEmailPrimary2() {
        return emailPrimary2;
    }

    public void setEmailPrimary2(final String emailPrimary2) {
        this.emailPrimary2 = emailPrimary2;
    }

    public String getEmailUseFor2_1() {
        return emailUseFor2_1;
    }

    public void setEmailUseFor2_1(final String emailUseFor2_1) {
        this.emailUseFor2_1 = emailUseFor2_1;
    }

    public String getEmailUseFor2_2() {
        return emailUseFor2_2;
    }

    public void setEmailUseFor2_2(final String emailUseFor2_2) {
        this.emailUseFor2_2 = emailUseFor2_2;
    }

    public String getEmailUseFor2_3() {
        return emailUseFor2_3;
    }

    public void setEmailUseFor2_3(final String emailUseFor2_3) {
        this.emailUseFor2_3 = emailUseFor2_3;
    }

    public String getEmailUseFor2_4() {
        return emailUseFor2_4;
    }

    public void setEmailUseFor2_4(final String emailUseFor2_4) {
        this.emailUseFor2_4 = emailUseFor2_4;
    }

    public String getUseForTenanted2_1() {
        return useForTenanted2_1;
    }

    public void setUseForTenanted2_1(final String useForTenanted2_1) {
        this.useForTenanted2_1 = useForTenanted2_1;
    }

    public String getUseForTenanted2_2() {
        return useForTenanted2_2;
    }

    public void setUseForTenanted2_2(final String useForTenanted2_2) {
        this.useForTenanted2_2 = useForTenanted2_2;
    }

    public String getUseForTenanted2_3() {
        return useForTenanted2_3;
    }

    public void setUseForTenanted2_3(final String useForTenanted2_3) {
        this.useForTenanted2_3 = useForTenanted2_3;
    }

    public String getUseForTenanted2_4() {
        return useForTenanted2_4;
    }

    public void setUseForTenanted2_4(final String useForTenanted2_4) {
        this.useForTenanted2_4 = useForTenanted2_4;
    }

    public String getEmailId3() {
        return emailId3;
    }

    public void setEmailId3(final String emailId3) {
        this.emailId3 = emailId3;
    }

    public String getEmailAddress3() {
        return emailAddress3;
    }

    public void setEmailAddress3(final String emailAddress3) {
        this.emailAddress3 = emailAddress3;
    }

    public String getEmailPrimary3() {
        return emailPrimary3;
    }

    public void setEmailPrimary3(final String emailPrimary3) {
        this.emailPrimary3 = emailPrimary3;
    }

    public String getEmailUseFor3_1() {
        return emailUseFor3_1;
    }

    public void setEmailUseFor3_1(final String emailUseFor3_1) {
        this.emailUseFor3_1 = emailUseFor3_1;
    }

    public String getEmailUseFor3_2() {
        return emailUseFor3_2;
    }

    public void setEmailUseFor3_2(final String emailUseFor3_2) {
        this.emailUseFor3_2 = emailUseFor3_2;
    }

    public String getEmailUseFor3_3() {
        return emailUseFor3_3;
    }

    public void setEmailUseFor3_3(final String emailUseFor3_3) {
        this.emailUseFor3_3 = emailUseFor3_3;
    }

    public String getEmailUseFor3_4() {
        return emailUseFor3_4;
    }

    public void setEmailUseFor3_4(final String emailUseFor3_4) {
        this.emailUseFor3_4 = emailUseFor3_4;
    }

    public String getUseForTenanted3_1() {
        return useForTenanted3_1;
    }

    public void setUseForTenanted3_1(final String useForTenanted3_1) {
        this.useForTenanted3_1 = useForTenanted3_1;
    }

    public String getUseForTenanted3_2() {
        return useForTenanted3_2;
    }

    public void setUseForTenanted3_2(final String useForTenanted3_2) {
        this.useForTenanted3_2 = useForTenanted3_2;
    }

    public String getUseForTenanted3_3() {
        return useForTenanted3_3;
    }

    public void setUseForTenanted3_3(final String useForTenanted3_3) {
        this.useForTenanted3_3 = useForTenanted3_3;
    }

    public String getUseForTenanted3_4() {
        return useForTenanted3_4;
    }

    public void setUseForTenanted3_4(final String useForTenanted3_4) {
        this.useForTenanted3_4 = useForTenanted3_4;
    }

    public String getEmailId4() {
        return emailId4;
    }

    public void setEmailId4(final String emailId4) {
        this.emailId4 = emailId4;
    }

    public String getEmailAddress4() {
        return emailAddress4;
    }

    public void setEmailAddress4(final String emailAddress4) {
        this.emailAddress4 = emailAddress4;
    }

    public String getEmailPrimary4() {
        return emailPrimary4;
    }

    public void setEmailPrimary4(final String emailPrimary4) {
        this.emailPrimary4 = emailPrimary4;
    }

    public String getEmailUseFor4_1() {
        return emailUseFor4_1;
    }

    public void setEmailUseFor4_1(final String emailUseFor4_1) {
        this.emailUseFor4_1 = emailUseFor4_1;
    }

    public String getEmailUseFor4_2() {
        return emailUseFor4_2;
    }

    public void setEmailUseFor4_2(final String emailUseFor4_2) {
        this.emailUseFor4_2 = emailUseFor4_2;
    }

    public String getEmailUseFor4_3() {
        return emailUseFor4_3;
    }

    public void setEmailUseFor4_3(final String emailUseFor4_3) {
        this.emailUseFor4_3 = emailUseFor4_3;
    }

    public String getEmailUseFor4_4() {
        return emailUseFor4_4;
    }

    public void setEmailUseFor4_4(final String emailUseFor4_4) {
        this.emailUseFor4_4 = emailUseFor4_4;
    }

    public String getUseForTenanted4_1() {
        return useForTenanted4_1;
    }

    public void setUseForTenanted4_1(final String useForTenanted4_1) {
        this.useForTenanted4_1 = useForTenanted4_1;
    }

    public String getUseForTenanted4_2() {
        return useForTenanted4_2;
    }

    public void setUseForTenanted4_2(final String useForTenanted4_2) {
        this.useForTenanted4_2 = useForTenanted4_2;
    }

    public String getUseForTenanted4_3() {
        return useForTenanted4_3;
    }

    public void setUseForTenanted4_3(final String useForTenanted4_3) {
        this.useForTenanted4_3 = useForTenanted4_3;
    }

    public String getUseForTenanted4_4() {
        return useForTenanted4_4;
    }

    public void setUseForTenanted4_4(final String useForTenanted4_4) {
        this.useForTenanted4_4 = useForTenanted4_4;
    }

    public String getEmailId5() {
        return emailId5;
    }

    public void setEmailId5(final String emailId5) {
        this.emailId5 = emailId5;
    }

    public String getEmailAddress5() {
        return emailAddress5;
    }

    public void setEmailAddress5(final String emailAddress5) {
        this.emailAddress5 = emailAddress5;
    }

    public String getEmailPrimary5() {
        return emailPrimary5;
    }

    public void setEmailPrimary5(final String emailPrimary5) {
        this.emailPrimary5 = emailPrimary5;
    }

    public String getEmailUseFor5_1() {
        return emailUseFor5_1;
    }

    public void setEmailUseFor5_1(final String emailUseFor5_1) {
        this.emailUseFor5_1 = emailUseFor5_1;
    }

    public String getEmailUseFor5_2() {
        return emailUseFor5_2;
    }

    public void setEmailUseFor5_2(final String emailUseFor5_2) {
        this.emailUseFor5_2 = emailUseFor5_2;
    }

    public String getEmailUseFor5_3() {
        return emailUseFor5_3;
    }

    public void setEmailUseFor5_3(final String emailUseFor5_3) {
        this.emailUseFor5_3 = emailUseFor5_3;
    }

    public String getEmailUseFor5_4() {
        return emailUseFor5_4;
    }

    public void setEmailUseFor5_4(final String emailUseFor5_4) {
        this.emailUseFor5_4 = emailUseFor5_4;
    }

    public String getUseForTenanted5_1() {
        return useForTenanted5_1;
    }

    public void setUseForTenanted5_1(final String useForTenanted5_1) {
        this.useForTenanted5_1 = useForTenanted5_1;
    }

    public String getUseForTenanted5_2() {
        return useForTenanted5_2;
    }

    public void setUseForTenanted5_2(final String useForTenanted5_2) {
        this.useForTenanted5_2 = useForTenanted5_2;
    }

    public String getUseForTenanted5_3() {
        return useForTenanted5_3;
    }

    public void setUseForTenanted5_3(final String useForTenanted5_3) {
        this.useForTenanted5_3 = useForTenanted5_3;
    }

    public String getUseForTenanted5_4() {
        return useForTenanted5_4;
    }

    public void setUseForTenanted5_4(final String useForTenanted5_4) {
        this.useForTenanted5_4 = useForTenanted5_4;
    }

    public String getEmailId6() {
        return emailId6;
    }

    public void setEmailId6(final String emailId6) {
        this.emailId6 = emailId6;
    }

    public String getEmailAddress6() {
        return emailAddress6;
    }

    public void setEmailAddress6(final String emailAddress6) {
        this.emailAddress6 = emailAddress6;
    }

    public String getEmailPrimary6() {
        return emailPrimary6;
    }

    public void setEmailPrimary6(final String emailPrimary6) {
        this.emailPrimary6 = emailPrimary6;
    }

    public String getEmailUseFor6_1() {
        return emailUseFor6_1;
    }

    public void setEmailUseFor6_1(final String emailUseFor6_1) {
        this.emailUseFor6_1 = emailUseFor6_1;
    }

    public String getEmailUseFor6_2() {
        return emailUseFor6_2;
    }

    public void setEmailUseFor6_2(final String emailUseFor6_2) {
        this.emailUseFor6_2 = emailUseFor6_2;
    }

    public String getEmailUseFor6_3() {
        return emailUseFor6_3;
    }

    public void setEmailUseFor6_3(final String emailUseFor6_3) {
        this.emailUseFor6_3 = emailUseFor6_3;
    }

    public String getEmailUseFor6_4() {
        return emailUseFor6_4;
    }

    public void setEmailUseFor6_4(final String emailUseFor6_4) {
        this.emailUseFor6_4 = emailUseFor6_4;
    }

    public String getUseForTenanted6_1() {
        return useForTenanted6_1;
    }

    public void setUseForTenanted6_1(final String useForTenanted6_1) {
        this.useForTenanted6_1 = useForTenanted6_1;
    }

    public String getUseForTenanted6_2() {
        return useForTenanted6_2;
    }

    public void setUseForTenanted6_2(final String useForTenanted6_2) {
        this.useForTenanted6_2 = useForTenanted6_2;
    }

    public String getUseForTenanted6_3() {
        return useForTenanted6_3;
    }

    public void setUseForTenanted6_3(final String useForTenanted6_3) {
        this.useForTenanted6_3 = useForTenanted6_3;
    }

    public String getUseForTenanted6_4() {
        return useForTenanted6_4;
    }

    public void setUseForTenanted6_4(final String useForTenanted6_4) {
        this.useForTenanted6_4 = useForTenanted6_4;
    }

    public String getEmailId7() {
        return emailId7;
    }

    public void setEmailId7(final String emailId7) {
        this.emailId7 = emailId7;
    }

    public String getEmailAddress7() {
        return emailAddress7;
    }

    public void setEmailAddress7(final String emailAddress7) {
        this.emailAddress7 = emailAddress7;
    }

    public String getEmailPrimary7() {
        return emailPrimary7;
    }

    public void setEmailPrimary7(final String emailPrimary7) {
        this.emailPrimary7 = emailPrimary7;
    }

    public String getEmailUseFor7_1() {
        return emailUseFor7_1;
    }

    public void setEmailUseFor7_1(final String emailUseFor7_1) {
        this.emailUseFor7_1 = emailUseFor7_1;
    }

    public String getEmailUseFor7_2() {
        return emailUseFor7_2;
    }

    public void setEmailUseFor7_2(final String emailUseFor7_2) {
        this.emailUseFor7_2 = emailUseFor7_2;
    }

    public String getEmailUseFor7_3() {
        return emailUseFor7_3;
    }

    public void setEmailUseFor7_3(final String emailUseFor7_3) {
        this.emailUseFor7_3 = emailUseFor7_3;
    }

    public String getEmailUseFor7_4() {
        return emailUseFor7_4;
    }

    public void setEmailUseFor7_4(final String emailUseFor7_4) {
        this.emailUseFor7_4 = emailUseFor7_4;
    }

    public String getUseForTenanted7_1() {
        return useForTenanted7_1;
    }

    public void setUseForTenanted7_1(final String useForTenanted7_1) {
        this.useForTenanted7_1 = useForTenanted7_1;
    }

    public String getUseForTenanted7_2() {
        return useForTenanted7_2;
    }

    public void setUseForTenanted7_2(final String useForTenanted7_2) {
        this.useForTenanted7_2 = useForTenanted7_2;
    }

    public String getUseForTenanted7_3() {
        return useForTenanted7_3;
    }

    public void setUseForTenanted7_3(final String useForTenanted7_3) {
        this.useForTenanted7_3 = useForTenanted7_3;
    }

    public String getUseForTenanted7_4() {
        return useForTenanted7_4;
    }

    public void setUseForTenanted7_4(final String useForTenanted7_4) {
        this.useForTenanted7_4 = useForTenanted7_4;
    }

}
