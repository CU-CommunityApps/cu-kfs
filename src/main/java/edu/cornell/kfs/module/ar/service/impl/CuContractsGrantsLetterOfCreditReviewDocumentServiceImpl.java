package edu.cornell.kfs.module.ar.service.impl;

import org.kuali.kfs.module.ar.document.service.impl.ContractsGrantsLetterOfCreditReviewDocumentServiceImpl;

/*
 * CU Customization.  We backported FINP-10147 changes into our overlay class
 * ContractsGrantsInvoiceCreateDocumentServiceImpl.  FINP-10147 changes to 
 * ContractsGrantsLetterOfCreditReviewDocumentServiceImpl as FINP-10147 edited
 * lines in ContractsGrantsLetterOfCreditReviewDocumentServiceImpl that do not
 * exist in our version of financials
 * This extension exists so our upgrade review tools will draw attention to this class
 * and changes considered so we don't add any bugs fixed by FINP-10147
 */
public class CuContractsGrantsLetterOfCreditReviewDocumentServiceImpl
        extends ContractsGrantsLetterOfCreditReviewDocumentServiceImpl {

}
