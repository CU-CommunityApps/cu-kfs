package edu.cornell.kfs.krad.dao.impl;

import java.util.List;

import org.kuali.kfs.krad.dao.BusinessObjectDao;
import org.kuali.kfs.krad.dao.impl.DocumentDaoOjb;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentAdHocService;
import org.springframework.transaction.annotation.Transactional;

/**
 * Subclass of DocumentDaoOjb that adds transactional support,
 * to reapply the transactional functionality that used to be present
 * when older KFS versions relied on Rice's DocumentDaoProxy.
 * 
 * Due to whatever mechanism Rice uses for transaction interception,
 * the relevant service methods had to be overridden to allow
 * for the transaction handling to work. The overridden methods
 * simply delegate to the equivalent superclass methods in this case.
 */
@Transactional
public class CuDocumentDaoOjb extends DocumentDaoOjb {

    public CuDocumentDaoOjb(BusinessObjectDao businessObjectDao, DocumentAdHocService documentAdHocService) {
        super(businessObjectDao, documentAdHocService);
    }

    @Override
    public <T extends Document> T save(T document) {
        return super.save(document);
    }

    @Override
    public <T extends Document> T findByDocumentHeaderId(Class<T> clazz, String id) {
        return super.findByDocumentHeaderId(clazz, id);
    }

    @Override
    public <T extends Document> List<T> findByDocumentHeaderIds(Class<T> clazz, List<String> idList) {
        return super.findByDocumentHeaderIds(clazz, idList);
    }

    @Override
    public BusinessObjectDao getBusinessObjectDao() {
        return super.getBusinessObjectDao();
    }

    @Override
    public DocumentAdHocService getDocumentAdHocService() {
        return super.getDocumentAdHocService();
    }

}
