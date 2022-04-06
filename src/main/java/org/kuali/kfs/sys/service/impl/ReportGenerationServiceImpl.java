/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2021 Kuali, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.sys.service.impl;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSConstants.ReportGeneration;
import org.kuali.kfs.sys.service.FileStorageService;
import org.kuali.kfs.sys.service.ReportGenerationService;
import org.kuali.rice.core.api.datetime.DateTimeService;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * To provide utilities that can generate reports with JasperReport
 */
/*
 * CU Customization: Backport the FINP-7916 changes into the 2021-01-21 version of this file.
 * This overlay should be removed when we upgrade to the 2021-11-17 financials patch or later.
 */
public class ReportGenerationServiceImpl implements ReportGenerationService {

    private static final Logger LOG = LogManager.getLogger();

    protected DateTimeService dateTimeService;
    protected FileStorageService fileStorageService;

    public void generateReportToPdfFile(Map<String, Object> reportData, String template, String reportFileName) {
        List<String> data = Arrays.asList(KFSConstants.EMPTY_STRING);
        JRDataSource dataSource = new JRBeanCollectionDataSource(data);

        generateReportToPdfFile(reportData, dataSource, template, reportFileName);
    }

    /**
     * The dataSource can be an instance of JRDataSource, java.util.Collection or object array.
     */
    public void generateReportToPdfFile(Map<String, Object> reportData, Object dataSource, String template,
            String reportFileName) {
        reportFileName = reportFileName + ReportGeneration.PDF_FILE_EXTENSION;
        String directoryName = StringUtils.substringBeforeLast(reportFileName, fileStorageService.separator());
        if (!fileStorageService.directoryExists(directoryName)) {
            fileStorageService.mkdir(directoryName);
        }

        fileStorageService.open(reportFileName, file -> generateReportToOutputStream(reportData, dataSource, template,
                file.getOutputStream()));
    }

    /**
     * Updates the report data map with any values that report generation needs (for instance, substituting in the
     * temporary directory into the report subdirectory)
     *
     * @param reportData the original report data
     * @return a decorated version of report data
     */
    protected Map<String, Object> decorateReportData(Map<String, Object> reportData) {
        Map<String, Object> decoratedReportData = new ConcurrentHashMap<>(reportData);
        if (reportData.containsKey(ReportGeneration.PARAMETER_NAME_SUBREPORT_DIR)) {
            decoratedReportData.put(ReportGeneration.PARAMETER_NAME_SUBREPORT_DIR,
                    new File(System.getProperty("java.io.tmpdir").concat(File.separator).concat(reportData.get(
                            ReportGeneration.PARAMETER_NAME_SUBREPORT_DIR).toString())).getAbsolutePath()
                            .concat(File.separator));
        }
        return decoratedReportData;
    }

    public void generateReportToOutputStream(Map<String, Object> reportData, Object dataSource, String template,
            OutputStream baos) {
        ClassPathResource resource = getReportTemplateClassPathResource(template.concat(
                ReportGeneration.DESIGN_FILE_EXTENSION));
        if (resource == null || !resource.exists()) {
            throw new IllegalArgumentException("Cannot find the template file: " +
                    template.concat(ReportGeneration.DESIGN_FILE_EXTENSION));
        }

        try {
            if (reportData != null && reportData.containsKey(ReportGeneration.PARAMETER_NAME_SUBREPORT_TEMPLATE_NAME)) {
                Map<String, String> subReports = (Map<String, String>) reportData.get(
                        ReportGeneration.PARAMETER_NAME_SUBREPORT_TEMPLATE_NAME);
                String subReportDirectory = (String) reportData.get(ReportGeneration.PARAMETER_NAME_SUBREPORT_DIR);
                compileSubReports(subReports, subReportDirectory);
            }

            String designTemplateName = template.concat(ReportGeneration.DESIGN_FILE_EXTENSION);
            InputStream jasperReport = new FileInputStream(compileReportTemplate(designTemplateName));

            JRDataSource jrDataSource = convertReportData(dataSource);

            JasperRunManager.runReportToPdfStream(jasperReport, baos, decorateReportData(reportData), jrDataSource);
        } catch (Exception e) {
            LOG.error(e);
            throw new RuntimeException("Fail to generate report.", e);
        }
    }

    // Taken from org.springframework.ui.jasperreports.JasperReportsUtils in Spring 4.3.x; it was removed in 5.x
    public static JRDataSource convertReportData(Object value) throws IllegalArgumentException {
        if (value instanceof JRDataSource) {
            return (JRDataSource) value;
        } else if (value instanceof Collection) {
            return new JRBeanCollectionDataSource((Collection<?>) value);
        } else if (value instanceof Object[]) {
            return new JRBeanArrayDataSource((Object[]) value);
        } else {
            throw new IllegalArgumentException("Value [" + value + "] cannot be converted to a JRDataSource");
        }
    }

    @Override
    public String buildFullFileName(Date runDate, String directory, String fileName, String extension) {
        String runtimeStamp = dateTimeService.toDateTimeStringForFilename(runDate);
        String fileNamePattern = "{0}" + fileStorageService.separator() + "{1}_{2}{3}";
        return MessageFormat.format(fileNamePattern, directory, fileName, runtimeStamp, extension);
    }

    /**
     * get a class path resource that references to the given report template
     *
     * @param reportTemplateName the given report template name with its full-qualified package name. It may not
     *                           include extension. If an extension is included in the name, it should be prefixed
     *                           ".jasper" or '.jrxml".
     * @return a class path resource that references to the given report template
     */
    protected ClassPathResource getReportTemplateClassPathResource(String reportTemplateName) {
        if (reportTemplateName.endsWith(ReportGeneration.DESIGN_FILE_EXTENSION)
                || reportTemplateName.endsWith(ReportGeneration.JASPER_REPORT_EXTENSION)) {
            return new ClassPathResource(reportTemplateName);
        }

        String jasperReport = reportTemplateName.concat(ReportGeneration.JASPER_REPORT_EXTENSION);
        ClassPathResource resource = new ClassPathResource(jasperReport);
        if (resource.exists()) {
            return resource;
        }

        String designTemplate = reportTemplateName.concat(ReportGeneration.DESIGN_FILE_EXTENSION);
        resource = new ClassPathResource(designTemplate);
        return resource;
    }

    /**
     * compile the report template xml file into a Jasper report file if the compiled file does not exist or is out of
     * update
     *
     * @param template the name of the template file, without an extension
     * @return an input stream where the intermediary report was written
     */
    protected File compileReportTemplate(String template) throws JRException, IOException {
        ClassPathResource designTemplateResource = new ClassPathResource(template);

        if (!designTemplateResource.exists()) {
            throw new RuntimeException("The design template file does not exist: " + template);
        }

        File tempJasperDir = new File(System.getProperty("java.io.tmpdir") + File.separator +
                template.replaceAll("\\/[^\\/]+$", ""));
        if (!tempJasperDir.exists()) {
            FileUtils.forceMkdir(tempJasperDir);
        }

        File tempJasperFile = new File(System.getProperty("java.io.tmpdir") + File.separator +
                template.replace(ReportGeneration.DESIGN_FILE_EXTENSION, "")
                        .concat(ReportGeneration.JASPER_REPORT_EXTENSION));
        if (!tempJasperFile.exists()) {
            JasperCompileManager.compileReportToStream(designTemplateResource.getInputStream(),
                    new FileOutputStream(tempJasperFile));
        }

        return tempJasperFile;
    }

    /**
     * compile the given sub reports
     *
     * @param subReports         the sub report Map that hold the sub report template names indexed with keys
     * @param subReportDirectory the directory where sub report templates are located
     */
    protected void compileSubReports(Map<String, String> subReports, String subReportDirectory) throws Exception {
        for (Map.Entry<String, String> entry : subReports.entrySet()) {
            final String designTemplateName = subReportDirectory + entry.getValue() +
                    ReportGeneration.DESIGN_FILE_EXTENSION;
            compileReportTemplate(designTemplateName);
        }
    }

    public void setDateTimeService(DateTimeService dateTimeService) {
        this.dateTimeService = dateTimeService;
    }

    public void setFileStorageService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }
}
