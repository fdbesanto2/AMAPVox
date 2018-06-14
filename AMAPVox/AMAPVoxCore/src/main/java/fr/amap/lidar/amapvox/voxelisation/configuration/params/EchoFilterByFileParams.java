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
import java.util.Arrays;

/**
 *
 * @author pverley
 */
public class EchoFilterByFileParams {

    private final File file;
    private final boolean discard;

    public EchoFilterByFileParams(String file, boolean discard) {
        this.file = new File(file);
        this.discard = discard;
    }

    public File getFile() {
        return file;
    }

    public boolean discardEchoes() {
        return discard;
    }

    public IteratorWithException<Echoes> iterator() throws Exception {

        IteratorWE it = new IteratorWE();
        it.init();
        return it;
    }

    /**
     * Converts string array of integers into boolean array. reverse arguments
     * returns the negation of the boolean array
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

    private class IteratorWE implements IteratorWithException<Echoes> {

        private boolean hasNextCalled;
        private Echoes currentShot;
        private final String sep = "\t";
        private int l = 1;
        private BufferedReader reader;
        
        void init() throws Exception {
            try {
                reader = new BufferedReader(new FileReader(file));
                //skip header
                reader.readLine();
            } catch (IOException ex) {
                throw new Exception("Error reading echo filter file " + file.getName(), ex);
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

        private Echoes getNextShot() throws Exception {

            String line;
            try {
                if ((line = reader.readLine()) != null) {
                    l++;
                    String[] shotLine = line.split(sep);
                    return new Echoes(Integer.valueOf(shotLine[0]), toBoolean(Arrays.copyOfRange(shotLine, 1, shotLine.length), !discard));
                } else {
                    reader.close();
                }
            } catch (IOException | NumberFormatException ex) {
                throw new Exception("Error reading echo filter file " + file.getName() + " at line " + l, ex);
            }
            return null;
        }

        @Override
        public Echoes next() throws Exception {

            if (hasNextCalled) {
                hasNextCalled = false;
                return currentShot;
            } else {
                return getNextShot();
            }
        }
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
