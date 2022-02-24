package edu.cornell.kfs.module.purap.document.service.impl;

import org.kuali.kfs.module.purap.PurchaseOrderStatuses;
import org.kuali.kfs.module.purap.document.LineItemReceivingDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderAmendmentDocument;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;
import org.kuali.kfs.module.purap.document.ReceivingDocument;
import org.kuali.kfs.module.purap.document.service.LogicContainer;
import org.kuali.kfs.module.purap.document.service.impl.ReceivingServiceImpl;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.krad.UserSession;
import org.kuali.kfs.krad.bo.Note;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.KRADConstants;
import org.kuali.kfs.module.purap.PurapConstants;

import edu.cornell.kfs.module.purap.document.CuPurchaseOrderAmendmentDocument;

public class CuReceivingServiceImpl extends ReceivingServiceImpl {

    protected void spawnPoAmendmentForUnorderedItems(ReceivingDocument receivingDocument, PurchaseOrderDocument po){

        //if receiving line document
        if (receivingDocument instanceof LineItemReceivingDocument) {
            LineItemReceivingDocument rlDoc = (LineItemReceivingDocument)receivingDocument;

            //if a new item has been added spawn a purchase order amendment
            if( hasNewUnorderedItem((LineItemReceivingDocument)receivingDocument) ){
                String newSessionUserId = KFSConstants.SYSTEM_USER;
                try {

                    LogicContainer logicToRun = new LogicContainer() {
                        @Override
                        public Object runLogic(Object[] objects) {
                            LineItemReceivingDocument rlDoc = (LineItemReceivingDocument)objects[0];
                            String poDocNumber = (String)objects[1];

                            //create a PO amendment
                            PurchaseOrderAmendmentDocument amendmentPo = (PurchaseOrderAmendmentDocument) purchaseOrderService.createAndSavePotentialChangeDocument(poDocNumber, PurapConstants.PurapDocTypeCodes.PURCHASE_ORDER_AMENDMENT_DOCUMENT, PurchaseOrderStatuses.APPDOC_AMENDMENT);

                            // KFSPTS-1769, KFSUPGRADE-339
                            ((CuPurchaseOrderAmendmentDocument)amendmentPo).setSpawnPoa(true);
                            //add new lines to amendement
                            addUnorderedItemsToAmendment(amendmentPo, rlDoc);

                            //route amendment
                            documentService.routeDocument(amendmentPo, null, null);

                            //add note to amendment po document
                            String note = "Purchase Order Amendment " + amendmentPo.getPurapDocumentIdentifier() + " (document id " + amendmentPo.getDocumentNumber() + ") created for new unordered line items due to Receiving (document id " + rlDoc.getDocumentNumber() + ")";

                            Note noteObj = documentService.createNoteFromDocument(amendmentPo, note);
                            amendmentPo.addNote(noteObj);
                            noteService.save(noteObj);

                            return null;
                        }
                    };

                    purapService.performLogicWithFakedUserSession(newSessionUserId, logicToRun, new Object[] { rlDoc, po.getDocumentNumber() });
                }
                catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    
    @Override
    public String getReceivingDeliveryCampusCode(PurchaseOrderDocument po){
        if (GlobalVariables.getUserSession() == null) {
            GlobalVariables.setUserSession(new UserSession(KRADConstants.SYSTEM_USER));
            GlobalVariables.clear();
        }
        return super.getReceivingDeliveryCampusCode(po);

    }    
}
