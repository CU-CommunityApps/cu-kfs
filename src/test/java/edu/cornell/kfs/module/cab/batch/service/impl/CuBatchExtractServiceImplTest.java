package edu.cornell.kfs.module.cab.batch.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.kuali.kfs.gl.businessobject.Entry;
import org.kuali.kfs.module.cab.batch.service.BatchExtractService;
import org.kuali.kfs.module.purap.document.PaymentRequestDocument;
import org.kuali.kfs.module.purap.document.VendorCreditMemoDocument;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.krad.service.BusinessObjectService;

import edu.cornell.kfs.module.cab.fixture.EntryFixture;

@ConfigureContext
public class CuBatchExtractServiceImplTest extends KualiTestBase {
	
	private BatchExtractService batchExtractService;
	private CuBatchExtractServiceImpl cuBatchExtractServiceImpl;
	private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CuBatchExtractServiceImpl.class);
	private BusinessObjectService businessObjectService;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		batchExtractService =  SpringContext.getBean(BatchExtractService.class);
		businessObjectService = SpringContext.getBean(BusinessObjectService.class);
		cuBatchExtractServiceImpl = new CuBatchExtractServiceImpl();
		cuBatchExtractServiceImpl.setBusinessObjectService(businessObjectService);
	}
	
	public void testFindCreditMemoDocument () {
		
		Entry theEntry = EntryFixture.VCM_ONE.createEntry();
	
		VendorCreditMemoDocument vcm = cuBatchExtractServiceImpl.findCreditMemoDocument(EntryFixture.VCM_TWO.createEntry());

		assertTrue("vcm isn't null", null!=vcm);
		
		VendorCreditMemoDocument vendorCreditMemoDocument = cuBatchExtractServiceImpl.findCreditMemoDocument(theEntry);
	
		assertTrue("vendor credit memo isn't null", null!=vendorCreditMemoDocument);
		
		vendorCreditMemoDocument = cuBatchExtractServiceImpl.findCreditMemoDocument(EntryFixture.VCM_THREE.createEntry());
		
		assertTrue("vendor credit memo is null", null==vendorCreditMemoDocument);
		
	}
	
	public void testFindPaymentRequestDocument() {
		PaymentRequestDocument prd = cuBatchExtractServiceImpl.findPaymentRequestDocument(EntryFixture.PREQ_ONE.createEntry());
		
		assertTrue("Payment request document is not null", prd!=null);
		
		prd = cuBatchExtractServiceImpl.findPaymentRequestDocument(EntryFixture.PREQ_THREE.createEntry());
		
		assertTrue("Payment request document is null", prd==null);
		
	}
	
	public void testSeparatePOLines() {
		Collection<Entry> glEntries = new ArrayList<Entry>();
		glEntries.add(EntryFixture.VCM_ONE.createEntry());
		glEntries.add(EntryFixture.VCM_TWO.createEntry());
		glEntries.add(EntryFixture.PREQ_ONE.createEntry());
		glEntries.add(EntryFixture.PREQ_TWO.createEntry());
		
		List<Entry> fpEntries = (List) new ArrayList<Entry>();
		List<Entry> purapEntries = (List) new ArrayList<Entry>();
		
		
		cuBatchExtractServiceImpl.separatePOLines(fpEntries, purapEntries, glEntries);
	
		System.out.println("fpEntries size: " + fpEntries.size());
		
		System.out.println("purapEntries size: " + purapEntries.size());
		
		assertTrue("fpEntries size is zero", fpEntries.size() == 0);
		
		assertTrue("purapEntries size is greater than zero", purapEntries.size() > 0);
		
	}
}
