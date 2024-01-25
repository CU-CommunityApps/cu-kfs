package com.rsmart.kuali.kfs.cr.document;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.kuali.kfs.core.api.util.type.KualiInteger;
import org.kuali.kfs.kns.document.MaintenanceDocument;
import org.kuali.kfs.kns.document.MaintenanceDocumentBase;
import org.kuali.kfs.kns.maintenance.Maintainable;
import org.kuali.kfs.kns.maintenance.MaintainableImpl;
import org.kuali.kfs.sys.businessobject.DocumentHeader;

import com.rsmart.kuali.kfs.cr.businessobject.CheckReconciliation;

@SuppressWarnings("deprecation")
public class CheckReconciliationMaintainableImplTest {

    private CheckReconciliationMaintainableImpl checkReconciliationMaintainable;
    private Map<String, String[]> requestParameters;

    private static final String CHECK_NUMBER = "12345678";

    @BeforeEach
    void setUp() throws Exception {
        Configurator.setLevel(CheckReconciliationMaintainableImpl.class.getName(), Level.DEBUG);
        checkReconciliationMaintainable = new CheckReconciliationMaintainableImpl();
        requestParameters = new HashMap<String, String[]>();
    }

    @AfterEach
    void tearDown() throws Exception {
        checkReconciliationMaintainable = null;
        requestParameters = null;
    }

    @ParameterizedTest
    @MethodSource("processAfterEditParams")
    void testProcessAfterEdit(String checkNumberString, String startingOrgRefId, String expectedOrgRefId) {
        MaintenanceDocument document = prepareMaintenanceDocument(checkNumberString, startingOrgRefId);
        checkReconciliationMaintainable.processAfterEdit(document, requestParameters);
        assertEquals(expectedOrgRefId, document.getDocumentHeader().getOrganizationDocumentNumber());
    }

    static Stream<Arguments> processAfterEditParams() {
        return Stream.of(
                Arguments.of(CHECK_NUMBER, null, CHECK_NUMBER),
                Arguments.of(CHECK_NUMBER, StringUtils.EMPTY, CHECK_NUMBER),
                Arguments.of(CHECK_NUMBER, StringUtils.SPACE, CHECK_NUMBER), 
                Arguments.of(CHECK_NUMBER, "FOO", "FOO")
        );
    }

    private MaintenanceDocument prepareMaintenanceDocument(String checkNumberString, String startingOrgRefId) {
        MaintenanceDocument document = new MaintenanceDocumentBase();
        DocumentHeader dh = new DocumentHeader();
        dh.setOrganizationDocumentNumber(startingOrgRefId);
        document.setDocumentHeader(dh);

        CheckReconciliation newCr = new CheckReconciliation();
        KualiInteger checkNumber = new KualiInteger(checkNumberString);
        newCr.setCheckNumber(checkNumber);

        Maintainable newMaintainable = new MaintainableImpl();
        newMaintainable.setBusinessObject(newCr);
        document.setNewMaintainableObject(newMaintainable);
        return document;
    }

}
