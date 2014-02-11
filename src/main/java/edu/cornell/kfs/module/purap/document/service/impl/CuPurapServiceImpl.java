package edu.cornell.kfs.module.purap.document.service.impl;

import java.util.Iterator;
import java.util.List;

import org.kuali.kfs.module.purap.document.service.impl.PurapServiceImpl;
import org.kuali.kfs.sys.service.NonTransactional;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.krad.document.Document;
import org.kuali.rice.krad.exception.InfrastructureException;

import edu.cornell.kfs.module.purap.businessobject.IWantView;

@NonTransactional
public class CuPurapServiceImpl extends PurapServiceImpl {

    // ==== CU Customization (KFSPTS-1656): Save IWantDocument routing data. ====
    @Override
    public void saveRoutingDataForRelatedDocuments(Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        super.saveRoutingDataForRelatedDocuments(accountsPayablePurchasingDocumentLinkIdentifier);
        
        try {
            // Save IWNT routing data.
            @SuppressWarnings("unchecked")
            List<IWantView> iWantViews = getRelatedViews(IWantView.class, accountsPayablePurchasingDocumentLinkIdentifier);
            for (Iterator<IWantView> iterator = iWantViews.iterator(); iterator.hasNext();) {
                IWantView view = (IWantView) iterator.next();
                Document doc = documentService.getByDocumentHeaderId(view.getDocumentNumber());
                doc.getDocumentHeader().getWorkflowDocument().saveDocumentData();
            }

            
        } catch (WorkflowException e) {
            throw new InfrastructureException("unable to save routing data for related docs", e);
        }
    }

    // ==== CU Customization (KFSPTS-1656): Get IWantDocument views. ====
    @Override
    public List<String> getRelatedDocumentIds(Integer accountsPayablePurchasingDocumentLinkIdentifier) {
        List<String> documentIdList = super.getRelatedDocumentIds(accountsPayablePurchasingDocumentLinkIdentifier);

        // Get IWNT views.
        @SuppressWarnings("unchecked")
        List<IWantView> iWantViews = getRelatedViews(IWantView.class, accountsPayablePurchasingDocumentLinkIdentifier);
        for (Iterator<IWantView> iterator = iWantViews.iterator(); iterator.hasNext();) {
            IWantView view = (IWantView) iterator.next();
            documentIdList.add(view.getDocumentNumber());
        }

        return documentIdList;
    }
}
