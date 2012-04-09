package edu.cornell.kfs.module.bc.businessobject;

import java.util.LinkedHashMap;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kns.bo.PersistableBusinessObjectBase;
import org.kuali.rice.kns.bo.TransientBusinessObjectBase;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

import edu.cornell.kfs.module.bc.CUBCConstants;

public class SipImportData extends PersistableBusinessObjectBase {  
	protected String unitId;
	protected String hrDeptId;
	protected String kfsDeptId;
	protected String deptName;
	protected String positionNbr;
	protected String posDescr;
	protected String emplId;
	protected String personNm;
	protected String sipEligFlag;
	protected String emplType;
	protected String emplRcd;
	protected String jobCode;
	protected String jobCdDescShrt;
	protected String jobFamily;
	protected KualiDecimal posFte;
	protected String posGradeDflt;
	protected String cuStateCert;
	protected String compFreq;
	protected KualiDecimal annlRt;
	protected KualiDecimal compRt;
	protected KualiDecimal jobStdHrs;
	protected KualiDecimal wrkMnths;
	protected String jobFunc;
	protected String jobFuncDesc;
	protected KualiDecimal incToMin;
	protected KualiDecimal equity;
	protected KualiDecimal merit;
	protected String note;
	protected KualiDecimal deferred;
	protected String cuAbbrFlag;
	protected KualiDecimal apptTotIntndAmt;
	protected KualiDecimal apptRqstFteQty;
	protected String positionType;

    @Override
    protected LinkedHashMap toStringMapper() {
        // TODO Auto-generated method stub
        return null;
    }

	/**
	 * @return the unitId
	 */
	public String getUnitId() {
		return unitId;
	}

	/**
	 * @param unitId the unitId to set
	 */
	public void setUnitId(String unitId) {
		this.unitId = unitId;
	}

	/**
	 * @return the hrDeptId
	 */
	public String getHrDeptId() {
		return hrDeptId;
	}

	/**
	 * @param hrDeptId the hrDeptId to set
	 */
	public void setHrDeptId(String hrDeptId) {
		this.hrDeptId = hrDeptId;
	}

	/**
	 * @return the kfsDeptId
	 */
	public String getKfsDeptId() {
		return kfsDeptId;
	}

	/**
	 * @param kfsDeptId the kfsDeptId to set
	 */
	public void setKfsDeptId(String kfsDeptId) {
		this.kfsDeptId = kfsDeptId;
	}

	/**
	 * @return the deptName
	 */
	public String getDeptName() {
		return deptName;
	}

	/**
	 * @param deptName the deptName to set
	 */
	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	/**
	 * @return the positionNbr
	 */
	public String getPositionNbr() {
		return positionNbr;
	}

	/**
	 * @param positionNbr the positionNbr to set
	 */
	public void setPositionNbr(String positionNbr) {
		this.positionNbr = positionNbr;
	}

	/**
	 * @return the posDescr
	 */
	public String getPosDescr() {
		return posDescr;
	}

	/**
	 * @param posDescr the posDescr to set
	 */
	public void setPosDescr(String posDescr) {
		this.posDescr = posDescr;
	}

	/**
	 * @return the emplId
	 */
	public String getEmplId() {
		return emplId;
	}

	/**
	 * @param emplId the emplId to set
	 */
	public void setEmplId(String emplId) {
		this.emplId = emplId;
	}

	/**
	 * @return the personNm
	 */
	public String getPersonNm() {
		return personNm;
	}

	/**
	 * @param personNm the personNm to set
	 */
	public void setPersonNm(String personNm) {
		this.personNm = personNm;
	}

	/**
	 * @return the sipEligFlag
	 */
	public String getSipEligFlag() {
		return sipEligFlag;
	}

	/**
	 * @param sipEligFlag the sipEligFlag to set
	 */
	public void setSipEligFlag(String sipEligFlag) {
		this.sipEligFlag = sipEligFlag;
	}

	/**
	 * @return the emplType
	 */
	public String getEmplType() {
		return emplType;
	}

	/**
	 * @param emplType the emplType to set
	 */
	public void setEmplType(String emplType) {
		this.emplType = emplType;
	}

	/**
	 * @return the emplRcd
	 */
	public String getEmplRcd() {
		return emplRcd;
	}

	/**
	 * @param emplRcd the emplRcd to set
	 */
	public void setEmplRcd(String emplRcd) {
		this.emplRcd = emplRcd;
	}

	/**
	 * @return the jobCode
	 */
	public String getJobCode() {
		return jobCode;
	}

	/**
	 * @param jobCode the jobCode to set
	 */
	public void setJobCode(String jobCode) {
		this.jobCode = jobCode;
	}

	/**
	 * @return the jobCdDescShrt
	 */
	public String getJobCdDescShrt() {
		return jobCdDescShrt;
	}

	/**
	 * @param jobCdDescShrt the jobCdDescShrt to set
	 */
	public void setJobCdDescShrt(String jobCdDescShrt) {
		this.jobCdDescShrt = jobCdDescShrt;
	}

	/**
	 * @return the jobFamily
	 */
	public String getJobFamily() {
		return jobFamily;
	}

	/**
	 * @param jobFamily the jobFamily to set
	 */
	public void setJobFamily(String jobFamily) {
		this.jobFamily = jobFamily;
	}

	/**
	 * @return the posFte
	 */
	public KualiDecimal getPosFte() {
		return (ObjectUtils.isNull(posFte) ? KualiDecimal.ZERO : posFte);
	}

	/**
	 * @param posFte the posFte to set
	 */
	public void setPosFte(KualiDecimal posFte) {
		this.posFte = posFte;
	}

	/**
	 * @return the posGradeDflt
	 */
	public String getPosGradeDflt() {
		return posGradeDflt;
	}

	/**
	 * @param posGradeDflt the posGradeDflt to set
	 */
	public void setPosGradeDflt(String posGradeDflt) {
		this.posGradeDflt = posGradeDflt;
	}

	/**
	 * @return the cuStateCert
	 */
	public String getCuStateCert() {
		return cuStateCert;
	}

	/**
	 * @param cuStateCert the cuStateCert to set
	 */
	public void setCuStateCert(String cuStateCert) {
		this.cuStateCert = cuStateCert;
	}

	/**
	 * @return the compFreq
	 */
	public String getCompFreq() {
		return compFreq;
	}

	/**
	 * @param compFreq the compFreq to set
	 */
	public void setCompFreq(String compFreq) {
		this.compFreq = compFreq;
	}

	/**
	 * @return the annlRt
	 */
	public KualiDecimal getAnnlRt() {
		return (ObjectUtils.isNull(annlRt) ? KualiDecimal.ZERO : annlRt);
	}

	/**
	 * @param annlRt the annlRt to set
	 */
	public void setAnnlRt(KualiDecimal annlRt) {
		this.annlRt = annlRt;
	}

	/**
	 * @return the compRt
	 */
	public KualiDecimal getCompRt() {
		return (ObjectUtils.isNull(compRt) ? KualiDecimal.ZERO : compRt);
	}

	/**
	 * @param compRt the compRt to set
	 */
	public void setCompRt(KualiDecimal compRt) {
		this.compRt = compRt;
	}

	/**
	 * @return the jobStdHrs
	 */
	public KualiDecimal getJobStdHrs() {
		return (ObjectUtils.isNull(jobStdHrs) ? KualiDecimal.ZERO : jobStdHrs);
	}

	/**
	 * @param jobStdHrs the jobStdHrs to set
	 */
	public void setJobStdHrs(KualiDecimal jobStdHrs) {
		this.jobStdHrs = jobStdHrs;
	}

	/**
	 * @return the wrkMnths
	 */
	public KualiDecimal getWrkMnths() {
		return (ObjectUtils.isNull(wrkMnths) ? KualiDecimal.ZERO : wrkMnths);
	}

	/**
	 * @param wrkMnths the wrkMnths to set
	 */
	public void setWrkMnths(KualiDecimal wrkMnths) {
		this.wrkMnths = wrkMnths;
	}

	/**
	 * @return the jobFunc
	 */
	public String getJobFunc() {
		return jobFunc;
	}

	/**
	 * @param jobFunc the jobFunc to set
	 */
	public void setJobFunc(String jobFunc) {
		this.jobFunc = jobFunc;
	}

	/**
	 * @return the jobFuncDesc
	 */
	public String getJobFuncDesc() {
		return jobFuncDesc;
	}

	/**
	 * @param jobFuncDesc the jobFuncDesc to set
	 */
	public void setJobFuncDesc(String jobFuncDesc) {
		this.jobFuncDesc = jobFuncDesc;
	}

	/**
	 * @return the incToMin
	 */
	public KualiDecimal getIncToMin() {
		return (ObjectUtils.isNull(incToMin) ? KualiDecimal.ZERO : incToMin);
	}

	/**
	 * @param incToMin the incToMin to set
	 */
	public void setIncToMin(KualiDecimal incToMin) {
		this.incToMin = incToMin;
	}

	/**
	 * @return the equity
	 */
	public KualiDecimal getEquity() {
		return (ObjectUtils.isNull(equity) ? KualiDecimal.ZERO : equity);
	}

	/**
	 * @param equity the equity to set
	 */
	public void setEquity(KualiDecimal equity) {
		this.equity = equity;
	}

	/**
	 * @return the merit
	 */
	public KualiDecimal getMerit() {
		return (ObjectUtils.isNull(merit) ? KualiDecimal.ZERO : merit);
	}

	/**
	 * @param merit the merit to set
	 */
	public void setMerit(KualiDecimal merit) {
		this.merit = merit;
	}

	/**
	 * @return the note
	 */
	public String getNote() {
		return note;
	}

	/**
	 * @param note the note to set
	 */
	public void setNote(String note) {
		this.note = note;
	}

	/**
	 * @return the deferred
	 */
	public KualiDecimal getDeferred() {
		return (ObjectUtils.isNull(deferred) ? KualiDecimal.ZERO : deferred);
	}

	/**
	 * @param deferred the deferred to set
	 */
	public void setDeferred(KualiDecimal deferred) {
		this.deferred = deferred;
	}

	/**
	 * @return the cuAbbrFlag
	 */
	public String getCuAbbrFlag() {
		return cuAbbrFlag;
	}

	/**
	 * @param cuAbbrFlag the cuAbbrFlag to set
	 */
	public void setCuAbbrFlag(String cuAbbrFlag) {
		this.cuAbbrFlag = cuAbbrFlag;
	}

	/**
	 * @return the apptTotIntndAmt
	 */
	public KualiDecimal getApptTotIntndAmt() {
		return (ObjectUtils.isNull(apptTotIntndAmt) ? KualiDecimal.ZERO : apptTotIntndAmt);
	}

	/**
	 * @param apptTotIntndAmt the apptTotIntndAmt to set
	 */
	public void setApptTotIntndAmt(KualiDecimal apptTotIntndAmt) {
		this.apptTotIntndAmt = apptTotIntndAmt;
	}

	/**
	 * @return the apptRqstFteQty
	 */
	public KualiDecimal getApptRqstFteQty() {
		return (ObjectUtils.isNull(apptRqstFteQty) ? KualiDecimal.ZERO : apptRqstFteQty);
	}

	/**
	 * @param apptRqstFteQty the apptRqstFteQty to set
	 */
	public void setApptRqstFteQty(KualiDecimal apptRqstFteQty) {
		this.apptRqstFteQty = apptRqstFteQty;
	}

	/**
	 * @return the positionType
	 */
	public String getPositionType() {
		return positionType;
	}

	/**
	 * @param positionType the positionType to set
	 */
	public void setPositionType(String positionType) {
		this.positionType = positionType;
	}
}
