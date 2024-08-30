package edu.cornell.kfs.module.ar.document.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.kuali.kfs.core.api.datetime.DateTimeService;
import org.kuali.kfs.core.api.util.type.KualiDecimal;
import org.kuali.kfs.module.ar.report.util.CustomerStatementDetailReportDataHolder;
import org.kuali.kfs.module.ar.report.util.CustomerStatementReportDataHolder;
import org.kuali.kfs.sys.KFSConstants.ReportGeneration;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.report.ReportInfoHolder;
import org.kuali.kfs.sys.service.impl.FileSystemFileStorageServiceImpl;
import org.kuali.kfs.sys.service.impl.ReportGenerationServiceImpl;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import edu.cornell.kfs.sys.CUKFSConstants;
import edu.cornell.kfs.sys.service.impl.TestDateTimeServiceImpl;
import edu.cornell.kfs.sys.util.CreateTestDirectories;
import net.sf.jasperreports.engine.JRParameter;

/*
 * Heavily modified version of ReportGenerationServiceTest that has been configured to generate
 * sample Billing Statement PDFs.
 */
@Execution(ExecutionMode.SAME_THREAD)
@CreateTestDirectories(
        baseDirectory = "test/ar-billing/",
        subDirectories = {
                "test/ar-billing/reports/ar/"
        }
)
public class CuBillingStatementTest {

    private static final String DETAIL_FORMAT = "Detail";
    private static final String SUMMARY_FORMAT = "Summary";

    private TestDateTimeServiceImpl dateTimeService;
    private ReportGenerationServiceImpl reportGenerationService;
    private ReportInfoHolder customerStatementReportInfo;
    private ReportInfoHolder customerDetailStatementReportInfo;

    @BeforeEach
    void setUp() throws Exception {
        dateTimeService = new TestDateTimeServiceImpl();
        dateTimeService.afterPropertiesSet();

        reportGenerationService = new ReportGenerationServiceImpl();
        reportGenerationService.setFileStorageService(new FileSystemFileStorageServiceImpl());
        reportGenerationService.setDateTimeService(dateTimeService);

        customerStatementReportInfo = buildReportInfoHolder(
                "SummaryStatement", "statementDetails", "SubSummaryStatement");
        customerDetailStatementReportInfo = buildReportInfoHolder(
                "DetailStatement", "subDetailStatement", "SubDetailStatement");
    }

    private ReportInfoHolder buildReportInfoHolder(
            final String reportTemplateName, final String subReportKey, final String subReportTemplateName) {
        final ReportInfoHolder infoHolder = new ReportInfoHolder();
        infoHolder.setReportTitle("Customer Statement Report");
        infoHolder.setReportFileName("customer_statement");
        infoHolder.setReportsDirectory(getAbsolutePath("test/ar-billing/reports/ar/"));
        infoHolder.setResourceBundleBaseName("org/kuali/kfs/module/ar/report/messages");
        infoHolder.setReportTemplateClassPath("org/kuali/kfs/module/ar/report/");
        infoHolder.setReportTemplateName(reportTemplateName);
        infoHolder.setSubReportTemplateClassPath("org/kuali/kfs/module/ar/report/");
        infoHolder.setSubReports(Map.of(subReportKey, subReportTemplateName));
        return infoHolder;
    }

    private String getAbsolutePath(final String path) {
        final File file = new File(path);
        final String absolutePath = file.getAbsolutePath();
        return StringUtils.replace(absolutePath, CUKFSConstants.BACKSLASH, CUKFSConstants.SLASH);
    }

    @AfterEach
    void tearDown() throws Exception {
        customerDetailStatementReportInfo = null;
        customerStatementReportInfo = null;
        reportGenerationService = null;
        dateTimeService = null;
    }

    @Test
    void testBuildDetailBillingStatement() throws Exception {
        final File billingStatement = doWithTestDateTimeServiceInSpringContext(() -> {
            final CustomerStatementReportDataHolder reportData = buildDummyDetailStatement();
            return generateReport(reportData, new java.util.Date(), DETAIL_FORMAT);
        });
        assertNotNull(billingStatement, "'Detail' billing statement file reference should not have been null");
        assertTrue(billingStatement.exists(), "'Detail' billing statement file does not exist");
    }

    @Test
    void testBuildSummaryBillingStatement() throws Exception {
        final File billingStatement = doWithTestDateTimeServiceInSpringContext(() -> {
            final CustomerStatementReportDataHolder reportData = buildDummySummaryStatement();
            return generateReport(reportData, new java.util.Date(), SUMMARY_FORMAT);
        });
        assertNotNull(billingStatement, "'Summary' billing statement file reference should not have been null");
        assertTrue(billingStatement.exists(), "'Summary' billing statement file does not exist");
    }

    private <T> T doWithTestDateTimeServiceInSpringContext(final Supplier<T> task) {
        try (
                final MockedStatic<SpringContext> mockContext = Mockito.mockStatic(SpringContext.class)
        ) {
            mockContext.when(() -> SpringContext.getBean(DateTimeService.class))
                    .thenReturn(dateTimeService);
            return task.get();
        }
    }

    /*
     * Copied and tweaked the CustomerStatementReportServiceImpl.generateReport() method
     * that used to exist in base code.
     */
    private File generateReport(CustomerStatementReportDataHolder reportDataHolder, java.util.Date runDate,
            String statementFormat) {
        String reportFileName = customerStatementReportInfo.getReportFileName();
        String reportDirectory = customerStatementReportInfo.getReportsDirectory();
        String reportTemplateClassPath = customerStatementReportInfo.getReportTemplateClassPath();
        ResourceBundle resourceBundle = customerStatementReportInfo.getResourceBundle();
        String subReportTemplateClassPath = customerStatementReportInfo.getSubReportTemplateClassPath();
        String reportTemplateName;
        Map<String, String> subReports;
        if (statementFormat.equalsIgnoreCase(SUMMARY_FORMAT)) {
            reportTemplateName = customerStatementReportInfo.getReportTemplateName();
            subReports = customerStatementReportInfo.getSubReports();
        } else {
            reportTemplateName = customerDetailStatementReportInfo.getReportTemplateName();
            subReports = customerDetailStatementReportInfo.getSubReports();
        }

        Map<String, Object> reportData = reportDataHolder.getReportData();
        reportData.put(JRParameter.REPORT_RESOURCE_BUNDLE, resourceBundle);
        reportData.put(ReportGeneration.PARAMETER_NAME_SUBREPORT_DIR, subReportTemplateClassPath);
        reportData.put(ReportGeneration.PARAMETER_NAME_SUBREPORT_TEMPLATE_NAME, subReports);

        String template = reportTemplateClassPath + reportTemplateName;
        String fullReportFileName = reportGenerationService.buildFullFileName(runDate, reportDirectory,
                reportFileName, "");
        File report = new File(fullReportFileName + ".pdf");
        reportGenerationService.generateReportToPdfFile(reportData, template, fullReportFileName);
        return report;
    }

    private CustomerStatementReportDataHolder buildDummyDetailStatement() {
        return buildStatementData(
                buildDetailData("55123456", "2024-08-15", "Sample Customer Credit Memo",
                        null, 25.99, "Processing Org Name", "XXXXX1234", "Credit Memo"),
                buildDetailData("55123457", "2024-08-15", "Sample Invoice",
                        150.00, null, "Processing Org Name", "XXXXX1234", "Invoice"),
                buildDetailData("55123458", "2024-08-15", "Sample Payment Application",
                        null, 105.00, "Processing Org Name", "XXXXX1234", "Payment"),
                buildDetailData("55123459", "2024-08-15", "Sample Invoice Writeoff",
                        null, 16.33, "Processing Org Name", "XXXXX1234", "Writeoff")
        );
    }

    private CustomerStatementReportDataHolder buildDummySummaryStatement() {
        return buildStatementData(
                buildDetailData("55123457", "2024-08-15", "Sample Invoice",
                        150.00, 130.99, "Processing Org Name", "XXXXX1234", "Invoice"));
    }

    private CustomerStatementReportDataHolder buildStatementData(
            final CustomerStatementDetailReportDataHolder... details) {
        final CustomerStatementReportDataHolder statement = new CustomerStatementReportDataHolder();
        statement.setInvoice(buildDummyInvoiceMap());
        statement.setCustomer(buildDummyCustomerMap());
        statement.setSysinfo(buildDummySysInfoMap());
        statement.setDetails(List.of(details));
        return statement;
    }

    private CustomerStatementDetailReportDataHolder buildDetailData(
            final String documentNumber, final String documentFinalDate, final String documentDescription,
            final Double financialDocumentTotalAmountCharge, final Double financialDocumentTotalAmountCredit,
            final String orgName, final String fein, final String docType) {
        final KualiDecimal actualCharge = financialDocumentTotalAmountCharge != null
                ? new KualiDecimal(financialDocumentTotalAmountCharge) : null;
        final KualiDecimal actualCredit = financialDocumentTotalAmountCredit != null
                ? new KualiDecimal(financialDocumentTotalAmountCredit) : null;
        final CustomerStatementDetailReportDataHolder detail = new CustomerStatementDetailReportDataHolder(
                documentDescription, actualCharge);
        detail.setDocumentNumber(documentNumber);
        detail.setDocumentFinalDate(java.sql.Date.valueOf(documentFinalDate));
        detail.setFinancialDocumentTotalAmountCredit(actualCredit);
        detail.setOrgName(orgName);
        detail.setFein(fein);
        detail.setDocType(docType);
        return detail;
    }

    private Map<String, String> buildDummyInvoiceMap() {
        return buildDummyDataMap(
                "createDate",
                "customerOrg",
                "billingOrgName",
                "previousBalance",
                "lastReportedDate",
                "amountDue",
                "dueDate",
                "ocrLine",
                "billingOrgFax",
                "billingOrgPhone",
                "total0to30",
                "total31to60",
                "total61to90",
                "total90toSYSPR",
                "totalAmountDue"
        );
    }

    private Map<String, String> buildDummyCustomerMap() {
        return buildDummyDataMap(
                "id",
                "billToName",
                "billToStreetAddressLine1",
                "billToStreetAddressLine2",
                "billToCityStateZip",
                "billToCountry"
        );
    }

    private Map<String, String> buildDummySysInfoMap() {
        return buildDummyDataMap(
                "checkPayableTo",
                "remitToName",
                "remitToAddressLine1",
                "remitToAddressLine2",
                "remitToCityStateZip",
                "univName",
                "univAddr",
                "FEIN"
        );
    }

    private Map<String, String> buildDummyDataMap(final String... keys) {
        return Stream.of(keys)
                .collect(Collectors.toUnmodifiableMap(
                        key -> key, key -> "[" + key + "]"));
    }

}
