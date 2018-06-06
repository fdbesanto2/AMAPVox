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
import java.util.Arrays;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 *
 * @author pverley
 */
public class EchoFilterByFileParams {

    private final File file;
    private final boolean discard;
    private final static Logger LOGGER = Logger.getLogger(EchoFilterByFileParams.class);

    public EchoFilterByFileParams(String file, String behavior) {
        this.file = new File(file);
        discard = behavior.equalsIgnoreCase("discard");
    }

    public File getFile() {
        return file;
    }

    public boolean discardEchoes() {
        return discard;
    }

    public Iterator<Echoes> iterator() throws IOException {

        BufferedReader reader = new BufferedReader(new FileReader(file));
        //skip header
        reader.readLine();

        return new Iterator<Echoes>() {

            boolean hasNextCalled;
            Echoes currentShot;
            String sep = "\t";

            @Override
            public boolean hasNext() {

                if (!hasNextCalled) {
                    hasNextCalled = true;
                    currentShot = getNextShot();
                }

                return currentShot != null;
            }

            private Echoes getNextShot() {

                String line;
                try {
                    if ((line = reader.readLine()) != null) {
                        String[] shotLine = line.split(sep);
                        return new Echoes(Integer.valueOf(shotLine[0]), toBoolean(Arrays.copyOfRange(shotLine, 1, shotLine.length), !discard));
                    } else {
                        reader.close();
                    }
                } catch (IOException | NumberFormatException ex) {
                    LOGGER.warn("Echo filter by shot index and echo rank " + ex.toString(), ex);
                }
                return null;
            }

            @Override
            public Echoes next() {

                if (hasNextCalled) {
                    hasNextCalled = false;
                    return currentShot;
                } else {
                    return getNextShot();
                }
            }
        };
    }

    /**
     * Converts string array of integers into boolean array.
     * reverse arguments returns the negation of the boolean array
     */
    private boolean[] toBoolean(String[] str, boolean reverse) {
        boolean[] bln = new boolean[str.length];
        for (int i = 0; i < bln.length; i++) {
            bln[i] = reverse
                    ? (Integer.valueOf(str[i]) != 1)
                    : (Integer.valueOf(str[i]) == 1);
        }
        return bln;
    }

    public class Echoes {

        public final int shotID;
        public final boolean[] discarded;

        public Echoes(int shotID, boolean[] retained) {
            this.shotID = shotID;
            this.discarded = retained;
        }
    }

}
