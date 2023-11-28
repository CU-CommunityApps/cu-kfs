package edu.cornell.kfs.kew.routeheader.service.impl;

import java.sql.Timestamp;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.kew.routeheader.service.impl.RouteHeaderServiceImpl;

import edu.cornell.kfs.kew.routeheader.dao.CuDocumentRouteHeaderDAO;
import edu.cornell.kfs.kew.routeheader.service.CuRouteHeaderService;

public class CuRouteHeaderServiceImpl extends RouteHeaderServiceImpl implements CuRouteHeaderService {

    @Override
    public Map<String, Timestamp> getFinalizedDatesForDocumentType(
            final String documentTypeName, final Timestamp startDate,
            final Timestamp endDate) {
        if (StringUtils.isBlank(documentTypeName)) {
            throw new IllegalArgumentException("documentTypeName cannot be blank");
        } else if (startDate == null) {
            throw new IllegalArgumentException("startDate cannot be null");
        } else if (endDate == null) {
            throw new IllegalArgumentException("endDate cannot be null");
        } else if (startDate.compareTo(endDate) > 0) {
            throw new IllegalArgumentException("startDate cannot be later than endDate");
        }
        return getCuRouteHeaderDAO().getFinalizedDatesForDocumentType(documentTypeName, startDate, endDate);
    }

    private CuDocumentRouteHeaderDAO getCuRouteHeaderDAO() {
        return (CuDocumentRouteHeaderDAO) getRouteHeaderDAO();
    }

}
