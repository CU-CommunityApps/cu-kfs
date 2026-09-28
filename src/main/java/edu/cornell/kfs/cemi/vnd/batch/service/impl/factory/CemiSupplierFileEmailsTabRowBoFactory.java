package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.Validate;

import edu.cornell.kfs.cemi.vnd.CemiSupplierConstants;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierEmailBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiSupplierFileEmailsTabRowBo;

public class CemiSupplierFileEmailsTabRowBoFactory {

    private String supplierId;
    private List<CemiSupplierEmailBo> emailAddresses;

    public CemiSupplierFileEmailsTabRowBoFactory(final String supplierId,
            final List<CemiSupplierEmailBo> emailAddresses) {
        Validate.notBlank(supplierId, "supplierId cannot be blank");
        Validate.isTrue(CollectionUtils.size(emailAddresses) >= CemiSupplierConstants.MAX_SUPPLIER_EMAIL_ENTRIES,
                "emailAddresses list must have at least %s entries, with empty placeholder BOs for any absent emails",
                CemiSupplierConstants.MAX_SUPPLIER_EMAIL_ENTRIES);
        this.supplierId = supplierId;
        this.emailAddresses = emailAddresses;
    }

    public static CemiSupplierFileEmailsTabRowBo createTabRowBoFrom(final String supplierId,
            final List<CemiSupplierEmailBo> emailAddresses) {
        final CemiSupplierFileEmailsTabRowBoFactory factory = new CemiSupplierFileEmailsTabRowBoFactory(
                supplierId, emailAddresses);
        return factory.createCemiSupplierFileEmailsTabRowBo();
    }

    public CemiSupplierFileEmailsTabRowBo createCemiSupplierFileEmailsTabRowBo() {
        final CemiSupplierFileEmailsTabRowBo emailRowBo = new CemiSupplierFileEmailsTabRowBo();

        final CemiSupplierEmailBo email1 = emailAddresses.get(0);
        final CemiSupplierEmailBo email2 = emailAddresses.get(1);
        final CemiSupplierEmailBo email3 = emailAddresses.get(2);
        final CemiSupplierEmailBo email4 = emailAddresses.get(3);
        final CemiSupplierEmailBo email5 = emailAddresses.get(4);
        final CemiSupplierEmailBo email6 = emailAddresses.get(5);
        final CemiSupplierEmailBo email7 = emailAddresses.get(6);

        emailRowBo.setSupplierId(supplierId);
        emailRowBo.setEmailId1(email1.getEmailId());
        emailRowBo.setEmailAddress1(email1.getEmailAddress());
        emailRowBo.setEmailPrimary1(email1.getEmailPrimary());
        emailRowBo.setEmailUseFor1_1(email1.getEmailUseFor1());
        emailRowBo.setEmailUseFor1_2(email1.getEmailUseFor2());
        emailRowBo.setEmailUseFor1_3(email1.getEmailUseFor3());
        emailRowBo.setEmailUseFor1_4(email1.getEmailUseFor4());
        emailRowBo.setUseForTenanted1_1(email1.getUseForTenanted1());
        emailRowBo.setUseForTenanted1_2(email1.getUseForTenanted2());
        emailRowBo.setUseForTenanted1_3(email1.getUseForTenanted3());
        emailRowBo.setUseForTenanted1_4(email1.getUseForTenanted4());
        emailRowBo.setEmailId2(email2.getEmailId());
        emailRowBo.setEmailAddress2(email2.getEmailAddress());
        emailRowBo.setEmailPrimary2(email2.getEmailPrimary());
        emailRowBo.setEmailUseFor2_1(email2.getEmailUseFor1());
        emailRowBo.setEmailUseFor2_2(email2.getEmailUseFor2());
        emailRowBo.setEmailUseFor2_3(email2.getEmailUseFor3());
        emailRowBo.setEmailUseFor2_4(email2.getEmailUseFor4());
        emailRowBo.setUseForTenanted2_1(email2.getUseForTenanted1());
        emailRowBo.setUseForTenanted2_2(email2.getUseForTenanted2());
        emailRowBo.setUseForTenanted2_3(email2.getUseForTenanted3());
        emailRowBo.setUseForTenanted2_4(email2.getUseForTenanted4());
        emailRowBo.setEmailId3(email3.getEmailId());
        emailRowBo.setEmailAddress3(email3.getEmailAddress());
        emailRowBo.setEmailPrimary3(email3.getEmailPrimary());
        emailRowBo.setEmailUseFor3_1(email3.getEmailUseFor1());
        emailRowBo.setEmailUseFor3_2(email3.getEmailUseFor2());
        emailRowBo.setEmailUseFor3_3(email3.getEmailUseFor3());
        emailRowBo.setEmailUseFor3_4(email3.getEmailUseFor4());
        emailRowBo.setUseForTenanted3_1(email3.getUseForTenanted1());
        emailRowBo.setUseForTenanted3_2(email3.getUseForTenanted2());
        emailRowBo.setUseForTenanted3_3(email3.getUseForTenanted3());
        emailRowBo.setUseForTenanted3_4(email3.getUseForTenanted4());
        emailRowBo.setEmailId4(email4.getEmailId());
        emailRowBo.setEmailAddress4(email4.getEmailAddress());
        emailRowBo.setEmailPrimary4(email4.getEmailPrimary());
        emailRowBo.setEmailUseFor4_1(email4.getEmailUseFor1());
        emailRowBo.setEmailUseFor4_2(email4.getEmailUseFor2());
        emailRowBo.setEmailUseFor4_3(email4.getEmailUseFor3());
        emailRowBo.setEmailUseFor4_4(email4.getEmailUseFor4());
        emailRowBo.setUseForTenanted4_1(email4.getUseForTenanted1());
        emailRowBo.setUseForTenanted4_2(email4.getUseForTenanted2());
        emailRowBo.setUseForTenanted4_3(email4.getUseForTenanted3());
        emailRowBo.setUseForTenanted4_4(email4.getUseForTenanted4());
        emailRowBo.setEmailId5(email5.getEmailId());
        emailRowBo.setEmailAddress5(email5.getEmailAddress());
        emailRowBo.setEmailPrimary5(email5.getEmailPrimary());
        emailRowBo.setEmailUseFor5_1(email5.getEmailUseFor1());
        emailRowBo.setEmailUseFor5_2(email5.getEmailUseFor2());
        emailRowBo.setEmailUseFor5_3(email5.getEmailUseFor3());
        emailRowBo.setEmailUseFor5_4(email5.getEmailUseFor4());
        emailRowBo.setUseForTenanted5_1(email5.getUseForTenanted1());
        emailRowBo.setUseForTenanted5_2(email5.getUseForTenanted2());
        emailRowBo.setUseForTenanted5_3(email5.getUseForTenanted3());
        emailRowBo.setUseForTenanted5_4(email5.getUseForTenanted4());
        emailRowBo.setEmailId6(email6.getEmailId());
        emailRowBo.setEmailAddress6(email6.getEmailAddress());
        emailRowBo.setEmailPrimary6(email6.getEmailPrimary());
        emailRowBo.setEmailUseFor6_1(email6.getEmailUseFor1());
        emailRowBo.setEmailUseFor6_2(email6.getEmailUseFor2());
        emailRowBo.setEmailUseFor6_3(email6.getEmailUseFor3());
        emailRowBo.setEmailUseFor6_4(email6.getEmailUseFor4());
        emailRowBo.setUseForTenanted6_1(email6.getUseForTenanted1());
        emailRowBo.setUseForTenanted6_2(email6.getUseForTenanted2());
        emailRowBo.setUseForTenanted6_3(email6.getUseForTenanted3());
        emailRowBo.setUseForTenanted6_4(email6.getUseForTenanted4());
        emailRowBo.setEmailId7(email7.getEmailId());
        emailRowBo.setEmailAddress7(email7.getEmailAddress());
        emailRowBo.setEmailPrimary7(email7.getEmailPrimary());
        emailRowBo.setEmailUseFor7_1(email7.getEmailUseFor1());
        emailRowBo.setEmailUseFor7_2(email7.getEmailUseFor2());
        emailRowBo.setEmailUseFor7_3(email7.getEmailUseFor3());
        emailRowBo.setEmailUseFor7_4(email7.getEmailUseFor4());
        emailRowBo.setUseForTenanted7_1(email7.getUseForTenanted1());
        emailRowBo.setUseForTenanted7_2(email7.getUseForTenanted2());
        emailRowBo.setUseForTenanted7_3(email7.getUseForTenanted3());
        emailRowBo.setUseForTenanted7_4(email7.getUseForTenanted4());

        return emailRowBo;
    }

}
