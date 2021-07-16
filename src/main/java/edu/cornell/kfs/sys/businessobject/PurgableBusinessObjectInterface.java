package edu.cornell.kfs.sys.businessobject;

import org.kuali.kfs.sys.KFSConstants;

public interface PurgableBusinessObjectInterface {
    
    String buildObjectSpecifiicPurgableRecordData();
    
    public default String buildPurgableRecordingString() {
        StringBuilder sb = new StringBuilder(this.getClass().getName());
        sb.append(KFSConstants.SQUARE_BRACKET_LEFT).append(KFSConstants.NEWLINE);
        sb.append(buildObjectSpecifiicPurgableRecordData());
        sb.append(KFSConstants.SQUARE_BRACKET_RIGHT);
        return sb.toString();
    }

}
