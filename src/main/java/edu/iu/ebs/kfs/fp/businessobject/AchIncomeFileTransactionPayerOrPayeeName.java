package edu.iu.ebs.kfs.fp.businessobject;

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
public class AchIncomeFileTransactionPayerOrPayeeName extends
		AbstractAchIncomeFileDataElement {
    
	private String idQualifier;
	private String idCode;

	public String getIdQualifier() {
		return idQualifier;
	}

	public void setIdQualifier(String idQualifier) {
		this.idQualifier = idQualifier;
	}

	public String getIdCode() {
		return idCode;
	}

	public void setIdCode(String idCode) {
		this.idCode = idCode;
	}
}
