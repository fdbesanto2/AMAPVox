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
public class ShotFilter2D implements Iterable<Shot> {

    private final static boolean DEBUG = false;

    private final Iterator<Shot> shotsIterator;

    private final static int DEFAULT_FILTERING_WINDOW_LENGTH = 3;
    private final static double DEFAULT_MIN_RANGE = 1.5;

    private final int filteringWindowLength;
    private final int columnShotsListWidth;  // nombre de colonnes de la fenêtre glissante (tableau de tirs pour avoir le voisinage en 2D)
    private final double minRange; // meters

    private final static double DIFF_ANGLE = Math.toDegrees(Math.PI / 8);
    private List<List<Shot>> shotsColumnsList = new ArrayList();

    private Shot prevShot = null;


    /**
     *
     * @param shotIterator
     */
    public ShotFilter2D(Iterator<Shot> shotIterator) {

        this(shotIterator, DEFAULT_FILTERING_WINDOW_LENGTH, DEFAULT_MIN_RANGE);
    }

    /**
     *
     * @param shotIterator
     * @param filteringWindowLength half-size of the neighborhood filtering
     * squared window
     * @param minRange minimum distance (m) of reliable echoes detection
     */
    public ShotFilter2D(Iterator<Shot> shotIterator, int filteringWindowLength, double minRange) {

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

            //!!!!!à incrémenter quelque part
            private int nbOfAnalyzedCols = 0;  /// number of analyzed columns
            private int nbOfSuppressedCols = 0; // number of suppressed columns from shotsArray

            private void updateShotColumnsList() {

                boolean chOfCol = false;
                List<Shot> shotsColumn = new ArrayList();

                // supprimer col 0 de la liste de colonnes de tirs, et décaler les autres colonnes
                // uniquement si le dernier tir n'a pas encore été atteint
                // et si le nombre de colonnes analysées et > fenetre de filtrage
                if (!this.lastShotReached && (this.nbOfAnalyzedCols > filteringWindowLength)) {
                    this.nbOfSuppressedCols++;

                    int nbOfColsInShotsColumnsList = shotsColumnsList.size();

                    for (int i = 1; i < nbOfColsInShotsColumnsList; i++) {
                        List<Shot> currCol = (List) shotsColumnsList.get(i);
                        shotsColumnsList.set(i - 1, currCol);
                    }
                }

                while ((chOfCol == false) && (shotsIterator.hasNext())) { // build columnShotsList

                    Shot currentShot = shotsIterator.next();
                    chOfCol = changeOfCol(prevShot, currentShot);
                    prevShot = copyShot(currentShot);

                    if (!chOfCol) {
                        shotsColumn.add(currentShot);
                    }
                }
                if (!shotsIterator.hasNext()) {
                    this.lastShotReached = true;
                }

                // add shotsColumn to shotsColumnsList
                if (!shotsColumn.isEmpty()) {
                    shotsColumnsList.add(shotsColumn);
                }
            }

            public boolean isTrueEmptyShot(int colNb, int lineNb) { // true except if we find a non empty neighbour shot having range0 < minRange
                boolean isTrueEmptyShot = true;

                for (int i = colNb - filteringWindowLength; i <= colNb + filteringWindowLength; i++) {
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
                
                int shotNb = 0;

                int colNb = 0; // column beeing analyzed

                if (DEBUG && (colNb % 100) == 0) {
                    System.out.println("Analysing colNb:" + colNb);
                }

                if (DEBUG && (shotNb % 100) == 0) {
                    System.out.println("Analysing shotNb:" + shotNb);
                }

                if(currentCol >= shotsColumnsList.size()){
                    return null;
                }
                
                List<Shot> currShotsColumn = shotsColumnsList.get(currentCol);
                int colSize = currShotsColumn.size();
                

                shotNb++;

                Shot currentShot = currShotsColumn.get(currentRow);

                currentRow++;

                if(currentRow == colSize){
                    currentRow = 0;
                    colNb++;
                    this.updateShotColumnsList();
                    currentCol++;
                }

                if (currentShot.isEmpty()) {  // presumably empty shot

                    boolean isTrueEmptyShot = this.isTrueEmptyShot(currentCol, currentRow);
                    if (isTrueEmptyShot) {
                        return currentShot;
                    } else { // false EmptyShot
                        return getNextShot();
                        //do nothing: shot is lost
                    }
                } else {// non empty shot
                    return currentShot;
                }

                //Change of Col
                //System.out.println("\n analyze shots: changement de colonne pour shotNb: " + shotNb);
                
                //nbOfAnalyzedCols++; //to uncomment
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
        
        //RxpExtraction rxpReader = new RxpExtraction();
        //rxpReader.openRxpFile(new File("/media/forestview01/partageLidar/ClaudiaLavalley/CorrectionTirsSansRetour/DATAPochette/700101_010249.rxp"), RxpExtraction.REFLECTANCE, RxpExtraction.DEVIATION);
        
        //Iterator<fr.amap.amapvox.io.tls.rxp.Shot> iterator = rxpReader.iterator();
        
        //RxpShotIteratorConverter converter = new RxpShotIteratorConverter(iterator);
        
        ShotReader shotReader = new ShotReader(new File("/media/forestview01/partageLidar/ClaudiaLavalley/CorrectionTirsSansRetour/DATAPochette/700101_010249_Pochette60cm.rxp.txt"));
        
        TxtShotIteratorConverter converter = new TxtShotIteratorConverter(shotReader.iterator());
        
        ShotFilter2D filter = new ShotFilter2D(converter.iterator());
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/home/julien/Bureau/tmp/test_algo_faux_tirs/700101_010249_Pochette60cm.txt")));
        
        Iterator<fr.amap.lidar.amapvox.shot.Shot> filterIterator = filter.iterator();
        
        writer.write("XDirection YDirection ZDirection Range0 EmptyShot \n");
        
        int count = 0;
        while(filterIterator.hasNext()){
            
            fr.amap.lidar.amapvox.shot.Shot filteredShot = filterIterator.next();
            writer.write(filteredShot.direction.x+" "+filteredShot.direction.y+" "+filteredShot.direction.z+" "+filteredShot.getFirstRange()+" "+(filteredShot.isEmpty() ? "1"  : "0")+"\n");
            
            count++;
        }
        
        writer.close();
        //rxpReader.close();
    }

}
