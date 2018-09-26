/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.configuration.params;

import fr.amap.commons.util.IteratorWithException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author pverley
 */
public class EchoesWeightByFileParams {

    private final File file;
    private IteratorWE it;
    private EchoesWeight weight;

    public EchoesWeightByFileParams(String file) {
        this.file = new File(file);
    }

    public File getFile() {
        return file;
    }

    public void init() throws Exception {
        it = new IteratorWE();
        it.init();
        weight = it.next();
    }

    public IteratorWithException<EchoesWeight> iterator() throws Exception {

        IteratorWE it = new IteratorWE();
        it.init();
        return it;
    }

    private class IteratorWE implements IteratorWithException<EchoesWeight> {

        private boolean hasNextCalled;
        private EchoesWeight currentShot;
        private final String sep = "\t";
        private int l = 1;
        private BufferedReader reader;

        void init() throws Exception {
            try {
                reader = new BufferedReader(new FileReader(file));
                //skip header
                reader.readLine();
            } catch (Exception ex) {
                throw new Exception("Error reading echo weight file " + file.getName(), ex);
            }
        }

        @Override
        public boolean hasNext() throws Exception {

            if (!hasNextCalled) {
                hasNextCalled = true;
                currentShot = getNextShot();
            }

            return currentShot != null;
        }

        private EchoesWeight getNextShot() throws Exception {

            String line;
            try {
                if ((line = reader.readLine()) != null) {
                    l++;
                    String[] shotLine = line.split(sep);
                    return new EchoesWeight(Integer.valueOf(shotLine[0]), Double.valueOf(shotLine[1]));
                } else {
                    reader.close();
                }
            } catch (IOException | NumberFormatException ex) {
                throw new Exception("Error reading echo weight file " + file.getName() + " at line " + l, ex);
            }
            return null;
        }

        @Override
        public EchoesWeight next() throws Exception {

            if (hasNextCalled) {
                hasNextCalled = false;
                return currentShot;
            } else {
                return getNextShot();
            }
        }
    }

    public double getWeightCorrection(int shotID) throws Exception {

        // correction factor set to one by default (no correction)
        double weightCorr = 1.d;
        // match shot ID
        while (null != weight && weight.shotID < shotID) {
            weight = it.next();
        }
       // if shot ID matched, get weight correction factor
        if (null != weight && weight.shotID == shotID) {
            weightCorr = weight.weight;
        }
        // return weight correction factor
        return weightCorr;
    }

    public class EchoesWeight {

        public final int shotID;
        public final double weight;

        public EchoesWeight(int shotID, double weight) {
            this.shotID = shotID;
            this.weight = weight;
        }
    }

}
