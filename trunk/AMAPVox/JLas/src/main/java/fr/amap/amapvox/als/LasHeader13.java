
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
 * Represents the structure of a las file header version 1.3
 * @author calcul
 */

public class LasHeader13 extends LasHeader12 {
    
    private BigInteger startOfWaveformDataPacketRecord;

    public BigInteger getStartOfWaveformDataPacketRecord() {
        return startOfWaveformDataPacketRecord;
    }

    public void setStartOfWaveformDataPacketRecord(BigInteger startOfWaveformDataPacketRecord) {
        this.startOfWaveformDataPacketRecord = startOfWaveformDataPacketRecord;
    }
    
}