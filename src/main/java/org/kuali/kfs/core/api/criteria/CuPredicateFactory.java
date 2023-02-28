package org.kuali.kfs.core.api.criteria;

/*
 * CU customization: The noEqual method has been removed from the base code PredicateFactory class with FINP-8446, 5/18/22, that cleaned unused messages.
 * This CU customized class has been placed in the org.kuali.kfs package rather than the edu.cornell.kfs package to allow for access to the CriteriaSupportUtils class.
 */
public final class CuPredicateFactory {
    
    private CuPredicateFactory() {
        throw new IllegalArgumentException("do not call");
    }
    
    /**
     * Creates a predicate representing not equals comparison.  Defines that the property
     * represented by the given path should <strong>not</strong> be
     * equal to the specified value.
     *
     * <p>Supports the following types of values:
     *
     * <ul>
     *   <li>character data</li>
     *   <li>decimals</li>
     *   <li>integers</li>
     *   <li>date-time</li>
     * </ul>
     *
     * @param propertyPath the path to the property which should be evaluated
     * @param value        the value to compare with the property value located at the
     *                     propertyPath
     * @return a predicate
     * @throws IllegalArgumentException if the propertyPath is null
     * @throws IllegalArgumentException if the value is null or of an invalid type
     */
    public static Predicate notEqual(String propertyPath, Object value) {
        return new NotEqualPredicate(propertyPath, CriteriaSupportUtils.determineCriteriaValue(value));
    }

}
