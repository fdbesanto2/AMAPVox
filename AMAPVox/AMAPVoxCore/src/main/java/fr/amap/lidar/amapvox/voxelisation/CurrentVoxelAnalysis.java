package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import java.io.BufferedWriter;
import javax.vecmath.Point3i;

import java.io.IOException;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

public class CurrentVoxelAnalysis extends AbstractVoxelAnalysis {

    public CurrentVoxelAnalysis(Raster terrain, VoxelAnalysisCfg cfg, boolean beamSectionEnabled) throws Exception {
        super(terrain, cfg);
        this.beamSectionEnabled = beamSectionEnabled;
    }
    
    public CurrentVoxelAnalysis(Raster terrain, VoxelAnalysisCfg cfg) throws Exception {
        this(terrain, cfg, true);
    }

    private final static Logger LOGGER = Logger.getLogger(CurrentVoxelAnalysis.class);

    private boolean groundEnergySet = false;

    private boolean shotChanged = false;
    private Voxel lastVoxelSampled;
    private int lastShotId;

    private BufferedWriter shotSegmentWriter;
    
    private final int transMode = 1;
    private final int pathLengthMode = 1; //1 = mode A, 2 = mode B
    
    private final boolean beamSectionEnabled;

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
            double beamSurface = beamSectionEnabled
                    ? Math.pow((Math.tan(0.5d * laserSpec.getBeamDivergence()) * distance) + 0.5d * laserSpec.getBeamDiameterAtExit(), 2) * Math.PI
                    : 1.d;

            double beamVolume = 0;
            double beamVolumeIn = 0;
            double beamFractionOut = 0;
            double beamFractionIn = 0;
            double rayLength = 0;

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
                    rayLength = distOriginNextVoxel - distOriginCurrentVoxel;
                    // increment total optical length in current voxel
                    vox.lgTotal += rayLength;
                    // increment number of shots going through current voxel
                    vox.nbSampling++;
                    // increment mean angle in current voxel
                    vox.angleMean += shot.getAngle();
                    // unintercepted beam volume
                    beamVolume = beamSurface * rayLength;
                    // fraction of the beam entering current voxel (rounded to 5 digits)
                    beamFractionIn = (Math.round(residualEnergy * 10000) / 10000.0);
                    // beam volume in current voxel
                    beamVolumeIn = beamFractionIn * beamVolume;
                    // increment total beam volume in current voxel
                    vox.bvEntering += beamVolumeIn;

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

                // rayLength is approximated to full optical path inside current voxel
                // hence overestimated, unless it is last echo and ray length
                // estimation mode is set to 1
                rayLength = (lastEcho && pathLengthMode == 1)
                        ? distOriginEcho - distOriginCurrentVoxel
                        : distOriginNextVoxel - distOriginCurrentVoxel;

                if (shotID == lastShotId && lastVoxelSampled != null && lastVoxelSampled == vox) {
                    // pour n'échantillonner qu'une fois le voxel pour un tir
                    // phv 20180831: do not understand how we can reach this statement
                } else {

                    vox.nbSampling++;
                    vox.lgTotal += rayLength;
                    vox.angleMean += shot.getAngle();
                    beamVolume = beamSurface * rayLength;
                    beamFractionIn = (Math.round(residualEnergy * 10000) / 10000.0);
                    beamVolumeIn = beamFractionIn * beamVolume;
                    vox.bvEntering += beamVolumeIn;
                    lastVoxelSampled = vox;
                    lastShotId = shotID;
                }

                if (keepEcho) {

                    vox.nbEchos++;
                    beamFractionOut = (Math.round(beamFraction * 10000) / 10000.0);
                    beamVolume = beamSurface * rayLength;
                    vox.bvIntercepted += (beamFractionOut * beamVolume);

                } else if (parameters.getGroundEnergyParams() != null
                        && parameters.getGroundEnergyParams().isCalculateGroundEnergy()
                        && parameters.infos.getType() != VoxelSpaceInfos.Type.TLS && !groundEnergySet) {
                    groundEnergy[vox.i][vox.j].groundEnergyActual += residualEnergy;
                    groundEnergy[vox.i][vox.j].groundEnergyPotential++;

                    groundEnergySet = true;
                }
            }

            // additional calculation for transmittance mode 2 and 3
            if (transMode > 1) {
                double transNorm = 0.d;
                switch (transMode) {
                    case 2:
                        transNorm = ((beamFractionIn - beamFractionOut) / beamFractionIn) * beamVolume;
                        break;
                    case 3:
                        if (rayLength > 0) {
                            transNorm = Math.pow(((beamFractionIn - beamFractionOut) / beamFractionIn), 1 / rayLength) * beamVolumeIn;
                        }
                        break;
                }
                vox.transmittance_tmp += transNorm;
                vox.cumulatedBeamVolumIn += beamVolumeIn;
                vox.cumulatedBeamVolume += beamVolume;
                if (cfg.isExportShotSegment()) {
                    double currentNormalizedTrans = transNorm / beamVolumeIn;
                    try {
                        shotSegmentWriter.write(vox.i + " " + vox.j + " " + vox.k + " " + currentNormalizedTrans + " " + beamVolumeIn + "\n");
                    } catch (IOException ex) {
                        LOGGER.error("Error exporting shot segment " + shotID + " for transmittance mode " + transMode, ex);
                    }
                }
            }
        }
    }
    
    @Override
    public double computeTransmittance(Voxel voxel) {
        
        double normalizedTransmittance;
        switch (transMode) {
            case 2:
                normalizedTransmittance = computeNormTransmittanceMode2(voxel.transmittance_tmp, voxel.cumulatedBeamVolume, voxel.lMeanTotal);
                break;
            case 3:
                //normalizedTransmittance = computeNormTransmittanceV2(voxel.transmittance_tmp, voxel.sumSurfMulLength);
                normalizedTransmittance = computeNormTransmittanceMode3(voxel.transmittance_tmp, voxel.cumulatedBeamVolumIn); //CL
                break;

            case 1:
            default:
                double transmittance = computeTransmittance(voxel.bvEntering, voxel.bvIntercepted);
                normalizedTransmittance = computeNormTransmittance(transmittance, voxel.lMeanTotal);
        }
        return normalizedTransmittance;
    }
    
    private double computeTransmittance(double bvEntering, double bvIntercepted) {

        double transmittance;

        if (bvEntering == 0) {

            transmittance = Float.NaN;

        } else if (bvIntercepted > bvEntering) {
            transmittance = Float.NaN;

        } else {
            transmittance = (bvEntering - bvIntercepted) / bvEntering;
        }

        return transmittance;
    }

    private double computeNormTransmittanceMode2(double transmittance, double sumSurfMulLength, double lMeanTotal) {
        double normalizedTransmittance = Math.pow((transmittance / sumSurfMulLength), 1 / lMeanTotal);
        return normalizedTransmittance;
    }

    private double computeNormTransmittanceMode3(double transmittance, double sumSurfMulLength) {
        double normalizedTransmittance = transmittance / sumSurfMulLength;
        return normalizedTransmittance;
    }

    private double computeNormTransmittance(double transmittance, double lMeanTotal) {
        double normalizedTransmittance = Math.pow(transmittance, 1 / lMeanTotal);
        return normalizedTransmittance;
    }
}
