package edu.cornell.kfs.module.purap.businessobject;

import java.util.ArrayList;
import java.util.Collection;

import edu.cornell.kfs.module.purap.document.BatchIWantDocument;

public class IWantDocumentBatchFeed {
	
	protected Collection<BatchIWantDocument> batchIWantDocuments;
	
	public IWantDocumentBatchFeed() {

		batchIWantDocuments = new ArrayList<BatchIWantDocument>();
	}

	public Collection<BatchIWantDocument> getBatchIWantDocuments() {
		return batchIWantDocuments;
	}

	public void setBatchIWantDocuments(
			Collection<BatchIWantDocument> batchIWantDocuments) {
		this.batchIWantDocuments = batchIWantDocuments;
	}
	
	public void addIWantDocument(BatchIWantDocument batchIWantDocument){
		batchIWantDocuments.add(batchIWantDocument);
	}

}
