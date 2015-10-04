/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.als.las;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VariableLengthRecord {
    
    private int reserved;
    private String userID;
    private int recordID;
    private int recordLengthAfterHeader;
    private String description;

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    public void setUserID(char[] userID) {
        this.userID = String.valueOf(userID);
    }

    public void setRecordID(int recordID) {
        this.recordID = recordID;
    }

    public void setRecordLengthAfterHeader(int recordLengthAfterHeader) {
        this.recordLengthAfterHeader = recordLengthAfterHeader;
    }

    public void setDescription(char[] description) {
        this.description = String.valueOf(description);
    }
    
    
}
