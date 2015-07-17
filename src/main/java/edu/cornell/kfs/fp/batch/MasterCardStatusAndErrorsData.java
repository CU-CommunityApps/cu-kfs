/*
 * Copyright 2012 The Kuali Foundation.
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.cornell.kfs.fp.batch;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.kuali.kfs.sys.Message;
import org.kuali.rice.core.api.util.type.KualiDecimal;

import edu.cornell.kfs.fp.CuFPPropertyConstants;
import edu.cornell.kfs.fp.businessobject.MasterCardTransactionDetail;

/**
 * This object holds statistical and error data pertaining to the MasterCard
 * batch input process. This data is used by the report writer service to
 * generate the user report.
 *
 * @author Dave Raines
 * @version $Revision$
 */
public class MasterCardStatusAndErrorsData
{
  private int cardHolderInputRecords;
  private int proCardTransactionInputRecords;

  private int cardHoldersProcessed;
  private int proCardTransactionsProcessed;

  private int cardHoldersInError;
  private int proCardTransactionsInError;

  List<Message> cardHolderErrors;
  List<Message> proCardTransactionErrors;

  private KualiDecimal totalTransactionsAmount;
  private KualiDecimal totalErrorAmount;
  private NumberFormat currencyFormatter;

  public MasterCardStatusAndErrorsData()
  {
    currencyFormatter = NumberFormat.getCurrencyInstance();
    cardHolderErrors = new ArrayList<Message>();
    proCardTransactionErrors = new ArrayList<Message>();
    totalTransactionsAmount = KualiDecimal.ZERO;
    totalErrorAmount        = KualiDecimal.ZERO;
  }

  /**
   * Returns number of cardHolderInputRecords
   */
  public int getCardHolderInputRecords()
  {
    return cardHolderInputRecords;
  }

  /**
   * Increments cardHolderInputRecords
   */
  public void incrementCardHolderInputRecords()
  {
    this.cardHolderInputRecords++;
  }

  /**
   * Returns number of proCardTransactionInputRecords
   */
  public int getProCardTransactionInputRecords()
  {
    return proCardTransactionInputRecords;
  }

  /**
   * Increments proCardTransactionInputRecords
   */
  public void incrementProCardTransactionInputRecords()
  {
    this.proCardTransactionInputRecords++;
  }

  /**
   * Returns number of cardHoldersProcessed
   */
  public int getCardHoldersProcessed()
  {
    return cardHoldersProcessed;
  }

  /**
   * Increments cardHoldersProcessed
   */
  public void incrementCardHoldersProcessed()
  {
    this.cardHoldersProcessed++;
  }

  /**
   * Returns number of proCardTransactionsProcessed
   */
  public int getProCardTransactionsProcessed()
  {
    return proCardTransactionsProcessed;
  }

  /**
   * Increments proCardTransactionsProcessed
   */
  public void incrementProCardTransactionsProcessed()
  {
    this.proCardTransactionsProcessed++;
  }

  /**
   * Returns number of cardHoldersInError
   */
  public int getCardHoldersInError()
  {
    return cardHoldersInError;
  }

  /**
   * Increments cardHoldersInError
   */
  public void incrementCardHoldersInError()
  {
    this.cardHoldersInError++;
  }

  /**
   * Returns number of proCardTransactionsInError
   */
  public int getProCardTransactionsInError()
  {
    return proCardTransactionsInError;
  }

  /**
   * Increments proCardTransactionsInError
   */
  public void incrementProCardTransactionsInError()
  {
    this.proCardTransactionsInError++;
  }

  /**
   * Returns list of card holder error messages.
   */
  public List<Message> getCardHolderErrors()
  {
    return cardHolderErrors;
  }

  /**
   * Adds a card holder error message.
   */
  public void addCardHolderError(Message cardHolderError)
  {
    this.cardHolderErrors.add(cardHolderError);
  }

  /**
   * Adds a card holder error message.
   */
  public void addCardHolderError(String cardNumber, String name, String accountNumber)
  {
    Message cardHolderError =
      new Message("Cardholder record %s with card holder name %s has invalid account number %s",
                  Message.TYPE_WARNING,
                  cardNumber, name, accountNumber);
    this.cardHolderErrors.add(cardHolderError);
  }

  /**
   * returns list of procurement card transaction error messages.
   */
  public List<Message> getProCardTransactionErrors()
  {
    return proCardTransactionErrors;
  }

  /**
   * Adds a procurement card transaction error message.
   */
  public void addProCardTransactionError(Message proCardTransactionError)
  {
    this.proCardTransactionErrors.add(proCardTransactionError);
  }

  /**
   * Adds a procurement card transaction error message.
   */
  public void addProCardTransactionError(String cardNumber, KualiDecimal amount)
  {
    Message proCardTransactionError =
      new Message("ProcurementCard transaction %s with amount %s does not exist in the card holder detail table.",
                  Message.TYPE_WARNING,
                  cardNumber,
                  currencyFormatter.format(amount.doubleValue()));
    this.proCardTransactionErrors.add(proCardTransactionError);
    this.totalErrorAmount = totalErrorAmount.add(amount);
  }

  /**
   * returns total amount as formatted string
   */
  public String getTotalTransactionsAmount()
  {
    return currencyFormatter.format(totalTransactionsAmount.doubleValue());
  }

  /**
   * returns total amount as formatted string
   */
  public String getTotalErrorAmount()
  {
    return currencyFormatter.format(totalErrorAmount.doubleValue());
  }

  /**
   * Adds or subtracts transaction amount from the total amount based on
   * debit/credit indicator.
   *
   * @param transDetail
   */
  public void incrementTotalAmount(MasterCardTransactionDetail transDetail)
  {
    String debitCreditInd = transDetail.getDebitCreditInd();
    if (CuFPPropertyConstants.DEBIT_CODE.equals(debitCreditInd))
    {
      totalTransactionsAmount = totalTransactionsAmount.add(transDetail.getTransactionAmount());
    }
    else if (CuFPPropertyConstants.CREDIT_CODE.equals(debitCreditInd))
    {
      totalTransactionsAmount = totalTransactionsAmount.subtract(transDetail.getTransactionAmount());
    }
  }

}
