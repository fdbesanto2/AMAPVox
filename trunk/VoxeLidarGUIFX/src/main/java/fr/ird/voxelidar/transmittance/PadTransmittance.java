///**
// *
// */
//package fr.ird.voxelidar.transmittance;
//
//import fr.ird.voxelidar.voxelisation.raytracing.util.BoundingBox3d;
//import java.awt.Color;
//import java.awt.Graphics2D;
//import java.awt.image.BufferedImage;
//import java.io.BufferedReader;
//import java.io.BufferedWriter;
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.StringTokenizer;
//
//import javax.imageio.ImageIO;
//import javax.swing.JDialog;
//import javax.vecmath.Point3d;
//import javax.vecmath.Point3i;
//import javax.vecmath.Vector3d;
//
//import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelSpace;
//import fr.ird.voxelidar.voxelisation.raytracing.voxel.Scene;
//import jeeb.workspace.archimedes.util.ImageView;
///**
// * @author dauzat
// *
// */
//public class PadTransmittance {
//
//    static BufferedReader reader = null;
//    static VoxelSpace voxSpace = null;
//    static TLSVoxel voxels[][][] = null;
//    static Point3d vsMin = new Point3d();
//    static Point3d vsMax = new Point3d();
//    static Point3i splitting = new Point3i();
//    static double transmissionMonth[][][];
//    static float mnt[][];
//    static float mntZmax;
//    static float mntZmin;
//
//    static ArrayList<Point3d> positions;
//
//    private class TLSVoxel {
//
//        float padBV = 0;
//    }
//
//    static PadTransmittance padTr = new PadTransmittance();
//
//    /**
//     * @param args
//     */
//    public static void main(String[] args) {
//
//        boolean images = false;
//        float latitudeRadian = (float) Math.toRadians(4);
//        boolean onlyUpward = true;
//        int scale = 4; // 2 => 46 directions; 3 => 136 directions
//        Turtle turtle = new Turtle(scale, onlyUpward);
//        System.out.println("Turtle built with " + turtle.getNbDirections() + " sectors");
//
//        List<File> fileList = new ArrayList<>();
//
//        for (File file : fileList) {
//
//            System.out.println("===== " + file.getAbsolutePath() + " =====");
//
//            // read data
//            readData(file);
//
//            getSensorPositions();
//
//            // Clearness coefficient
//            float kt[] = {0.356f, 0.312f, 0.394f, 0.389f, 0.331f, 0.349f, 0.443f, 0.470f, 0.490f, 0.508f, 0.454f, 0.383f};
//            ArrayList<Time> day1 = new ArrayList<>();
//            ArrayList<Time> day2 = new ArrayList<>();
//            ArrayList<IncidentRadiation> solRad = new ArrayList<>();
//            for (int m = 0; m < 12; m++) {
//                day1.add(new Time(2015, 1 + (m * 30), 0, 0));
//                day2.add(new Time(2015, (m + 1) * 30, 24, 0));
//                solRad.add(SolarRadiation.globalTurtleIntegrate(turtle, latitudeRadian, kt[m], day1.get(m), day2.get(m)));
//            }
//
//            int n = 0;
//            double transmitted;
//
//            transmissionMonth = new double[splitting.x][][];
//            for (int x = 0; x < splitting.x; x++) {
//                transmissionMonth[x] = new double[splitting.y][];
//                for (int y = 0; y < splitting.y; y++) {
//                    transmissionMonth[x][y] = new double[12];
//                    for (int m = 0; m < 12; m++) {
//                        transmissionMonth[x][y][m] = 0;
//                    }
//                }
//            }
//
//            // TRANSMITTANCE
//            System.out.println("Computation of transmittance");
//            int month = 0;
//
//            IncidentRadiation ir = solRad.get(month);
//			//		for (int t=0; t<turtle.getNbDirections(); t++) {
//            //			ir = solRad.get(month);
//            //			System.out.println("dir"+t+"\t"+ir.directions[t]+"\t"+ir.directionalGlobals[t]);
//            //		}
//
//            for (Point3d pos : positions) {
//                int i = (int) ((pos.x - vsMin.x) / voxSpace.getVoxelSize().x);
//                int j = (int) ((pos.y - vsMin.y) / voxSpace.getVoxelSize().y);
//                //			transmission[i][j] = 0;
//                for (int t = 0; t < turtle.getNbDirections(); t++) {
//                    Vector3d dir = new Vector3d(ir.directions[t]);
//                    dir.normalize();
//
//                    List<Double> distances = distToVoxelWalls(new Point3d(pos), dir);
//                    transmitted = directionalTransmittance(new Point3d(pos), distances, dir);
//                    for (int m = 0; m < 12; m++) {
//                        ir = solRad.get(m);
//                        transmissionMonth[i][j][m] += transmitted * ir.directionalGlobals[t];
//                    }
//                }
//                //			transmission[i][j] /= ir.global;
//                for (int m = 0; m < 12; m++) {
//                    ir = solRad.get(m);
//                    transmissionMonth[i][j][m] /= ir.global;
//                }
//
//                n++;
//
//                if (n % 1000 == 0) {
//                    if (n % 10000 == 0) {
//                        System.out.print(n + "/" + positions.size() + "\n");
//                    } else {
//                        System.out.print(".");
//                    }
//                }
//            }
//
//            File txtFile = new File(file.getAbsolutePath() + ".txt");
//
//            try {
//                writeTransmittance(txtFile);
//            } catch (IOException e1) {
//                e1.printStackTrace();
//            }
//
//            if (images) {
//                BufferedImage bimg = imageTransmitted();
//                File imgFile = new File(file.getAbsolutePath() + ".bmp");
//
//                try {
//                    ImageIO.write(bimg, "bmp", imgFile);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                ImageView view = new ImageView(bimg, "test");
//                view.view(new JDialog());
//                imageMNT();
//            }
//        }
//    }
//
//    private static void readData(File inputFile) {
//
//        FileInputStream file;
//
//        try {
//            file = new FileInputStream(inputFile);
//            reader = new BufferedReader(new InputStreamReader(file));
//        } catch (Exception e) {
//            System.out.println("Can't open file: " + inputFile.getAbsolutePath());
//            System.exit(0);
//        }
//
//        // parse header and create Voxel Space
//        try {
//            voxSpace = parseHeader();
//        } catch (IOException e1) {
//            e1.printStackTrace();
//        }
//
//        try {
//            System.out.println("header: " + reader.readLine());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        String line = null;
//        int nbScans = 0;
//        System.out.println("Data parsing");
//        try {
//            while ((line = reader.readLine()) != null) {
//
//                parseLineData(line);
//                nbScans++;
//                if (nbScans % 100000 == 0) {
//                    if (nbScans % 1000000 == 0) {
//                        System.out.print(" " + nbScans + " shots\n");
//                    } else {
//                        System.out.print(".");
//                    }
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        System.out.println(nbScans + "scans\n");
//
//    }
//
//    private static void parseLineData(String line) throws IOException {
//
//        String[] temps = line.split(" ");
//
//        int i = Integer.valueOf(temps[0]);
//        int j = Integer.valueOf(temps[1]);
//        int k = Integer.valueOf(temps[2]);
//        if (k == 0) {
//            mnt[i][j] = mntZmin - Float.valueOf(temps[7]);
//        }
//
//        if (temps[3].contains("NaN")) {
//            voxels[i][j][k].padBV = 0;
//        } else {
//            voxels[i][j][k].padBV = Float.valueOf(temps[3]);
//        }
//    }
//
//    private static VoxelSpace parseHeader() throws IOException {
//
//        String line;
//        StringTokenizer str;
//        String delimiter = " ";
//
//        do {
//            line = reader.readLine();
//            if (line.startsWith("#min_corner")) {
//                str = new StringTokenizer(line, delimiter);
//                str.nextToken().toString();
//                vsMin.x = Float.parseFloat(str.nextToken());
//                vsMin.y = Float.parseFloat(str.nextToken());
//                vsMin.z = Float.parseFloat(str.nextToken());
//            }
//            if (line.startsWith("#max_corner")) {
//                str = new StringTokenizer(line, delimiter);
//                str.nextToken().toString();
//                vsMax.x = Float.parseFloat(str.nextToken());
//                vsMax.y = Float.parseFloat(str.nextToken());
//                vsMax.z = Float.parseFloat(str.nextToken());
//            }
//            if (line.startsWith("#split")) {
//                str = new StringTokenizer(line, delimiter);
//                str.nextToken().toString();
//                splitting.x = Integer.parseInt(str.nextToken());
//                splitting.y = Integer.parseInt(str.nextToken());
//                splitting.z = Integer.parseInt(str.nextToken());
//            }
//        } while (line.startsWith("#type") != true);
//
//        System.out.println("VOXEL SPACE");
//        System.out.println("Min corner: " + vsMin);
//        System.out.println("Max corner: " + vsMax);
//        System.out.println("Splitting: " + splitting);
//
//        createVoxelTable();
//        allocateMNT();
//
//        Scene scene = new Scene();
//        scene.setBoundingBox(new BoundingBox3d(vsMin, vsMax));
//
//        return new VoxelSpace(new BoundingBox3d(vsMin, vsMax), splitting, 0);
//    }
//
//    public static List<Double> distToVoxelWalls(Point3d origin, Vector3d direction) {
//
//        // coordinates in voxel units
//        Point3d min = new Point3d(voxSpace.getBoundingBox().min);
//        Point3d max = new Point3d(voxSpace.getBoundingBox().max);
//        Point3d voxSize = new Point3d();
//        voxSize.x = (max.x - min.x) / (double) voxSpace.getSplitting().x;
//        voxSize.y = (max.y - min.y) / (double) voxSpace.getSplitting().y;
//        voxSize.z = (max.z - min.z) / (double) voxSpace.getSplitting().z;
//
//        // point where the ray exits form the top of the bounding box
//        Point3d exit = new Point3d(direction);
//        double dist = (max.z - origin.z) / direction.z;
//        exit.scale(dist);
//        exit.add(origin);
////		System.out.println("exit: "+exit);
//
//        Point3i o = new Point3i((int) ((origin.x - min.x) / voxSize.x), (int) ((origin.y - min.y) / voxSize.y), (int) ((origin.z - min.z) / voxSize.z));
//        Point3i e = new Point3i((int) ((exit.x - min.x) / voxSize.x), (int) ((exit.y - min.y) / voxSize.y), (int) ((exit.z - min.z) / voxSize.z));
//
//        List<Double> distances = new ArrayList<Double>();
//
//        Vector3d oe = new Vector3d(exit);
//        oe.sub(origin);
//        distances.add(0.0);
//        distances.add(oe.length());
//
//        // voxel walls in X
//        int minX = Math.min(o.x, e.x);
//        int maxX = Math.max(o.x, e.x);
//        for (int m = minX; m < maxX; m++) {
//            double dx = (m + 1) * voxSize.x;
//            dx += min.x - origin.x;
//            dx /= direction.x;
//            distances.add(dx);
//        }
//
//        // voxel walls in Y
//        int minY = Math.min(o.y, e.y);
//        int maxY = Math.max(o.y, e.y);
//        for (int m = minY; m < maxY; m++) {
//            double dy = (m + 1) * voxSize.y;
//            dy += min.y - origin.y;
//            dy /= direction.y;
//            distances.add(dy);
//        }
//
//        // voxel walls in Z
//        int minZ = Math.min(o.z, e.z);
//        int maxZ = Math.max(o.z, e.z);
//        for (int m = minZ; m < maxZ; m++) {
//            double dz = (m + 1) * voxSize.z;
//            dz += min.z - origin.z;
//            dz /= direction.z;
//            distances.add(dz);
//        }
//
//        Collections.sort(distances);
//
//        return distances;
//    }
//
//    public static double directionalTransmittance(Point3d origin, List<Double> distances, Vector3d direction) {
//
//        Point3d min = new Point3d(voxSpace.getBoundingBox().min);
//        Point3d max = new Point3d(voxSpace.getBoundingBox().max);
//        Point3d voxSize = new Point3d();
//        voxSize.x = (max.x - min.x) / (double) voxSpace.getSplitting().x;
//        voxSize.y = (max.y - min.y) / (double) voxSpace.getSplitting().y;
//        voxSize.z = (max.z - min.z) / (double) voxSpace.getSplitting().z;
//        double dMoy;
//        Point3d pMoy;
//
//        double d1 = 0;
//        double transmitted = 1;
//        for (Double d2 : distances) {
//            double pathLength = d2 - d1;
//            dMoy = (d1 + d2) / 2.0;
//            pMoy = new Point3d(direction);
//            pMoy.scale(dMoy);
//            pMoy.add(origin);
//            pMoy.sub(min);
//            int i = (int) (pMoy.x / voxSize.x);
//            int j = (int) (pMoy.y / voxSize.y);
//            int k = (int) (pMoy.z / voxSize.z);
//
//            // no toricity option (rajouter des modulo pour g\E9rer l'option "torique"
//            if (i < 0 || j < 0 || k < 0 || i >= splitting.x || j >= splitting.y || k >= splitting.z) {
//                break;
//            }
//
//            // Test if current voxel is below the ground level
//            if (pMoy.z < mnt[i][j]) {
//                transmitted = 0;
//            } else {
//                transmitted *= Math.exp(-0.5 * voxels[i][j][k].padBV * pathLength);
//            }
//            d1 = d2;
//        }
//
//        return transmitted;
//    }
//
//    private static void getSensorPositions() {
//        positions = new ArrayList<>();
//
//		// whole plot
////		for (int i=0; i<voxSpace.getSplitting().x; i++) {
////			float tx = (0.5f + (float) i) * voxSpace.getVoxelSize().x;
////			for (int j=0; j<voxSpace.getSplitting().y; j++) {
////				float ty = (0.5f + (float) j) * voxSpace.getVoxelSize().y;
////				Point3f pos = new Point3f(vsMin);
////				pos.add(new Point3f(tx, ty, mnt[i][j]+0.1f));
////				positions.add (pos);
////			}
////		}
//        // Smaller plot at center
//        int size = 10;	// 
//        for (int i = (splitting.x / 2) - size; i < (splitting.x / 2) + size; i++) {
//            double tx = (0.5 + (double) i) * voxSpace.getVoxelSize().x;
//            for (int j = (splitting.y / 2) - size; j < (splitting.y / 2) + size; j++) {
//                double ty = (0.5f + (double) j) * voxSpace.getVoxelSize().y;
//                Point3d pos = new Point3d(vsMin);
//                pos.add(new Point3d(tx, ty, mnt[i][j] + 0.1f));
//                positions.add(pos);
//            }
//        }
//
//        System.out.println("nb positions= " + positions.size());
//    }
//
//    public IncidentRadiation incidentRadiation() {
//        float latitudeRadian = (float) Math.toRadians(4);
//        float clearness = 0.5f;
//        Time time1 = new Time(2015, 180, 6, 0);	// (year, int doy, int hour, int minutes);
//        Time time2 = new Time(2015, 180, 20, 0);	// (year, int doy, int hour, int minutes);
//        boolean onlyUpward = true;
//        int scale = 1; // 2 => 46 directions; 3 => 136 directions
//        Turtle turtle = new Turtle(scale, onlyUpward);
//        IncidentRadiation ir = SolarRadiation.globalTurtleIntegrate(turtle, latitudeRadian, clearness, time1, time2);
//        System.out.println("Turtle built with " + turtle.getNbDirections() + " sectors");
//        for (int t = 0; t < turtle.getNbDirections(); t++) {
//            System.out.println("dir" + t + "\t" + ir.directions[t] + "\t" + ir.directionalGlobals[t]);
//        }
//
//        return ir;
//    }
//
//    public static void createVoxelTable() {
//
//        voxSpace = new VoxelSpace(new BoundingBox3d(vsMin, vsMax), splitting, 0);
//
//        // allocate voxels
//        System.out.println("allocate Voxels");
//        voxels = new TLSVoxel[splitting.x][][];
//        for (int x = 0; x < splitting.x; x++) {
//            voxels[x] = new TLSVoxel[splitting.y][];
//            for (int y = 0; y < splitting.y; y++) {
//                voxels[x][y] = new TLSVoxel[splitting.z];
//                for (int z = 0; z < splitting.z; z++) {
//                    voxels[x][y][z] = padTr.new TLSVoxel();
//                }
//            }
//        }
//    }
//
//    public static void allocateMNT() {
//
//        // allocate MNT
//        System.out.println("allocate MNT");
//        mnt = new float[splitting.x][];
//        for (int x = 0; x < splitting.x; x++) {
//            mnt[x] = new float[splitting.y];
//            for (int y = 0; y < splitting.y; y++) {
//                mnt[x][y] = (float) vsMin.z;
//            }
//        }
//    }
//
//    private static void writeTransmittance(File file) throws IOException {
//
//        FileWriter fw = new FileWriter(file.getAbsoluteFile());
//        BufferedWriter bw = new BufferedWriter(fw);
//        bw.write("Voxel space\n");
//        bw.write("  min corner:\t" + vsMin + "\n");
//        bw.write("  max corner:\t" + vsMax + "\n");
//        bw.write("  splitting:\t" + splitting + "\n\n");
//
//        bw.write("index X\tindex Y\tJanuary\tFebruary\tMarch\tApril\tMay\tJune\tJuly\tAugust\tSeptember\tOctober\tNovember\tDecember\n");
//        float mean[] = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
//        int size = 10;	// 
//        int n = 0;
//        for (int i = (splitting.x / 2) - size; i < (splitting.x / 2) + size; i++) {
//            for (int j = (splitting.y / 2) - size; j < (splitting.y / 2) + size; j++) {
//                bw.write(i + "\t" + j);
//                n++;
//                for (int m = 0; m < 12; m++) {
//                    bw.write("\t" + transmissionMonth[i][j][m]);
//                    mean[m] += transmissionMonth[i][j][m];
//                }
//                bw.write("\n");
//            }
//        }
//        float yearlyMean = 0;
//        bw.write("\nmonthly\tMEAN");
//        for (int m = 0; m < 12; m++) {
//            mean[m] /= (float) n;
//            bw.write("\t" + mean[m]);
//            yearlyMean += mean[m];
//        }
//        yearlyMean /= 12;
//        bw.write("\nYEARLY\tMEAN\t" + yearlyMean);
//        bw.write("\n");
//        bw.close();
//    }
//
//    public static BufferedImage imageTransmitted() {
//
//        boolean test = false;
//        int zoom = 2;
//
//        BufferedImage bimg = new BufferedImage(splitting.x * zoom, splitting.y * zoom, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = bimg.createGraphics();
//
//        // background
//        g.setColor(new Color(80, 30, 0));
//        g.fillRect(0, 0, splitting.x * zoom, splitting.y * zoom);
//
//        if (!test) {
//            for (int i = 0; i < splitting.x; i++) {
//                for (int j = 0; j < splitting.y; j++) {
//                    float col = (float) (transmissionMonth[i][j][0] / 0.1);
//                    col = Math.min(col, 1);
//                    g.setColor(Colouring.rainbow(col));
//                    int jj = splitting.y - j - 1;
//                    g.fillRect(i * zoom, jj * zoom, zoom, zoom);
//                }
//            }
//        }
//
//        // test image PAD
//        if (test) {
//            double density[][] = new double[splitting.x][];
//            for (int i = 0; i < splitting.x; i++) {
//                density[i] = new double[splitting.y];
//            }
//            for (int i = 0; i < splitting.x; i++) {
//                for (int j = 0; j < splitting.y; j++) {
//                    for (int k = 0; k < splitting.z; k++) {
//                        density[i][j] += voxels[i][j][k].padBV;
//                    }
//                }
//            }
//            double maxDensity = 0;
//            for (int i = 0; i < splitting.x; i++) {
//                for (int j = 0; j < splitting.y; j++) {
//                    maxDensity = Math.max(maxDensity, density[i][j]);
//                }
//            }
//            System.out.println("max density: " + maxDensity);
//            for (int i = 0; i < splitting.x; i++) {
//                for (int j = 0; j < splitting.y; j++) {
//                    float col = (float) (density[i][j] / maxDensity);
//                    col = Math.min(col, 1);
//                    g.setColor(Colouring.rainbow(col));
//                    int jj = splitting.y - j - 1;
//                    g.fillRect(i * zoom, jj * zoom, zoom, zoom);
//                }
//            }
//        }
//
//        return bimg;
//    }
//
//    public static void imageMNT() {
//
//        int zoom = 2;
//
//        BufferedImage bimg = new BufferedImage(splitting.x * zoom, splitting.y * zoom, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = bimg.createGraphics();
//
//        mntZmin = mnt[0][0];
//        mntZmax = mnt[0][0];
//        for (int i = 0; i < splitting.x; i++) {
//            for (int j = 0; j < splitting.y; j++) {
//                mntZmin = Math.min(mntZmin, mnt[i][j]);
//                mntZmax = Math.max(mntZmax, mnt[i][j]);
//            }
//        }
//
//        System.out.println("MNTmin:\t" + mntZmin + "\t(VSmin= " + vsMin.z + ")");
//        System.out.println("MNTmax:\t" + mntZmax + "\t(VSmax= " + vsMax.z + ")");
//
//        // background
//        g.setColor(new Color(80, 30, 0));
//        g.fillRect(0, 0, splitting.x * zoom, splitting.y * zoom);
//
//        for (int i = 0; i < splitting.x; i++) {
//            for (int j = 0; j < splitting.y; j++) {
//                float col = ((mnt[i][j] - mntZmin) / (mntZmax - mntZmin));
//                col = Math.min(col, 1);
//                g.setColor(Colouring.rainbow(col));
//                int jj = splitting.y - j - 1;
//                g.fillRect(i * zoom, jj * zoom, zoom, zoom);
//            }
//        }
//
//        String fileName = "mnt" + ".bmp";
//        ImageView view = new ImageView(bimg, fileName);
//        view.view(new JDialog());
//    }
//
//}
