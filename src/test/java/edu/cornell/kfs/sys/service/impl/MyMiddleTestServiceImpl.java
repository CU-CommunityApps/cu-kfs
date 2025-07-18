package edu.cornell.kfs.sys.service.impl;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.kuali.kfs.ksb.messaging.threadpool.KSBThreadPool;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.service.MyMiddleTestService;
import edu.cornell.kfs.sys.service.MyTestService;

public class MyMiddleTestServiceImpl implements MyMiddleTestService {

    private KSBThreadPool threadPool;
    private MyTestService myTestService;

    @Transactional
    @Override
    public void triggerUpdates() throws Exception {
        final List<Callable<String>> callables = IntStream.range(0, 100)
                .mapToObj(this::createTask)
                .collect(Collectors.toUnmodifiableList());
        threadPool.invokeAll(callables);
        threadPool.invokeAll(callables);
        threadPool.invokeAll(callables);
        threadPool.invokeAll(callables);
        threadPool.invokeAll(callables);
    }

    private Callable<String> createTask(final int index) {
        final String indexStr = String.valueOf(index);
        return () -> {
            myTestService.mergeTestObject(indexStr);
            return indexStr;
        };
    }

    public void setThreadPool(final KSBThreadPool threadPool) {
        this.threadPool = threadPool;
    }

    public void setMyTestService(final MyTestService myTestService) {
        this.myTestService = myTestService;
    }

}
