package edu.cornell.kfs.krad.dao;

import org.kuali.kfs.krad.bo.Attachment;
import org.kuali.kfs.krad.dao.AttachmentDao;

public interface CuAttachmentDao extends AttachmentDao{

    /**
     * Retrieve attachment by a given attachmentIdentifier
     *
     * @param attachmentIdentifier
     * @return
     */
    Attachment getAttachmentByAttachmentId(String attachmentIdentifier);

}
