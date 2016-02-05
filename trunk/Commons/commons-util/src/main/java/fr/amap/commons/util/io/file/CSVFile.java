/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.io.file;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * CSVFile class defines how a CSV file should be read (header, separator, etc...)
 * @author Julien Heurtebize
 */
public class CSVFile extends File{
    
    private boolean hasHeader;
    private long headerIndex;
    private String columnSeparator;
    private long nbOfLinesToSkip;
    private long nbOfLinesToRead;
    private Map<String, Integer> columnAssignment;
            
    public CSVFile(String pathname) {
        
        super(pathname);
        
        columnSeparator = ",";
        hasHeader = true;
        headerIndex = 0;
        nbOfLinesToSkip = 1;
        nbOfLinesToRead = Long.MAX_VALUE;
        columnAssignment = new HashMap<>();
    }

    public boolean isHasHeader() {
        return hasHeader;
    }

    public void setHasHeader(boolean hasHeader) {
        this.hasHeader = hasHeader;
    }

    public String getColumnSeparator() {
        return columnSeparator;
    }

    public void setColumnSeparator(String columnSeparator) {
        this.columnSeparator = columnSeparator;
    }

    public long getHeaderIndex() {
        return headerIndex;
    }

    public void setHeaderIndex(long headerIndex) {
        this.headerIndex = headerIndex;
    }

    public long getNbOfLinesToSkip() {
        return nbOfLinesToSkip;
    }

    public void setNbOfLinesToSkip(long nbOfLinesToSkip) {
        this.nbOfLinesToSkip = nbOfLinesToSkip;
    }

    public long getNbOfLinesToRead() {
        return nbOfLinesToRead;
    }

    public void setNbOfLinesToRead(long nbOfLinesToRead) {
        this.nbOfLinesToRead = nbOfLinesToRead;
    }

    public Map<String, Integer> getColumnAssignment() {
        return columnAssignment;
    }

    public void setColumnAssignment(Map<String, Integer> columnAssignment) {
        this.columnAssignment = columnAssignment;
    }
    
}
