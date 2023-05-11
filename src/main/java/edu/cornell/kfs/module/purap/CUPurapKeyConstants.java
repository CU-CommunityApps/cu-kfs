package edu.cornell.kfs.module.purap;

/**
 *
 */
public class CUPurapKeyConstants {
	public static final String ERROR_PAYMENT_REQUEST_ITEM_TOTAL_NOT_EQUAL = "error.paymentRequest.item.TotalInvoice.notEqual";
	
    public static final String PURAP_ITEM_NONQTY = "error.purap.item.itemtype.nonqty";
    public static final String PURAP_ITEM_NEW_NONQTY = "error.purap.item.new.itemtype.nonqty";
    public static final String PURAP_MOPOT_REQUIRED_DATA_MISSING = "error.purap.mopot.required.data.missing";  //KFSPTS-1458
    
    public static final String I_WANT_ITEMS_SOLE_SOURCE_NOTE = "note.iWant.document.itemSoleSource";
    
    //error messages
    public static final String ERROR_IWNT_CONMPLETE_ORDER_OPTION_REQUIRED = "error.iWant.document.completeOption.required";
    public static final String ERROR_IWNT_REQUISITION_EXISTS = "error.iWant.document.requisition.exists";
    public static final String ERROR_IWNT_CREATOR_CANNOT_ROUTE_TO_SELF = "error.iWant.document.creator.cannot.route.to.self";
    public static final String ERROR_IWNT_APPROVER_CANNOT_ROUTE_TO_SELF = "error.iWant.document.approver.cannot.route.to.self";
    
    public static final String ERROR_POA_INITIATOR_CANNOT_ADHOC_TO_FO = "error.poa.initiator.cannot.adhoc.to.fo";
    public static final String ERROR_ADD_NEW_NOTE_SEND_TO_VENDOR_NO_ATT = "errors.add.new.note.sendToVendor.noAtt";
    public static final String ERROR_EXCEED_SQ_NUMBER_OF_ATT_LIMIT = "errors.exceed.sq.number.of.att.limit";
    public static final String ERROR_REASON_IS_REQUIRED = "errors.reason.is.required";
    public static final String ERROR_ATT_SEND_TO_VENDOR_FILE_SIZE_OVER_LIMIT = "errors.att.send.to.vendor.filesize.over.limit";

    public static final String ATTACHMENT_QUESTION_CONFIRM_CHANGE = "attachment.message.confirm.change";
    
    // KFSPTS-2096
    public static final String PURAP_MIX_ITEM_QTY_NONQTY = "error.purap.mix.item.itemtype.nonqty";

  //KFSPTS-2527
    public static final String ERROR_DV_OR_REQ_ALREADY_CREATED_FROM_IWNT = "error.document.iwnt.dvOrReqAlreadyCreated";

    // KFSUPGRADE-779
    public static final String ERROR_PAYMENTMETHODCODE_MUSTMATCHPREQ = "error.document.creditmemo.paymentmethodcode.mustmatchpreq";
    
    public static final String ERROR_REJECT_INVOICE_PO_VENDOR_MISMATCH = "errors.reject.invoice.po.vendor.mismatch";

    public static final String IWNT_NOTE_CREATE_DV = "iwnt.note.create.dv";
    public static final String IWNT_NOTE_CREATE_REQS = "iwnt.note.create.reqs";

}
