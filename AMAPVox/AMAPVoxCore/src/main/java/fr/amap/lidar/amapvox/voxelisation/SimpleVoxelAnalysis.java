package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.commons.raster.asc.Raster;
import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

        if ((nShotsProcessed % 1000000) == 0) {
            LOGGER.info("Progress: shot index " + nShotsProcessed + " (processed " + (nShotsProcessed - nShotsDiscarded) + " discarded " + nShotsDiscarded + ")");
        }

        if (retainShot(shot)) {
            // shot propagation
            propagation(shot, extractEchoes(shot));
        } else {
            nShotsDiscarded++;
        }
        // increment number of shots processed
        nShotsProcessed++;
    }

    private List<VAEcho> extractEchoes(Shot shot) throws Exception {

        List<VAEcho> echoes = new ArrayList();

        // number of echoes in the shot
        int nEchoes = shot.getEchoesNumber();

        if (nEchoes > 0) {
            // preprocessing of the echoes
            int nRetainedEchoes = 0;
            boolean[] echoRetained = new boolean[nEchoes];
            for (int k = 0; k < shot.getEchoesNumber(); k++) {
                if (retainEcho(shot.echoes[k])) {
                    echoRetained[k] = true;
                    nRetainedEchoes++;
                }
            }

            if (nRetainedEchoes > 0) {
                // list retained echoes only
                Shot.Echo[] rEchoes = new Shot.Echo[nRetainedEchoes];
                int kFiltered = 0;
                for (int k = 0; k < shot.getEchoesNumber(); k++) {
                    if (echoRetained[k]) {
                        rEchoes[kFiltered++] = shot.echoes[k];
                    }
                }

                // find echoes in same voxel
                boolean[] sameVoxelAsPreviousEcho = new boolean[nRetainedEchoes];
                for (int k = 1; k < nRetainedEchoes; k++) {
                    sameVoxelAsPreviousEcho[k] = this.isInsideSameVoxel(rEchoes[k - 1], rEchoes[k]);
                }

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

                echoes.add(new VAEcho(rEchoes[0], beamAttenuation(0, nRetainedEchoes, weightCorr)));
                int kUniq = 0;
                for (int k = 1; k < nRetainedEchoes; k++) {
                    double beamAttenuation = beamAttenuation(k, nRetainedEchoes, weightCorr);
                    if (sameVoxelAsPreviousEcho[k]) {
                        echoes.get(kUniq).bfIntercepted += beamAttenuation;
                        echoes.get(kUniq).nEcho++;
                    } else {
                        echoes.add(new VAEcho(rEchoes[k], beamAttenuation));
                        kUniq++;
                    }
                }
            }
        }
        return echoes;
    }

    private void propagation(Shot shot, List<VAEcho> echoes) throws Exception {

        // normalize shot direction and create shot line
        shot.direction.normalize();
        LineElement shotLine = new LineSegment(shot.origin, shot.direction, 999999);

        // first voxel crossed by the shot
        VoxelCrossingContext context = voxelManager.getFirstVoxelV2(shotLine);

        if (null != context) {
            double beamFractionIn = 1.d;
            Iterator<VAEcho> it = echoes.iterator();
            VAEcho echo = it.hasNext() ? it.next() : null;
            boolean reachedGround = false;
            do {
                // initialise current voxel and put it in local variable
                Point3i indices = context.indices;
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
                double dIn = context.length;
                // get next voxel
                context = voxelManager.CrossVoxel(shotLine, indices);
                // distance from shot origin to shot interception point with next voxel
                double dOut = context.length;
                double rayLength = dOut - dIn;

                // increment potential beam volume
                voxel.bvPotential += (beamSurface * rayLength);
                // increment total beam fraction in current voxel
                voxel.bvEntering += rayPonderationEnabled
                        ? beamFractionIn * beamSurface * rayLength
                        : beamFractionIn * beamSurface;
                // increment total optical length in current voxel
                voxel.lgTotal += rayLength;
                // increment number of shots crossing current voxel
                voxel.nbSampling++;
                // increment mean angle in current voxel
                voxel.angleMean += shot.getAngle();

                if (groundEnergyEnabled && closeToGround(voxel)) {
                    groundEnergy[voxel.i][voxel.j].groundEnergyPotential++;
                    groundEnergy[voxel.i][voxel.j].groundEnergyActual += beamFractionIn;
                    reachedGround = true;
                }

                if (null != echo && isEchoInsideVoxel(echo.echo.location, indices)) {
                    // increment intercepted beam fraction inside voxel
                    voxel.bvIntercepted += rayPonderationEnabled
                            ? echo.bfIntercepted * beamSurface * rayLength
                            : echo.bfIntercepted * beamSurface;
                    // increment number of echoes inside voxel
                    voxel.nbEchos += echo.nEcho;
                    // decrement beamFractionIn for next voxel
                    beamFractionIn -= echo.bfIntercepted;
                    // load next echo
                    echo = it.hasNext() ? it.next() : null;
                }
            } while (beamFractionIn > 0.d && context.indices != null && !reachedGround);
        }
    }

    private boolean isEchoInsideVoxel(Point3d echo, Point3i indexVoxel) {

        Point3i indexEcho = voxelManager.getVoxelIndicesFromPoint(echo);

        return indexEcho.equals(indexVoxel);
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

    private class VAEcho {

        private final Shot.Echo echo;
        private double bfIntercepted;
        private int nEcho;

        VAEcho(Shot.Echo echo, double bfIntercepted) {
            this.echo = echo;
            this.bfIntercepted = bfIntercepted;
            this.nEcho = 1;
        }

    }
}
