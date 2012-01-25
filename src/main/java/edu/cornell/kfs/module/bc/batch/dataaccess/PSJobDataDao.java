package edu.cornell.kfs.module.bc.batch.dataaccess;

import java.util.Collection;
import java.util.List;

import edu.cornell.kfs.module.bc.businessobject.PSJobData;

public interface PSJobDataDao {

    /**
     * Gets all the executives from the database.
     * 
     * @return the executives
     */
    public Collection<PSJobData> getExistingExecutives();

    /**
     * Gets a collection of PSPositionData entries by a list of position numbers.
     * 
     * @param positionNumber
     * @return a collection of PSPositionData entries by a list of position numbers.
     */
    public Collection<PSJobData> getPSJobDataEntriesByPositionNumbers(List<String> positionNumbers);
}
