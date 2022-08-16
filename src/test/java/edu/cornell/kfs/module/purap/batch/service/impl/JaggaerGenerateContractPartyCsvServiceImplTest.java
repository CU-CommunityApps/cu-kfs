package edu.cornell.kfs.module.purap.batch.service.impl;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import edu.cornell.kfs.module.purap.batch.dataaccess.JaggaerUploadDao;

class JaggaerGenerateContractPartyCsvServiceImplTest {
    
    private JaggaerGenerateContractPartyCsvServiceImpl jaggaerGenerateContractPartyCsvServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        jaggaerGenerateContractPartyCsvServiceImpl = new JaggaerGenerateContractPartyCsvServiceImpl();
    }

    @AfterEach
    void tearDown() throws Exception {
        jaggaerGenerateContractPartyCsvServiceImpl = null;
    }
    
    private JaggaerUploadDao buildMockJaggaerUploadDao() {
        JaggaerUploadDao dao = Mockito.mock(JaggaerUploadDao.class);
        //Mockito.when(dao.findJaggaerContractAddress()).thenReturn(value);
        //Mockito.when(dao.findJaggaerContractParty()).thenReturn(value);
        return dao;
    }

    @Test
    void test() {
        fail("Not yet implemented");
    }

}
