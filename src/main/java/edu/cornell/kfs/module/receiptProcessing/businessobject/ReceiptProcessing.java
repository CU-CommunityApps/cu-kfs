package edu.cornell.kfs.module.receiptProcessing.businessobject;




public class ReceiptProcessing  {

    /**
     * @author cab379
     */
    
    private String cardHolder;
    private String amount;
    private String purchasedate;
    private String SharePointPath;
    private String filename;
        

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
        return this.getCardHolder() + "," + this.getAmount() + "," + this.getPurchasedate() + "," + this.getSharePointPath() + "," + this.getFilename() + ",";
    }
    
    public String noMatch() {
        String line = returnBoLine() + "0\n";
        return line;        
    }
    
    public String match() {
        String line = returnBoLine();
        return line;        
    }
    
    public String multipleMatch() {
        String line = returnBoLine() + "1\n";
        return line;        
    }
    
    public String badData() {
        String line = returnBoLine() + "2\n";
        return line;        
    }

    public String getSharePointPath() {
        return SharePointPath;
    }

    public void setSharePointPath(String sharePointPath) {
        SharePointPath = sharePointPath;
    }

}
