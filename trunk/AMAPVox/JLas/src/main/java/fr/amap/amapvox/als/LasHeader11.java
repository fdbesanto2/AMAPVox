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

/**
 *
 * @author calcul
 */

public class LasHeader11 extends LasHeader {

    private int globalEncoding;
    private int fileSourceId;

    public int getGlobalEncoding() {
        return globalEncoding;
    }

    public void setGlobalEncoding(int globalEncoding) {
        this.globalEncoding = globalEncoding;
    }

    /**
     * 
     * @return This field is a value between 1 and 65,535, inclusive. A value of zero (0) is interpreted to 
    mean that an ID has not been assigned. In this case, processing software is free to assign any valid number.<br/>
    Note that this scheme allows a LIDAR project to contain up to 65,535 unique 
    sources. A source can be considered an original flight line or it can be the result of merge and/or 
    extract operations. 
     */
    @Override
    public int getFileSourceId() {
        return fileSourceId;
    }

    /**
     * 
     * @return  Day, expressed as an unsigned short, on which this file was created. 
    Day is computed as the Greenwich Mean Time (GMT) day. January 1 is considered day 1. 
     */
    @Override
    public int getFileCreationDayOfYear() {
        return super.getFileCreationDayOfYear();
    }

    /**
     * 
     * @param fileSourceId
     */
    public void setFileSourceId(int fileSourceId) {
        this.fileSourceId = fileSourceId;
    }


}
