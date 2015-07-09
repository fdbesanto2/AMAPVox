/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.amap.amapvox.als;

import java.math.BigInteger;

/**
 *
 * @author calcul
 */


public class LasHeader14 extends LasHeader13 {
    
    private BigInteger StartOfFirstExtendedVariableLengthRecord;
    private long NumberOfExtendedVariableLengthRecords;
    private BigInteger extendedNumberOfPointRecords;
    private BigInteger[] extendedNumberOfPointsByReturn;

    public long getNumberOfExtendedVariableLengthRecords() {
        return NumberOfExtendedVariableLengthRecords;
    }

    public void setNumberOfExtendedVariableLengthRecords(long NumberOfExtendedVariableLengthRecords) {
        this.NumberOfExtendedVariableLengthRecords = NumberOfExtendedVariableLengthRecords;
    }

    public BigInteger getStartOfFirstExtendedVariableLengthRecord() {
        return StartOfFirstExtendedVariableLengthRecord;
    }

    public void setStartOfFirstExtendedVariableLengthRecord(BigInteger StartOfFirstExtendedVariableLengthRecord) {
        this.StartOfFirstExtendedVariableLengthRecord = StartOfFirstExtendedVariableLengthRecord;
    }

    public BigInteger getExtendedNumberOfPointRecords() {
        return extendedNumberOfPointRecords;
    }

    public void setExtendedNumberOfPointRecords(BigInteger extendedNumberOfPointRecords) {
        this.extendedNumberOfPointRecords = extendedNumberOfPointRecords;
    }

    public BigInteger[] getExtendedNumberOfPointsByReturn() {
        return extendedNumberOfPointsByReturn;
    }

    public void setExtendedNumberOfPointsByReturn(BigInteger[] extendedNumberOfPointsByReturn) {
        this.extendedNumberOfPointsByReturn = extendedNumberOfPointsByReturn;
    }
    
}