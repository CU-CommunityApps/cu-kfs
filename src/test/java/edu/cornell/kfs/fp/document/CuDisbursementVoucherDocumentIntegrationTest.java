package edu.cornell.kfs.fp.document;

import static org.kuali.kfs.sys.fixture.UserNameFixture.ccs1;

import java.sql.Date;
import java.util.Calendar;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.kuali.kfs.fp.businessobject.DisbursementVoucherNonresidentTax;
import org.kuali.kfs.fp.document.DisbursementVoucherDocument;
import org.kuali.kfs.kns.util.KNSGlobalVariables;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.vnd.businessobject.VendorAddress;
import org.kuali.kfs.vnd.businessobject.VendorDetail;
import org.kuali.kfs.vnd.document.service.VendorService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.kew.api.exception.WorkflowException;

import edu.cornell.kfs.fp.document.service.CuDisbursementVoucherDefaultDueDateService;

@ConfigureContext(session = ccs1)
public class CuDisbursementVoucherDocumentIntegrationTest extends KualiIntegTestBase {

    private CuDisbursementVoucherDocument cuDisbursementVoucherDocument;
    private CuDisbursementVoucherDefaultDueDateService cuDisbursementVoucherDefaultDueDateService;
    private DocumentService documentService;
    private VendorService vendorService;

    @Override
    public void setUp() throws Exception {
        cuDisbursementVoucherDefaultDueDateService = SpringContext.getBean(CuDisbursementVoucherDefaultDueDateService.class);
        documentService = SpringContext.getBean(DocumentService.class);
        vendorService = SpringContext.getBean(VendorService.class);
        cuDisbursementVoucherDocument = setupCuDisbursementVoucherDocument();
        KNSGlobalVariables.getMessageList().clear();
    }

    @Test
    public void testToCopy() throws WorkflowException {
        String payeeidNumber = cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber();

        cuDisbursementVoucherDocument.toCopy();

        assertEquals("Test Document Description", cuDisbursementVoucherDocument.getDocumentHeader().getDocumentDescription());
        assertEquals("Salino, Catherine C.", cuDisbursementVoucherDocument.getDisbVchrContactPersonName());
        assertEquals("IT", cuDisbursementVoucherDocument.getCampusCode());
        assertEquals(cuDisbursementVoucherDefaultDueDateService.findDefaultDueDate(), cuDisbursementVoucherDocument.getDisbursementVoucherDueDate());
        assertEquals("O", cuDisbursementVoucherDocument.getDisbursementVoucherDocumentationLocationCode());
        assertEquals("JPCD", cuDisbursementVoucherDocument.getDisbVchrBankCode());

        assertEquals("607-255-9466", cuDisbursementVoucherDocument.getDisbVchrContactPhoneNumber());
        assertEquals("ccs1@cornell.edu", cuDisbursementVoucherDocument.getDisbVchrContactEmailId());
        assertEquals(StringUtils.EMPTY, cuDisbursementVoucherDocument.getDisbVchrPayeeTaxControlCode());

        assertEquals(new KualiDecimal(86.00), cuDisbursementVoucherDocument.getDisbVchrCheckTotalAmount());

        DisbursementVoucherNonresidentTax disbursementVoucherNonresidentTax = cuDisbursementVoucherDocument.getDvNonresidentTax();
        assertNull(disbursementVoucherNonresidentTax.getDocumentNumber());
        assertNull(disbursementVoucherNonresidentTax.getFederalIncomeTaxPercent());
        assertNull(disbursementVoucherNonresidentTax.getStateIncomeTaxPercent());
        assertNull(disbursementVoucherNonresidentTax.getIncomeClassCode());
        assertNull(disbursementVoucherNonresidentTax.getPostalCountryCode());
        assertFalse(disbursementVoucherNonresidentTax.isIncomeTaxTreatyExemptCode());
        assertFalse(disbursementVoucherNonresidentTax.isForeignSourceIncomeCode());
        assertFalse(disbursementVoucherNonresidentTax.isIncomeTaxGrossUpCode());
        assertNull(disbursementVoucherNonresidentTax.getReferenceFinancialSystemOriginationCode());
        assertNull(disbursementVoucherNonresidentTax.getReferenceFinancialDocumentNumber());
        assertNull(disbursementVoucherNonresidentTax.getFinancialDocumentAccountingLineText());
        assertNull(disbursementVoucherNonresidentTax.getTaxNQIId());
        assertFalse(disbursementVoucherNonresidentTax.isTaxOtherExemptIndicator());
        assertFalse(disbursementVoucherNonresidentTax.isTaxUSAIDPerDiemIndicator());
        assertNull(disbursementVoucherNonresidentTax.getTaxSpecialW4Amount());
        assertNull(disbursementVoucherNonresidentTax.getIncomeClass());

        assertFalse(cuDisbursementVoucherDocument.getWireTransfer().isWireTransferFeeWaiverIndicator());

        assertNull(cuDisbursementVoucherDocument.getExtractDate());
        assertNull(cuDisbursementVoucherDocument.getPaidDate());
        assertNull(cuDisbursementVoucherDocument.getCancelDate());
        assertEquals(KFSConstants.DocumentStatusCodes.INITIATED, cuDisbursementVoucherDocument.getDocumentHeader().getFinancialDocumentStatusCode());

        assertEquals(0, KNSGlobalVariables.getMessageList().size());
        assertEquals(payeeidNumber, cuDisbursementVoucherDocument.getDvPayeeDetail().getDisbVchrPayeeIdNumber());
    }

    private CuDisbursementVoucherDocument setupCuDisbursementVoucherDocument() throws WorkflowException{
        CuDisbursementVoucherDocument dv = (CuDisbursementVoucherDocument) SpringContext.getBean(DocumentService.class).getNewDocument(DisbursementVoucherDocument.class);

        dv.getDocumentHeader().setDocumentDescription("Test Document Description");
        dv.getDocumentHeader().setExplanation("Stuff");
        dv.initiateDocument();

        VendorDetail vendor = vendorService.getVendorDetail("13366-0");
        VendorAddress vendoraddress = vendorService.getVendorDefaultAddress(
                vendor.getVendorHeaderGeneratedIdentifier(), vendor.getVendorDetailAssignedIdentifier(), "RM", "");

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

        return dv;
    }
    

}