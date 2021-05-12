package edu.cornell.kfs.sys.service.impl.fixture;

import edu.cornell.kfs.sys.fixture.CuAccountingLineFixture;
import org.kuali.kfs.fp.document.IndirectCostAdjustmentDocument;
import org.kuali.kfs.sys.DocumentTestUtils;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kew.api.exception.WorkflowException;
import org.kuali.kfs.krad.service.DocumentService;

public enum IndirectCostAdjustmentDocumentFixture {
    ICA_GOOD();
    public IndirectCostAdjustmentDocument createIndirectCostAdjustmentDocument() throws IllegalAccessException, InstantiationException, WorkflowException {
        IndirectCostAdjustmentDocument document = DocumentTestUtils.createDocument(SpringContext.getBean(DocumentService.class),  IndirectCostAdjustmentDocument.class);
    
        CuAccountingLineFixture.ICA_LINE_1.addAsSourceTo(document);
        CuAccountingLineFixture.ICA_LINE_2.addAsTargetTo(document);

 
        return document;
   }

}
