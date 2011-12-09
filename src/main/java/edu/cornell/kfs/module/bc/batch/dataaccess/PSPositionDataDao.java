package edu.cornell.kfs.module.bc.batch.dataaccess;

public interface PSPositionDataDao {

    /**
     * Checks if Position is in HR / P has a "Budgeted Position" = "Y"
     * 
     * @param positionNumber
     * 
     * @return true if budgeted, false otherwise
     */
    public boolean isPositionBudgeted(String positionNumber);

}
