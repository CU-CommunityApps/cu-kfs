/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2024 Kuali, Inc.
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
package org.kuali.kfs.module.cg.service.impl;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.coreservice.framework.parameter.ParameterService;
import org.kuali.kfs.datadictionary.legacy.DataDictionaryService;
import org.kuali.kfs.krad.service.BusinessObjectService;
import org.kuali.kfs.module.cg.batch.CfdaBatchStep;
import org.kuali.kfs.module.cg.businessobject.CFDA;
import org.kuali.kfs.module.cg.businessobject.CfdaUpdateResults;
import org.kuali.kfs.module.cg.businessobject.options.CatalogOfFederalDomesticAssistanceMaintenanceTypeIdFinder;
import org.kuali.kfs.module.cg.service.CfdaService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
// CU customization to fix how getGovCodes method processes "Not Applicable" duplicate keys
public class CfdaServiceImpl implements CfdaService {
    private static final String NOT_APPLICABLE_CFDA_NBR_VALUE = "Not Applicable";
    
    private static final CSVParser CSV_PARSER =
            new CSVParserBuilder()
                    .withSeparator(',')
                    .withQuoteChar('"')
                    .withEscapeChar(Character.MIN_VALUE)
                    .build();
    private static final Logger LOG = LogManager.getLogger();

    private BusinessObjectService businessObjectService;
    private ParameterService parameterService;
    private DataDictionaryService dataDictionaryService;

    @Transactional
    @Override
    public CfdaUpdateResults update() {
        final CfdaUpdateResults results = new CfdaUpdateResults();
        final Map<String, CFDA> govMap = getGovCodes();
        final Map<String, CFDA> kfsMap = getKfsCodes();

        results.setNumberOfRecordsInKfsDatabase(kfsMap.keySet().size());
        results.setNumberOfRecordsRetrievedFromWebSite(govMap.keySet().size());

        for (final Object key : kfsMap.keySet()) {
            final CFDA cfdaKfs = kfsMap.get(key);
            final CFDA cfdaGov = govMap.get(key);

            if (cfdaKfs.getCfdaMaintenanceTypeId().startsWith("M")) {
                // Leave it alone. It's maintained manually.
                results.setNumberOfRecordsNotUpdatedBecauseManual(1 + results.getNumberOfRecordsNotUpdatedBecauseManual());
            } else if (cfdaKfs.getCfdaMaintenanceTypeId().startsWith("A")) {

                if (cfdaGov == null) {
                    if (cfdaKfs.isActive()) {
                        cfdaKfs.setActive(false);
                        businessObjectService.save(cfdaKfs);
                        results.setNumberOfRecordsDeactivatedBecauseNoLongerOnWebSite(results.getNumberOfRecordsDeactivatedBecauseNoLongerOnWebSite() + 1);
                    } else {
                        // Leave it alone for historical purposes
                        results.setNumberOfRecrodsNotUpdatedForHistoricalPurposes(results.getNumberOfRecrodsNotUpdatedForHistoricalPurposes() + 1);
                    }
                } else {
                    if (cfdaKfs.isActive()) {
                        results.setNumberOfRecordsUpdatedBecauseAutomatic(results.getNumberOfRecordsUpdatedBecauseAutomatic() + 1);
                    } else {
                        cfdaKfs.setActive(true);
                        results.setNumberOfRecordsReActivated(results.getNumberOfRecordsReActivated() + 1);
                    }

                    cfdaKfs.setCfdaProgramTitleName(cfdaGov.getCfdaProgramTitleName());
                    businessObjectService.save(cfdaKfs);
                }
            }

            // Remove it from the govMap so we know what codes from the govMap don't already exist in KFS.
            govMap.remove(key);
        }

        // What's left in govMap now is just the codes that don't exist in KFS
        for (final String key : govMap.keySet()) {
            final CFDA cfdaGov = govMap.get(key);
            businessObjectService.save(cfdaGov);
            results.setNumberOfRecordsNewlyAddedFromWebSite(results.getNumberOfRecordsNewlyAddedFromWebSite() + 1);
        }

        return results;
    }

    @Override
    public CFDA getByPrimaryId(final String cfdaNumber) {
        if (StringUtils.isBlank(cfdaNumber)) {
            return null;
        }
        return businessObjectService.findBySinglePrimaryKey(CFDA.class, cfdaNumber.trim());
    }

    // CU customization to fix duplicate "Not Applicable" keys
    public Map<String, CFDA> getGovCodes() {
        final String govURL = parameterService.getParameterValueAsString(CfdaBatchStep.class, KFSConstants.SOURCE_URL_PARAMETER);

        LOG.info("Getting government file from URL {} for update", govURL);

        final Resource csvFile = getCfdaResource(govURL);
        if (csvFile != null && csvFile.exists() && csvFile.isReadable()) {
            try (
                    InputStreamReader inputStreamReader =
                            new InputStreamReader(csvFile.getInputStream(), StandardCharsets.UTF_8);
                    CSVReader reader =
                            new CSVReaderBuilder(inputStreamReader)
                                    .withCSVParser(CSV_PARSER)
                                    .withSkipLines(1)
                                    .build();
            ) {
                final List<String[]> records = reader.readAll();

                return records
                    .stream()
                    .map(record ->
                        new CFDA(record[1], trimProgramTitleName(record[0]),
                            CatalogOfFederalDomesticAssistanceMaintenanceTypeIdFinder.CFDA_MAINTENANCE_AUTOMATIC_TYPE_ID, true)
                    )
                    .filter(cfda -> !NOT_APPLICABLE_CFDA_NBR_VALUE.equals(cfda.getCfdaNumber()))
                    .collect(Collectors.toMap(CFDA::getCfdaNumber, value -> value));
            } catch (final CsvException | IOException e) {
                throw new RuntimeException("The file could not be retrieved from " + govURL, e);
            }
        } else {
            throw new RuntimeException("The file could not be retrieved from " + govURL);
        }
    }

    Resource getCfdaResource(final String govURL) {
        return new DefaultResourceLoader(getClass().getClassLoader()).getResource(govURL);
    }

    Map<String, CFDA> getKfsCodes() {
        final Collection<CFDA> kfsCodes = businessObjectService.findAll(CFDA.class);

        return kfsCodes
            .stream()
            .collect(Collectors.toMap(CFDA::getCfdaNumber, value -> value));
    }

    private String trimProgramTitleName(final String programTitleName) {
        return StringUtils.substring(programTitleName, 0,
            dataDictionaryService.getAttributeMaxLength(CFDA.class, KFSPropertyConstants.CFDA_PROGRAM_TITLE_NAME));
    }

    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public void setParameterService(final ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setDataDictionaryService(final DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }
}
