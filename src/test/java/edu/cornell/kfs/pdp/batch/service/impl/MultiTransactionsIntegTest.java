package edu.cornell.kfs.pdp.batch.service.impl;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.krad.service.DocumentService;
import org.kuali.kfs.ksb.messaging.threadpool.KSBThreadPool;
import org.kuali.kfs.sys.ConfigureContext;
import org.kuali.kfs.sys.context.KualiIntegTestBase;
import org.kuali.kfs.sys.context.SpringContext;

import edu.cornell.kfs.pdp.batch.service.PayeeACHAccountDocumentService;

public class MultiTransactionsIntegTest extends KualiIntegTestBase {

    private static final Logger LOG = LogManager.getLogger();

    private static final DummyIntegTest dummyTest = new DummyIntegTest();

    private PayeeACHAccountDocumentService payeeACHAccountDocumentService;
    private DocumentService documentService;
    private KSBThreadPool threadPool;

    @Override
    public void setUp() throws Exception {
        if (!dummyTest.springContextInitialized) {
            try {
                dummyTest.runBare();
            } catch (final Throwable t) {
                throw new Exception(t);
            }
        }
        super.setUp();
        payeeACHAccountDocumentService = SpringContext.getBean(PayeeACHAccountDocumentService.class);
        documentService = SpringContext.getBean(DocumentService.class);
        threadPool = SpringContext.getBean(KSBThreadPool.class);
    }

    @Override
    public void tearDown() throws Exception {
        payeeACHAccountDocumentService = null;
        documentService = null;
        threadPool = null;
        super.tearDown();
    }

    public void testDailyReportJobExecution() throws Exception {
        final List<String> docIds = List.of("55000000", "59429967", "59428520", "59427254");
        final List<Callable<Boolean>> tasks = IntStream.range(0, 75)
                .mapToObj(index -> createDocExistsTask(index, docIds))
                .collect(Collectors.toUnmodifiableList());

        List<Future<Boolean>> results = threadPool.invokeAll(tasks);
        int i = 0;
        for (Future<Boolean> result : results) {
            final Boolean boolResult = result.get();
            LOG.info("Search {} had outcome of {}", i, boolResult);
            i++;
        }
    }

    private Callable<Boolean> createDocExistsTask(final int index, final List<String> docIds) {
        final int elementIndex = index % docIds.size();
        final boolean runACHDetailSearchFirst = Math.round(Math.random()) == 1L;
        final String docId = docIds.get(elementIndex);
        return () -> {
            if (runACHDetailSearchFirst) {
                runACHDetailSearch(index);
            }
            LOG.info("Running search {} for document {}", index, docId);
            final boolean result = documentService.documentExists(docId);
            if (!runACHDetailSearchFirst) {
                runACHDetailSearch(index);
            }
            return result;
        };
    }

    private void runACHDetailSearch(final int index) throws Exception {
        LOG.info("Running search {} for ACH Details", index);
        payeeACHAccountDocumentService.getPersistedPayeeACHAccountExtractDetails();
    }

    @ConfigureContext
    private static class DummyIntegTest extends KualiIntegTestBase {
        private boolean springContextInitialized = false;

        private DummyIntegTest() {
            setName("runTest");
        }

        @Override
        protected void runTest() {
            springContextInitialized = true;
        }
    }

}
