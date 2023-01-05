package edu.cornell.kfs.fp.document.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.util.List;

import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;

@ConfigureContext(session = ccs1)
public class DebitDeterminerServiceImplIntegTest extends KualiIntegTestBase {
	private DocumentService documentService;
	private DebitDeterminerServiceImpl debitDeterminerService;
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        documentService = SpringContext.getBean(DocumentService.class);
        debitDeterminerService = DebitDeterminerServiceImpl.class.newInstance();
	}
                  
	public void test(){
		
		CuDisbursementVoucherDocument dv = (CuDisbursementVoucherDocument) SpringContext.getBean(DocumentService.class).getNewDocument(DisbursementVoucherDocument.class);
		
        if(dv != null) {
			dv.getDocumentHeader().setDocumentDescription("Test Document Description");
			dv.getDocumentHeader().setExplanation("Stuff");			
			
			dv.initiateDocument();			

			VendorDetail vendor = SpringContext.getBean(VendorService.class).getVendorDetail("13366-0");
			VendorAddress vendoraddress = SpringContext.getBean(VendorService.class).getVendorDefaultAddress(vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier(),
					"RM", "");
			
			System.out.println(vendoraddress.getVendorCityName()+"\n");
			
			dv.templateVendor(vendor, vendoraddress);
			
			dv.setPayeeAssigned(true);
			
			dv.getDvPayeeDetail().setDisbVchrPaymentReasonCode("S");
			
			dv.setDisbVchrCheckTotalAmount(new KualiDecimal(86.00));
			dv.setDisbVchrPaymentMethodCode("P");

			dv.setDisbVchrCheckStubText("check text");
			
			SourceAccountingLine accountingLine = new SourceAccountingLine();								 
						
			
			accountingLine.setChartOfAccountsCode("IT");
			accountingLine.setAccountNumber("G081040");
			accountingLine.setFinancialObjectCode("8462");
			accountingLine.setAmount((new KualiDecimal(-14.00)));
			
			
			 accountingLine.setPostingYear(dv.getPostingYear());
			 accountingLine.setDocumentNumber(dv.getDocumentNumber());

		
			dv.addSourceAccountingLine(accountingLine);
						
			SourceAccountingLine accountingLine2 = new SourceAccountingLine();	
			
			accountingLine2.setChartOfAccountsCode("IT");
			accountingLine2.setAccountNumber("1453611");
			accountingLine2.setFinancialObjectCode("8462");
			accountingLine2.setAmount((new KualiDecimal(100.00)));
			
			
			accountingLine2.setPostingYear(dv.getPostingYear());
			accountingLine2.setDocumentNumber(dv.getDocumentNumber());
			
			dv.addSourceAccountingLine(accountingLine2);
			
			documentService.saveDocument(dv);
			
		}			        
		
		List<GeneralLedgerPendingEntrySourceDetail> glpeS = dv.getGeneralLedgerPendingEntrySourceDetails();		
		GeneralLedgerPendingEntrySourceDetail postable = glpeS.get(0);
		

		System.out.println("GL Detail"+postable.toString()+"\n");
		
		assertFalse(debitDeterminerService.isDebitConsideringNothingPositiveOnly(dv, postable));
		
	}
    

}