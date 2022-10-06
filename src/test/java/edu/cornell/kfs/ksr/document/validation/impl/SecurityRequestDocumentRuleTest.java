package edu.cornell.kfs.ksr.document.validation.impl;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SecurityRequestDocumentRuleTest {

    private SecurityRequestDocumentRule securityRequestDocumentRule;

    @Before
    public void setUp() {
        securityRequestDocumentRule = new SecurityRequestDocumentRule();
    }

    @Test
    public void testValidatePrimaryDepartmentCodeValid() {
        boolean result = securityRequestDocumentRule.validateDepartmentCode("IT-3802");
        assertTrue(result);
    }

    @Test
    public void testValidatePrimaryDepartmentCodeBlank() {
        boolean result = securityRequestDocumentRule.validateDepartmentCode("");
        assertFalse(result);
    }

    @Test
    public void testValidatePrimaryDepartmentCodeDefaultNoOrg() {
        boolean result = securityRequestDocumentRule.validateDepartmentCode("IT-");
        assertFalse(result);
    }

    @Test
    public void testValidatePrimaryDepartmentCodeSpace() {
        boolean result = securityRequestDocumentRule.validateDepartmentCode(" IT-3802");
        assertFalse(result);

        result = securityRequestDocumentRule.validateDepartmentCode("IT -3802");
        assertFalse(result);

        result = securityRequestDocumentRule.validateDepartmentCode("IT- 3802");
        assertFalse(result);

        result = securityRequestDocumentRule.validateDepartmentCode("IT-3802 ");
        assertFalse(result);

        result = securityRequestDocumentRule.validateDepartmentCode("IT- ");
        assertFalse(result);
    }

}
