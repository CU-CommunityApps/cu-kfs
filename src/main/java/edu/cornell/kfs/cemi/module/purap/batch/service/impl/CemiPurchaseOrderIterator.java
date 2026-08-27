package edu.cornell.kfs.cemi.module.purap.batch.service.impl;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.document.Document;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.module.purap.document.PurchaseOrderDocument;

import edu.cornell.kfs.cemi.module.purap.CemiPurchaseOrderConstants;
import edu.cornell.kfs.cemi.module.purap.batch.businessobject.CemiPurchaseOrderIdBo;

public class CemiPurchaseOrderIterator implements Iterator<PurchaseOrderDocument> {

    private final Iterator<CemiPurchaseOrderIdBo> purchaseOrderIdsIterator;
    private final DocumentService documentService;
    private final DataDictionaryService dataDictionaryService;

    private Iterator<Document> documentSubIterator;

    /*
     * NOTE: It is assumed that the provided iterator will return results in ascending order by document number.
     *       This class's PO retrieval logic will preserve the same ordering.
     */
    public CemiPurchaseOrderIterator(final Iterator<CemiPurchaseOrderIdBo> purchaseOrderIdsIterator,
            final DocumentService documentService, final DataDictionaryService dataDictionaryService) {
        Validate.notNull(purchaseOrderIdsIterator, "purchaseOrderIdsIterator cannot be null");
        Validate.notNull(documentService, "documentService cannot be null");
        Validate.notNull(dataDictionaryService, "dataDictionaryService cannot be null");
        this.purchaseOrderIdsIterator = purchaseOrderIdsIterator;
        this.documentService = documentService;
        this.dataDictionaryService = dataDictionaryService;
        this.documentSubIterator = IteratorUtils.emptyIterator();
    }

    @Override
    public boolean hasNext() {
        return documentSubIterator.hasNext() || purchaseOrderIdsIterator.hasNext();
    }

    @Override
    public PurchaseOrderDocument next() {
        Validate.validState(hasNext(), "There are no more Purchase Orders left in this Iterator");
        if (!documentSubIterator.hasNext()) {
            documentSubIterator = createNewDocumentSubIterator();
        }
        return (PurchaseOrderDocument) documentSubIterator.next();
    }

    private Iterator<Document> createNewDocumentSubIterator() {
        final CemiPurchaseOrderIdBo[] nextIds = getNextBatchOfIds();
        Validate.validState(nextIds.length > 0, "There should have been at least one more Purchase Order to load");

        final Map<String, List<String>> docIdsGroupedByDocType = Arrays.stream(nextIds)
                .collect(Collectors.groupingBy(CemiPurchaseOrderIdBo::getDocumentTypeName, 
                        Collectors.mapping(CemiPurchaseOrderIdBo::getDocumentNumber, Collectors.toUnmodifiableList())));

        final List<Document> nextDocuments = docIdsGroupedByDocType.entrySet().stream()
                .map(entry -> getPurchaseOrderDocuments(entry.getKey(), entry.getValue()))
                .flatMap(List::stream)
                .sorted(Comparator.comparing(Document::getDocumentNumber))
                .collect(Collectors.toUnmodifiableList());

        return nextDocuments.iterator();
    }

    private CemiPurchaseOrderIdBo[] getNextBatchOfIds() {
        final Stream.Builder<CemiPurchaseOrderIdBo> nextIds = Stream.builder();
        int idCount = 0;
        while (purchaseOrderIdsIterator.hasNext()
                && idCount < CemiPurchaseOrderConstants.MAX_PURCHASE_ORDER_PRELOAD_BATCH_SIZE) {
            nextIds.add(purchaseOrderIdsIterator.next());
            idCount++;
        }
        return nextIds.build().toArray(CemiPurchaseOrderIdBo[]::new);
    }

    private List<Document> getPurchaseOrderDocuments(final String documentTypeName, final List<String> documentIds) {
        final Class<? extends Document> documentClass = dataDictionaryService.getDocumentClassByTypeName(documentTypeName);
        Validate.validState(documentClass != null, "Could not find document class for doc type: %s", documentTypeName);

        final List<Document> results = documentService.getDocumentsByListOfDocumentHeaderIds(documentClass, documentIds);
        final Set<String> foundDocumentIds = results.stream()
                .map(Document::getDocumentNumber)
                .collect(Collectors.toUnmodifiableSet());
        Validate.validState(documentIds.size() == foundDocumentIds.size(),
                "Search should have returned %s documents, but it actually returned %s documents instead",
                documentIds.size(), foundDocumentIds.size());

        final boolean documentIdsMatch = documentIds.stream().allMatch(foundDocumentIds::contains);
        Validate.validState(documentIdsMatch, "Found mismatched document results; expected: %s, actual: %s",
                documentIds, foundDocumentIds);

        return results;
    }

}
