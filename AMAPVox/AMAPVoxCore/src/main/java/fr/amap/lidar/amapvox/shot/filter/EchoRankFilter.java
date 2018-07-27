/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.shot.filter;

import fr.amap.commons.util.IteratorWithException;
import fr.amap.commons.util.filter.Filter;
import fr.amap.lidar.amapvox.shot.Shot;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import org.apache.log4j.Logger;

/**
 *
 * @author pverley
 */
public class EchoRankFilter implements Filter<Shot.Echo> {

    private final File file;
    private final Behavior behavior;
    private IteratorWithException<Echoes> iterator;
    private Echoes echoes;
    // logger
    private final static Logger LOGGER = Logger.getLogger(EchoRankFilter.class);

    public EchoRankFilter(String file, Behavior behavior) {
        this.file = new File(file);
        this.behavior = behavior;
    }
    
    @Override
    public boolean equals(Object o) {

        if (null != o && o instanceof EchoRankFilter) {
            EchoRankFilter f = (EchoRankFilter) o;
            return file.equals(f.getFile()) && behavior.equals(f.behavior);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.file);
        hash = 79 * hash + Objects.hashCode(this.behavior);
        return hash;
    }

    @Override
    public void init() throws Exception {
        iterator = iterator();
        echoes = iterator.next();
    }

    public File getFile() {
        return file;
    }

    public Behavior behavior() {
        return behavior;
    }

    @Override
    public boolean accept(Shot.Echo echo) throws Exception {

        if (echo.rank >= 0 && null != echoes) {
            int shotID = echo.shot.index;
            while (null != echoes && echoes.shotID < shotID) {
                echoes = iterator.next();
            }
            if (null != echoes && echoes.shotID == shotID) {
                return echoes.retained[echo.rank];
            }
        }
        // by default accept all echoes from shot not listed in CSV file
        return true;
    }

    private IteratorWithException<Echoes> iterator() throws Exception {

        IteratorWE it = new IteratorWE();
        it.init();
        return it;
    }

    private class IteratorWE implements IteratorWithException<Echoes> {

        private boolean hasNextCalled;
        private Echoes currentShot;
        private final String sep = "\t";
        private int l = 1;
        private BufferedReader reader;

        void init() throws Exception {
            try {
                LOGGER.info("Open echoes filtering file " + file);
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
                    return new Echoes(Integer.valueOf(shotLine[0]), toBoolean(Arrays.copyOfRange(shotLine, 1, shotLine.length), behavior.equals(Behavior.DISCARD)));
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

        /**
         * Converts string array of integers into boolean array. reverse
         * arguments returns the negation of the boolean array
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
    }

    private class Echoes {

        private final int shotID;
        private final boolean[] retained;

        public Echoes(int shotID, boolean[] retained) {
            this.shotID = shotID;
            this.retained = retained;
        }
    }
}
