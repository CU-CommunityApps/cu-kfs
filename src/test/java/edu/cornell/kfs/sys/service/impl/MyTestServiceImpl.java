package edu.cornell.kfs.sys.service.impl;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import edu.cornell.kfs.sys.dataaccess.TestDataHelperDao;
import edu.cornell.kfs.sys.service.MyTestService;
import edu.cornell.kfs.sys.util.CuSqlChunk;
import edu.cornell.kfs.sys.util.CuSqlQuery;

public class MyTestServiceImpl implements MyTestService {

    private TestDataHelperDao testDataHelperDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void mergeTestObject(final String id) {
        final String newLabel = Math.random() + "_LBL";
        System.out.println(id + " " + newLabel);
        final CuSqlQuery query = new CuSqlChunk()
                .append("UPDATE KFS.MY_TEST_TABLE SET MY_LABEL = ").appendAsParameter(newLabel)
                .append(" WHERE MY_KEY = ").appendAsParameter(id)
                .toQuery();
        testDataHelperDao.executeUpdate(query);
    }

    public void setTestDataHelperDao(final TestDataHelperDao testDataHelperDao) {
        this.testDataHelperDao = testDataHelperDao;
    }

}
