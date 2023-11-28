package edu.cornell.kfs.module.purap.identity;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.module.purap.identity.PotentiallySensitiveDocumentRoleTypeServiceImpl;
import org.kuali.kfs.module.purap.identity.PurapKimAttributes;

/**
 * Custom subclass of PotentiallySensitiveDocumentRoleTypeServiceImpl that allows for more
 * types of boolean values other than true/false for the document sensitivity indicator.
 */
public class CuPotentiallySensitiveDocumentRoleTypeServiceImpl extends PotentiallySensitiveDocumentRoleTypeServiceImpl {

    // Copied and tweaked constants from OrganizationOptionalHierarchyRoleTypeServiceImpl.
    public static final String DOCUMENT_SENSITIVE_FALSE_VALUE = "n";
    public static final List<String> TRUE_VALUES = Arrays.asList(new String[] { "yes", "y", "true", "t", "on", "1", "enabled" });

    /**
     * Overridden to perform improved matching for the document sensitivity indicator.
     * 
     * @see org.kuali.kfs.kns.kim.type.DataDictionaryTypeServiceBase#performMatch(java.util.Map, java.util.Map)
     */
    @Override
    protected boolean performMatch(final Map<String, String> inputAttributes, final Map<String, String> storedAttributes) {
        // Copied the superclass's literal matching, and tweaked it to do custom matching for the document sensitivity attribute.
        if (storedAttributes == null || inputAttributes == null) {
            return true;
        }
        for (final Map.Entry<String, String> entry : storedAttributes.entrySet()) {
            if (PurapKimAttributes.DOCUMENT_SENSITIVE.equals(entry.getKey())) {
                if (inputAttributes.containsKey(entry.getKey())) {
                    final String inputValue = inputAttributes.get(entry.getKey());
                    // For matching to succeed, the sensitive-doc attributes have to be both true or both false. (We assume null means false.)
                    if (TRUE_VALUES.contains(inputValue == null ? DOCUMENT_SENSITIVE_FALSE_VALUE : inputValue.toLowerCase(Locale.US))
                            != TRUE_VALUES.contains(entry.getValue() == null ? DOCUMENT_SENSITIVE_FALSE_VALUE : entry.getValue().toLowerCase(Locale.US))) {
                        return false;
                    }
                }
            } else if (inputAttributes.containsKey(entry.getKey()) && !StringUtils.equals(inputAttributes.get(entry.getKey()), entry.getValue())) {
                return false;
            }
        }
        
        return true;
    }
}
