package edu.cornell.kfs.kew.routeheader.dao;

import java.sql.Timestamp;
import java.util.Map;

import org.kuali.kfs.kew.routeheader.dao.DocumentRouteHeaderDAO;

public interface CuDocumentRouteHeaderDAO extends DocumentRouteHeaderDAO {

    Map<String, Timestamp> getFinalizedDatesForDocumentType(String documentTypeName, Timestamp startDate,
            Timestamp endDate);

}
