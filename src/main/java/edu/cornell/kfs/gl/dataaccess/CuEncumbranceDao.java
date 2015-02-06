package edu.cornell.kfs.gl.dataaccess;

import java.util.Collection;
import java.util.Map;

import org.kuali.kfs.gl.dataaccess.EncumbranceDao;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public interface CuEncumbranceDao extends EncumbranceDao {
    /**
     * this is for KFSPTS-1786 begin
     */
    @SuppressWarnings("rawtypes")
    KualiDecimal getEncumbrances(Map<String, String> input,Collection encumbranceCodes);
    /**
     * this is for KFSPTS-1786 end
     */ 

}
