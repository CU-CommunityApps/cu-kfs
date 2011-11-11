package edu.cornell.kfs.module.bc.businessobject;

import java.util.LinkedHashMap;

import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.util.KualiDecimal;

public class PSJobCode extends PersistableBusinessObjectBase {
    protected String jobCode;
    protected String jobCodeDesc;
    protected String jobCodeDescShort;
    protected String cuObjectCode;
    protected KualiDecimal jobStandardHours;
    protected String jobFamily;

    @Override
    protected LinkedHashMap toStringMapper() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getJobCode() {
        return jobCode;
    }

    public void setJobCode(String jobCode) {
        this.jobCode = jobCode;
    }

    public String getJobCodeDesc() {
        return jobCodeDesc;
    }

    public void setJobCodeDesc(String jobCodeDesc) {
        this.jobCodeDesc = jobCodeDesc;
    }

    public String getJobCodeDescShort() {
        return jobCodeDescShort;
    }

    public void setJobCodeDescShort(String jobCodeDescShort) {
        this.jobCodeDescShort = jobCodeDescShort;
    }

    public String getCuObjectCode() {
        return cuObjectCode;
    }

    public void setCuObjectCode(String cuObjectCode) {
        this.cuObjectCode = cuObjectCode;
    }

    public KualiDecimal getJobStandardHours() {
        return jobStandardHours;
    }

    public void setJobStandardHours(KualiDecimal jobStandardHours) {
        this.jobStandardHours = jobStandardHours;
    }

    public String getJobFamily() {
        return jobFamily;
    }

    public void setJobFamily(String jobFamily) {
        this.jobFamily = jobFamily;
    }

}
