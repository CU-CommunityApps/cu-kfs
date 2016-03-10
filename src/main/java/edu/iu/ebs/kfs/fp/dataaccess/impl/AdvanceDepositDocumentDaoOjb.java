package edu.iu.ebs.kfs.fp.dataaccess.impl;

import java.util.List;

import org.apache.ojb.broker.query.Criteria;
import org.apache.ojb.broker.query.QueryByCriteria;
import org.kuali.kfs.fp.document.AdvanceDepositDocument;
import org.kuali.rice.core.framework.persistence.ojb.dao.PlatformAwareDaoBaseOjb;

import edu.iu.ebs.kfs.fp.dataaccess.AdvanceDepositDocumentDao;

/**
Copyright Indiana University
   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as
   published by the Free Software Foundation, either version 3 of the
   License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.
   
   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
 
public class AdvanceDepositDocumentDaoOjb extends  PlatformAwareDaoBaseOjb implements AdvanceDepositDocumentDao  {
    
    public List<AdvanceDepositDocument> getAdvanceDepositDocumentByCriteria(Criteria criteria){
        return (List<AdvanceDepositDocument>) getPersistenceBrokerTemplate().getCollectionByQuery(new QueryByCriteria(AdvanceDepositDocument.class, criteria));
    }

}
