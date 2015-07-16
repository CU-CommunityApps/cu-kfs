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
package edu.cornell.kfs.fp.businessobject;


public class ProcurementCardGenericDetail extends ProcurementCardGenericBase
{

	private static final long serialVersionUID = 1L;
    private String documentNumber;
    private Integer financialDocumentTransactionLineNumber;

    public ProcurementCardGenericDetail()
    {
        super();
    }

    public String getDocumentNumber()
    {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber)
    {
        this.documentNumber = documentNumber;
    }

    public Integer getFinancialDocumentTransactionLineNumber()
    {
        return financialDocumentTransactionLineNumber;
    }

    public void setFinancialDocumentTransactionLineNumber(Integer financialDocumentTransactionLineNumber)
    {
        this.financialDocumentTransactionLineNumber = financialDocumentTransactionLineNumber;
    }

}
