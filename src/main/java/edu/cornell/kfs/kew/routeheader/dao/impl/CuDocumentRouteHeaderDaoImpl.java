package edu.cornell.kfs.kew.routeheader.dao.impl;

import java.sql.Timestamp;
import java.util.Map;

import org.kuali.kfs.core.framework.persistence.platform.DatabasePlatform;
import org.kuali.kfs.kew.doctype.service.DocumentTypeService;
import org.kuali.kfs.kew.routeheader.dao.impl.DocumentRouteHeaderDAOOjbImpl;
import org.kuali.kfs.kew.routeheader.dao.impl.DocumentRouteHeaderDaoImpl;
import org.springframework.jdbc.core.JdbcTemplate;

import edu.cornell.kfs.kew.routeheader.dao.CuDocumentRouteHeaderDAO;

public class CuDocumentRouteHeaderDaoImpl extends DocumentRouteHeaderDaoImpl implements CuDocumentRouteHeaderDAO {

    private final CuDocumentRouteHeaderDAOOjbImpl cuDocumentRouteHeaderDaoOjb;

    public CuDocumentRouteHeaderDaoImpl(
            final DatabasePlatform databasePlatform,
            final DocumentRouteHeaderDAOOjbImpl documentRouteHeaderDaoOjb,
            final DocumentTypeService documentTypeService,
            final JdbcTemplate jdbcTemplate
    ) {
        super(databasePlatform, documentRouteHeaderDaoOjb, documentTypeService, jdbcTemplate);
        this.cuDocumentRouteHeaderDaoOjb = (CuDocumentRouteHeaderDAOOjbImpl) documentRouteHeaderDaoOjb;
    }

    @Override
    public Map<String, Timestamp> getFinalizedDatesForDocumentType(
            final String documentTypeName, final Timestamp startDate, final Timestamp endDate) {
        return cuDocumentRouteHeaderDaoOjb.getFinalizedDatesForDocumentType(documentTypeName, startDate, endDate);
    }

}
