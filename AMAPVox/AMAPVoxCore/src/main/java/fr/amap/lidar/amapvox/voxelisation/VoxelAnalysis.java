package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import javax.vecmath.Point3i;

import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

public class VoxelAnalysis extends AbstractVoxelAnalysis {

    public VoxelAnalysis(Raster terrain, VoxelAnalysisCfg cfg) throws Exception {
        super(terrain, cfg);
    }

    private final static Logger LOGGER = Logger.getLogger(VoxelAnalysis.class);

    private boolean groundEnergySet = false;
    private boolean shotChanged = false;
    private Voxel lastVoxelSampled;
    private int lastShotId;

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
            Point3d origin = new Point3d(shot.origin);

            shotChanged = true;
            groundEnergySet = false;

            // vegetation free shot propagation (as if no vegetation in the scene)
            freePropagation(shot);

            if (shot.getEchoesNumber() == 0) {
                // empty shot
                boolean keep = retainEcho(shot.echoes[0]);
                double beamFraction = 1.d, residualEnergy = 1.d;
                propagate(origin, shot.echoes[0].location, beamFraction, residualEnergy, false, nShotsProcessed, shot, keep);
            } else {
                // shot with at least one echo.
                // look for specific weight attenuation (EchoesWeightByFileParams.java)
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

                double beamFractionPreviousEchoes = 0.d;
                int rankFirstEchoOfVoxel = 0;
                // loop over echoes
                for (Shot.Echo echo : shot.echoes) {
                    // next echo
                    Shot.Echo nextEcho = (echo.rank < shot.getEchoesNumber() - 1)
                            ? shot.echoes[echo.rank + 1]
                            : null;

                    // check whether current echo is the first echo of the shot in corresponding voxel
                    if (beamFractionPreviousEchoes == 0.d) {
                        rankFirstEchoOfVoxel = echo.rank;
                    }

                    // compute beam fraction of current echo
                    double beamFractionCurrentEcho = 1;
                    // beam fraction pondered by weight table
                    if (null != weightTable) {
                        beamFractionCurrentEcho *= weightTable[shot.getEchoesNumber() - 1][echo.rank];
                    }
                    // beam fraction pondered by weight from CSV file
                    beamFractionCurrentEcho *= weightCorr;

                    if (isInsideSameVoxel(echo, nextEcho)) {
                        // current echo and next echo are in same voxel
                        // increment beam fraction of previous echo and move to next echo
                        // propagation will be done at next echo with cumulated energy
                        beamFractionPreviousEchoes += beamFractionCurrentEcho;
                    } else {
                        // handle current echo and previous echoes that are in the same voxel
                        double beamFraction = beamFractionCurrentEcho + beamFractionPreviousEchoes;
                        // residual energy must be re-evaluated if custom attenuation factor provided
                        double residualEnergy = (weightCorr != 1)
                                ? computeResidualEnergy(shot.getEchoesNumber(), rankFirstEchoOfVoxel, weightCorr)
                                : residualEnergyTable[shot.getEchoesNumber() - 1][rankFirstEchoOfVoxel];
                        // whether current echo is last echo of the shot
                        boolean lastEcho = (echo.rank == shot.getEchoesNumber() - 1);
                        // whether current echo should be retained or discarded (echo filters)
                        boolean retain = retainEcho(echo);
                        // propagate echo
                        propagate(origin, echo.location, beamFraction, residualEnergy, lastEcho, nShotsProcessed, shot, retain);
                        // current echo set as origin of next echo
                        origin = new Point3d(echo.location);
                        // reset beam fraction previous echoes
                        beamFractionPreviousEchoes = 0.d;
                    }
                } // end loop over echoes
            }
        } else {
            nShotsDiscarded++;
        }
        // increment number of shots processed
        nShotsProcessed++;
    }

    /**
     *
     * @param origin current origin (origin start from the last echo)
     * @param echo current echo (position in voxel space)
     * @param beamFraction current beam fraction of laser shot
     * @param residualEnergy residual energy of laser beam
     * @param lastEcho is current echo the last or not
     * @param shotID current shot (id)
     * @param shot current shot processed
     * @param echoRank current echo processed (rank)
     */
    private void propagate(Point3d origin, Point3d echo, double beamFraction, double residualEnergy, boolean lastEcho, int shotID, Shot shot, boolean keepEcho) {

        // get shot line
        LineElement lineElement = new LineSegment(origin, echo);

        // first voxel crossed by the segment
        VoxelCrossingContext context = voxelManager.getFirstVoxelV2(lineElement);

        // distance from origin to echo
        double distOriginEcho = lineElement.getLength();

        while ((context != null) && (context.indices != null)) {

            // index current voxel
            Point3i indices = context.indices;

            // distance from origin to shot interception point with current voxel
            double distOriginCurrentVoxel = context.length;
            // get next voxel
            context = voxelManager.CrossVoxel(lineElement, context.indices);
            // distance from origin to shot interception point with next voxel
            double distOriginNextVoxel = context.length;

            // ensure current voxel is instantiated
            if (voxels[indices.x][indices.y][indices.z] == null) {
                voxels[indices.x][indices.y][indices.z] = initVoxel(indices.x, indices.y, indices.z);
            }
            // current vox in local variable
            Voxel vox = voxels[indices.x][indices.y][indices.z];

            // stop propagation if current voxel is below the ground
            if (parameters.getGroundEnergyParams() == null
                    || !parameters.getGroundEnergyParams().isCalculateGroundEnergy()) {
                if (vox.ground_distance < voxelManager.getVoxelSpace().getVoxelSize().z / 2.0f) {
                    break;
                }
            }

            // distance from shot origin to current voxel center
            Point3d voxelPosition = getPosition(new Point3i(indices.x, indices.y, indices.z));
            double distance = voxelPosition.distance(shot.origin);

            // beam surface in current voxel
            double beamSurface = constantBeamSection
                    ? 1.d
                    : Math.pow((Math.tan(0.5d * laserSpec.getBeamDivergence()) * distance) + 0.5d * laserSpec.getBeamDiameterAtExit(), 2) * Math.PI;

            // Assumption: when echo falls right on a voxel face, AMAPVox
            // considers it belongs to next voxel
            if (distOriginNextVoxel < distOriginEcho) {
                // CASE 1 distance to echo is greater than distance to next echo
                // hence current voxel is crossed without any interception

                if (shotID == lastShotId && lastVoxelSampled != null && lastVoxelSampled == vox) {
                    // the voxel has already been crossed by this shot at 
                    // previous call of the propagate function.
                } else {
                    // ray length in this case is the distance between 
                    // entering point of current voxel and entering point of
                    // next voxel
                    double rayLength = distOriginNextVoxel - distOriginCurrentVoxel;
                    // increment total optical length in current voxel
                    vox.lgTotal += rayLength;
                    // increment number of shots going through current voxel
                    vox.nbSampling++;
                    // increment mean angle in current voxel
                    vox.angleMean += shot.getAngle();
                    // increment total beam fraction in current voxel
                    vox.bvEntering += rayPonderationEnabled
                            ? residualEnergy * beamSurface * rayLength
                            : residualEnergy * beamSurface;
                    lastVoxelSampled = vox;
                    lastShotId = shotID;
                }

            } else if (distOriginCurrentVoxel >= distOriginEcho) {
                // CASE 2 distance to current voxel is greater than distance
                // to echo, hence echo has already been found and handled 
                // at previous iteration of the while loop

                if (shotChanged) {

                    // for ALS voxelisation check whether current shot reaches the ground
                    if (parameters.getGroundEnergyParams() != null
                            && parameters.getGroundEnergyParams().isCalculateGroundEnergy()
                            && parameters.infos.getType() != VoxelSpaceInfos.Type.TLS) {

                        if (vox.ground_distance < parameters.getDtmFilteringParams().getMinDTMDistance()) {
                            // current voxel is close enough to the ground to assume that the shot will hit it
                            groundEnergy[vox.i][vox.j].groundEnergyPotential++;
                            shotChanged = false;
                            // leave the while loop over the voxels
                            context = null;
                        }
                    } else {
                        // TLS voxelisation or ALS but ground energy calculation disabled
                        // leave the while loop over the voxels
                        context = null;
                    }
                }

            } else {
                // CASE 3 distOriginCurrentVoxel < distOriginEcho <= distOriginNextVoxel
                // echo is in the current voxel

                // for last echo: whether ray length should be truncated as
                // distance from voxel entering point to echo or virtually
                // extended until voxel exiting point
                double rayLength = (lastEcho && lastRayTruncated)
                        ? distOriginEcho - distOriginCurrentVoxel
                        : distOriginNextVoxel - distOriginCurrentVoxel;

                if (shotID == lastShotId && lastVoxelSampled != null && lastVoxelSampled == vox) {
                    // pour n'échantillonner qu'une fois le voxel pour un tir
                    // phv 20180831: do not understand how we can reach this statement
                } else {

                    vox.nbSampling++;
                    vox.lgTotal += rayLength;
                    vox.angleMean += shot.getAngle();
                    // increment total beam fraction in current voxel
                    vox.bvEntering += rayPonderationEnabled
                            ? residualEnergy * beamSurface * rayLength
                            : residualEnergy * beamSurface;
                    lastVoxelSampled = vox;
                    lastShotId = shotID;
                }

                if (keepEcho) {

                    vox.nbEchos++;
                    vox.bvIntercepted += rayPonderationEnabled
                            ? beamFraction * beamSurface * rayLength
                            : beamFraction * beamSurface;

                } else if (parameters.getGroundEnergyParams() != null
                        && parameters.getGroundEnergyParams().isCalculateGroundEnergy()
                        && parameters.infos.getType() != VoxelSpaceInfos.Type.TLS && !groundEnergySet) {
                    groundEnergy[vox.i][vox.j].groundEnergyActual += residualEnergy;
                    groundEnergy[vox.i][vox.j].groundEnergyPotential++;
                    groundEnergySet = true;
                }
            }
        }
    }
}
