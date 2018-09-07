package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.commons.raster.asc.Raster;
import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
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

        if ((nShotsProcessed % 1000000) == 0) {
            LOGGER.info("Progress: shot index " + nShotsProcessed + " (processed " + (nShotsProcessed - nShotsDiscarded) + " discarded " + nShotsDiscarded + ")");
        }

        if (retainShot(shot)) {
            shot.direction.normalize();

            // vegetation free shot propagation (as if no vegetation in the scene)
            freePropagation(shot);
            
            // shot propagation
            propagation(shot);

        } else {
            nShotsDiscarded++;
        }
        // increment number of shots processed
        nShotsProcessed++;
    }

    private void propagation(Shot shot) throws Exception {

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

        // shot origin
        Point3d origin = new Point3d(shot.origin);

        double beamFractionPreviousEchoes = 0.d;
        int rankFirstEcho = 0;
        boolean reachedGround = false;
        // loop over echoes
        for (Shot.Echo echo : shot.echoes) {
            // rank of current echo
            if (beamFractionPreviousEchoes == 0.d) {
                rankFirstEcho = echo.rank;
            }
            // next echo
            Shot.Echo nextEcho = (echo.rank < shot.getEchoesNumber() - 1)
                    ? shot.echoes[echo.rank + 1]
                    : null;

            // compute beam fraction of current echo
            double beamFractionCurrentEcho = 1;
            // beam fraction pondered by weight table
            if (null != weightTable && (shot.getEchoesNumber() > 0)) {
                beamFractionCurrentEcho *= weightTable[shot.getEchoesNumber() - 1][echo.rank];
            }
            // beam fraction pondered by weight from CSV file
            beamFractionCurrentEcho *= weightCorr;

            if ((null != nextEcho) && isInsideSameVoxel(echo, nextEcho)) {
                // current echo and next echo are in same voxel
                // increment beam fraction of previous echo and move to next echo
                // propagation will be done at next echo with cumulated energy
                beamFractionPreviousEchoes += beamFractionCurrentEcho;
                continue;
            }
            // set beam fraction after echo 
            double beamFractionOut = beamFractionCurrentEcho + beamFractionPreviousEchoes;
            // reset beamFractionPreviousEchoes
            beamFractionPreviousEchoes = 0.d;
            // set beam fraction before echo
            double beamFractionIn = computeResidualEnergy(shot.getEchoesNumber(), rankFirstEcho, weightCorr);

            // check whether current echo should be retained or discarded
            boolean retainEcho = retainEcho(echo);

            // optical segment:
            //   first iteration from origin to 1st echo (pseudo infinity if no echo)
            //   then from one echo to the next
            LineElement opticalSegment = new LineSegment(origin, echo.location);
            // length of current optical segment
            double lengthOpticalSegment = opticalSegment.getLength();

            // first voxel crossed by the current segment
            VoxelCrossingContext context = voxelManager.getFirstVoxelV2(opticalSegment);

            // loop over the voxels crossed by the optical segment
            while ((context != null) && (context.indices != null) && !reachedGround) {
                // index current voxel
                Point3i indices = context.indices;
                // distance from origin to shot interception point with current voxel
                double distOriginCurrentVoxel = context.length;
                // get next voxel
                context = voxelManager.CrossVoxel(opticalSegment, context.indices);
                // distance from origin to shot interception point with next voxel
                double distOriginNextVoxel = context.length;

                // ensure current voxel is instantiated
                if (voxels[indices.x][indices.y][indices.z] == null) {
                    voxels[indices.x][indices.y][indices.z] = initVoxel(indices.x, indices.y, indices.z);
                }
                // current vox in local variable
                Voxel voxel = voxels[indices.x][indices.y][indices.z];

                // stop propagation if current voxel is below the ground
                // and no ground energy calculation is awaited
                if (belowGround(voxel)) {
                    reachedGround = true;
                    break;
                }

                // beam surface in current voxel
                double surface = beamSection(shot, voxel, laserSpec);
                // optical length in current voxel
                // approximated as distance of the optical path accross the whole
                // voxel, even though it is shorter in reality
                double opticalLengthInVoxel = distOriginNextVoxel - distOriginCurrentVoxel;
                // increment total optical length in current voxel
                voxel.lgTotal += opticalLengthInVoxel;
                // increment number of shots going through current voxel
                voxel.nbSampling++;
                // increment mean angle in current voxel
                voxel.angleMean += shot.getAngle();
                // unintercepted beam volume
                double beamVolume = surface * opticalLengthInVoxel;
                // beam volume in current voxel
                double beamVolumeIn = beamFractionIn * beamVolume;
                // increment total beam volume in current voxel
                voxel.bvEntering += beamVolumeIn;
                // additional update of voxel variable if current echo is retained
                if (retainEcho) {
                    // update total number of echoes in current voxel
                    // ignore (thus underestimate) multiple echoes in same voxel
                    voxel.nbEchos++;
                    // increment total beam volume after interception
                    voxel.bvIntercepted += (beamFractionOut * beamVolume);
                }
                // update ground energy
                if (closeToGround(voxel)) {
                    groundEnergy[voxel.i][voxel.j].groundEnergyPotential++;
                    groundEnergy[voxel.i][voxel.j].groundEnergyActual += retainEcho
                            ? beamFractionOut
                            : beamFractionIn;
                    reachedGround = true;
                }

                // temporary test that will be deleted after verification
                if (lengthOpticalSegment <= distOriginCurrentVoxel) {
                    // CASE 2 optical segment is shorter than distance to current voxel
                    // this case should be handled specifically ?
                    //LOGGER.error("lengthOpticalSegment <= distOriginCurrentVoxel should never occur !");
                }
            } // end loop over voxels
            
            // current echo becomes the origin of the next optical segment
            origin = echo.location;
        } // end loop over echoes
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

        if (parameters.getGroundEnergyParams() == null
                || !parameters.getGroundEnergyParams().isCalculateGroundEnergy()) {
            if (voxel.ground_distance < voxelManager.getVoxelSpace().getVoxelSize().z / 2.0f) {
                return true;
            }
        }
        return false;
    }

    private boolean closeToGround(Voxel voxel) {

        if (parameters.getGroundEnergyParams() != null
                && parameters.getGroundEnergyParams().isCalculateGroundEnergy()
                && parameters.infos.getType() != VoxelSpaceInfos.Type.TLS) {
            return (voxel.ground_distance < parameters.getDtmFilteringParams().getMinDTMDistance());
        }
        // by default not close to ground
        return false;
    }
}
