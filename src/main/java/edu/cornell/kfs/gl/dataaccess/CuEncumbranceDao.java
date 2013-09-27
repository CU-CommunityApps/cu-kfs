package edu.cornell.kfs.gl.dataaccess;

import java.util.Collection;
import java.util.Iterator;

import org.kuali.kfs.gl.dataaccess.EncumbranceDao;

public interface CuEncumbranceDao extends EncumbranceDao {
    /**
     * Returns an Iterator of all encumbrances that need to be closed for the fiscal year and specified charts
     * 
     * @param fiscalYear a fiscal year to find encumbrances for
     * @param charts charts to find encumbrances for
     * @return an Iterator of encumbrances to close
     */
    public Iterator getEncumbrancesToClose(Integer fiscalYear, Collection<String> charts);


}
