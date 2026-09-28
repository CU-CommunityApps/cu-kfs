package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import java.util.List;

import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.vnd.businessobject.VendorContact;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants.CommunicationUsageTypes;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactEmailBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactGenericUsageBo;

public class CemiEntityContactEmailBoFactory {

    private static final Logger LOG = LogManager.getLogger();

    private List<VendorContact> mergedEmailContacts;
    private int emailIndex;

    public CemiEntityContactEmailBoFactory(final List<VendorContact> mergedEmailContacts, final int emailIndex) {
        Validate.notNull(mergedEmailContacts, "mergedEmailContacts cannot be null");
        Validate.isTrue(mergedEmailContacts.isEmpty() == (emailIndex <= 0),
                "emailIndex must be a positive value if, and only if, mergedEmailContacts is non-empty");
        this.mergedEmailContacts = mergedEmailContacts;
        this.emailIndex = emailIndex;
    }

    public static CemiEntityContactEmailBo createEmailBoFrom(final List<VendorContact> mergedEmailContacts,
            final int emailIndex) {
        final CemiEntityContactEmailBoFactory factory = new CemiEntityContactEmailBoFactory(
                mergedEmailContacts, emailIndex);
        return factory.createCemiEntityContactEmailBo();
    }

    public CemiEntityContactEmailBo createCemiEntityContactEmailBo() {
        final CemiEntityContactEmailBo emailBo = new CemiEntityContactEmailBo();
        final boolean writeEmailData = !mergedEmailContacts.isEmpty();
        final CemiEntityContactGenericUsageBo emailUsage = writeEmailData
                ? CemiEntityContactGenericUsageBoFactory.createUsageBoFrom(
                        CemiEntityContactConstants.ROW_ID_1, CommunicationUsageTypes.WORK, CemiBaseConstants.EMPTY_STRING)
                : CemiEntityContactGenericUsageBoFactory.createEmptyUsageBo();
        final String emailRowId = writeEmailData ? Integer.toString(emailIndex) : CemiBaseConstants.EMPTY_STRING;
        final String emailAddress = writeEmailData
                ? mergedEmailContacts.get(0).getVendorContactEmailAddress() : CemiBaseConstants.EMPTY_STRING;

        if (writeEmailData) {
            final VendorContact firstEmailContact = mergedEmailContacts.get(0);
            emailBo.setVendorContactGeneratedIdentifier(firstEmailContact.getVendorContactGeneratedIdentifier());
            LOG.debug("createCemiEntityContactEmailBo, Creating Entity Contact Email for Vendor Contact {} using {} "
                    + "merged email addresses", firstEmailContact.getVendorContactGeneratedIdentifier(),
                    mergedEmailContacts.size());
        }

        emailBo.setEmailRowId(emailRowId);
        emailBo.setDeleteEmail(CemiBaseConstants.EMPTY_STRING);
        emailBo.setDoNotReplaceAllEmail(CemiBaseConstants.EMPTY_STRING);
        emailBo.setEmailAddress(emailAddress);
        emailBo.setEmailComment(CemiBaseConstants.EMPTY_STRING);
        emailBo.setExistingEmailId(CemiBaseConstants.EMPTY_STRING);
        emailBo.setNewEmailId(CemiBaseConstants.EMPTY_STRING);
        emailBo.addEmailUsage(emailUsage);

        return emailBo;
    }

}
