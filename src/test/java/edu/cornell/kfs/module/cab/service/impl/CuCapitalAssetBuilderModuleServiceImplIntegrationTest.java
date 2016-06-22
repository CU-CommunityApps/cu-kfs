package edu.cornell.kfs.module.cab.service.impl;

import edu.cornell.kfs.module.cab.fixture.AccountingDocumentFixture;
import edu.cornell.kfs.module.cab.fixture.CapitalAssetInformationFixture;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.integration.cab.CapitalAssetBuilderModuleService;
import org.kuali.kfs.module.cab.service.impl.CapitalAssetBuilderModuleServiceImpl;
import org.kuali.kfs.module.cam.CamsConstants;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiTestBase;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.AccountingDocument;

import java.util.ArrayList;
import java.util.List;

@ConfigureContext
public class CuCapitalAssetBuilderModuleServiceImplIntegrationTest extends KualiTestBase {
	
	private CapitalAssetBuilderModuleService capitalAssetBuilderModuleService;

    @Override
    protected void setUp() throws Exception {
    	super.setUp();
		capitalAssetBuilderModuleService = SpringContext.getBean(CapitalAssetBuilderModuleServiceImpl.class);
    }

    public void testValidateAssetTagLocationLines() {
		CapitalAssetInformation capitalAssetInformation = CapitalAssetInformationFixture.ONE.createCapitalAssetInformation();
		int capitalAssetIndex = 0;
		AccountingDocument accountingDocument = AccountingDocumentFixture.ONE.createAccountingDocument();
		((CuCapitalAssetBuilderModuleServiceImpl)capitalAssetBuilderModuleService).validateAssetTagLocationLines(capitalAssetInformation, capitalAssetIndex, accountingDocument);
    }

	public void testBuildNoteTextForPurApDoc() {
		List<Long> assetNumbers = setupAssetNumbers();

		String noteText = ((CuCapitalAssetBuilderModuleServiceImpl)capitalAssetBuilderModuleService).buildNoteTextForPurApDoc(CamsConstants.DocumentTypeName.ASSET_EDIT, assetNumbers);
		assertEquals("Existing Asset Numbers have been applied for this document: 1,2,3,4", noteText);
	}

	public void testBuildNoteTextForPurApDocAssetGlobal() {
		List<Long> assetNumbers = setupAssetNumbers();

		String noteText = ((CuCapitalAssetBuilderModuleServiceImpl)capitalAssetBuilderModuleService).buildNoteTextForPurApDoc(CamsConstants.DocumentTypeName.ASSET_ADD_GLOBAL, assetNumbers);
		assertEquals("Asset Numbers have been created for this document: 1,2,3,4", noteText);
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
