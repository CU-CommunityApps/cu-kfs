package edu.cornell.kfs.coa.fixture;

import org.kuali.kfs.krad.service.BusinessObjectService;

import edu.cornell.kfs.coa.businessobject.ReversionCategory;

public enum ReversionCategoryFixture {
	A1_CATEGORY("A1", "Reversion", "A1", true),
	BOGUS_CATEGORY("A2", "BOUGS", "A2", true);
	
    public final String reversionCategoryCode;
    public final String reversionCategoryName;
    public final String reversionSortCode;
    public final boolean active;

	private ReversionCategoryFixture(String reversionCategoryCode, String reversionCategoryName, String reversionSortCode,  boolean active) {
        this.reversionCategoryCode = reversionCategoryCode;
        this.reversionCategoryName = reversionCategoryName;
        this.reversionSortCode = reversionSortCode;
        this.active = active;
	}
	
   public ReversionCategory createReversionCategory() {
	   ReversionCategory reversionCategory = new ReversionCategory();
	   reversionCategory.setReversionCategoryCode(this.reversionCategoryCode);
	   reversionCategory.setReversionCategoryName(this.reversionCategoryName);
	   reversionCategory.setReversionSortCode(this.reversionSortCode);
	   reversionCategory.setActive(this.active);
       return reversionCategory;
    }
 
    public ReversionCategory createAccountReversion(BusinessObjectService businessObjectService) {
        return (ReversionCategory) businessObjectService.retrieve(this.createReversionCategory());
    }
}
