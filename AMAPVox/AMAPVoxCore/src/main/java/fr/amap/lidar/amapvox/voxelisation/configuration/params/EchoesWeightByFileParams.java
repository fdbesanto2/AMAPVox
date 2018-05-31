/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.configuration.params;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 *
 * @author pverley
 */
public class EchoesWeightByFileParams {

    private final File weightFile;
    private final static Logger LOGGER = Logger.getLogger(EchoesWeightByFileParams.class);

    public EchoesWeightByFileParams(File weightFile) {
        this.weightFile = weightFile;
    }
    
    public File getFile() {
        return weightFile;
    }

    public Iterator<EchoesWeight> iterator() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(weightFile));
        //skip header
        reader.readLine();

        return new Iterator<EchoesWeight>() {

            boolean hasNextCalled;
            EchoesWeight currentShot;
            String sep = "\t";

            @Override
            public boolean hasNext() {

                if (!hasNextCalled) {
                    hasNextCalled = true;
                    currentShot = getNextShot();
                }

                return currentShot != null;
            }

            private EchoesWeight getNextShot() {

                String line;
                try {
                    if ((line = reader.readLine()) != null) {
                        String[] shotLine = line.split(sep);
                        int shotId = Integer.valueOf(shotLine[0]);
                        int nEchoWeights = shotLine.length - 1;
                        double[] echoWeights = new double[nEchoWeights];
                        for (int i = 0; i < nEchoWeights; i++) {
                            echoWeights[i] = Double.valueOf(shotLine[i + 1]);
                        }
                        return new EchoesWeight(shotId, echoWeights);
                    } else {
                        reader.close();
                    }
                } catch (IOException | NumberFormatException ex) {
                    LOGGER.warn(ex);
                }
                return null;
            }

            @Override
            public EchoesWeight next() {

                if (hasNextCalled) {
                    hasNextCalled = false;
                    return currentShot;
                } else {
                    return getNextShot();
                }
            }
        };
    }

    public class EchoesWeight {

        public final int shotID;
        public final double[] weights;

        public EchoesWeight(int shotID, double[] weights) {
            this.shotID = shotID;
            this.weights = weights;
        }
    }

}
