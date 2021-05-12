/*
 * Copyright 2009 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.coa.businessobject.inquiry;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.kns.inquiry.KualiInquirableImpl;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.kns.web.ui.Field;
import org.kuali.kfs.kns.web.ui.Row;
import org.kuali.kfs.kns.web.ui.Section;
import org.kuali.kfs.krad.bo.BusinessObject;

import edu.cornell.kfs.coa.service.AccountReversionService;


public class AccountReversionInquirable extends KualiInquirableImpl {
    private AccountReversionService accountReversionService;

    /**
     * Overridden to take out details with inactive categories
     * @see org.kuali.kfs.kns.inquiry.KualiInquirableImpl#getSections(org.kuali.kfs.kns.bo.BusinessObject)
     */
    @Override
    public List<Section> getSections(BusinessObject bo) {
        List<Section> sections = super.getSections(bo);
        if (accountReversionService == null) {
            accountReversionService = SpringContext.getBean(AccountReversionService.class);
        }
        for (Section section : sections) {
            for (Row row : section.getRows()) {
                List<Field> updatedFields = new ArrayList<Field>();
                for (Field field : row.getFields()) {
                    if (shouldIncludeField(field)) {
                        updatedFields.add(field);
                    }
                }
                row.setFields(updatedFields);
            }
        }
        return sections;
    }

    /**
     * Determines if the given field should be included in the updated row, once we take out inactive categories
     * @param field the field to check
     * @return true if the field should be included (ie, it doesn't describe an account reversion with an inactive category); false otherwise
     */
    protected boolean shouldIncludeField(Field field) {
        boolean includeField = true;
        if (field.getContainerRows() != null) {
            for (Row containerRow : field.getContainerRows()) {
                for (Field containedField : containerRow.getFields()) {
                    if (containedField.getPropertyName().matches("accountReversionDetails\\[\\d+\\]\\.accountReversionCategoryCode")) {
                        final String categoryValue = containedField.getPropertyValue();
                        includeField = accountReversionService.isCategoryActive(categoryValue);
                    }
                }
            }
        }
        return includeField;
    }
}