package edu.iu.ebs.kfs.fp.businessobject;

import java.sql.Date;
import java.util.LinkedHashMap;

import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;

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
public class AchIncomeNote extends PersistableBusinessObjectBase {
	private Integer sequenceNumber;
	private Integer noteLineNumber;
	private String noteText;
	
	
	protected LinkedHashMap toStringMapper_RICE20_REFACTORME() {
		LinkedHashMap toString = new LinkedHashMap();
		toString.put("sequenceNumber", sequenceNumber);
		toString.put("noteLineNumber", noteLineNumber);
		return toString;
	}

	public Integer getSequenceNumber() {
		return sequenceNumber;
	}

	public void setSequenceNumber(Integer sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}

	public Integer getNoteLineNumber() {
		return noteLineNumber;
	}

	public void setNoteLineNumber(Integer noteLineNumber) {
		this.noteLineNumber = noteLineNumber;
	}

	public String getNoteText() {
		return noteText;
	}

	public void setNoteText(String noteText) {
		this.noteText = noteText;
	}
}
