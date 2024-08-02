/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2023 Kuali, Inc.
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
package org.kuali.kfs.module.ar.service;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import edu.cornell.kfs.module.ar.CuArKeyConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.kuali.kfs.core.api.config.property.ConfigurationService;
import org.kuali.kfs.core.web.format.FormatException;
import org.kuali.kfs.krad.rules.rule.event.SaveDocumentEvent;
import org.kuali.kfs.krad.service.KualiRuleService;
import org.kuali.kfs.krad.util.GlobalVariables;
import org.kuali.kfs.krad.util.ObjectUtils;
import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.ArKeyConstants;
import org.kuali.kfs.module.ar.ArPropertyConstants;
import org.kuali.kfs.module.ar.businessobject.CashControlDetail;
import org.kuali.kfs.module.ar.document.CashControlDocument;
import org.kuali.kfs.module.ar.document.service.CashControlDocumentService;
import org.kuali.kfs.module.ar.document.validation.event.AddCashControlDetailEvent;
import org.kuali.kfs.module.ar.exception.CashControlDetailParserException;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.context.SpringContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

public class CashControlDetailImportService {

    /**
     * The default format defines the expected cashControlDetail property names and their order in the import file.
     * Please update this if the import file format changes (i.e. adding/deleting cashControlDetail properties, changing
     * their order).
     */
    protected static final String[] DEFAULT_FORMAT = {ArPropertyConstants.CashControlDetailFields.CUSTOMER_NUMBER,
            ArPropertyConstants.CashControlDetailFields.CUSTOMER_PAYMENT_MEDIUM_IDENTIFIER,
            ArPropertyConstants.CashControlDetailFields.CUSTOMER_PAYMENT_DATE,
            ArPropertyConstants.CashControlDetailFields.FINANCIAL_DOCUMENT_LINE_AMOUNT,
            ArPropertyConstants.CashControlDetailFields.CUSTOMER_PAYMENT_DESCRIPTION};

    private final CashControlDocumentService cashControlDocumentService;
    private final ConfigurationService configurationService;

    private Integer lineNo = 0;

    public CashControlDetailImportService(
            final CashControlDocumentService cashControlDocumentService, final ConfigurationService configurationService
    ) {
        Validate.isTrue(cashControlDocumentService != null, "cashControlDocumentService must be provided");
        this.cashControlDocumentService = cashControlDocumentService;
        Validate.isTrue(configurationService != null, "configurationService must be provided");
        this.configurationService = configurationService;
    }

    /**
     * Parses a line of cashControlDetail data from a csv file and retrieves the attributes as key-value string pairs
     * into a map.
     *
     * @param detailLineValues a string array of values read from a line in the cashControlDetail import file
     * @return a map containing cashControlDetail attribute name-value string pairs
     */
    private Map<String, String> retrieveCashControlDetailAttributes(final String[] detailLineValues) {
        final int attributeNamesCount = DEFAULT_FORMAT.length;
        final int attributeValuesCount = detailLineValues.length;
        if (attributeNamesCount != attributeValuesCount) {
            final String[] errorParams = {"" + attributeNamesCount, "" + attributeValuesCount, "" + lineNo};
            GlobalVariables.getMessageMap().putError(
                    KFSConstants.CASH_CONTROL_DETAILS_ERRORS,
                    CuArKeyConstants.CashControlDetailConstants.ERROR_DETAILPARSER_WRONG_PROPERTY_NUMBER,
                    errorParams
            );
            return Map.of();
        }

        final Map<String, String> cashControlDetailMap = new HashMap<>();
        for (int i = 0; i < attributeNamesCount; i++) {
            cashControlDetailMap.put(DEFAULT_FORMAT[i], detailLineValues[i]);
        }
        return cashControlDetailMap;
    }

    /**
     * Generates an cashControlDetail instance and populates it with the specified attribute map.
     *
     * @param cashControlDetailMap   the specified attribute map from which attributes are populated
     * @return the populated cashControlDetail
     */
    private CashControlDetail genCashControlDetailWithRetrievedAttributes(
            final Map<String, String> cashControlDetailMap
    ) {
        final CashControlDetail cashControlDetail = new CashControlDetail();

        boolean failed = false;
        for (final Entry<String, String> entry : cashControlDetailMap.entrySet()) {
            final String key = entry.getKey();
            final String value = entry.getValue();
            try {
                ObjectUtils.setObjectProperty(cashControlDetail, key, value);
            } catch (final FormatException e) {
                // continue to parse the rest of the cashControlDetail properties after the current property fails
                final String[] errorParams = {value, key, "" + lineNo};
                GlobalVariables.getMessageMap().putError(
                        KFSConstants.CASH_CONTROL_DETAILS_ERRORS,
                        CuArKeyConstants.CashControlDetailConstants.ERROR_DETAILPARSER_INVALID_NUMERIC_VALUE,
                        errorParams
                );
                failed = true;
            } catch (final IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                throw new RuntimeException("unable to complete cashControlDetail line population.", e);
            }
        }

        if (failed) {
            throw new CashControlDetailParserException(
                    "empty or invalid cashControlDetail properties in line " + lineNo + ")",
                    CuArKeyConstants.CashControlDetailConstants.ERROR_DETAILPARSER_DETAIL_PROPERTY,
                    "" + lineNo
            );
        }
        return cashControlDetail;
    }

    private Optional<CashControlDetail> parseCashControlDetail(final String[] detailLineValues) {
        final Map<String, String> cashControlDetailMap = retrieveCashControlDetailAttributes(detailLineValues);
        if (cashControlDetailMap.isEmpty()) {
            return Optional.empty();
        }
        final CashControlDetail cashControlDetail = genCashControlDetailWithRetrievedAttributes(cashControlDetailMap);

        cashControlDetail.refresh();
        return Optional.of(cashControlDetail);
    }

    private List<CashControlDetail> importCashControlDetails(
            final String fileName,
            final InputStream detailImportFileInputStream
    ) {
        // open input stream
        final List<CashControlDetail> importedCashControlDetails = new ArrayList<>();

        // parse cashControlDetails line by line
        lineNo = 0;
        boolean failed = false;
        String[] detailLineValues;
        try (CSVReader reader = new CSVReader(new InputStreamReader(detailImportFileInputStream,
                StandardCharsets.UTF_8
        ))) {
            while ((detailLineValues = reader.readNext()) != null) {
                lineNo++;
                final Optional<CashControlDetail> cashControlDetail = parseCashControlDetail(detailLineValues);
                if (cashControlDetail.isPresent()) {
                    importedCashControlDetails.add(cashControlDetail.get());
                } else {
                    // continue to parse the rest of the cashControlDetails after the current cashControlDetail fails
                    // error messages are already dealt with inside parseCashControlDetail, so no need to do anything
                    // here
                    failed = true;
                }
            }

            if (failed) {
                throw new CashControlDetailParserException(
                        "errors in parsing cashControlDetail lines in file " + fileName,
                        CuArKeyConstants.CashControlDetailConstants.ERROR_DETAILPARSER_DETAIL_LINE,
                        fileName
                );
            }
        } catch (final CsvException | IOException e) {
            throw new RuntimeException("unable to read line from BufferReader in CashControlDetailParserBase", e);
        }

        return importedCashControlDetails;
    }

    public void processDetailImportFile(
            final String fileName,
            final InputStream detailImportFileInputStream,
            final CashControlDocument cashControlDocument
    ) {
        try {
            final List<CashControlDetail> importedDetails =
                    importCashControlDetails(fileName, detailImportFileInputStream);
            // validate imported items
            boolean allPassed = true;
            for (final CashControlDetail cashControlDetail : importedDetails) {
                boolean rulePassed = true;
                String customerNumber = cashControlDetail.getCustomerNumber();
                if (StringUtils.isNotEmpty(customerNumber)) {
                    // force customer numbers to upper case, since it's a primary key
                    customerNumber = customerNumber.toUpperCase(Locale.US);
                }
                cashControlDetail.setCustomerNumber(customerNumber);

                // save the document, which will run business rules and make sure the doc is ready for lines
                final KualiRuleService ruleService = SpringContext.getBean(KualiRuleService.class);

                // apply save rules for the doc
                rulePassed &= ruleService.applyRules(new SaveDocumentEvent(KFSConstants.DOCUMENT_HEADER_ERRORS,
                        cashControlDocument
                ));

                // apply rules for the new cash control detail
                rulePassed &=
                        ruleService.applyRules(new AddCashControlDetailEvent(KFSConstants.CASH_CONTROL_DETAILS_ERRORS,
                                cashControlDocument,
                                cashControlDetail
                        ));

                if (!rulePassed) {
                    allPassed = false;
                }
            }

            // All succeed and go into document or none go in.
            if (allPassed) {
                for (final CashControlDetail cashControlDetail : importedDetails) {
                    // add cash control detail. implicitly saves the cash control document
                    cashControlDocumentService.addNewCashControlDetail(
                            configurationService.getPropertyValueAsString(ArKeyConstants.CREATED_BY_CASH_CTRL_DOC),
                            cashControlDocument,
                            cashControlDetail
                    );
                }
            }

        } catch (final CashControlDetailParserException e) {
            GlobalVariables.getMessageMap().putError(
                    ArConstants.NEW_CASH_CONTROL_DETAIL_ERROR_PATH_PREFIX,
                    e.getErrorKey(),
                    e.getErrorParameters()
            );
        }
    }
}