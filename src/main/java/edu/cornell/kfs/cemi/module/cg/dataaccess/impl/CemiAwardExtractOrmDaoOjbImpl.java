package edu.cornell.kfs.cemi.module.cg.dataaccess.impl;

import java.text.MessageFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.module.cg.businessobject.Award;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.vnd.VendorPropertyConstants;
import org.kuali.kfs.vnd.businessobject.VendorAddress;

import edu.cornell.kfs.cemi.module.cg.CemiAwardConstants;
import edu.cornell.kfs.cemi.module.cg.CemiAwardPropertyConstants;
import edu.cornell.kfs.cemi.module.cg.batch.businessobject.CemiAwardLegacyNovelutionBo;
import edu.cornell.kfs.cemi.module.cg.dataaccess.CemiAwardExtractOrmDao;
import edu.cornell.kfs.cemi.sys.CemiBaseConstants;
import edu.cornell.kfs.cemi.sys.dataaccess.impl.CemiOrmDaoOjbImplBase;
import edu.cornell.kfs.sys.util.CuOjbUtils;

public class CemiAwardExtractOrmDaoOjbImpl extends CemiOrmDaoOjbImplBase implements CemiAwardExtractOrmDao {

    @Override
    public Stream<Award> getAwardsForCemiAwardExtractAsCloseableStream() {
        final String proposalNumberCondition;
            
        // Local environment configuration property setting used by base class method call 
        // to reduce processing time for local development during CEMI project work.
            if (shouldUseLessDataDuringCemiDevelopment()) {
                // This conditional was added to reduce processing time for local development during CEMI project work.
                // The values were chosen for the WHERE clause to restrict the result set to roughly 1000 rows as
                // well as provide both old and new legacy business objects that had a variety of attributes for local verification. 
                proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
                        + "SELECT CGPRPSL_NBR FROM CEMI.CU_CEMI_AWD_EXTR_AWD_T"
                        + " WHERE CGPRPSL_NBR <= 139300 OR CGPRPSL_NBR >= 193300)";
            } else {
                proposalNumberCondition = "(A0.CGPRPSL_NBR) IN ("
                        + "SELECT CGPRPSL_NBR FROM CEMI.CU_CEMI_AWD_EXTR_AWD_T)";
            }
        final Criteria criteria = new Criteria();
            criteria.addSql(proposalNumberCondition);
            
        final QueryByCriteria query = new QueryByCriteria(Award.class, criteria);
        query.addOrderByAscending(KFSPropertyConstants.PROPOSAL_NUMBER);
        return CuOjbUtils.buildCloseableStreamForQueryResults(
                Award.class,
                () -> getPersistenceBrokerTemplate().getIteratorByQuery(query));
    }
    
    @Override
    public List<CemiAwardLegacyNovelutionBo> getAwardNovelutionAtributesForCemiAwardExtractAsCloseableStream(String awardProposalNumber) {
        Validate.isTrue(CemiBaseConstants.WORD_CHARS_PATTERN.matcher(awardProposalNumber).matches(),
                "awardProposalNumber must only contain word characters (letters, digits, underscores)");
        
        String spreadsheetKeyToSearchFor = MessageFormat.format(CemiAwardConstants.SPREADSHEET_KEY_FORMAT, awardProposalNumber);

        final Criteria criteria = new Criteria();
        criteria.addEqualTo(CemiAwardPropertyConstants.SPREADSHEET_KEY, spreadsheetKeyToSearchFor);

        final QueryByCriteria query = new QueryByCriteria(CemiAwardLegacyNovelutionBo.class, criteria);
        final Collection<?> results = getPersistenceBrokerTemplate().getCollectionByQuery(query);

        return results.stream()
                .map(CemiAwardLegacyNovelutionBo.class::cast)
                .collect(Collectors.toUnmodifiableList());
    }

}
