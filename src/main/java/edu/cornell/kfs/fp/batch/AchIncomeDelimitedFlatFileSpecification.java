package edu.cornell.kfs.fp.batch;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.batch.DelimitedFlatFileSpecification;
import org.kuali.kfs.sys.batch.FlatFileObjectSpecification;

/**
 Portions Modified 04/2016 and Copyright Cornell University

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
public class AchIncomeDelimitedFlatFileSpecification extends DelimitedFlatFileSpecification {

    @Override
    public void parseLineIntoObject(FlatFileObjectSpecification parseSpecification, String lineToParse, Object parseIntoObject, int lineNumber) {
        super.parseLineIntoObject(parseSpecification, removeEndOfLineCharacter(lineToParse), parseIntoObject, lineNumber);
    }

    protected String removeEndOfLineCharacter(String lineToParse) {
        if (StringUtils.isBlank(lineToParse))
            return lineToParse;
        else {
            return lineToParse.substring(0, lineToParse.length() - 1);//
        }
    }

}
