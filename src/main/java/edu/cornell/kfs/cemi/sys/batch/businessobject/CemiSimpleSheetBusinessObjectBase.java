package edu.cornell.kfs.cemi.sys.batch.businessobject;

/**
 * Base class for CEMI sheet BOs that don't need to concern themselves with having the BO field names
 * match the DTO field names. Fields are named colA, colB, ... , colAA, colAB, ... to correlate
 * to the numbered columns in an Excel spreadsheet. Only a bare bones subclass extending this one
 * is needed to help OJB differentiate between BOs; extra or overridden functionality is optional.
 * 
 * Note that this class extends from and uses the fields from CemiIndexedBusinessObjectBase.
 */
public abstract class CemiSimpleSheetBusinessObjectBase extends CemiIndexedBusinessObjectBase {

    private static final long serialVersionUID = -3908205073440342855L;

    private String colA;
    private String colB;
    private String colC;
    private String colD;
    private String colE;
    private String colF;
    private String colG;
    private String colH;
    private String colI;
    private String colJ;
    private String colK;
    private String colL;
    private String colM;
    private String colN;
    private String colO;
    private String colP;
    private String colQ;
    private String colR;
    private String colS;
    private String colT;
    private String colU;
    private String colV;
    private String colW;
    private String colX;
    private String colY;
    private String colZ;
    private String colAA;
    private String colAB;
    private String colAC;
    private String colAD;
    private String colAE;
    private String colAF;
    private String colAG;
    private String colAH;
    private String colAI;
    private String colAJ;
    private String colAK;
    private String colAL;
    private String colAM;
    private String colAN;
    private String colAO;
    private String colAP;
    private String colAQ;
    private String colAR;
    private String colAS;
    private String colAT;
    private String colAU;
    private String colAV;
    private String colAW;
    private String colAX;
    private String colAY;
    private String colAZ;
    private String colBA;
    private String colBB;
    private String colBC;
    private String colBD;
    private String colBE;
    private String colBF;
    private String colBG;
    private String colBH;
    private String colBI;
    private String colBJ;
    private String colBK;
    private String colBL;
    private String colBM;
    private String colBN;
    private String colBO;
    private String colBP;
    private String colBQ;
    private String colBR;
    private String colBS;
    private String colBT;
    private String colBU;
    private String colBV;
    private String colBW;
    private String colBX;
    private String colBY;
    private String colBZ;
    private String colCA;
    private String colCB;
    private String colCC;
    private String colCD;
    private String colCE;
    private String colCF;
    private String colCG;
    private String colCH;
    private String colCI;
    private String colCJ;
    private String colCK;
    private String colCL;
    private String colCM;
    private String colCN;
    private String colCO;
    private String colCP;
    private String colCQ;
    private String colCR;
    private String colCS;
    private String colCT;
    private String colCU;
    private String colCV;
    private String colCW;
    private String colCX;
    private String colCY;
    private String colCZ;
    private String colDA;
    private String colDB;
    private String colDC;
    private String colDD;
    private String colDE;
    private String colDF;
    private String colDG;
    private String colDH;
    private String colDI;
    private String colDJ;
    private String colDK;
    private String colDL;
    private String colDM;
    private String colDN;
    private String colDO;
    private String colDP;
    private String colDQ;
    private String colDR;
    private String colDS;
    private String colDT;
    private String colDU;
    private String colDV;
    private String colDW;
    private String colDX;
    private String colDY;
    private String colDZ;
    private String colEA;
    private String colEB;
    private String colEC;
    private String colED;
    private String colEE;
    private String colEF;
    private String colEG;
    private String colEH;
    private String colEI;
    private String colEJ;
    private String colEK;
    private String colEL;
    private String colEM;
    private String colEN;
    private String colEO;
    private String colEP;
    private String colEQ;
    private String colER;
    private String colES;
    private String colET;
    private String colEU;
    private String colEV;
    private String colEW;
    private String colEX;
    private String colEY;
    private String colEZ;
    private String colFA;
    private String colFB;
    private String colFC;
    private String colFD;
    private String colFE;
    private String colFF;
    private String colFG;
    private String colFH;
    private String colFI;
    private String colFJ;
    private String colFK;
    private String colFL;
    private String colFM;
    private String colFN;
    private String colFO;
    private String colFP;
    private String colFQ;
    private String colFR;
    private String colFS;
    private String colFT;
    private String colFU;
    private String colFV;
    private String colFW;
    private String colFX;
    private String colFY;
    private String colFZ;
    private String colGA;
    private String colGB;
    private String colGC;
    private String colGD;
    private String colGE;
    private String colGF;
    private String colGG;
    private String colGH;
    private String colGI;
    private String colGJ;
    private String colGK;
    private String colGL;
    private String colGM;
    private String colGN;
    private String colGO;
    private String colGP;
    private String colGQ;
    private String colGR;
    private String colGS;
    private String colGT;
    private String colGU;
    private String colGV;
    private String colGW;
    private String colGX;
    private String colGY;
    private String colGZ;
    private String colHA;
    private String colHB;
    private String colHC;
    private String colHD;
    private String colHE;
    private String colHF;
    private String colHG;
    private String colHH;
    private String colHI;
    private String colHJ;
    private String colHK;
    private String colHL;
    private String colHM;
    private String colHN;
    private String colHO;
    private String colHP;
    private String colHQ;
    private String colHR;
    private String colHS;
    private String colHT;
    private String colHU;
    private String colHV;
    private String colHW;
    private String colHX;
    private String colHY;
    private String colHZ;

    public String getColA() {
        return colA;
    }

    public void setColA(final String colA) {
        this.colA = colA;
    }

    public String getColB() {
        return colB;
    }

    public void setColB(final String colB) {
        this.colB = colB;
    }

    public String getColC() {
        return colC;
    }

    public void setColC(final String colC) {
        this.colC = colC;
    }

    public String getColD() {
        return colD;
    }

    public void setColD(final String colD) {
        this.colD = colD;
    }

    public String getColE() {
        return colE;
    }

    public void setColE(final String colE) {
        this.colE = colE;
    }

    public String getColF() {
        return colF;
    }

    public void setColF(final String colF) {
        this.colF = colF;
    }

    public String getColG() {
        return colG;
    }

    public void setColG(final String colG) {
        this.colG = colG;
    }

    public String getColH() {
        return colH;
    }

    public void setColH(final String colH) {
        this.colH = colH;
    }

    public String getColI() {
        return colI;
    }

    public void setColI(final String colI) {
        this.colI = colI;
    }

    public String getColJ() {
        return colJ;
    }

    public void setColJ(final String colJ) {
        this.colJ = colJ;
    }

    public String getColK() {
        return colK;
    }

    public void setColK(final String colK) {
        this.colK = colK;
    }

    public String getColL() {
        return colL;
    }

    public void setColL(final String colL) {
        this.colL = colL;
    }

    public String getColM() {
        return colM;
    }

    public void setColM(final String colM) {
        this.colM = colM;
    }

    public String getColN() {
        return colN;
    }

    public void setColN(final String colN) {
        this.colN = colN;
    }

    public String getColO() {
        return colO;
    }

    public void setColO(final String colO) {
        this.colO = colO;
    }

    public String getColP() {
        return colP;
    }

    public void setColP(final String colP) {
        this.colP = colP;
    }

    public String getColQ() {
        return colQ;
    }

    public void setColQ(final String colQ) {
        this.colQ = colQ;
    }

    public String getColR() {
        return colR;
    }

    public void setColR(final String colR) {
        this.colR = colR;
    }

    public String getColS() {
        return colS;
    }

    public void setColS(final String colS) {
        this.colS = colS;
    }

    public String getColT() {
        return colT;
    }

    public void setColT(final String colT) {
        this.colT = colT;
    }

    public String getColU() {
        return colU;
    }

    public void setColU(final String colU) {
        this.colU = colU;
    }

    public String getColV() {
        return colV;
    }

    public void setColV(final String colV) {
        this.colV = colV;
    }

    public String getColW() {
        return colW;
    }

    public void setColW(final String colW) {
        this.colW = colW;
    }

    public String getColX() {
        return colX;
    }

    public void setColX(final String colX) {
        this.colX = colX;
    }

    public String getColY() {
        return colY;
    }

    public void setColY(final String colY) {
        this.colY = colY;
    }

    public String getColZ() {
        return colZ;
    }

    public void setColZ(final String colZ) {
        this.colZ = colZ;
    }

    public String getColAA() {
        return colAA;
    }

    public void setColAA(final String colAA) {
        this.colAA = colAA;
    }

    public String getColAB() {
        return colAB;
    }

    public void setColAB(final String colAB) {
        this.colAB = colAB;
    }

    public String getColAC() {
        return colAC;
    }

    public void setColAC(final String colAC) {
        this.colAC = colAC;
    }

    public String getColAD() {
        return colAD;
    }

    public void setColAD(final String colAD) {
        this.colAD = colAD;
    }

    public String getColAE() {
        return colAE;
    }

    public void setColAE(final String colAE) {
        this.colAE = colAE;
    }

    public String getColAF() {
        return colAF;
    }

    public void setColAF(final String colAF) {
        this.colAF = colAF;
    }

    public String getColAG() {
        return colAG;
    }

    public void setColAG(final String colAG) {
        this.colAG = colAG;
    }

    public String getColAH() {
        return colAH;
    }

    public void setColAH(final String colAH) {
        this.colAH = colAH;
    }

    public String getColAI() {
        return colAI;
    }

    public void setColAI(final String colAI) {
        this.colAI = colAI;
    }

    public String getColAJ() {
        return colAJ;
    }

    public void setColAJ(final String colAJ) {
        this.colAJ = colAJ;
    }

    public String getColAK() {
        return colAK;
    }

    public void setColAK(final String colAK) {
        this.colAK = colAK;
    }

    public String getColAL() {
        return colAL;
    }

    public void setColAL(final String colAL) {
        this.colAL = colAL;
    }

    public String getColAM() {
        return colAM;
    }

    public void setColAM(final String colAM) {
        this.colAM = colAM;
    }

    public String getColAN() {
        return colAN;
    }

    public void setColAN(final String colAN) {
        this.colAN = colAN;
    }

    public String getColAO() {
        return colAO;
    }

    public void setColAO(final String colAO) {
        this.colAO = colAO;
    }

    public String getColAP() {
        return colAP;
    }

    public void setColAP(final String colAP) {
        this.colAP = colAP;
    }

    public String getColAQ() {
        return colAQ;
    }

    public void setColAQ(final String colAQ) {
        this.colAQ = colAQ;
    }

    public String getColAR() {
        return colAR;
    }

    public void setColAR(final String colAR) {
        this.colAR = colAR;
    }

    public String getColAS() {
        return colAS;
    }

    public void setColAS(final String colAS) {
        this.colAS = colAS;
    }

    public String getColAT() {
        return colAT;
    }

    public void setColAT(final String colAT) {
        this.colAT = colAT;
    }

    public String getColAU() {
        return colAU;
    }

    public void setColAU(final String colAU) {
        this.colAU = colAU;
    }

    public String getColAV() {
        return colAV;
    }

    public void setColAV(final String colAV) {
        this.colAV = colAV;
    }

    public String getColAW() {
        return colAW;
    }

    public void setColAW(final String colAW) {
        this.colAW = colAW;
    }

    public String getColAX() {
        return colAX;
    }

    public void setColAX(final String colAX) {
        this.colAX = colAX;
    }

    public String getColAY() {
        return colAY;
    }

    public void setColAY(final String colAY) {
        this.colAY = colAY;
    }

    public String getColAZ() {
        return colAZ;
    }

    public void setColAZ(final String colAZ) {
        this.colAZ = colAZ;
    }

    public String getColBA() {
        return colBA;
    }

    public void setColBA(final String colBA) {
        this.colBA = colBA;
    }

    public String getColBB() {
        return colBB;
    }

    public void setColBB(final String colBB) {
        this.colBB = colBB;
    }

    public String getColBC() {
        return colBC;
    }

    public void setColBC(final String colBC) {
        this.colBC = colBC;
    }

    public String getColBD() {
        return colBD;
    }

    public void setColBD(final String colBD) {
        this.colBD = colBD;
    }

    public String getColBE() {
        return colBE;
    }

    public void setColBE(final String colBE) {
        this.colBE = colBE;
    }

    public String getColBF() {
        return colBF;
    }

    public void setColBF(final String colBF) {
        this.colBF = colBF;
    }

    public String getColBG() {
        return colBG;
    }

    public void setColBG(final String colBG) {
        this.colBG = colBG;
    }

    public String getColBH() {
        return colBH;
    }

    public void setColBH(final String colBH) {
        this.colBH = colBH;
    }

    public String getColBI() {
        return colBI;
    }

    public void setColBI(final String colBI) {
        this.colBI = colBI;
    }

    public String getColBJ() {
        return colBJ;
    }

    public void setColBJ(final String colBJ) {
        this.colBJ = colBJ;
    }

    public String getColBK() {
        return colBK;
    }

    public void setColBK(final String colBK) {
        this.colBK = colBK;
    }

    public String getColBL() {
        return colBL;
    }

    public void setColBL(final String colBL) {
        this.colBL = colBL;
    }

    public String getColBM() {
        return colBM;
    }

    public void setColBM(final String colBM) {
        this.colBM = colBM;
    }

    public String getColBN() {
        return colBN;
    }

    public void setColBN(final String colBN) {
        this.colBN = colBN;
    }

    public String getColBO() {
        return colBO;
    }

    public void setColBO(final String colBO) {
        this.colBO = colBO;
    }

    public String getColBP() {
        return colBP;
    }

    public void setColBP(final String colBP) {
        this.colBP = colBP;
    }

    public String getColBQ() {
        return colBQ;
    }

    public void setColBQ(final String colBQ) {
        this.colBQ = colBQ;
    }

    public String getColBR() {
        return colBR;
    }

    public void setColBR(final String colBR) {
        this.colBR = colBR;
    }

    public String getColBS() {
        return colBS;
    }

    public void setColBS(final String colBS) {
        this.colBS = colBS;
    }

    public String getColBT() {
        return colBT;
    }

    public void setColBT(final String colBT) {
        this.colBT = colBT;
    }

    public String getColBU() {
        return colBU;
    }

    public void setColBU(final String colBU) {
        this.colBU = colBU;
    }

    public String getColBV() {
        return colBV;
    }

    public void setColBV(final String colBV) {
        this.colBV = colBV;
    }

    public String getColBW() {
        return colBW;
    }

    public void setColBW(final String colBW) {
        this.colBW = colBW;
    }

    public String getColBX() {
        return colBX;
    }

    public void setColBX(final String colBX) {
        this.colBX = colBX;
    }

    public String getColBY() {
        return colBY;
    }

    public void setColBY(final String colBY) {
        this.colBY = colBY;
    }

    public String getColBZ() {
        return colBZ;
    }

    public void setColBZ(final String colBZ) {
        this.colBZ = colBZ;
    }

    public String getColCA() {
        return colCA;
    }

    public void setColCA(final String colCA) {
        this.colCA = colCA;
    }

    public String getColCB() {
        return colCB;
    }

    public void setColCB(final String colCB) {
        this.colCB = colCB;
    }

    public String getColCC() {
        return colCC;
    }

    public void setColCC(final String colCC) {
        this.colCC = colCC;
    }

    public String getColCD() {
        return colCD;
    }

    public void setColCD(final String colCD) {
        this.colCD = colCD;
    }

    public String getColCE() {
        return colCE;
    }

    public void setColCE(final String colCE) {
        this.colCE = colCE;
    }

    public String getColCF() {
        return colCF;
    }

    public void setColCF(final String colCF) {
        this.colCF = colCF;
    }

    public String getColCG() {
        return colCG;
    }

    public void setColCG(final String colCG) {
        this.colCG = colCG;
    }

    public String getColCH() {
        return colCH;
    }

    public void setColCH(final String colCH) {
        this.colCH = colCH;
    }

    public String getColCI() {
        return colCI;
    }

    public void setColCI(final String colCI) {
        this.colCI = colCI;
    }

    public String getColCJ() {
        return colCJ;
    }

    public void setColCJ(final String colCJ) {
        this.colCJ = colCJ;
    }

    public String getColCK() {
        return colCK;
    }

    public void setColCK(final String colCK) {
        this.colCK = colCK;
    }

    public String getColCL() {
        return colCL;
    }

    public void setColCL(final String colCL) {
        this.colCL = colCL;
    }

    public String getColCM() {
        return colCM;
    }

    public void setColCM(final String colCM) {
        this.colCM = colCM;
    }

    public String getColCN() {
        return colCN;
    }

    public void setColCN(final String colCN) {
        this.colCN = colCN;
    }

    public String getColCO() {
        return colCO;
    }

    public void setColCO(final String colCO) {
        this.colCO = colCO;
    }

    public String getColCP() {
        return colCP;
    }

    public void setColCP(final String colCP) {
        this.colCP = colCP;
    }

    public String getColCQ() {
        return colCQ;
    }

    public void setColCQ(final String colCQ) {
        this.colCQ = colCQ;
    }

    public String getColCR() {
        return colCR;
    }

    public void setColCR(final String colCR) {
        this.colCR = colCR;
    }

    public String getColCS() {
        return colCS;
    }

    public void setColCS(final String colCS) {
        this.colCS = colCS;
    }

    public String getColCT() {
        return colCT;
    }

    public void setColCT(final String colCT) {
        this.colCT = colCT;
    }

    public String getColCU() {
        return colCU;
    }

    public void setColCU(final String colCU) {
        this.colCU = colCU;
    }

    public String getColCV() {
        return colCV;
    }

    public void setColCV(final String colCV) {
        this.colCV = colCV;
    }

    public String getColCW() {
        return colCW;
    }

    public void setColCW(final String colCW) {
        this.colCW = colCW;
    }

    public String getColCX() {
        return colCX;
    }

    public void setColCX(final String colCX) {
        this.colCX = colCX;
    }

    public String getColCY() {
        return colCY;
    }

    public void setColCY(final String colCY) {
        this.colCY = colCY;
    }

    public String getColCZ() {
        return colCZ;
    }

    public void setColCZ(final String colCZ) {
        this.colCZ = colCZ;
    }

    public String getColDA() {
        return colDA;
    }

    public void setColDA(final String colDA) {
        this.colDA = colDA;
    }

    public String getColDB() {
        return colDB;
    }

    public void setColDB(final String colDB) {
        this.colDB = colDB;
    }

    public String getColDC() {
        return colDC;
    }

    public void setColDC(final String colDC) {
        this.colDC = colDC;
    }

    public String getColDD() {
        return colDD;
    }

    public void setColDD(final String colDD) {
        this.colDD = colDD;
    }

    public String getColDE() {
        return colDE;
    }

    public void setColDE(final String colDE) {
        this.colDE = colDE;
    }

    public String getColDF() {
        return colDF;
    }

    public void setColDF(final String colDF) {
        this.colDF = colDF;
    }

    public String getColDG() {
        return colDG;
    }

    public void setColDG(final String colDG) {
        this.colDG = colDG;
    }

    public String getColDH() {
        return colDH;
    }

    public void setColDH(final String colDH) {
        this.colDH = colDH;
    }

    public String getColDI() {
        return colDI;
    }

    public void setColDI(final String colDI) {
        this.colDI = colDI;
    }

    public String getColDJ() {
        return colDJ;
    }

    public void setColDJ(final String colDJ) {
        this.colDJ = colDJ;
    }

    public String getColDK() {
        return colDK;
    }

    public void setColDK(final String colDK) {
        this.colDK = colDK;
    }

    public String getColDL() {
        return colDL;
    }

    public void setColDL(final String colDL) {
        this.colDL = colDL;
    }

    public String getColDM() {
        return colDM;
    }

    public void setColDM(final String colDM) {
        this.colDM = colDM;
    }

    public String getColDN() {
        return colDN;
    }

    public void setColDN(final String colDN) {
        this.colDN = colDN;
    }

    public String getColDO() {
        return colDO;
    }

    public void setColDO(final String colDO) {
        this.colDO = colDO;
    }

    public String getColDP() {
        return colDP;
    }

    public void setColDP(final String colDP) {
        this.colDP = colDP;
    }

    public String getColDQ() {
        return colDQ;
    }

    public void setColDQ(final String colDQ) {
        this.colDQ = colDQ;
    }

    public String getColDR() {
        return colDR;
    }

    public void setColDR(final String colDR) {
        this.colDR = colDR;
    }

    public String getColDS() {
        return colDS;
    }

    public void setColDS(final String colDS) {
        this.colDS = colDS;
    }

    public String getColDT() {
        return colDT;
    }

    public void setColDT(final String colDT) {
        this.colDT = colDT;
    }

    public String getColDU() {
        return colDU;
    }

    public void setColDU(final String colDU) {
        this.colDU = colDU;
    }

    public String getColDV() {
        return colDV;
    }

    public void setColDV(final String colDV) {
        this.colDV = colDV;
    }

    public String getColDW() {
        return colDW;
    }

    public void setColDW(final String colDW) {
        this.colDW = colDW;
    }

    public String getColDX() {
        return colDX;
    }

    public void setColDX(final String colDX) {
        this.colDX = colDX;
    }

    public String getColDY() {
        return colDY;
    }

    public void setColDY(final String colDY) {
        this.colDY = colDY;
    }

    public String getColDZ() {
        return colDZ;
    }

    public void setColDZ(final String colDZ) {
        this.colDZ = colDZ;
    }

    public String getColEA() {
        return colEA;
    }

    public void setColEA(final String colEA) {
        this.colEA = colEA;
    }

    public String getColEB() {
        return colEB;
    }

    public void setColEB(final String colEB) {
        this.colEB = colEB;
    }

    public String getColEC() {
        return colEC;
    }

    public void setColEC(final String colEC) {
        this.colEC = colEC;
    }

    public String getColED() {
        return colED;
    }

    public void setColED(final String colED) {
        this.colED = colED;
    }

    public String getColEE() {
        return colEE;
    }

    public void setColEE(final String colEE) {
        this.colEE = colEE;
    }

    public String getColEF() {
        return colEF;
    }

    public void setColEF(final String colEF) {
        this.colEF = colEF;
    }

    public String getColEG() {
        return colEG;
    }

    public void setColEG(final String colEG) {
        this.colEG = colEG;
    }

    public String getColEH() {
        return colEH;
    }

    public void setColEH(final String colEH) {
        this.colEH = colEH;
    }

    public String getColEI() {
        return colEI;
    }

    public void setColEI(final String colEI) {
        this.colEI = colEI;
    }

    public String getColEJ() {
        return colEJ;
    }

    public void setColEJ(final String colEJ) {
        this.colEJ = colEJ;
    }

    public String getColEK() {
        return colEK;
    }

    public void setColEK(final String colEK) {
        this.colEK = colEK;
    }

    public String getColEL() {
        return colEL;
    }

    public void setColEL(final String colEL) {
        this.colEL = colEL;
    }

    public String getColEM() {
        return colEM;
    }

    public void setColEM(final String colEM) {
        this.colEM = colEM;
    }

    public String getColEN() {
        return colEN;
    }

    public void setColEN(final String colEN) {
        this.colEN = colEN;
    }

    public String getColEO() {
        return colEO;
    }

    public void setColEO(final String colEO) {
        this.colEO = colEO;
    }

    public String getColEP() {
        return colEP;
    }

    public void setColEP(final String colEP) {
        this.colEP = colEP;
    }

    public String getColEQ() {
        return colEQ;
    }

    public void setColEQ(final String colEQ) {
        this.colEQ = colEQ;
    }

    public String getColER() {
        return colER;
    }

    public void setColER(final String colER) {
        this.colER = colER;
    }

    public String getColES() {
        return colES;
    }

    public void setColES(final String colES) {
        this.colES = colES;
    }

    public String getColET() {
        return colET;
    }

    public void setColET(final String colET) {
        this.colET = colET;
    }

    public String getColEU() {
        return colEU;
    }

    public void setColEU(final String colEU) {
        this.colEU = colEU;
    }

    public String getColEV() {
        return colEV;
    }

    public void setColEV(final String colEV) {
        this.colEV = colEV;
    }

    public String getColEW() {
        return colEW;
    }

    public void setColEW(final String colEW) {
        this.colEW = colEW;
    }

    public String getColEX() {
        return colEX;
    }

    public void setColEX(final String colEX) {
        this.colEX = colEX;
    }

    public String getColEY() {
        return colEY;
    }

    public void setColEY(final String colEY) {
        this.colEY = colEY;
    }

    public String getColEZ() {
        return colEZ;
    }

    public void setColEZ(final String colEZ) {
        this.colEZ = colEZ;
    }

    public String getColFA() {
        return colFA;
    }

    public void setColFA(final String colFA) {
        this.colFA = colFA;
    }

    public String getColFB() {
        return colFB;
    }

    public void setColFB(final String colFB) {
        this.colFB = colFB;
    }

    public String getColFC() {
        return colFC;
    }

    public void setColFC(final String colFC) {
        this.colFC = colFC;
    }

    public String getColFD() {
        return colFD;
    }

    public void setColFD(final String colFD) {
        this.colFD = colFD;
    }

    public String getColFE() {
        return colFE;
    }

    public void setColFE(final String colFE) {
        this.colFE = colFE;
    }

    public String getColFF() {
        return colFF;
    }

    public void setColFF(final String colFF) {
        this.colFF = colFF;
    }

    public String getColFG() {
        return colFG;
    }

    public void setColFG(final String colFG) {
        this.colFG = colFG;
    }

    public String getColFH() {
        return colFH;
    }

    public void setColFH(final String colFH) {
        this.colFH = colFH;
    }

    public String getColFI() {
        return colFI;
    }

    public void setColFI(final String colFI) {
        this.colFI = colFI;
    }

    public String getColFJ() {
        return colFJ;
    }

    public void setColFJ(final String colFJ) {
        this.colFJ = colFJ;
    }

    public String getColFK() {
        return colFK;
    }

    public void setColFK(final String colFK) {
        this.colFK = colFK;
    }

    public String getColFL() {
        return colFL;
    }

    public void setColFL(final String colFL) {
        this.colFL = colFL;
    }

    public String getColFM() {
        return colFM;
    }

    public void setColFM(final String colFM) {
        this.colFM = colFM;
    }

    public String getColFN() {
        return colFN;
    }

    public void setColFN(final String colFN) {
        this.colFN = colFN;
    }

    public String getColFO() {
        return colFO;
    }

    public void setColFO(final String colFO) {
        this.colFO = colFO;
    }

    public String getColFP() {
        return colFP;
    }

    public void setColFP(final String colFP) {
        this.colFP = colFP;
    }

    public String getColFQ() {
        return colFQ;
    }

    public void setColFQ(final String colFQ) {
        this.colFQ = colFQ;
    }

    public String getColFR() {
        return colFR;
    }

    public void setColFR(final String colFR) {
        this.colFR = colFR;
    }

    public String getColFS() {
        return colFS;
    }

    public void setColFS(final String colFS) {
        this.colFS = colFS;
    }

    public String getColFT() {
        return colFT;
    }

    public void setColFT(final String colFT) {
        this.colFT = colFT;
    }

    public String getColFU() {
        return colFU;
    }

    public void setColFU(final String colFU) {
        this.colFU = colFU;
    }

    public String getColFV() {
        return colFV;
    }

    public void setColFV(final String colFV) {
        this.colFV = colFV;
    }

    public String getColFW() {
        return colFW;
    }

    public void setColFW(final String colFW) {
        this.colFW = colFW;
    }

    public String getColFX() {
        return colFX;
    }

    public void setColFX(final String colFX) {
        this.colFX = colFX;
    }

    public String getColFY() {
        return colFY;
    }

    public void setColFY(final String colFY) {
        this.colFY = colFY;
    }

    public String getColFZ() {
        return colFZ;
    }

    public void setColFZ(final String colFZ) {
        this.colFZ = colFZ;
    }

    public String getColGA() {
        return colGA;
    }

    public void setColGA(final String colGA) {
        this.colGA = colGA;
    }

    public String getColGB() {
        return colGB;
    }

    public void setColGB(final String colGB) {
        this.colGB = colGB;
    }

    public String getColGC() {
        return colGC;
    }

    public void setColGC(final String colGC) {
        this.colGC = colGC;
    }

    public String getColGD() {
        return colGD;
    }

    public void setColGD(final String colGD) {
        this.colGD = colGD;
    }

    public String getColGE() {
        return colGE;
    }

    public void setColGE(final String colGE) {
        this.colGE = colGE;
    }

    public String getColGF() {
        return colGF;
    }

    public void setColGF(final String colGF) {
        this.colGF = colGF;
    }

    public String getColGG() {
        return colGG;
    }

    public void setColGG(final String colGG) {
        this.colGG = colGG;
    }

    public String getColGH() {
        return colGH;
    }

    public void setColGH(final String colGH) {
        this.colGH = colGH;
    }

    public String getColGI() {
        return colGI;
    }

    public void setColGI(final String colGI) {
        this.colGI = colGI;
    }

    public String getColGJ() {
        return colGJ;
    }

    public void setColGJ(final String colGJ) {
        this.colGJ = colGJ;
    }

    public String getColGK() {
        return colGK;
    }

    public void setColGK(final String colGK) {
        this.colGK = colGK;
    }

    public String getColGL() {
        return colGL;
    }

    public void setColGL(final String colGL) {
        this.colGL = colGL;
    }

    public String getColGM() {
        return colGM;
    }

    public void setColGM(final String colGM) {
        this.colGM = colGM;
    }

    public String getColGN() {
        return colGN;
    }

    public void setColGN(final String colGN) {
        this.colGN = colGN;
    }

    public String getColGO() {
        return colGO;
    }

    public void setColGO(final String colGO) {
        this.colGO = colGO;
    }

    public String getColGP() {
        return colGP;
    }

    public void setColGP(final String colGP) {
        this.colGP = colGP;
    }

    public String getColGQ() {
        return colGQ;
    }

    public void setColGQ(final String colGQ) {
        this.colGQ = colGQ;
    }

    public String getColGR() {
        return colGR;
    }

    public void setColGR(final String colGR) {
        this.colGR = colGR;
    }

    public String getColGS() {
        return colGS;
    }

    public void setColGS(final String colGS) {
        this.colGS = colGS;
    }

    public String getColGT() {
        return colGT;
    }

    public void setColGT(final String colGT) {
        this.colGT = colGT;
    }

    public String getColGU() {
        return colGU;
    }

    public void setColGU(final String colGU) {
        this.colGU = colGU;
    }

    public String getColGV() {
        return colGV;
    }

    public void setColGV(final String colGV) {
        this.colGV = colGV;
    }

    public String getColGW() {
        return colGW;
    }

    public void setColGW(final String colGW) {
        this.colGW = colGW;
    }

    public String getColGX() {
        return colGX;
    }

    public void setColGX(final String colGX) {
        this.colGX = colGX;
    }

    public String getColGY() {
        return colGY;
    }

    public void setColGY(final String colGY) {
        this.colGY = colGY;
    }

    public String getColGZ() {
        return colGZ;
    }

    public void setColGZ(final String colGZ) {
        this.colGZ = colGZ;
    }

    public String getColHA() {
        return colHA;
    }

    public void setColHA(final String colHA) {
        this.colHA = colHA;
    }

    public String getColHB() {
        return colHB;
    }

    public void setColHB(final String colHB) {
        this.colHB = colHB;
    }

    public String getColHC() {
        return colHC;
    }

    public void setColHC(final String colHC) {
        this.colHC = colHC;
    }

    public String getColHD() {
        return colHD;
    }

    public void setColHD(final String colHD) {
        this.colHD = colHD;
    }

    public String getColHE() {
        return colHE;
    }

    public void setColHE(final String colHE) {
        this.colHE = colHE;
    }

    public String getColHF() {
        return colHF;
    }

    public void setColHF(final String colHF) {
        this.colHF = colHF;
    }

    public String getColHG() {
        return colHG;
    }

    public void setColHG(final String colHG) {
        this.colHG = colHG;
    }

    public String getColHH() {
        return colHH;
    }

    public void setColHH(final String colHH) {
        this.colHH = colHH;
    }

    public String getColHI() {
        return colHI;
    }

    public void setColHI(final String colHI) {
        this.colHI = colHI;
    }

    public String getColHJ() {
        return colHJ;
    }

    public void setColHJ(final String colHJ) {
        this.colHJ = colHJ;
    }

    public String getColHK() {
        return colHK;
    }

    public void setColHK(final String colHK) {
        this.colHK = colHK;
    }

    public String getColHL() {
        return colHL;
    }

    public void setColHL(final String colHL) {
        this.colHL = colHL;
    }

    public String getColHM() {
        return colHM;
    }

    public void setColHM(final String colHM) {
        this.colHM = colHM;
    }

    public String getColHN() {
        return colHN;
    }

    public void setColHN(final String colHN) {
        this.colHN = colHN;
    }

    public String getColHO() {
        return colHO;
    }

    public void setColHO(final String colHO) {
        this.colHO = colHO;
    }

    public String getColHP() {
        return colHP;
    }

    public void setColHP(final String colHP) {
        this.colHP = colHP;
    }

    public String getColHQ() {
        return colHQ;
    }

    public void setColHQ(final String colHQ) {
        this.colHQ = colHQ;
    }

    public String getColHR() {
        return colHR;
    }

    public void setColHR(final String colHR) {
        this.colHR = colHR;
    }

    public String getColHS() {
        return colHS;
    }

    public void setColHS(final String colHS) {
        this.colHS = colHS;
    }

    public String getColHT() {
        return colHT;
    }

    public void setColHT(final String colHT) {
        this.colHT = colHT;
    }

    public String getColHU() {
        return colHU;
    }

    public void setColHU(final String colHU) {
        this.colHU = colHU;
    }

    public String getColHV() {
        return colHV;
    }

    public void setColHV(final String colHV) {
        this.colHV = colHV;
    }

    public String getColHW() {
        return colHW;
    }

    public void setColHW(final String colHW) {
        this.colHW = colHW;
    }

    public String getColHX() {
        return colHX;
    }

    public void setColHX(final String colHX) {
        this.colHX = colHX;
    }

    public String getColHY() {
        return colHY;
    }

    public void setColHY(final String colHY) {
        this.colHY = colHY;
    }

    public String getColHZ() {
        return colHZ;
    }

    public void setColHZ(final String colHZ) {
        this.colHZ = colHZ;
    }

}
