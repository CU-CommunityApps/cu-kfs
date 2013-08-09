package edu.cornell.kfs.fp.businessobject;
import java.sql.Date;
import java.util.Map;

import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.businessobject.SourceAccountingLine;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.sys.CUKFSConstants.PreEncumbranceSourceAccountingLineConstants;
public class PreEncumbranceSourceAccountingLine extends SourceAccountingLine implements PreEncumbranceAccountingLine {  
        
        /**
         * 
         */
        private static final long serialVersionUID = 1L;
        private String partialTransactionCount;
        private KualiDecimal partialAmount;
        private Date endDate;
        private Date startDate;
        private String autoDisEncumberType;
        public PreEncumbranceSourceAccountingLine() {
                super();                
        }
        
        public String getPartialTransactionCount() {
                return partialTransactionCount;
        }
        
        public void setPartialTransactionCount(String partialTransactionCount) {
                this.partialTransactionCount = partialTransactionCount;
        }
        
        public KualiDecimal getPartialAmount() {
                return partialAmount;
        }
        
        public void setPartialAmount(KualiDecimal partialAmount) {
                this.partialAmount = partialAmount;
        }
        
        public String getAutoDisEncumberType() {
                return autoDisEncumberType;
        }
        
        public void setAutoDisEncumberType(String autoDisEncumberType) {
                this.autoDisEncumberType = autoDisEncumberType;
        }
        
        public Date getEndDate() {
                return endDate;
        }
        
        public void setEndDate(Date endDate) {
                this.endDate = endDate;
        }
        public Date getStartDate() {
                return startDate;
        }
        public void setStartDate(Date startDate) {
                this.startDate = startDate;
        }
        
        @Override
        public Map getValuesMap() {
                Map simpleValues = super.getValuesMap();
                simpleValues.put(PreEncumbranceSourceAccountingLineConstants.PARTIAL_TRANSACTION_COUNT, partialTransactionCount);
                simpleValues.put(PreEncumbranceSourceAccountingLineConstants.PARTIAL_AMOUNT, partialAmount);
                simpleValues.put(PreEncumbranceSourceAccountingLineConstants.START_DATE, startDate);
                simpleValues.put(PreEncumbranceSourceAccountingLineConstants.END_DATE, endDate);
                simpleValues.put(PreEncumbranceSourceAccountingLineConstants.AUTO_DISENCUMBER_TYPE, autoDisEncumberType);
        return simpleValues;
        }
        @Override
        public boolean isSourceAccountingLine() {
                return true;
        }
        
        @Override
        public boolean isTargetAccountingLine() {
                return false;
        }
        
        @Override
        public void copyFrom(AccountingLine other) {
                super.copyFrom(other);
                PreEncumbranceAccountingLineUtil.copyFrom(this, other);
        }
}