package edu.cornell.kfs.pmw.batch;

import org.apache.commons.lang3.StringUtils;


public enum SupplierCategory {
    US_INDIVIDUAL("US Individual"),
    US_ENTITY("US Entity"),
    FOREIGN_INDIVIDUAL("Foreign Individual"),
    FOREIGN_ENTITY("Foreign Entity");
    
    public final String description;
    
    private SupplierCategory(String description) {
        this.description = description;
    }
    
    public static SupplierCategory findSupplierCategoryByDescription(String description) {
        for (SupplierCategory category : SupplierCategory.values()) {
            if (StringUtils.equalsAnyIgnoreCase(category.description, description)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unable to find a Supplier Category with a description of " + description);
    }
}
