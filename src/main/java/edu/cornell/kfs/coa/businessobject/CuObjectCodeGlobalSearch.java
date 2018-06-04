package edu.cornell.kfs.coa.businessobject;

import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.coa.businessobject.ObjectCode;
import org.kuali.kfs.gl.businessobject.Balance;
import org.kuali.rice.core.api.util.type.KualiDecimal;

public class CuObjectCodeGlobalSearch extends ObjectCode {
    private static final long serialVersionUID = 7461668856007571783L;
    
    protected List<Balance> balances;
    
    public CuObjectCodeGlobalSearch() {
        super();
        balances = new ArrayList<Balance>();
    }

    public List<Balance> getBalances() {
        return balances;
    }

    public void setBalances(List<Balance> balances) {
        this.balances = balances;
    }

    public KualiDecimal getCalculatedBalance() {
        KualiDecimal calculatedBalance = KualiDecimal.ZERO;
        for (Balance balance : balances) {
            calculatedBalance = calculatedBalance.add(balance.getAccountLineAnnualBalanceAmount());
        }
        return calculatedBalance;
    }

}
