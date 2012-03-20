/*
 * Copyright 2005-2007 The Kuali Foundation
 *
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kew.docsearch.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.kuali.rice.kew.docsearch.DocSearchUtils;
import org.kuali.rice.kew.docsearch.SearchableAttribute;
import org.kuali.rice.kew.docsearch.SearchableAttributeValue;
import org.kuali.rice.kew.docsearch.service.SearchableAttributeProcessingService;
import org.kuali.rice.kew.doctype.bo.DocumentType;
import org.kuali.rice.kew.doctype.service.DocumentTypeService;
import org.kuali.rice.kew.exception.WorkflowRuntimeException;
import org.kuali.rice.kew.routeheader.DocumentRouteHeaderValueContent;
import org.kuali.rice.kew.service.KEWServiceLocator;
import org.kuali.rice.kns.util.ObjectUtils;


/**
 * Implementation of {@link SearchableAttributeProcessingService}.
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class SearchableAttributeProcessor implements SearchableAttributeProcessingService {

	private static Logger LOG = Logger.getLogger(SearchableAttribute.class);

	public void indexDocument(Long documentId) {
		indexDocument(documentId, true);
	}

	public void indexDocument(Long documentId, boolean useMostRecentDocType) {
		long t1 = System.currentTimeMillis();	
		if (LOG.isInfoEnabled()) {
			LOG.info("Indexing document " + documentId + " for document search...");
		}
		try {
			int count = 0;
			DocumentType documentType = KEWServiceLocator.getDocumentTypeService().findByDocumentId(documentId);
			while(ObjectUtils.isNull(documentType)) {
				Thread.sleep(1000);
				documentType = KEWServiceLocator.getDocumentTypeService().findByDocumentId(documentId);
				count++;
				if (count > 3) {
					LOG.error("Failed to retrieve Document: "+ documentId +". Indexing Failed");
					return;
				}

			}
			if (LOG.isInfoEnabled()) {
				LOG.info("docType: [" + documentType +"]");
			}
			DocumentRouteHeaderValueContent docContent = KEWServiceLocator.getRouteHeaderService().getContent(documentId);
			List<SearchableAttributeValue> attributes = buildSearchableAttributeValues(documentType, documentId, docContent.getDocumentContent(), useMostRecentDocType);
			KEWServiceLocator.getRouteHeaderService().updateRouteHeaderSearchValues(documentId, attributes);
		} catch (Exception e) {
			String errorMsg = "Encountered an error when attempting to index searchable attributes, requeuing.";
			LOG.error(errorMsg, e);
			throw new WorkflowRuntimeException(errorMsg,e);
		}
		long t2 = System.currentTimeMillis();
		if (LOG.isInfoEnabled()) {
			LOG.info("...finished indexing document " + documentId + " for document search, total time = " + (t2-t1) + " ms.");
		}
	}

	private List<SearchableAttributeValue> buildSearchableAttributeValues(DocumentType docType, Long documentId, String docContent, boolean useMostRecentDocType) {
		if (useMostRecentDocType) {
			if (LOG.isInfoEnabled()) {
				LOG.info("docType: [" + docType +"]; documentId: [" + documentId + "]");
			}
			
			DocumentTypeService docTypeService = KEWServiceLocator.getDocumentTypeService();
			String docTypeName = docType.getName();
			docType = docTypeService.findByName(docTypeName);
		}
		List<SearchableAttributeValue> searchableAttributeValues = new ArrayList<SearchableAttributeValue>();

		for (Iterator iterator = docType.getSearchableAttributes().iterator(); iterator.hasNext();) {
			SearchableAttribute searchableAttribute = (SearchableAttribute) iterator.next();
			List searchStorageValues = searchableAttribute.getSearchStorageValues(
					DocSearchUtils.getDocumentSearchContext(documentId.toString(), docType.getName(), docContent));
			if (searchStorageValues != null) {
				for (Iterator iterator2 = searchStorageValues.iterator(); iterator2.hasNext();) {
					SearchableAttributeValue searchableAttributeValue = (SearchableAttributeValue) iterator2.next();
					searchableAttributeValue.setRouteHeaderId(documentId);
					searchableAttributeValues.add(searchableAttributeValue);
					searchableAttributeValue.setRouteHeader(null); // let the routeHeaderId we set represent this reference
				}
			}
		}

		return searchableAttributeValues;
	}

}
