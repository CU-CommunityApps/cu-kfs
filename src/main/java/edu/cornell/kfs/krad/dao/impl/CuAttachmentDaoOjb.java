package edu.cornell.kfs.krad.dao.impl;

import edu.cornell.kfs.krad.CUKRADPropertyConstants;
import edu.cornell.kfs.krad.dao.CuAttachmentDao;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.dao.impl.AttachmentDaoOjb;

public class CuAttachmentDaoOjb extends AttachmentDaoOjb implements CuAttachmentDao {

    @Override
    public Attachment getAttachmentByAttachmentId(final String attachmentIdentifier) {
        final Criteria crit = new Criteria();
        crit.addEqualTo(CUKRADPropertyConstants.ATTACHMENT_IDENTIFIER, attachmentIdentifier);
        return (Attachment) this.getPersistenceBrokerTemplate().getObjectByQuery(new QueryByCriteria(Attachment.class, crit));
    }

}
