package edu.cornell.kfs.vnd.batch.service;

public interface VendorEmployeeComparisonService {

    void generateFileContainingPotentialVendorEmployees();

    boolean processResultsOfVendorEmployeeComparison();

}
