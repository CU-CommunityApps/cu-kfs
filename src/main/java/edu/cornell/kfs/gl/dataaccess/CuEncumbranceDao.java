package edu.cornell.kfs.gl.dataaccess;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.kuali.kfs.gl.dataaccess.EncumbranceDao;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public interface CuEncumbranceDao extends EncumbranceDao {
    /**
     * this is for KFSPTS-1786 begin
     */ 
    public KualiDecimal getEncumbrances(Map<String, String> input,Collection encumbranceCodes);
    /**
     * this is for KFSPTS-1786 end
     */ 
    
    /**
     * Returns an Iterator of all encumbrances that need to be closed for the fiscal year and specified charts
     * 
     * @param fiscalYear a fiscal year to find encumbrances for
     * @param charts charts to find encumbrances for
     * @return an Iterator of encumbrances to close
     */
    public Iterator getEncumbrancesToClose(Integer fiscalYear, Collection<String> charts);


}
