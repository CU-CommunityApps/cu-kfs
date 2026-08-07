package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.vnd.businessobject.VendorContact;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants.CommunicationUsageTypes;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactEmailBo;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactGenericUsageBo;

public class CemiEntityContactEmailBoFactory {

    private VendorContact vendorContact;
    private boolean writeEmailDataIfPresent;

    public CemiEntityContactEmailBoFactory(final VendorContact vendorContact, final boolean writeEmailDataIfPresent) {
        this.vendorContact = vendorContact;
        this.writeEmailDataIfPresent = writeEmailDataIfPresent;
    }

    public static CemiEntityContactEmailBo createEmailBoFrom(final VendorContact vendorContact,
            final boolean writeEmailDataIfPresent) {
        final CemiEntityContactEmailBoFactory factory = new CemiEntityContactEmailBoFactory(
                vendorContact, writeEmailDataIfPresent);
        return factory.createCemiEntityContactEmailBo();
    }

    public CemiEntityContactEmailBo createCemiEntityContactEmailBo() {
        final CemiEntityContactEmailBo emailBo = new CemiEntityContactEmailBo();
        final boolean writeEmailData = (writeEmailDataIfPresent
                && StringUtils.isNotBlank(vendorContact.getVendorContactEmailAddress()));
        final CemiEntityContactGenericUsageBo emailUsage = writeEmailData
                ? CemiEntityContactGenericUsageBoFactory.createUsageBoFrom(
                        CemiEntityContactConstants.ROW_ID_1, CommunicationUsageTypes.WORK, CemiBaseConstants.EMPTY_STRING)
                : CemiEntityContactGenericUsageBoFactory.createEmptyUsageBo();
        final String emailRowId = writeEmailData ? CemiEntityContactConstants.ROW_ID_1 : CemiBaseConstants.EMPTY_STRING;
        final String emailAddress = writeEmailData
                ? vendorContact.getVendorContactEmailAddress() : CemiBaseConstants.EMPTY_STRING;

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
