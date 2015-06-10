//package fr.ird.voxelidar.transmittance;
//
//import javax.vecmath.Point2f;
//import javax.vecmath.Vector3f;
//
//
///**
// * A utility class for computing the components of solar radiation.
// * 
// * @author J. Dauzat - May 2012
// */
//public class SolarRadiation {
//
//	/**
//	 * Partitioning of global into direct and diffuse components. Can be used
//	 * for periods of one hour or shorter (from de Jong 1980, cited by Spitters
//	 * et al., 1986)
//	 * 
//	 * @param global
//	 *            in Wm-2
//	 * @param clearness
//	 *            index: global / extra-terrestrial radiation;
//	 * @param sunElevation
//	 *            in radians
//	 */
//	static public void globalPartitioningHourly(IncidentRadiation ir,
//			float clearness, float sunElevation) {
//		float R, K;
//		if (ir.global <= 0) {
//			ir.direct = ir.diffuse = 0;
//			return;
//		}
//		if (sunElevation <= 0.0) {
//			ir.diffuse = ir.global;
//			ir.direct = 0;
//			return;
//		}
//
//		if (clearness <= 0.22) {
//			ir.diffuse = ir.global;
//			ir.direct = 0;
//			return;
//		}
//		if (clearness <= 0.35) {
//			ir.diffuse = (float) (ir.global * (1. - (6.4 * (clearness - 0.22) * (clearness - 0.22))));
//			ir.direct = ir.global - ir.diffuse;
//			return;
//		}
//
//		R = diffuseInGlobalHourlyClear(sunElevation);
//		K = (float) ((1.47 - R) / 1.66);
//
//		if (clearness <= K) {
//			ir.diffuse = (float) (ir.global * (1.47 - (1.66 * clearness)));
//			ir.direct = ir.global - ir.diffuse;
//			return;
//		} else {
//			ir.diffuse = ir.global * R;
//			ir.direct = ir.global - ir.diffuse;
//			return;
//		}
//	}
//
//	/**
//	 * Partitioning of global into direct and diffuse components. Can be used
//	 * for periods of one day or more (from de Jong 1980, cited by Spitters et
//	 * al., 1986)
//	 * 
//	 * @param clearness
//	 *            index: daily global:daily extra-terrestrial radiation
//	 */
//	static public void globalPartitioningDaily(IncidentRadiation ir,
//			float clearness) {
//		if (clearness < 0.07)
//			ir.diffuse = ir.global;
//		else if (clearness < 0.35)
//			ir.diffuse = ir.global * (1 - 2.3f * (clearness - 0.07f));
//		else if (clearness < 0.75)
//			ir.diffuse = ir.global * (1.33f - 1.46f * clearness);
//		else
//			ir.diffuse = ir.global * 0.23f;
//
//		ir.direct = ir.global - ir.diffuse;
//	}
//
//	/**
//	 * Computes directional global fluxes in turtle sectors
//	 * 
//	 * @param sun
//	 */
//	static public void globalInTurtle(IncidentRadiation ir, Sun sun,
//			Turtle turtle) {
//		float[] turtleDirect = new float[turtle.directions.length];
//		float[] turtleDiffuse = new float[turtle.directions.length];
//		ir.directionalGlobals = new float[turtle.directions.length];
//
//		if (ir.global > 0) {
//			float directDir = (float) (ir.direct / Math.cos(sun.zenith));
//			turtleDirect = Sun.directInTurtle(directDir, sun.direction, turtle); // TODO
//			float totalDirect = 0;
//			float totalDiffuse = 0;
//			for (int d = 0; d < turtle.directions.length; d++) {
//				float zenith = (float) turtle.getZenithAngle(d);
//				float azim = (float) turtle.getAzimuthAngle(d);
//				turtleDiffuse[d] = Sky.brightnessNorm(ir.diffuse, ir.global,
//						zenith, azim, sun.zenith, sun.azimuth);
//				float coeff = (float) Math.cos(zenith);
//				// convert to flux as measured on horizontal plane
//				turtleDirect[d] *= coeff;
//				turtleDiffuse[d] *= coeff;
//				totalDirect += turtleDirect[d];
//				totalDiffuse += turtleDiffuse[d];
//			}
//
//			for (int d = 0; d < turtle.directions.length; d++) {
//				turtleDiffuse[d] *= ir.diffuse / totalDiffuse;
//				if (totalDirect > 0)
//					turtleDirect[d] *= ir.direct / totalDirect;
//
//				ir.directionalGlobals[d] = turtleDirect[d] + turtleDiffuse[d];
//			}
//		}
//	}
//
//	// turtle rotated
//	// public void globalInTurtleNew (IncidentRadiation ir, Turtle turtle, Sun
//	// sun) {
//	// // Turtle turtle= new Turtle ();
//	// float[] turtleDirect = null;
//	// float[] turtleDiffuse = new float[46];
//	// ir.directionalGlobal = new float[46];
//	//
//	// if (ir.global > 0) {
//	// float directDir = (float) (ir.direct / Math.sin (sun.zenith));
//	// turtleDirect = sun.directInTurtle46 (turtle, directDir);
//	// float totalDirect = 0;
//	// float totalDiffuse = 0;
//	// for (int d = 0; d < 46; d++) {
//	// float zenith = (float) (Math.PI / 2f) - turtle.elev[d];
//	// float azim = turtle.azimut[d];
//	// // TODO: sampling around central direction
//	// turtleDiffuse[d] = Sky.brightnessNorm (ir.diffuse, ir.global, zenith,
//	// azim, sun.zenith, sun.azimuth);
//	// // "coeff" is applied to get radiation on a horizontal plane
//	// // float coeff= turtle.directions.get(d).getZ();
//	// float coeff = (float) Math.cos (zenith);
//	// totalDirect += turtleDirect[d] * coeff;
//	// totalDiffuse += turtleDiffuse[d] * coeff;
//	// }
//	// // System.out.println (totalDiffuse/this.diffuse);
//	// for (int d = 0; d < 46; d++) {
//	// turtleDiffuse[d] = turtleDiffuse[d] * ir.diffuse / totalDiffuse;
//	// if (totalDirect > 0) turtleDirect[d] = turtleDirect[d] * ir.direct /
//	// totalDirect;
//	//
//	// ir.directionalGlobal[d] = turtleDirect[d] + turtleDiffuse[d];
//	// }
//	// }
//	// }
//
//	/**
//	 * new method with subsampling within sectors
//	 * 
//	 * @param turtle
//	 * @param sun
//	 */
//	// public void globalInTurtle92 (Turtle92 turtle, Sun sun) {
//	// float[] turtleDirect = new float[46];
//	// directionalGlobal = new Float[46];
//	//
//	// if (this.global > 0) {
//	// float directDir = (float) (direct / Math.cos (sun.zenith));
//	// turtleDirect = sun.directInTurtle92 (turtle, directDir);
//	//
//	// /*
//	// * // calculation of sky brightness in sectors float sampleTotal= 0; for
//	// (int s=0; s<46;
//	// * s++) { float sectorBrightness= 0; directionalGlobal[s]= 0f; if
//	// * (turtle.sampling[s].pointList.get(0).z > 0) // upward { float
//	// sampleSolidAngle=
//	// * turtle.solidAngle[s]/turtle.sampling[s].pointList.size(); // Find sky
//	// brightness in
//	// * sampling directions for (int d=0;
//	// d<turtle.sampling[s].pointList.size(); d++) {
//	// * Point3D directionSky= turtle.sampling[s].pointList.get(d); if
//	// (directionSky.z > 0) {
//	// * float zenith= (float) Math.acos(directionSky.z); float azim=
//	// * PolarCoordinates.xyAzimuthTrigo(directionSky.x, directionSky.y); azim
//	// -= Math.PI / 2;
//	// * // azimuth 0 => North
//	// *
//	// * float sampleBrightness= Sky.brightness (this.diffuse, this.global,
//	// zenith, azim,
//	// * sun.zenith, sun.azimuth); // sampleBrightness*= directionSky.z;//XXX
//	// sampleTotal+=
//	// * sampleBrightness * sampleSolidAngle; sectorBrightness+=
//	// sampleBrightness; } }
//	// *
//	// * sectorBrightness /= turtle.sampling[s].pointList.size(); //
//	// sectorBrightness /=
//	// * turtle.directions[s].getZ(); // sectorBrightness /=
//	// * turtle.sampling[s].pointList.get(0).z;//XXX //
//	// System.out.println(sectorBrightness);
//	// * directionalGlobal[s] += sectorBrightness * turtle.solidAngle[s]; }
//	// directionalGlobal[s]+=
//	// * turtleDirect[s]; //System.out.println (directionalGlobal[s]); }
//	// */
//	// for (int s = 0; s < 46; s++) {
//	// directionalGlobal[s] = 0f;
//	// if (turtle.sampling[s].pointList.get (0).z > 0) // upward
//	// {
//	// float sectorBrightness = 0;
//	// float zenith = (float) Math.acos (turtle.sampling[s].pointList.get
//	// (0).z);
//	// float azim = PolarCoordinates.xyAzimuthTrigo
//	// (turtle.sampling[s].pointList.get (0).x,
//	// turtle.sampling[s].pointList.get (0).y);
//	// azim -= Math.PI / 2; // azimuth 0 => North
//	// sectorBrightness = Sky.brightness (this.diffuse, this.global, zenith,
//	// azim, sun.zenith,
//	// sun.azimuth);
//	// directionalGlobal[s] = sectorBrightness * turtle.solidAngle[s];
//	// }
//	// directionalGlobal[s] += turtleDirect[s];
//	// }
//	// }
//	// }
//
//	/**
//	 * Creates an IncidentRadiation object based on a turtle with 46 directions.
//	 * Radiation is integrated over the period time1 to time2.
//	 * 
//	 * @param clearness
//	 *            in [0,1]
//	 */
//	static public IncidentRadiation globalTurtleIntegrate(Turtle t,
//			float latitudeRadian, float clearness, Time time1, Time time2) {
//
//		IncidentRadiation ir = new IncidentRadiation(t.directions.length);
//		ir.setDirections(t.directions);
//
//		Sun sun = new Sun();
//
//		int doy;
//		int doy1 = time1.doy;
//		int doy2 = time2.doy;
//		float hd1, hd2;
//		for (doy = doy1; doy <= doy2; doy++) {
//
//			if (doy == doy1)
//				hd1 = time1.hourDecimal;
//			else
//				hd1 = 0f;
//			if (doy == doy2)
//				hd2 = time2.hourDecimal;
//			else
//				hd2 = 24f;
//
//			float timeStep = 0.5f;
//			float duration = timeStep;
//
//			for (float h = hd1; h < hd2; h += timeStep) {
//				duration = Math.min(timeStep, hd2 - h);
//				float hd = h + (duration / 2);
//				sun.position(latitudeRadian, doy, hd);
//				float globalMJ = clearness
//						* SolarRadiation.extraTerrestrialHourly(latitudeRadian,
//								doy, h, h + duration);
//				if ((globalMJ > 0) && (duration > 0)) {
//					IncidentRadiation radi = new IncidentRadiation(ir.getSize());
//					radi.global = globalMJ * 1000000 / (duration * 3600);
//					globalPartitioningHourly(radi, clearness, sun.elevation);
//					globalInTurtle(radi, sun, t);
//					globalCumulateMJ(ir, radi, duration);
//				}
//			}
//		}
//
//		return ir;
//	}
//
//	/**
//	 * Computes incident
//	 * 
//	 * @param turtle
//	 * @param latitude
//	 *            (degrees)
//	 * @param clearness
//	 *            : clearness index
//	 * @param time1
//	 * @param time2
//	 * @return cumulated radiation in MJ m-2
//	 */
//	// public static SolarRadiation globalInTurtle92Integrate (Turtle92 turtle,
//	// float latitude,
//	// float clearness, Time time1, Time time2) {
//	// Sun sun = new Sun ();
//	// boolean printDetails = false;
//	//
//	// SolarRadiation cumulatedRad = new SolarRadiation ();
//	// // System.out.println (cumulatedRad.toString());
//	//
//	// int doy;
//	// int doy1 = (time1.year * 365) + time1.doy;
//	// int doy2 = (time2.year * 365) + time2.doy;
//	// float hd1, hd2;
//	// // System.out.print("\nDOY ");
//	//
//	// for (doy = doy1; doy <= doy2; doy++) {
//	// // System.out.print(".");
//	// if (doy == time1.doy)
//	// hd1 = time1.hourDecimal;
//	// else
//	// hd1 = 0f;
//	// if (doy == time2.doy)
//	// hd2 = time2.hourDecimal;
//	// else
//	// hd2 = 24f;
//	//
//	// float timeStep = 0.1f;
//	// float duration = timeStep;
//	//
//	// if (printDetails)
//	// System.out.println ("\nFlux density along the period (W m2 s-1)");
//	// else
//	// System.out.print (".");
//	// for (float h = hd1; h < hd2; h += timeStep) {
//	// duration = Math.min (timeStep, hd2 - h);
//	// float hd = h + (duration / 2);
//	// sun.position (latitude, doy, hd);
//	// SolarRadiation radi = new SolarRadiation ();
//	// float globalMJ = clearness * SolarRadiation.extraTerrestrialHourly
//	// (latitude, doy, h, h +
//	// duration);
//	// if (globalMJ > 0) {
//	// if (duration > 0) {
//	// radi.global = globalMJ * 1000000 / (duration * 3600);
//	// }
//	// radi.globalPartitioningHourly (clearness, sun.elevation);
//	// radi.globalInTurtle92 (turtle, sun);
//	// float globalDir = 0;
//	// for (int s = 0; s < 46; s++) {
//	// double cosZenith = Math.cos (turtle.zenith[s]);
//	// if ((radi.global > 0) && (cosZenith > 0)) globalDir +=
//	// radi.directionalGlobal[s] * cosZenith;
//	// }
//	// cumulatedRad.globalCumulateMJ (radi, duration);
//	// } else {
//	// radi.global = radi.direct = radi.diffuse = 0;
//	// }
//	// if (printDetails) {
//	// // System.out.println (radi.toString());
//	// //
//	// System.out.print("D"+doy+" "+Time.hourMinute(h)+" to "+Time.hourMinute(h+duration));
//	// System.out.print ("D" + doy + " " + time1.toString () + " to " +
//	// time2.toString ());
//	// System.out.println ("\tGlobal= " + radi.global + "\t(direct=" +
//	// radi.direct + " diffuse=" +
//	// radi.diffuse + ")");
//	// }
//	//
//	// }
//	// }
//	// // System.out.println
//	// ("rrrrrrrrrrrrrrrrrrrrrrrrrrrrr"+cumulatedRad.toString());
//	//
//	// return cumulatedRad;
//	// }
//
//	/**
//	 * Computes incident
//	 * 
//	 * @param turtle
//	 * @param latitude
//	 *            (degrees)
//	 * @param clearness
//	 *            : clearness index
//	 * @param time1
//	 * @param time2
//	 * @return cumulated radiation in MJ m-2
//	 */
//	// public static SolarRadiation globalInTurtle92Yearly (Turtle92 turtle,
//	// float latitude, float
//	// clearness) {
//	// Time time1 = new Time (0, 0, 0);
//	// Time time2 = new Time (0, 365, 0);
//	// Sun sun = new Sun ();
//	//
//	// SolarRadiation cumulatedRad = new SolarRadiation ();
//	//
//	// int doy;
//	//
//	// float hd1;
//	// float hd2;
//	// // System.out.print("\nDOY ");
//	// int dayStep = 5;
//	// for (doy = time1.doy; doy <= time2.doy; doy += dayStep) {
//	// // System.out.print(".");
//	// if (doy == time1.doy)
//	// hd1 = time1.hourDecimal;
//	// else
//	// hd1 = 0f;
//	// if (doy == time2.doy)
//	// hd2 = time2.hourDecimal;
//	// else
//	// hd2 = 24f;
//	//
//	// float timeStep = 0.2f;
//	// for (float h = hd1; h < hd2; h += timeStep) {
//	// timeStep = Math.min (timeStep, hd2 - h);
//	// float hd = h + (timeStep / 2);
//	// sun.position (latitude, doy, hd);
//	//
//	// SolarRadiation radi = new SolarRadiation ();
//	// float duration = timeStep;
//	// float globalMJ = clearness * SolarRadiation.extraTerrestrialHourly
//	// (latitude, doy, h, h +
//	// duration);
//	// if (duration > 0) radi.global = globalMJ * 1000000 / (duration * 3600);
//	//
//	// radi.globalPartitioningHourly (clearness, sun.elevation);
//	//
//	// radi.globalInTurtle92 (turtle, sun);
//	//
//	// float globalDir = 0;
//	// for (int s = 0; s < 46; s++) {
//	// double cosZenith = Math.cos (turtle.zenith[s]);
//	// if ((radi.global > 0) && (cosZenith > 0)) globalDir +=
//	// radi.directionalGlobal[s] * cosZenith;
//	// }
//	//
//	// cumulatedRad.globalCumulateMJ (radi, duration * dayStep);
//	// }
//	// }
//	//
//	// return cumulatedRad;
//	// }
//
//	/**
//	 * Assess proportion of SOC in diffuse radiation by comparing
//	 * (diffuse/global) to diffuseGlobalHourlyClear (the ratio is between R, for
//	 * clear sky, and 1, for overcast sky (SOC)
//	 * 
//	 * @param sunElevation
//	 *            (radians)
//	 * @return ratio (diffuse SOC : diffuse total)
//	 */
//	// public static float socInDiffuseHourly (IncidentRadiation ir, float
//	// sunElevation) {
//	// float fractionSoc;
//	//
//	// if ((ir.global <= 0) || (sunElevation <= 0)) {
//	// fractionSoc = 0.5f;
//	// return fractionSoc;
//	// }
//	//
//	// float R = diffuseInGlobalHourlyClear (sunElevation);
//	//
//	// fractionSoc = ((ir.diffuse / ir.global) - R) / (1 - R);
//	//
//	// return fractionSoc;
//	// }
//
//	/**
//	 * Assess proportion of SOC in diffuse radiation by comparing
//	 * (diffuse/global) to diffuseGlobalHourlyClear (the ratio is between R, for
//	 * clear sky, and 1, for overcast sky (SOC)
//	 * 
//	 * @param sunElevation
//	 *            (radians)
//	 * @return ratio (diffuse SOC : diffuse total)
//	 */
//	public static float socInDiffuseHourly(float diffuseGlobalRatio,
//			float sunElevation) {
//		float fractionSoc;
//
//		if (sunElevation <= 0) {
//			fractionSoc = 0.5f;
//			return fractionSoc;
//		}
//
//		float R = diffuseInGlobalHourlyClear(sunElevation);
//		fractionSoc = (diffuseGlobalRatio - R) / (1 - R);
//		fractionSoc = Math.max(fractionSoc, 0);
//		// System.out.print("\tdiffuse/global= "+diffuseGlobalRatio+"\tR= "+
//		// R+"\t"+"fraction SOC "+fractionSoc+"\t");
//
//		return fractionSoc;
//	}
//
//	/**
//	 * Assess proportion of SOC in diffuse radiation by comparing
//	 * (diffuse/global) to diffuseGlobalDailyClear ((diffuse/global) is 0.23 for
//	 * clear sky and 1 for overcast sky (SOC)
//	 * 
//	 * @return ratio (diffuse SOC : diffuse total)
//	 */
//	public static float socInDiffuseDaily(IncidentRadiation ir) {
//		float fractionSOC = ((ir.diffuse / ir.global) - 0.23f) / (1 - 0.23f);
//		fractionSOC = Math.max(fractionSOC, 0);
//
//		return fractionSOC;
//	}
//
//	/**
//	 * R is the the ratio (diffuse:global) under clear sky conditions (from de
//	 * Jong 1980, cited by Spitters et al., 1986)
//	 * 
//	 * @param sunElevation
//	 *            (radians)
//	 * @return R= diffuse / global
//	 */
//	private static float diffuseInGlobalHourlyClear(float sunElevation) {
//		double sinSunEl = Math.sin(sunElevation);
//		float R = (float) (0.847 - (1.61 * sinSunEl) + (1.04 * sinSunEl * sinSunEl));
//		return R;
//	}
//
//	public static void globalDirectDiffuseDaily(IncidentRadiation ir, int doy,
//			float clearness, float latitude) {
//		ir.global = clearness * extraTerrestrialDaily(latitude, doy);
//		globalPartitioningDaily(ir, clearness);
//	}
//
//	// public void globalDirectDiffusehourlyOld (float latitude, int doy, float
//	// sunElevation, float
//	// clearness)
//	// {
//	// global= clearness * extraTerrestrialHourlyOld (latitude, doy,
//	// sunElevation);
//	// globalPartitioningHourly (clearness, sunElevation);
//	// }
//
//	// public void globalDirectDiffusehourly (float clearness, float
//	// sunElevation)
//	// {
//	// globalPartitioningHourly (clearness, sunElevation);
//	// }
//
//	// /** Daily incident solar radiation above the atmosphere
//	// * @param doy: Day of Year
//	// * @return average incident flux in Watts m-2
//	// */
//	// static float extraTerrestrialDailyWm2 (int doy)
//	// {
//	// float ext_rad;
//	//
//	// ext_rad = 1370; // solar constant (I.E.A., 1978; in Spitters et al.,
//	// 1986)
//	// ext_rad *= 1.0 + 0.033* Math.cos (2*Math.PI*(float) doy/365);
//	//
//	// return (ext_rad);
//	// }
//
//	/**
//	 * Daily incident solar radiation above the atmosphere
//	 * 
//	 * @param doy
//	 *            : Day of Year
//	 * @return incident flux in MJ m-2
//	 */
//	public static float extraTerrestrialDaily(float latitude, int doy) {
//		float solarCste = 0.0820f; // in MJ m-2 min-1 (<=> SolarCste= 1367 in J
//									// m-2 s-1)
//		double doyAngle = 2 * Math.PI * doy / 365f;
//		double declination = 0.409 * Math.sin(doyAngle - 1.39); // !!!
//		double sun_earth = 1 + (0.033 * Math.cos(doyAngle)); // inverse relative
//																// distance
//		// double sunSetHourAngle= -Math.tan(latitude)*Math.tan(declination);
//		// sunSetHourAngle= Math.max(-1, sunSetHourAngle);
//		// sunSetHourAngle= Math.min( 1, sunSetHourAngle);
//		// sunSetHourAngle= Math.acos(sunSetHourAngle);
//		double sunsetHourAngle = Sun.sunsetHourAngle(latitude,
//				(float) declination);
//
//		double extra_rad;
//		extra_rad = sunsetHourAngle * Math.sin(latitude)
//				* Math.sin(declination);
//		extra_rad += Math.cos(latitude) * Math.cos(declination)
//				* Math.sin(sunsetHourAngle);
//		extra_rad *= (24 * 60 * solarCste * sun_earth / Math.PI);
//
//		return (float) extra_rad;
//	}
//
//	// /** Incident solar radiation above the atmosphere for a given sun
//	// elevation
//	// * @param doy: Day of Year
//	// * @param sunElevation: sun elevation in radians
//	// * @return incident flux in Watts m-2
//	// */
//	// float extraTerrestrialHourlyWm2 (int doy, float sunElevation)
//	// {
//	// return (float) (extraTerrestrialDailyWm2 (doy) * Math.sin
//	// (sunElevation));
//	// }
//
//	// /** Incident solar radiation above the atmosphere for a given sun
//	// elevation
//	// * @param doy: Day of Year
//	// * @param sunElevation: sun elevation in radians
//	// * @return incident flux in Watts m-2
//	// */
//	// float extraTerrestrialHourlyOld (float latitude, int doy, float
//	// sunElevation)
//	// {
//	// return (float) (extraTerrestrialDaily (latitude, doy) * Math.sin
//	// (sunElevation));
//	// }
//
//	/**
//	 * Hourly (or shorter time laps) incident solar radiation above the
//	 * atmosphere
//	 * 
//	 * @param latitude
//	 *            (degrees)
//	 * @param doy
//	 *            : Day of Year
//	 * @param time1
//	 *            , time2: begin && end of period in decimal hour
//	 * @return incident flux in MJ m-2
//	 */
//	public static float extraTerrestrialHourly(float latitudeRadian, int doy,
//			float time1, float time2) {
//		double b = 2 * Math.PI * (doy - 81) / 364.;
//
//		// Sc: seasonal correction for solar time [hour]
//		double Sc = (0.1645 * Math.sin(2 * b)) - (0.1255 * Math.cos(b))
//				- (0.025 * Math.sin(b));
//		double doyAngle = 2 * Math.PI * doy / 365.;
//		double declination = 0.409 * Math.sin(doyAngle - 1.39); // !!!
//
//		double sunSetHAngle = Sun.sunsetHourAngle(latitudeRadian,
//				(float) declination);
//		// solar time angles
//		double sTA1 = (Math.PI / 12.) * (time1 + Sc - 12);
//		double sTA2 = (Math.PI / 12.) * (time2 + Sc - 12);
//
//		sTA1 = Math.max(sTA1, -sunSetHAngle);
//		sTA2 = Math.min(sTA2, sunSetHAngle);
//		if (sTA2 - sTA1 <= 0) {
//			return 0;
//		}
//
//		double solarCste = 0.0820; // in MJ m-2 min-1 (<=> SolarCste= 1367 in J
//									// m-2 s-1)
//		double sun_earth = 1 + (0.033 * Math.cos(doyAngle)); // inverse relative
//																// distance
//
//		double extra_rad;
//		extra_rad = (sTA2 - sTA1) * Math.sin(latitudeRadian)
//				* Math.sin(declination);
//		extra_rad += Math.cos(latitudeRadian) * Math.cos(declination)
//				* (Math.sin(sTA2) - Math.sin(sTA1));
//		extra_rad *= (12 * 60 * solarCste * sun_earth / Math.PI);
//
//		return (float) extra_rad;
//	}
//
//	// public void turtleRadiation (Sun sun)
//	// {
//	// Turtle turtle= new Turtle ();
//	// for (int d=0; d<turtle.directions.size(); d++)
//	// {
//	// directionalFluxes= new Float [turtle.directions.size()];
//	// directionalFluxes[d]= 0f;
//	// }
//	// }
//
//	/**
//	 * Add ir2 components to ir1
//	 */
//	static public void globalCumulateMJ(IncidentRadiation ir1,
//			IncidentRadiation ir2, float durationHd) {
//		// transform Watt s-1 m-2 to MJ m-2 (note: Watt= Joule s-1)
//		float factor = durationHd * 3600 / 1000000;
//		ir1.global += factor * ir2.global;
//		ir1.direct += factor * ir2.direct;
//		ir1.diffuse += factor * ir2.diffuse;
//
//		if (ir2.global > 0) {
//			for (int dir = 0; dir < ir1.directionalGlobals.length; dir++)
//				ir1.directionalGlobals[dir] += factor
//						* ir2.directionalGlobals[dir];
//		}
//
//		// if (ir2.globalTurtle[0] != null)
//		// {
//		// if (this.globalTurtle==null)
//		// {
//		// this.globalTurtle= new Float [ir2.globalTurtle.length];
//		// for (int dir=0; dir<this.globalTurtle.length; dir++)
//		// this.globalTurtle[dir]= 0f;
//		// }
//		//
//		// for (int dir=0; dir<globalTurtle.length; dir++)
//		// {
//		// if ((ir2.globalTurtle[dir]!=null)&&(ir2.globalTurtle[dir]>0)){
//		// this.globalTurtle[dir] += factor * ir2.globalTurtle[dir];
//		// }
//		// }
//		// }
//
//	}
//
//	/**
//	 * This method computes irradiances of 46 directions of diffuse light. The
//	 * direct irradiance is stored in 47 directions of turtle
//	 * 
//	 * @param ir
//	 * @param sun
//	 * @param turtle
//	 */
//	static public void diffuseDirectInTurtle(IncidentRadiation ir, Sun sun,
//			Turtle turtle) {
//		float[] turtleDiffuse = new float[turtle.directions.length];
//		ir.directionalGlobals = new float[turtle.directions.length + 1];
//		if (sun.elevation > 0)
//			ir.directionalGlobals[turtle.directions.length] = (float) (ir.direct);
//		// ir.directionalGlobals[turtle.directions.length] = (float) (ir.direct/
//		// Math.cos (sun.zenith));
//		else
//			ir.directionalGlobals[turtle.directions.length] = 0;
//
//		if (ir.global > 0) {
//
//			float totalDiffuse = 0;
//			for (int d = 0; d < turtle.directions.length; d++) {
//				float zenith = (float) turtle.getZenithAngle(d);
//				float azim = (float) turtle.getAzimuthAngle(d);
//				turtleDiffuse[d] = Sky.brightnessNorm(ir.diffuse, ir.global,
//						zenith, azim, sun.zenith, sun.azimuth);
//				float coeff = (float) Math.cos(zenith);
//
//				turtleDiffuse[d] *= coeff;
//				totalDiffuse += turtleDiffuse[d];
//			}
//			for (int d = 0; d < turtle.directions.length; d++) {
//				turtleDiffuse[d] *= ir.diffuse / totalDiffuse;
//				ir.directionalGlobals[d] = turtleDiffuse[d];
//			}
//		}
//	}
//
//	/**
//	 * Creates an IncidentRadiation object based on a turtle with 47 directions.
//	 * the (n-1)-th first directions are diffuse. The n-th direction is the
//	 * direct. If the direct radiation is null, incident radiation is on 46
//	 * directions. Radiation is integrated over the small period time1 to time2.
//	 * 
//	 * This method corresponds with globalTurtleIntegrate() but time integration
//	 * is outside
//	 * 
//	 * @param latitudeRadian
//	 * @param clearness
//	 * @param time1
//	 * @param time2
//	 * @return
//	 */
//	static public IncidentRadiation seperateDirectFromDiffuseTurtle(Turtle t,
//			float latitudeRadian, float clearness, Time time1, Time time2,
//			float timeStep) {
//
//		IncidentRadiation ir = null;
//		if (time1.doy != time2.doy)
//			System.out
//					.println("Problem, diffuseDirectTurtle47() works if time1.doy == time2.doy");
//		int doy1, doy2;
//
//		float hd1 = time1.hourDecimal;
//		float hd2 = time2.hourDecimal;
//
//		float timeStepLoop = timeStep;
//
//		Sun sun = new Sun();
//		sun.position(latitudeRadian, time1.doy, (hd1 + hd2) * 0.5f);
//
//		// Turtle t = new Turtle(sun.direction);
//		ir = new IncidentRadiation(t.directions.length + 1);
//		for (int i = 0; i < t.directions.length; i++)
//			ir.directions[i] = t.directions[i];
//		ir.directions[t.directions.length] = sun.direction;
//
//		for (int year = time1.year; year <= time2.year; year++) {
//			if (year == time1.year)
//				doy1 = time1.doy;
//			else
//				doy1 = 0;
//			if (year == time2.year)
//				doy2 = time2.doy;
//			else
//				doy2 = 365;
//
//			for (int doy = doy1; doy <= doy2; doy++) {
//				Point2f sunriseSunset = Sun.sunriseSunsetHours(latitudeRadian,
//						doy);
//				if (doy == doy1 && time1.hourDecimal > sunriseSunset.x)
//					hd1 = time1.hourDecimal;
//				else
//					hd1 = sunriseSunset.x;
//				if (doy == doy2 && time2.hourDecimal < sunriseSunset.y)
//					hd2 = time2.hourDecimal;
//				else
//					hd2 = sunriseSunset.y;
//
//				for (float h = hd1; h < hd2; h += timeStep) {
//					float duration = Math.min(timeStepLoop, hd2 - h);
//
//					Sun sunLoop = new Sun();
//					sunLoop.position(latitudeRadian, time1.doy, h + duration
//							* 0.5f);
//
//					IncidentRadiation radi = new IncidentRadiation(
//							t.directions.length + 1);
//
//					float globalMJ = clearness
//							* SolarRadiation.extraTerrestrialHourly(
//									latitudeRadian, doy, h, h + duration);
//					if ((globalMJ > 0) && (duration > 0)) {
//						radi.global = globalMJ * 1000000 / (duration * 3600);
//						globalPartitioningHourly(radi, clearness,
//								sunLoop.elevation);
//						diffuseDirectInTurtle(radi, sunLoop, t);
//						globalCumulateMJ(ir, radi, duration);
//					}
//				}
//			}
//		}
//		return ir;
//	}
//
//	/**
//	 * Creates an IncidentRadiation object based on a turtle with N directions.
//	 * the N-1 first directions correspond to diffuse beam. The Nth direction is
//	 * the direct beam. If the direct direction.z is less than 1E-6, the
//	 * direction is automaticaly set on (0,0,1) Radiation is integrated over the
//	 * small period time1 to time2.
//	 * 
//	 * 
//	 * @param t
//	 *            Turtle
//	 * @param latitudeRadian
//	 *            The latitude on radian
//	 * @param time1
//	 *            timestart
//	 * @param time2
//	 *            timestop
//	 * @param timeStep
//	 * @param rGh
//	 *            Global radiation on horizontal surface
//	 * @param rDh
//	 *            Diffuse radiation on horizontal surface
//	 * @return
//	 */
//	static public IncidentRadiation createTurtleWithRGhAndRDh(Turtle t,
//			float latitudeRadian, Time time1, float timeStep, float rGh,
//			float rDh) {
//
//		IncidentRadiation ir = null;
//
//		float hd1 = time1.hourDecimal;
//
//		float timeStepLoop = timeStep;
//
//		Sun sun = new Sun();
//		sun.position(latitudeRadian, time1.doy, hd1);
//
//		ir = new IncidentRadiation(t.directions.length + 1);
//		for (int i = 0; i < t.directions.length; i++)
//			ir.directions[i] = t.directions[i];
//
//		ir.directions[t.directions.length] = sun.direction;
//		// if (ir.directions[t.directions.length].z < 1E-6)
//		// ir.directions[t.directions.length] = new Vector3f(0, 0, 1);
//
//		float duration = timeStepLoop;
//
//		Sun sunLoop = new Sun();
//		sunLoop.position(latitudeRadian, time1.doy, hd1);
//
//		// PARTITIONNATE DIRECT DIFFUSE FROM EXTRA TERRESTRIAL RADIATION
//		// float cosZen = (float) Math.max(0, Math.cos(sunLoop.zenith));
//		// float globalMJ = SolarRadiation.extraTerrestrialHourly(
//		// latitudeRadian, time1.doy, hd1-timeStep*0.5f, hd1+timeStep*0.5f);
//		//
//		// float clearness = 0;
//		// if (globalMJ!=0 && cosZen != 0)
//		// clearness = (rGh * (duration * 3600/1000000)/cosZen)/globalMJ ;
//		//
//		// IncidentRadiation radiaWithoutRDh = new
//		// IncidentRadiation(t.directions.length + 1);
//		// radiaWithoutRDh.global = rGh;
//		// globalPartitioningHourly(radiaWithoutRDh, clearness,
//		// sunLoop.elevation);
//
//		IncidentRadiation radi = new IncidentRadiation(t.directions.length + 1);
//		radi.global = rGh;
//		radi.diffuse = rDh;
//		radi.direct = radi.global - radi.diffuse;
//		diffuseDirectInTurtle(radi, sunLoop, t);
//		globalCumulateMJ(ir, radi, duration);
//
//		return ir;
//	}
//
//	static public/* IncidentRadiation */void createTurtleWithRGh(
//			float latitudeRadian, Time time, float timeStep, float rGh,
//			float zenith) {
//
//		float extraRad = extraTerrestrialHourly(latitudeRadian, time.doy,
//				time.hourDecimal - timeStep * 0.5f, time.hourDecimal + timeStep
//						* 0.5f);
//		float factor = 1000000 / (timeStep * 3600);
//		extraRad *= factor;
//		// System.out.println("Extraterrestrial Radiation = " + extraRad +
//		// " W/m2");
//		if (zenith < Math.PI * 0.5f) {
//			float kt = (float) (rGh / (extraRad / Math.cos(zenith)));
//			// System.out.println("kT = " + kt);
//
//			float kd = reindlMethod(kt, zenith);
//			// System.out.println("kDReindl (" + Math.toDegrees(zenith) +
//			// "�) = " + kd);
//
//			// Variability :
//			Sun sunA = new Sun();
//			sunA.position(latitudeRadian, time.doy, time.hourDecimal - timeStep
//					* 0.5f);
//			Sun sunB = new Sun();
//			sunB.position(latitudeRadian, time.doy, time.hourDecimal);
//			Sun sunC = new Sun();
//			sunC.position(latitudeRadian, time.doy, time.hourDecimal + timeStep
//					* 0.5f);
//			// float kd2 = skartveitOlsethModel(kt, sunA, sunB, sunC);
//			// System.out.println("kD (SkartveitAndOlset) = " + kd2);
//
//			float kd2 = skartveitAndOlseth1998(kt, sunB);
//			// System.out.println("kD (SkartveitAndOlset) = " + kd2);
//			System.out.print(extraRad + "\t" + kd + "\t" + kd2);
//		}
//	}
//
//	private static double fkt(double kt, double k0, double k1) {
//		double k = 0.5f * (1 + Math.sin(Math.PI
//				* ((kt - k0) / (k1 - k0) - 0.5f)));
//		return k;
//	}
//
//	private static double fktp(double k0, double k1) {
//		double k = 0.5f * (1 + Math.sin(Math.PI
//				* ((1.09 * k1 - k0) / (k1 - k0) - 0.5f)));
//		return k;
//	}
//
//	/**
//	 * @deprecated Skartveit A. and Olseth J.A. 1986 This model tended to
//	 *             overestimate the diffuse fraction in under cloudnless sky
//	 * @param kt
//	 * @param sunA
//	 * @param sunB
//	 * @param sunC
//	 */
//	private static float skartveitOlsethModel(float kt, Sun sunA, Sun sunB,
//			Sun sunC) {
//		double kd = 0;
//		double k0 = 0.2;
//		double elevationInDegrees = Math.toDegrees(sunB.elevation);
//		double k1 = 0.87 - 0.56 * Math.exp(-0.06 * elevationInDegrees); // In
//																		// article
//		double k2 = 1.09 * k1;// In article
//		double d1 = 0.15 + 0.43 * Math.exp(-0.06 * elevationInDegrees);// in
//																		// article
//
//		double k = fkt(kt, k0, k1);
//
//		double kp = fktp(k0, k1);
//		double a = 0.27;
//
//		double alpha = Math.pow(1 / Math.sin(sunB.elevation), 0.6);
//		double kbMax = Math.pow(0.81, alpha);
//		double epsilon = 1 - (1 - d1) * (a * Math.sqrt(kp) + (1 - a) * k * k);
//		double ktmax = (kbMax + k2 * k2) / (1 - k2);
//
//		if (kt <= k0)
//			kd = 1;
//		else if (kt <= k2)
//			kd = 1 - (1 - d1) * (a * Math.sqrt(k) + (1 - a) * k * k);
//		else
//			kd = 1 - k2 * (1 - epsilon) / kt;
//
//		return (float) kd;
//
//	}
//
//	/**
//	 * <i>An Hourly Diffuse Fraction Model With Correction for Variability and
//	 * Surface Albedo</i></br> Skartveit A. and Olseth J.A. and Tuft
//	 * M.E.</br></br> This model tended to overestimate the diffuse fraction in
//	 * under cloudnless sky </br> </br>
//	 * 
//	 * It's composed of four cases. </br> 1 - No significant beam irradiance
//	 * </br> 2 - Brocken clouds</br> 3 - Cloudless skies</br> 4 - Cloudless
//	 * skies</br>
//	 * 
//	 * @param kt
//	 * @param sunA
//	 * @param sunB
//	 * @param sunC
//	 */
//	public static float skartveitAndOlseth1998(float kt, Sun sunB) {
//		// double frac = ();
//		// double sigma3 = Math.sqrt(frac );
//
//		double kd = 0;
//		// double k0 = 0.2;//in 1986
//		double k0 = 0.22;// in 1998
//		double elevationInDegrees = Math.toDegrees(sunB.elevation);
//		double k1 = 0.83 - 0.56 * Math.exp(-0.06 * elevationInDegrees); // in
//																		// 1998
//		// double k1 = 0.87 - 0.56 * Math.exp(-0.06 * elevationInDegrees); //in
//		// 1986
//		double k2 = 0.95 * k1; // in 1998
//		// double k2 = 1.09 * k1;//in 1986
//		double d1 = 0.07 + 0.046 * ((90 - elevationInDegrees) / (elevationInDegrees + 3)); // in
//																							// 1998
//		// double d1 = 0.15 + 0.43 * Math.exp(-0.06 * elevationInDegrees);//in
//		// 1986
//		double sigma = kt / k1;
//		double Delta = computeVariability(kt, sigma, elevationInDegrees);
//		// System.out.print(Delta + "\t");
//		double K = fkt(kt, k0, k1);
//		double Kd = fkt(k2, k0, k1);
//		double d2 = 1 - (1 - d1)
//				* (0.11 * Math.sqrt(Kd) + 0.15 * Kd + 0.74 * Kd * Kd);
//		// double kp = fktp(k0, k1);
//		double alpha = Math.pow(1 / Math.sin(sunB.elevation), 0.6);
//		double kbMax = Math.pow(0.81, alpha);
//		// double epsilon = 1 - (1 - d1) * (a* Math.sqrt(kp) + (1 - a ) * k *
//		// k);
//		double ktmax = ((kbMax + d2 * k2) / (1 - k2))
//				/ ((1 + d2 * k2) / (1 - k2));
//		//
//
//		double dMax = d2 * k2 * (1 - ktmax) / (ktmax * (1 - k2));
//		if (kt <= k0)
//			kd = 1;
//		else if (kt <= k2)
//			kd = 1 - (1 - d1)
//					* (0.11 * Math.sqrt(K) + 0.15f * K + 0.74 * K * K);
//		else if (kt <= ktmax)
//			kd = d2 * k2 * (1 - kt) / (kt * (1 - k2));
//		else
//			kd = 1 - ktmax * (1 - dMax) / kt;
//
//		return (float) kd;
//
//	}
//
//	private static double computeVariability(float kt, double sigma,
//			double elevationInDegrees) {
//		double kx = 0.56 - 0.32 * Math.exp(-0.06 * elevationInDegrees);
//		double kL = (kt - 0.14) / (kx - 0.14);
//		double kR = (kt - kx) / 0.71;
//		double result = 0;
//		if (0.14 <= kt && kt <= kx)
//			result = -3 * kL * kL * (1 - kL) * Math.pow(sigma, 1.3);
//
//		else if (kt <= kx + 0.71)
//			result = 3 * kR * (1 - kR) * (1 - kR) * Math.pow(sigma, 0.6);
//
//		else if (kt <= 0.14 || kt >= kx + 0.71)
//			result = 0;
//
//		return result;
//	}
//
//	/**
//	 * <i>Diffuse fraction correlations</i></br> Reindl D.T., Beckman W.A. and
//	 * Duffie J.A. 1990 </br></br> Based on Liu and Jordan</br> It's composed of
//	 * three cases. </br> 1 - cloudy sky (low clearness index) Constraint kd <=
//	 * 1.0 (to verify) </br> 2 - partly cloudy sky</br> 3 - clear sky weather
//	 * (high clearness index) Constraint kd >= 0.1 (to verify)</br>
//	 * 
//	 * 
//	 * 
//	 * @param kt
//	 *            the portion of horizontal extraterrestrial radiation (= I/(I0
//	 *            cos (Zenith)) clearness index
//	 * @param zenith
//	 *            in radian
//	 * @param phi
//	 *            relative humidity (fraction)
//	 * @param tA
//	 *            ambient temperature
//	 * @return kd the diffuse fraction (the portion of diffuse radiation)
//	 */
//	static public float reindlMethod(float kt, float zenith, float phi, float tA) {
//		double kd = 0;
//		if (kt <= 0.3) {
//			kd = 1.0 - 0.232 * kt + 0.0239 * Math.sin(Math.PI / 2 - zenith)
//					- 0.000682 * tA + 0.0195 * phi;
//			kd = Math.min(1, kd);
//		} else if (kt < 0.78) { // if (kt > 0.3 && kt < 0.78)
//			kd = 1.329 - 1.716 * kt + 0.267 * Math.sin(Math.PI / 2 - zenith)
//					- 0.00357 * tA + 0.106 * phi;
//			kd = Math.max(0.1, kd);
//		} else {// if (kt >= 0.78)
//			kd = 0.426 * kt - 0.256 * Math.sin(Math.PI / 2 - zenith) + 0.00349
//					* tA + 0.0734 * phi;
//			kd = Math.max(0.1, kd);
//		}
//		return (float) kd;
//	}
//
//	/**
//	 * <i>Diffuse fraction correlations</i></br> Reindl D.T., Beckman W.A. and
//	 * Duffie J.A. 1990 </br></br> Based on Liu and Jordan</br> It's composed of
//	 * three cases. </br> 1 - cloudy sky (low clearness index) Constraint kd <=
//	 * 1.0 (to verify) </br> 2 - partly cloudy sky</br> 3 - clear sky weather
//	 * (high clearness index) Constraint kd >= 0.1 (to verify)</br>
//	 * 
//	 * 
//	 * 
//	 * @param kt
//	 *            the portion of horizontal extraterrestrial radiation (= I/(I0
//	 *            cos (Zenith)) clearness index
//	 * @param zenith
//	 *            in radian
//	 * @return kd the diffuse fraction (the portion of diffuse radiation)
//	 */
//	static public float reindlMethod(float kt, float zenith) {
//		double kd = 0;
//		if (kt <= 0.3) {
//			kd = 1.020 - 0.254 * kt + 0.0123 * Math.sin(Math.PI / 2 - zenith);
//			kd = Math.min(1.0, kd);
//		} else if (kt < 0.78) {// if (kt > 0.3 && kt < 0.78)
//			kd = (1.400 - 1.749 * kt + 0.177 * Math.sin(Math.PI / 2 - zenith));
//			kd = Math.max(0.1, kd);
//		} else {
//			kd = 0.486 * kt - 0.182 * Math.sin(Math.PI / 2 - zenith);
//		}
//		return (float) kd;
//	}
//
//	/**
//	 * <i> Application of the Radiosity Approach to the radiation balance in
//	 * Complex terrain - Phd</i></br> Helbig</br></br> Based on Reindl D.T.,
//	 * Beckman W.A. and Duffie J.A. 1990</br> It's composed of three cases.
//	 * </br> 1 - cloudy sky (low clearness index) Constraint kd <= 1.0 (to
//	 * verify) </br> 2 - partly cloudy sky</br> 3 - clear sky weather (high
//	 * clearness index) Constraint kd >= 0.1 (to verify)</br>
//	 * 
//	 * 
//	 * 
//	 * @param kt
//	 *            the portion of horizontal extraterrestrial radiation (= I/(I0
//	 *            cos (Zenith)) clearness index
//	 * @param zenith
//	 *            in radian
//	 * @return kd the diffuse fraction (the portion of diffuse radiation)
//	 */
//	static public float helbigMethod(float kt, float zenith) {
//		double kd = 0;
//		if (kt <= 0.3) {
//			kd = 1.020 - 0.248 * kt;
//			kd = Math.min(1.0, kd);
//		} else if (kt < 0.78) {// if (kt > 0.3 && kt < 0.78)
//			kd = (1.400 - 1.749 * kt + 0.177 * Math.sin(Math.PI / 2 - zenith));
//			kd = Math.max(0.1, kd);
//		} else {
//			kd = 0.147f;
//		}
//		return (float) kd;
//	}
//
//	/**
//	 * <i>Diffuse fraction correlations</i></br> Reindl D.T., Beckman W.A. and
//	 * Duffie J.A. 1990 </br></br> Based on Liu and Jordan</br> It's composed of
//	 * three cases. </br> 1 - cloudy sky (low clearness index) Constraint kd <=
//	 * 1.0 (to verify) </br> 2 - partly cloudy sky</br> 3 - clear sky weather
//	 * (high clearness index) Constraint kd >= 0.1 (to verify)</br>
//	 * 
//	 * 
//	 * 
//	 * @param kt
//	 *            the portion of horizontal extraterrestrial radiation (= I/(I0
//	 *            cos (Zenith)) clearness index
//	 * @return kd the diffuse fraction (the portion of diffuse radiation)
//	 */
//	static public float reindlMethod(float kt) {
//		double kd = 0;
//		if (kt <= 0.3) {
//			kd = 1.020 - 0.248 * kt;
//			kd = Math.min(1.0, kd);
//			// if (kd > 1.0)
//		} else if (kt < 0.78) { // if (kt > 0.3 && kt < 0.78)
//			kd = (1.45 - 1.67 * kt);
//			kd = Math.max(0.1, kd);
//		} else {
//			kd = 0.147f;
//			kd = Math.max(0.1, kd);
//		}
//
//		return (float) kd;
//	}
//
//	/**
//	 * @deprecated Abandonned technique because we need to implement a MCMC.
//	 *             this method takes one minute long.
//	 */
//	static public double bolandRidleyLauret(float kt, Time time,
//			float ktSunrise, float ktSunset, float zenith, float latitudeRadian) {
//
//		double result = 0;
//
//		Point2f sunriseSunset = Sun
//				.sunriseSunsetHours(latitudeRadian, time.doy);
//		double persistenceFactor = 0;
//		if (time.hourDecimal < sunriseSunset.x
//				&& time.hourDecimal > sunriseSunset.y)
//			persistenceFactor = (ktSunrise + ktSunset) * 0.5f;
//		else if (time.hourDecimal == sunriseSunset.x)
//			persistenceFactor = ktSunset;
//		else if (time.hourDecimal == sunriseSunset.y)
//			persistenceFactor = ktSunrise;
//
//		double aS = time.hourDecimal;
//		double phi = zenith;
//		double kT = 0.5f; // Daily clearness index : HOW?
//		result = 1 + Math.exp(-5.32 + 7.28 * kt - 0.03 * aS - 0.0047 * phi
//				+ 1.72 * kT + 1.08 * persistenceFactor);
//		result = 1 / result;
//		return result;
//	}
//
//	/**
//	 * Boland J, Ridley BH, Brown BM. Model of diffuse solar diation Simplified
//	 * model
//	 * 
//	 * @param kt
//	 * @return
//	 */
//	static public float bolandRidleyLauret(float kt) {
//		double result = 0;
//		result = 1 + Math.exp(-5.0033 + 8.6025 * kt);
//		result = 1 / result;
//		return (float) result;
//	}
//
//	/**
//	 * <i> A Quasi-Physical Model for Converting Hourly Global Horizontal to Direct Normal Insolation</i></br>
//	 * Maxwell Eugene L.</br>
//	 * 
//	 * Exponential Model
//	 * @param kt
//	 * @param sun
//	 * @return direct beam from extraterrestrial radiation. </br> In = Kn * I0
//	 *         </br> where In (direct normal irradiance), I0(Extraterrestrial
//	 *         radiation)</br> To get the diffuse fraction just compute kt = 1 -
//	 *         I0 * Kn/I
//	 * 
//	 */
//	static public float maxwellMethod(float kt, Sun sun) {
//		double a, b, c;
//		if (kt <= 0.6) {
//			a = 0.512 - 1.560 * kt + 2.286 * kt * kt - 2.222 * kt * kt * kt;
//			b = 0.370 + 0.962 * kt;
//			c = -0.280 + 0.932 * kt - 2.048 * kt * kt;
//		} else {
//			a = -5.743 + 21.77 * kt - 27.49 * kt * kt + 11.56 * kt * kt * kt;
//			b = 41.4 - 118.5 * kt + 66.05 * kt * kt + 31.9 * kt * kt * kt;
//			c = -47.01 + 184.2 * kt - 222 * kt * kt + 73.81 * kt * kt * kt;
//		}
//		double mair = 1 / (Math.sin(sun.elevation) + 0.50572 / Math.pow(
//				sun.elevation + 6.07995, 1.6364));
//		double knc = 0.866 - 0.122 * mair + 0.0121 * mair * mair - 0.000653
//				* mair * mair * mair + 0.000014 * mair * mair * mair * mair;
//		double deltaKn = a + b * Math.exp(c * mair);
//		double kn = knc - deltaKn;
//
//		return (float) kn;
//	}
//	public static float durr(float kt, Sun sun){
//		double elevationInDegree = Math.toDegrees(sun.elevation);
//		// Optical air mass by Kasten and Young, 1989
//		double m = 1/(Math.sin(sun.elevation) + 0.15 *Math.pow (elevationInDegree + 3.885, -1.253));
//		
//		
//		return (float) m;
//	}
//
//	public static void main(String[] args) {
//		float latitudeRadian = (float) Math.toRadians(43.1f);
//		Sun sunB = new Sun();
//		float hourDecimal = 12;
//		float time1 = hourDecimal - 0.5f;
//		float time2 = hourDecimal + 0.5f;
//		int doy = 180;
//		System.out.println("kt\tkd Skartveit\tkd Reindl1\tkd Reindl2\tkd Helbig\tkd Boland\tkd Maxwell");
//		sunB.position(latitudeRadian, doy, hourDecimal);
//		for (float kt = 0.0f; kt < 1.0f; kt += 0.01) {
//
//			float kd = skartveitAndOlseth1998(kt, sunB);
//			float kd1 = reindlMethod(kt);
//			float kd2 = reindlMethod(kt, sunB.zenith);
//			float kd3 = helbigMethod(kt, sunB.zenith);
//			float kd4 = bolandRidleyLauret(kt);
//			float kN = maxwellMethod(kt, sunB);
//
//			float i0 = extraTerrestrialHourly(latitudeRadian, doy, time1, time2);
//
//			Double i = kt * i0 * Math.cos(sunB.zenith);
//			float kd5 = 1;
//			if (i != 0 && kt>0.2)
//				kd5 = (float) (1 - i0 * kN / i);
//
//			System.out.println(kt + "\t" + kd + "\t" + kd1 + "\t" + kd2 + "\t"
//					+ kd3 + "\t" + kd4 + "\t" + kd5);
//		}
//
//	}
//
//}
