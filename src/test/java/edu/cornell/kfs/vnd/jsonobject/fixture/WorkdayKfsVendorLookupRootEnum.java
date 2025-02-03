package edu.cornell.kfs.vnd.jsonobject.fixture;

import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;

public enum WorkdayKfsVendorLookupRootEnum {
    ACTIVE("src/test/resources/edu/cornell/kfs/vnd/jsonobject/fixture/WorkdayKfsVendorLookupRoot_active.json", WorkdayKfsVendorLookupResultEnum.ACTIVE, true),
    INACTIVE("src/test/resources/edu/cornell/kfs/vnd/jsonobject/fixture/WorkdayKfsVendorLookupRoot_inactive.json", WorkdayKfsVendorLookupResultEnum.INACTIVE, false),
    EMPTY("src/test/resources/edu/cornell/kfs/vnd/jsonobject/fixture/WorkdayKfsVendorLookupRoot_empty.json", null, false);
    
    public final String fileName;
    public final WorkdayKfsVendorLookupResultEnum result;
    public final boolean activeResult;
    
    private WorkdayKfsVendorLookupRootEnum(String fileName, WorkdayKfsVendorLookupResultEnum result, boolean activeResult) {
        this.fileName = fileName;
        this.result = result;
        this.activeResult = activeResult;
    }
    
    public WorkdayKfsVendorLookupRoot toWorkdayKfsVendorLookupRoot() {
        WorkdayKfsVendorLookupRoot root = new WorkdayKfsVendorLookupRoot();
        if (result != null) {
            root.getResults().add(result.toWorkdayKfsVendorLookupResult());
        }
        return root;
    }
}
