package edu.cornell.kfs.sys.document.authorization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kim.impl.identity.Person;
import org.kuali.kfs.kns.document.authorization.MaintenanceDocumentAuthorizerBase;
import org.kuali.kfs.krad.document.Document;

public class ApiEndpointDescriptorAuthorizer extends MaintenanceDocumentAuthorizerBase {
    private static final Logger LOG = LogManager.getLogger();

    @Override
    public boolean canCopy(final Document document, final Person user) {
        LOG.info("canCopy, entering");
        return false;
    }
}
