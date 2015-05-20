package edu.cornell.kfs.module.receiptProcessing.businessobject;




public class ReceiptProcessing  {

    /**
     * @author cab379
     */
    
    private String cardHolder;
    private String amount;
    private String purchasedate;
    private String filePath;
    private String filename;
    private String cardHolderNetID;
    private String sourceUniqueID;
    private String eDocNumber;
        

    public String getCardHolder() {
        return cardHolder;
    }

    public void setCardHolder(String cardHolder) {
        this.cardHolder = cardHolder;
    }   

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getPurchasedate() {
        return purchasedate;
    }

    public void setPurchasedate(String purchasedate) {
        this.purchasedate = purchasedate;
    }

    public String returnBoLine() {
        return this.getCardHolder() + "," + this.getAmount() + "," + this.getPurchasedate() + "," + this.getFilePath() + "," + this.getFilename() + "," + this.getCardHolderNetID() + "," + this.getSourceUniqueID() + ",";
    }
    
    public String noMatch() {
        String line = returnBoLine() + this.geteDocNumber() + "," + "0\n";
        return line;        
    }
    
    public String match(String eDocNumber) {
        String line = returnBoLine() + eDocNumber;
        return line;        
    }
    
    public String multipleMatch() {
        String line = returnBoLine() + this.geteDocNumber() + "," + "1\n";
        return line;        
    }
    
    public String badData() {
        String line = returnBoLine() + this.geteDocNumber() + "," + "2\n";
        return line;        
    }   
    
    public String attachOnlyError(){
        String line = returnBoLine() + this.geteDocNumber() + "," + "9\n";
        return line;  
    }

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getCardHolderNetID() {
		return cardHolderNetID;
	}

	public void setCardHolderNetID(String cardHolderNetID) {
		this.cardHolderNetID = cardHolderNetID;
	}

	public String getSourceUniqueID() {
		return sourceUniqueID;
	}

	public void setSourceUniqueID(String sourceUniqueID) {
		this.sourceUniqueID = sourceUniqueID;
	}

	public String geteDocNumber() {
		return eDocNumber;
	}

	public void seteDocNumber(String eDocNumber) {
		this.eDocNumber = eDocNumber;
	}

}
