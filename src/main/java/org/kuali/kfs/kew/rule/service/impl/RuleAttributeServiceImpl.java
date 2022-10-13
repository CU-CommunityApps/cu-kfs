/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 *
 * Copyright 2005-2022 Kuali, Inc.
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
package org.kuali.kfs.kew.rule.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.kuali.kfs.kew.exception.WorkflowServiceError;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorException;
import org.kuali.kfs.kew.exception.WorkflowServiceErrorImpl;
import org.kuali.kfs.kew.rule.bo.RuleAttribute;
import org.kuali.kfs.kew.rule.dao.RuleAttributeDAO;
import org.kuali.kfs.kew.rule.service.RuleAttributeService;
import org.kuali.kfs.kew.xml.RuleAttributeXmlParser;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/* Cornell Customization: backport redis; backport redis fix on FINP-8169 */
public class RuleAttributeServiceImpl implements RuleAttributeService {

    private static final Logger LOG = LogManager.getLogger();

    private static final String RULE_ATTRIBUTE_NAME_REQUIRED = "rule.attribute.name.required";
    private static final String RULE_ATTRIBUTE_CLASS_REQUIRED = "rule.attribute.className.required";

    private static final String XML_FILE_NOT_FOUND = "general.error.filenotfound";
    private static final String XML_PARSE_ERROR = "general.error.parsexml";

    private RuleAttributeDAO ruleAttributeDAO;

    @CacheEvict(value = RuleAttribute.CACHE_NAME, allEntries = true)
    public void save(RuleAttribute ruleAttribute) {
        validate(ruleAttribute);
        getRuleAttributeDAO().save(ruleAttribute);
    }

    @Cacheable(cacheNames = RuleAttribute.CACHE_NAME, key = "'{" + RuleAttribute.CACHE_NAME + "}|id=' + #p0")
    public RuleAttribute findByRuleAttributeId(String ruleAttributeId) {
        return getRuleAttributeDAO().findByRuleAttributeId(ruleAttributeId);
    }

    @Cacheable(cacheNames = RuleAttribute.CACHE_NAME, key = "'{" + RuleAttribute.CACHE_NAME + "}|name=' + #p0")
    public RuleAttribute findByName(String name) {
        return getRuleAttributeDAO().findByName(name);
    }

    public RuleAttributeDAO getRuleAttributeDAO() {
        return ruleAttributeDAO;
    }

    public void setRuleAttributeDAO(RuleAttributeDAO ruleAttributeDAO) {
        this.ruleAttributeDAO = ruleAttributeDAO;
    }

    private void validate(RuleAttribute ruleAttribute) {
        LOG.debug("validating ruleAttribute");
        List<WorkflowServiceError> errors = new ArrayList<>();
        if (ruleAttribute.getName() == null || ruleAttribute.getName().trim().equals("")) {
            errors.add(new WorkflowServiceErrorImpl("Please enter a rule attribute name.",
                    RULE_ATTRIBUTE_NAME_REQUIRED));
            LOG.error("Rule attribute name is missing");
        } else {
            ruleAttribute.setName(ruleAttribute.getName().trim());
            if (ruleAttribute.getId() == null) {
                RuleAttribute nameInUse = findByName(ruleAttribute.getName());
                if (nameInUse != null) {
                    errors.add(new WorkflowServiceErrorImpl("Rule attribute name already in use",
                            "routetemplate.ruleattribute.name.duplicate"));
                    LOG.error("Rule attribute name already in use");
                }
            }
        }
        if (ruleAttribute.getResourceDescriptor() == null ||
                ruleAttribute.getResourceDescriptor().trim().equals("")) {
            errors.add(new WorkflowServiceErrorImpl("Please enter a rule attribute class name.",
                    RULE_ATTRIBUTE_CLASS_REQUIRED));
            LOG.error("Rule attribute class name is missing");
        } else {
            ruleAttribute.setResourceDescriptor(ruleAttribute.getResourceDescriptor().trim());
        }

        LOG.debug("end validating ruleAttribute");
        if (!errors.isEmpty()) {
            throw new WorkflowServiceErrorException("RuleAttribute Validation Error", errors);
        }
    }

    public void loadXml(InputStream inputStream, String principalId) {
        RuleAttributeXmlParser parser = new RuleAttributeXmlParser();
        try {
            parser.parseRuleAttributes(inputStream);
        } catch (FileNotFoundException e) {
            throw new WorkflowServiceErrorException("XML file not found",
                    new WorkflowServiceErrorImpl("Rule Attribute XML file not found", XML_FILE_NOT_FOUND));
        } catch (Exception e) {
            LOG.error("Error loading xml file", e);
            throw new WorkflowServiceErrorException("Error loading xml file",
                    new WorkflowServiceErrorImpl("Error loading xml file", XML_PARSE_ERROR));
        }
    }
}
