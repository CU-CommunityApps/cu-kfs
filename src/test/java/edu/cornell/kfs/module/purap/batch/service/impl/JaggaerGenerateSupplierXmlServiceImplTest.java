package edu.cornell.kfs.module.purap.batch.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.JaggaerBuilder;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.Supplier;
import edu.cornell.kfs.module.purap.jaggaer.supplier.xml.SupplierSyncMessage;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;

public class JaggaerGenerateSupplierXmlServiceImplTest {
    
    private JaggaerGenerateSupplierXmlServiceImpl jaggaerGenerateSupplierXmlServiceImpl;

    @BeforeEach
    void setUp() throws Exception {
        jaggaerGenerateSupplierXmlServiceImpl = new JaggaerGenerateSupplierXmlServiceImpl();
        TestDateTimeServiceImpl dateTimeService = new TestDateTimeServiceImpl();
        jaggaerGenerateSupplierXmlServiceImpl.setDateTimeService(dateTimeService);
        dateTimeService.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() throws Exception {
        jaggaerGenerateSupplierXmlServiceImpl = null;
    }

    @ParameterizedTest
    @MethodSource("provideForTestBuildSupplierSyncMessageList")
    void testBuildSupplierSyncMessageList(int numberOfSuppliers, int maximumNumberOfSuppliersPerListItem, int expectedNumberOfSupplierSyncMessage) {
        List<Supplier> suppliers = buildListOfSuppliers(numberOfSuppliers);
        List<SupplierSyncMessage> messages  = jaggaerGenerateSupplierXmlServiceImpl.buildSupplierSyncMessageList(suppliers, maximumNumberOfSuppliersPerListItem);
        assertEquals(expectedNumberOfSupplierSyncMessage, messages.size());
    }
    
    private static Stream<Arguments> provideForTestBuildSupplierSyncMessageList() {
        return Stream.of(
          Arguments.of(1, 1, 1),
          Arguments.of(2, 1, 2),
          Arguments.of(2, 2, 1),
          Arguments.of(100, 30, 4),
          Arguments.of(100, 500, 1),
          Arguments.of(0, 1, 0)
        );
    }
    
    private List<Supplier> buildListOfSuppliers(int numberOfSuppliers) {
        List<Supplier> suppliers = new ArrayList<>();
        for (int i = 0; i < numberOfSuppliers; i++) {
            Supplier supplier = new Supplier();
            supplier.setName(JaggaerBuilder.buildName("Test supplier " + i));
            suppliers.add(supplier);
        }
        return suppliers;
    }
    
    @Test
    void testDateFormatters() {
        long dateTime = Long.parseLong("1684345725727");
        String actualFileNameDateString = JaggaerGenerateSupplierXmlServiceImpl.DATE_FORMATTER_FOR_FILE_NAME.print(dateTime);
        String actualHeaderDateString = JaggaerGenerateSupplierXmlServiceImpl.DATE_FORMATTER_FOR_HEADER_DATE.print(dateTime);
        assertEquals("20230517_134845727", actualFileNameDateString);
        assertEquals("2023-05-17T17:48:45.727Z", actualHeaderDateString);
    }
}
