package edu.cornell.kfs.vnd.jsonobject.fixture;

import edu.cornell.kfs.vnd.jsonobject.WorkdayKfsVendorLookupRoot;

public enum WorkdayKfsVendorLookupRootEnum {
    ACTIVE("src/test/resources/edu/cornell/kfs/vnd/jsonobject/fixture/WorkdayKfsVendorLookupRoot_active.json", WorkdayKfsVendorLookupResultEnum.ACTIVE),
    INACTIVE("src/test/resources/edu/cornell/kfs/vnd/jsonobject/fixture/WorkdayKfsVendorLookupRoot_inactive.json", WorkdayKfsVendorLookupResultEnum.INACTIVE),
    EMPTY("src/test/resources/edu/cornell/kfs/vnd/jsonobject/fixture/WorkdayKfsVendorLookupRoot_empty.json", null);
    
    public final String fileName;
    public final WorkdayKfsVendorLookupResultEnum result;
    
    private WorkdayKfsVendorLookupRootEnum(String fileName, WorkdayKfsVendorLookupResultEnum result) {
        this.fileName = fileName;
        this.result = result;
    }
    
    public WorkdayKfsVendorLookupRoot toWorkdayKfsVendorLookupRoot() {
        WorkdayKfsVendorLookupRoot root = new WorkdayKfsVendorLookupRoot();
        if (result != null) {
            root.getResults().add(result.toWorkdayKfsVendorLookupResult());
        }
        return root;
    }
}
