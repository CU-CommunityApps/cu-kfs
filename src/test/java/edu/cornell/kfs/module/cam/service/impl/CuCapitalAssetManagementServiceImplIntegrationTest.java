package edu.cornell.kfs.module.cam.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.module.cam.service.impl.CapitalAssetManagementServiceImpl;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;

import edu.cornell.kfs.module.cam.fixture.AccountingDocumentFixture;
import edu.cornell.kfs.module.cam.fixture.CapitalAssetInformationFixture;
import junit.framework.TestCase;

@ConfigureContext
public class CuCapitalAssetManagementServiceImplIntegrationTest extends KualiIntegTestBase {
	
	private CapitalAssetManagementServiceImpl capitalAssetManagementService;

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
		capitalAssetManagementService = SpringContext.getBean(CapitalAssetManagementServiceImpl.class);
    }

    public void testValidateAssetTagLocationLines() {
		CapitalAssetInformation capitalAssetInformation = CapitalAssetInformationFixture.ONE.createCapitalAssetInformation();
		int capitalAssetIndex = 0;
		AccountingDocument accountingDocument = AccountingDocumentFixture.ONE.createAccountingDocument();
		((CuCapitalAssetManagementServiceImpl) capitalAssetManagementService).validateAssetTagLocationLines(capitalAssetInformation, capitalAssetIndex, accountingDocument);
    }

	public void testBuildNoteTextForPurApDoc() {
		List<Long> assetNumbers = setupAssetNumbers();

		String noteText = ((CuCapitalAssetManagementServiceImpl) capitalAssetManagementService).buildNoteTextForPurApDoc(CamsConstants.DocumentTypeName.ASSET_EDIT, assetNumbers);
		TestCase.assertEquals("Existing Asset Numbers have been applied for this document: 1,2,3,4", noteText);
	}

	public void testBuildNoteTextForPurApDocAssetGlobal() {
		List<Long> assetNumbers = setupAssetNumbers();

		String noteText = ((CuCapitalAssetManagementServiceImpl) capitalAssetManagementService).buildNoteTextForPurApDoc(CamsConstants.DocumentTypeName.ASSET_ADD_GLOBAL, assetNumbers);
		TestCase.assertEquals("Asset Numbers have been created for this document: 1,2,3,4", noteText);
	}

	private List<Long> setupAssetNumbers() {
		List<Long> assetNumbers = new ArrayList<>();
		assetNumbers.add(new Long(1));
		assetNumbers.add(new Long(2));
		assetNumbers.add(new Long(3));
		assetNumbers.add(new Long(4));
		return assetNumbers;
	}

}
