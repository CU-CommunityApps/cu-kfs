package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.sys.KFSConstants;

public interface PurgeableBusinessObjectInterface {
    
    String buildObjectSpecificPurgeableRecordData();
    
    public default String buildPurgeableRecordingString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append(KFSConstants.SQUARE_BRACKET_LEFT).append(KFSConstants.NEWLINE);
        sb.append(buildObjectSpecificPurgeableRecordData());
        sb.append(KFSConstants.SQUARE_BRACKET_RIGHT);
        return sb.toString();
    }

}
