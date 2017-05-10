/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.shot.filters;

import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.shot.converters.RxpShotIteratorConverter;
import fr.amap.lidar.amapvox.shot.converters.TxtShotIteratorConverter;
import fr.amap.lidar.format.shot.ShotReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author claudia
 */
public class ShotFilter2D_RXP implements Iterable<Shot> {

    private final static boolean DEBUG = true;

    private final Iterator<Shot> shotsIterator;

    private final static int DEFAULT_FILTERING_WINDOW_LENGTH = 3;
    private final static double DEFAULT_MIN_RANGE = 1.5;

    private final int filteringWindowLength;
    private final int columnShotsListWidth;  // nombre de colonnes de la fenêtre glissante (tableau de tirs pour avoir le voisinage en 2D)
    private final double minRange; // meters
    
    private static int nbOfInitialShots = 0;

    //private final static double DIFF_ANGLE = Math.toDegrees(Math.PI / 8);
    private final static double DIFF_ANGLE = 4.0;
    private List<List<Shot>> shotsColumnsList = new ArrayList();

    private Shot prevShot = null;
    private Shot firstNextColumnShot = null;


    /**
     *
     * @param shotIterator
     */
    public ShotFilter2D_RXP(Iterator<Shot> shotIterator) {

        this(shotIterator, DEFAULT_FILTERING_WINDOW_LENGTH, DEFAULT_MIN_RANGE);
    }

    /**
     *
     * @param shotIterator
     * @param filteringWindowLength half-size of the neighborhood filtering
     * squared window
     * @param minRange minimum distance (m) of reliable echoes detection
     */
    public ShotFilter2D_RXP(Iterator<Shot> shotIterator, int filteringWindowLength, double minRange) {

        this.filteringWindowLength = filteringWindowLength;
        this.minRange = minRange;

        this.columnShotsListWidth = 4 * this.filteringWindowLength;

        this.shotsIterator = shotIterator;
    }

    @Override
    public Iterator<Shot> iterator() {
        
        initShotsColumnsList();

        return new Iterator<Shot>() {

            private boolean hasNextCalled = false;
            private Shot shot;
            private boolean lastShotReached = false;
            
            private int currentCol;
            private int currentRow;
            private int currentShotNb = 0;  
            
            private int nbOfSuppressedCols = 0; // number of suppressed columns from shotsArray

            private void updateShotColumnsList() {

                boolean chOfCol = false;
                List<Shot> shotsColumn = new ArrayList();
                int nbOfColsInShotsColumnsList = 0;
                
                if(firstNextColumnShot != null && !this.lastShotReached){
                    shotsColumn.add(firstNextColumnShot);
                }
                    
                while ((chOfCol == false) && (shotsIterator.hasNext())) { // build columnShotsList
                    
                    Shot currentShot = shotsIterator.next();
                    nbOfInitialShots ++;
                    
                    chOfCol = changeOfCol(prevShot, currentShot);
                    prevShot = copyShot(currentShot);

                    if (!chOfCol) {
                        shotsColumn.add(currentShot);
                    }
                    else{
                        firstNextColumnShot = copyShot(currentShot);
                    }
                }
                if (!shotsIterator.hasNext()) {
                    this.lastShotReached = true;
                }
                
                // shift columns of shots to preserve shotsColumnsList size
                // only if the last shot has not been yet reached
                // and if the nomber of analyze columns is greater that filtering window half-size
                if (!this.lastShotReached && (this.currentCol > filteringWindowLength)) {
                    this.nbOfSuppressedCols++;

                    nbOfColsInShotsColumnsList = shotsColumnsList.size();

                    for (int i = 1; i < nbOfColsInShotsColumnsList; i++) {
                        List<Shot> currCol = (List) shotsColumnsList.get(i);
                        shotsColumnsList.set(i - 1, currCol);
                    }
                }
                
                // add shotsColumn to shotsColumnsList
                // if it is not empty and if the number of analyzed columns is not greater than filterWindow half-size yet 
                // or if the last shot has been reached
                if (!shotsColumn.isEmpty()) {
                    if(this.currentCol <= filteringWindowLength || this.lastShotReached){
                        shotsColumnsList.add(shotsColumn);
                    }
                    else{
                        shotsColumnsList.set(nbOfColsInShotsColumnsList - 1, shotsColumn);
                    }
                    //shotsColumnsList.add(shotsColumn);
                }
            }

            public boolean isTrueEmptyShot(int colNb, int lineNb) { // true except if we find a non empty neighbour shot having range0 < minRange
                boolean isTrueEmptyShot = true;

                for (int i = (colNb - this.nbOfSuppressedCols) - filteringWindowLength; i <= (colNb - this.nbOfSuppressedCols) + filteringWindowLength; i++) {
                    for (int j = lineNb - filteringWindowLength; j <= lineNb + filteringWindowLength; j++) {
                        if (i >= 0 && i < shotsColumnsList.size()) {
                            List<Shot> currCol = shotsColumnsList.get(i);
                            if (j >= 0 && j < currCol.size()) {
                                Shot currentShot = currCol.get(j);
                                if (currentShot.isEmpty() == false) {
                                    if (currentShot.getFirstRange() < minRange) // non empty neighbour having range0 < minRange: this is not a REAL empty shot
                                    {
                                        return false;
                                    }
                                }
                            }
                        }
                    }
                }
                return isTrueEmptyShot;  // si aucun des tirs voisins n'est plein, on a pas d'information, donc on retourne vide

            }
            
            private Shot getNextShot(){
                
                if (DEBUG && (currentCol % 100) == 0) {
                    System.out.println("Analysing colNb:" + currentCol);
                }

//                if (DEBUG && (currentShotNb % 100) == 0) {
//                    System.out.println("Analysing shotNb:" + currentShotNb);
//                }

                if( (currentCol - this.nbOfSuppressedCols) >= shotsColumnsList.size()){
                    return null;
                }
                
                List<Shot> currShotsColumn = shotsColumnsList.get(currentCol - this.nbOfSuppressedCols);
                int colSize = currShotsColumn.size();
                

                currentShotNb++;

                Shot currentShot = currShotsColumn.get(currentRow);

                
                if (currentShot.isEmpty()) {  // presumably empty shot

                    boolean isTrueEmptyShot = this.isTrueEmptyShot(currentCol, currentRow);
                    
                    currentRow++; // a line forward
                    if(currentRow == colSize){ // new column
                        currentRow = 0;
                        this.updateShotColumnsList();
                        currentCol++;
                    }

                    if (isTrueEmptyShot) {
                        return currentShot;
                    } else { // false EmptyShot
                        return getNextShot();
                        //do nothing: shot is lost
                    }
                } else {// non empty shot
                    
                    currentRow++; // a line forward
                    if(currentRow == colSize){ // new column
                        currentRow = 0;
                        this.updateShotColumnsList();
                        currentCol++;
                    }

                    
                    return currentShot;
                }
                
            }

            @Override
            public boolean hasNext() {
                
                if(!hasNextCalled){
                    hasNextCalled = true;
                    shot = getNextShot();
                }
                
                return shot != null;
            }

            @Override
            public Shot next() {
                if(hasNextCalled){
                    hasNextCalled = false;
                    return shot;
                }else{
                    return getNextShot();
                }
            }
        };
    }

    private static double getShotElevation(Shot shot) {

        return Math.toDegrees(Math.acos(shot.direction.z));
    }

    private static boolean changeOfCol(Shot prevShot, Shot currShot) {

        boolean chOfCol = false;

        if (prevShot != null) {
            double prevElevation = getShotElevation(prevShot);
            double currElevation = getShotElevation(currShot);
            chOfCol = (Math.abs(prevElevation - currElevation) > DIFF_ANGLE);
        }

        //prevShot = new Shot(currShot);
        return chOfCol;
    }

    private static Shot copyShot(Shot shot) {
        Shot copyShot = new Shot(shot);
        return copyShot;
    }

    private void initShotsColumnsList() {

        boolean chOfCol = false;
        List<Shot> columnShotsList = new ArrayList();
        shotsColumnsList = new ArrayList<>();
        int shotsArrayLines = 0;
        int shotsArrayCols = 0;

        int shotNb = 1;
        int colNb = 0;

        Shot currentShot = null;

        // build list of columns of Shots for the first N columns
        while (shotsIterator.hasNext() && colNb <= this.columnShotsListWidth) {

            while (chOfCol == false && shotsIterator.hasNext()) {
                //System.out.println("shotNb in createShotsArrayInt= " + shotNb);
                currentShot = shotsIterator.next();
                nbOfInitialShots++;
                
                chOfCol = changeOfCol(prevShot, currentShot);

                if (chOfCol) {
                    colNb++;
                    if (DEBUG) {
                        System.out.println("ChgOfCol at shotNb: " + shotNb);
                    }
                } else {
                    columnShotsList.add(currentShot);
                }

                prevShot = copyShot(currentShot);
                shotNb++;
            }
            if (columnShotsList.size() > shotsArrayLines) {
                shotsArrayLines = columnShotsList.size();
            }
            shotsColumnsList.add(columnShotsList);
            columnShotsList = new ArrayList();
            columnShotsList.add(currentShot);
            chOfCol = false;
        }
    }
    
    public static void main(String[] args) throws Exception {
        
        //RXP READER:
        RxpExtraction rxpReader = new RxpExtraction();
        rxpReader.openRxpFile(new File("/home/claudia/DATA/testsPochette/InputFiles/700101_010355.rxp"), RxpExtraction.AMPLITUDE, RxpExtraction.REFLECTANCE);
        //rxpReader.openRxpFile(new File("/home/claudia/DATA/testsCouloir/161004_094941.rxp"), RxpExtraction.AMPLITUDE, RxpExtraction.REFLECTANCE);
        
        Iterator<fr.amap.amapvox.io.tls.rxp.Shot> iterator = rxpReader.iterator();        
        RxpShotIteratorConverter converter = new RxpShotIteratorConverter(iterator);       
        
        ShotFilter2D_RXP filter = new ShotFilter2D_RXP(converter.iterator());
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/claudia/DATA/testsPochette/OutputFiles/700101_010355_Filter2DAMAPVoxRXPnew.txt")));
        //BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/claudia/DATA/testsCouloir/161004_094941_Filter2DAMAPVoxRXP.txt")));
        
        Iterator<fr.amap.lidar.amapvox.shot.Shot> filterIterator = filter.iterator();
        
        writer.write("XDirection YDirection ZDirection Range0 EmptyShot \n");
        
        int count = 0;
        while(filterIterator.hasNext()){
            
            fr.amap.lidar.amapvox.shot.Shot filteredShot = filterIterator.next();
            writer.write(filteredShot.direction.x+" "+filteredShot.direction.y+" "+filteredShot.direction.z+" "+filteredShot.getFirstRange()+" "+(filteredShot.isEmpty() ? "1"  : "0")+"\n");
            
            count++;
        }
        
        if(DEBUG){
            System.out.println("nb Of initial RXPShots=" + nbOfInitialShots);
            System.out.println("nb Of remaining RXPShots=" + count);
        }
        
        writer.close();
        rxpReader.close();
    }

}
