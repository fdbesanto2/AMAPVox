package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.commons.raster.asc.Raster;
import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import javax.vecmath.Point3i;

import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

public class SimpleVoxelAnalysis extends AbstractVoxelAnalysis {

    private final static Logger LOGGER = Logger.getLogger(SimpleVoxelAnalysis.class);

    public SimpleVoxelAnalysis(Raster terrain, VoxelAnalysisCfg cfg) throws Exception {
        super(terrain, cfg);
    }

    @Override
    public void processOneShot(Shot shot) throws Exception {

        if (voxelManager == null) {
            LOGGER.error("VoxelManager not initialized, what happened??");
            return;
        }

        if ((nShots % 1000000) == 0) {
            LOGGER.info("[voxelisation] shot " + nShots + " (processed " + nShotsProcessed + ", discarded " + nShotsDiscarded + ", out " + nShotsOut + ")");
        }

        if (retainShot(shot)) {
            // shot propagation
            if (propagation(shot, extractEchoesContext(shot))) {
                // increment number of shots processed
                nShotsProcessed++;
            } else {
                nShotsOut++;
            }
        } else {
            nShotsDiscarded++;
        }
        nShots++;
    }

    private boolean propagation(Shot shot, EchoesContext echoesContext) throws Exception {

        // normalize shot direction and create shot line
        shot.direction.normalize();
        LineElement shotLine = new LineSegment(shot.origin, shot.direction, 999999);

        // first voxel crossed by the shot
        VoxelCrossingContext voxelCrossing = voxelManager.getFirstVoxelV2(shotLine);

        if (null != voxelCrossing) {
            // initialise entering beam fraction (100%)
            double beamFractionIn = 1.d;
            // load first echo
            int rank = 0;
            Shot.Echo echo = echoesContext.nEchoes > 0 ? shot.echoes[rank] : null;
            // loop over the voxels crossed by the shot
            Point3i indices;
            do {
                // initialise current voxel and put it in local variable
                indices = voxelCrossing.indices;
                // instantiate voxel on the fly when first encountered
                if (voxels[indices.x][indices.y][indices.z] == null) {
                    voxels[indices.x][indices.y][indices.z] = initVoxel(indices.x, indices.y, indices.z);
                }
                Voxel voxel = voxels[indices.x][indices.y][indices.z];

                // stop propagation if current voxel is below the ground
                if (!groundEnergyEnabled && belowGround(voxel)) {
                    break;
                }

                // compute beam surface at voxel centre
                double beamSurface = constantBeamSection
                        ? 1.d
                        : beamSection(shot, voxel, laserSpec);

                // ray length within voxel
                // distance from shot origin to shot interception point with current voxel
                double dIn = voxelCrossing.length;
                // get next voxel
                voxelCrossing = voxelManager.CrossVoxel(shotLine, indices);
                // distance from shot origin to shot interception point with next voxel
                double dOut = voxelCrossing.length;
                double rayLength = dOut - dIn;

                // increment potential beam volume
                voxel.bvPotential += (beamSurface * rayLength);

                // reset intercepted beam fraction in current voxel
                double bfIntercepted = 0.d;

                // case 1: echo not null and falls inside current voxel
                if (null != echo && isEchoInsideVoxel(echo.location, indices)) {
                    // while loop over echoes in same voxel
                    while (null != echo && isEchoInsideVoxel(echo.location, indices)) {

                        // intercepted beam fraction
                        double bfInterceptedEcho = rayPonderationEnabled
                                ? echoesContext.bfIntercepted[rank] * beamSurface * rayLength
                                : echoesContext.bfIntercepted[rank] * beamSurface;
                        bfIntercepted += echoesContext.bfIntercepted[rank];

                        if (echoesContext.retained[rank]) {
                            // echo inside current voxel & retained
                            if (!echoesContext.sameVoxelAsPreviousEcho[rank]) {
                                // first echo inside current voxel & retained
                                // increment some voxel state variables for first retained echo only
                                // increment entering beam fraction in current voxel
                                voxel.bvEntering += rayPonderationEnabled
                                        ? beamFractionIn * beamSurface * rayLength
                                        : beamFractionIn * beamSurface;
                                // increment total optical length in current voxel
                                voxel.lgTotal += rayLength;
                                // increment number of shots crossing current voxel
                                voxel.nbSampling++;
                                // increment mean angle in current voxel
                                voxel.angleMean += shot.getAngle();
                                // increment intercepted beam fraction inside voxel
                            }
                            // increment other voxel state variables for every retained echo
                            // increment intercepted beam fraction  in current voxel
                            voxel.bvIntercepted += bfInterceptedEcho;
                            // increment number of echoes in current voxel
                            voxel.nbEchos++;
                        }
                        // next echo 
                        rank++;
                        echo = rank < echoesContext.nEchoes ? shot.echoes[rank] : null;
                    }
                } else {
                    // case 2: echo is null or does not fall inside current voxel
                    // increment entering beam fraction in current voxel
                    voxel.bvEntering += rayPonderationEnabled
                            ? beamFractionIn * beamSurface * rayLength
                            : beamFractionIn * beamSurface;
                    // increment total optical length in current voxel
                    voxel.lgTotal += rayLength;
                    // increment number of shots crossing current voxel
                    voxel.nbSampling++;
                    // increment mean angle in current voxel
                    voxel.angleMean += shot.getAngle();
                }

                // increment ground energy
                if (groundEnergyEnabled && closeToGround(voxel)) {
                    groundEnergy[voxel.i][voxel.j].groundEnergyPotential++;
                    groundEnergy[voxel.i][voxel.j].groundEnergyActual += beamFractionIn;
                }

                // decrement beamFractionIn for next voxel
                beamFractionIn -= bfIntercepted;
            } while (beamFractionIn > 0.d && voxelCrossing.indices != null);
            // shot went through the voxel space
            return true;
        }
        // shot did not go through the voxel space
        return false;
    }

    private boolean isEchoInsideVoxel(Point3d echo, Point3i indexVoxel) {

        Point3i indexEcho = voxelManager.getVoxelIndicesFromPoint(echo);

        return (null != indexEcho) && (indexEcho.equals(indexVoxel));
    }

    private double beamAttenuation(int rankEcho, int nEcho, double weightCorr) {

        // compute beam fraction of current echo
        double beamFractionCurrentEcho = 1;
        // beam fraction pondered by weight table
        if (null != weightTable && (nEcho > 0)) {
            beamFractionCurrentEcho *= weightTable[nEcho - 1][rankEcho];
        }
        // beam fraction pondered by weight from CSV file
        beamFractionCurrentEcho *= weightCorr;
        return beamFractionCurrentEcho;
    }

    /**
     * Compute the average beam section of the shot in given voxel by estimating
     * it at voxel centre.
     *
     * @param shot, a shot with a define origin
     * @param voxel
     * @param spec, laser specification (beam divergence and diameter at exit)
     * @return the beam section calculated at voxel centre.
     */
    private double beamSection(Shot shot, Voxel voxel, LaserSpecification spec) {

        // distance from shot origin to current voxel center
        Point3d voxelPosition = getPosition(new Point3i(voxel.i, voxel.j, voxel.k));
        double distance = voxelPosition.distance(shot.origin);

        // beam surface in current voxel
        return Math.pow((Math.tan(0.5d * spec.getBeamDivergence()) * distance) + 0.5d * spec.getBeamDiameterAtExit(), 2) * Math.PI;
    }

    /**
     * check whether current voxel centre vertical coordinate is below the
     * ground
     */
    private boolean belowGround(Voxel voxel) {
        return (voxel.ground_distance < voxelManager.getVoxelSpace().getVoxelSize().z / 2.0f);
    }

    private boolean closeToGround(Voxel voxel) {
        return (voxel.ground_distance < parameters.getDtmFilteringParams().getMinDTMDistance());
    }

    private EchoesContext extractEchoesContext(Shot shot) throws Exception {

        EchoesContext context = new EchoesContext(shot.getEchoesNumber());

        if (context.nEchoes > 0) {
            for (int k = 0; k < context.nEchoes; k++) {
                context.retained[k] = retainEcho(shot.echoes[k]);
            }

            // find echoes in same voxel
            for (int k = 1; k < context.nEchoes; k++) {
                context.sameVoxelAsPreviousEcho[k] = isInsideSameVoxel(shot.echoes[k - 1], shot.echoes[k]);
            }

            // specific weight attenuation (EchoesWeightByFileParams.java) for this shot
            double weightCorr = getWeightCorrection(shot);
            for (int k = 0; k < context.nEchoes; k++) {
                context.bfIntercepted[k] = beamAttenuation(k, context.nEchoes, weightCorr);
            }
        }

        return context;
    }

    /**
     * Specific weight attenuation (EchoesWeightByFileParams.java) for this shot
     */
    private double getWeightCorrection(Shot shot) throws Exception {

        // specific weight attenuation (EchoesWeightByFileParams.java) for this shot
        double weightCorr = 1.d;
        if (null != echoesWeight) {
            int shotID = shot.index;
            while (null != echoesWeight && echoesWeight.shotID < shotID) {
                echoesWeight = weightIterator.next();
            }
            // beam fraction pondered by weight from CSV file
            if (null != echoesWeight && echoesWeight.shotID == shotID) {
                weightCorr = echoesWeight.weight;
            }
        }
        return weightCorr;
    }

    private class EchoesContext {

        private final int nEchoes;
        private final boolean retained[];
        private final double[] bfIntercepted;
        private final boolean[] sameVoxelAsPreviousEcho;

        EchoesContext(int nEchoes) {
            this.nEchoes = nEchoes;
            this.retained = new boolean[nEchoes];
            this.bfIntercepted = new double[nEchoes];
            this.sameVoxelAsPreviousEcho = new boolean[nEchoes];
        }
    }
}
