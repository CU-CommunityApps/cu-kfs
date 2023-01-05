package edu.cornell.kfs.fp.document.service.impl;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.sql.Date;

import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.fp.dataaccess.DisbursementVoucherDao;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.pdp.businessobject.PaymentGroup;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.PaymentSourceHelperService;
import org.kuali.kfs.sys.service.GeneralLedgerPendingEntryService;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.parameter.ParameterEvaluatorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.document.CuDisbursementVoucherDocument;
import edu.cornell.kfs.fp.service.CUPaymentMethodGeneralLedgerPendingEntryService;

@ConfigureContext(session = ccs1)
public class CuDisbursementVoucherExtractionHelperServiceImplIntegTest extends KualiIntegTestBase {
	private DocumentService documentService;
	private CuDisbursementVoucherExtractionHelperServiceImpl cuDisbursementVoucherExtractionHelperService;
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        documentService = SpringContext.getBean(DocumentService.class);
        cuDisbursementVoucherExtractionHelperService = CuDisbursementVoucherExtractionHelperServiceImpl.class.newInstance();
        cuDisbursementVoucherExtractionHelperService.setPaymentMethodGeneralLedgerPendingEntryService(SpringContext.getBean(CUPaymentMethodGeneralLedgerPendingEntryService.class));
        cuDisbursementVoucherExtractionHelperService.setBusinessObjectService(SpringContext.getBean(BusinessObjectService.class));
        cuDisbursementVoucherExtractionHelperService.setDocumentService(documentService);
        cuDisbursementVoucherExtractionHelperService.setGeneralLedgerPendingEntryService(SpringContext.getBean(GeneralLedgerPendingEntryService.class));
        cuDisbursementVoucherExtractionHelperService.setParameterService(SpringContext.getBean(ParameterService.class));
        cuDisbursementVoucherExtractionHelperService.setParameterEvaluatorService(SpringContext.getBean(ParameterEvaluatorService.class));
        cuDisbursementVoucherExtractionHelperService.setVendorService(SpringContext.getBean(VendorService.class));
        cuDisbursementVoucherExtractionHelperService.setPaymentSourceHelperService(SpringContext.getBean(PaymentSourceHelperService.class));
        cuDisbursementVoucherExtractionHelperService.setDisbursementVoucherDao(SpringContext.getBean(DisbursementVoucherDao.class));
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
			dv.setCampusCode("IT");
			
			SourceAccountingLine accountingLine = new SourceAccountingLine();								 
						
			
			accountingLine.setChartOfAccountsCode("IT");
			accountingLine.setAccountNumber("G081040");
			accountingLine.setFinancialObjectCode("8462");
			accountingLine.setAmount((new KualiDecimal(86.00)));
						
			accountingLine.setPostingYear(dv.getPostingYear());
			accountingLine.setDocumentNumber(dv.getDocumentNumber());

		
			dv.addSourceAccountingLine(accountingLine);

			documentService.saveDocument(dv);
			
		}			        
		
        Date transactionTimestamp = new Date(SpringContext.getBean(DateTimeService.class).getCurrentDate().getTime());
        Date processRunDate = new java.sql.Date(transactionTimestamp.getTime());
		PaymentGroup pg = cuDisbursementVoucherExtractionHelperService.createPaymentGroup(dv, processRunDate);
		
		assertTrue(pg.getPaymentDetails().get(0).getCustPaymentDocNbr().equalsIgnoreCase(dv.getDocumentNumber()));
		
	}
    

}