package edu.cornell.kfs.cemi.vnd.batch.service.impl.factory;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.vnd.businessobject.VendorContact;

import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants;
import edu.cornell.kfs.cemi.vnd.CemiEntityContactConstants.CommunicationUsageTypes;
import edu.cornell.kfs.cemi.vnd.batch.businessobject.CemiEntityContactEmailBo;

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
        if (!writeEmailDataIfPresent || StringUtils.isBlank(vendorContact.getVendorContactEmailAddress())) {
            return emailBo;
        }

        emailBo.setEmailRowId(CemiEntityContactConstants.ROW_ID_1);
        emailBo.setDeleteEmail(null);
        emailBo.setDoNotReplaceAllEmail(null);
        emailBo.setEmailAddress(vendorContact.getVendorContactEmailAddress());
        emailBo.setEmailComment(null);
        emailBo.setEmailUsageRowId(CemiEntityContactConstants.ROW_ID_1);
        emailBo.setEmailUsagePublic(CemiBaseConstants.YES);
        emailBo.setEmailUsageTypeRowId(CemiEntityContactConstants.ROW_ID_1);
        emailBo.setEmailUsageTypePrimary(CemiBaseConstants.YES);
        emailBo.setEmailUsageType(CommunicationUsageTypes.WORK);
        emailBo.setEmailUseFor(null);
        emailBo.setEmailUseForTenanted(null);
        emailBo.setEmailUsageComments(null);
        emailBo.setExistingEmailId(null);
        emailBo.setNewEmailId(null);

        return emailBo;
    }

}
