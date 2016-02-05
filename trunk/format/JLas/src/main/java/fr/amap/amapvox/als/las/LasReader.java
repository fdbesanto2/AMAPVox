/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.als.las;

import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.LasHeader11;
import fr.amap.amapvox.als.LasHeader12;
import fr.amap.amapvox.als.LasHeader13;
import fr.amap.commons.util.io.LittleEndianUtility;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.commons.lang3.ArrayUtils;

/**
 * This class is devoted to read a LASer (*.las) file.
 * It allows to get the header in a simple basic format (V1.0) and get an iterator on the points of the file.<br>
 * It uses a native library compiled for 64 bits systems, Linux and Windows.<br><br>
 * 
 * @see <a href="http://www.asprs.org/Committee-General/LASer-LAS-File-Format-Exchange-Activities.html">ASPRS Specification</a>
 * 
 * 
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LasReader implements Iterable<PointDataRecordFormat> {
    
    private File file;
    private ArrayList<VariableLengthRecord> variableLengthRecords;
    private LasHeader header;

    public ArrayList<VariableLengthRecord> getVariableLengthRecords() {
        return variableLengthRecords;
    }

    public LasHeader getHeader() {
        return header;
    }
    
    
    private LasHeader readHeader10(DataInputStream dis, LasHeader header) throws IOException {

        char bytefs1 = (char) dis.readByte();
        char bytefs2 = (char) dis.readByte();
        char bytefs3 = (char) dis.readByte();
        char bytefs4 = (char) dis.readByte();
        char[] fileSignature = new char[]{bytefs1, bytefs2, bytefs3, bytefs4};
        header.setFileSignature(fileSignature);

        int byteGE = dis.readByte() + dis.readByte() + dis.readByte() + dis.readByte();
        header.setReserved(byteGE);

        long pigd1 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setProjectIdGuidData1(pigd1);

        int pigd2 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData2(pigd2);

        int pigd3 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData3(pigd3);

        double pigd4 = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setProjectIdGuidData4(pigd4);

        byte vM = dis.readByte();
        header.setVersionMajor(vM);

        byte vm = dis.readByte();
        header.setVersionMinor(vm);

        char[] si = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setSystemIdentifier(si);

        char[] gs = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setGeneratingSoftware(gs);

        short fcdoy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationDayOfYear(fcdoy);

        short fcy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationYear(fcy);

        short hs = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setHeaderSize(hs);

        long otpd = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setOffsetToPointData(otpd);

        long novlr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfVariableLengthRecords(novlr);

        byte pdfID = dis.readByte();
        header.setPointDataFormatID(pdfID);

        short pdrl = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setPointDataRecordLength(pdrl);

        long nopr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfPointrecords(nopr);

        long nopbyr1 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr2 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr3 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr4 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr5 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});

        long[] nopbyr = new long[]{nopbyr1, nopbyr2, nopbyr3, nopbyr4, nopbyr5};

        header.setNumberOfPointsByReturn(nopbyr);

        double sxf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxScaleFactor(sxf);

        double syf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyScaleFactor(syf);

        double szf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzScaleFactor(szf);

        double xoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxOffset(xoff);

        double yoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyOffset(yoff);

        double zoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzOffset(zoff);

        double maxX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxX(maxX);

        double minX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinX(minX);

        double maxY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxY(maxY);

        double minY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinY(minY);

        double maxZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxZ(maxZ);

        double minZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinZ(minZ);

        return header;
    }

    private LasHeader readHeader11(DataInputStream dis, LasHeader11 header) throws IOException {

        char bytefs1 = (char) dis.readByte();
        char bytefs2 = (char) dis.readByte();
        char bytefs3 = (char) dis.readByte();
        char bytefs4 = (char) dis.readByte();
        char[] fileSignature = new char[]{bytefs1, bytefs2, bytefs3, bytefs4};
        header.setFileSignature(fileSignature);

        int byteFsi = dis.readByte() + dis.readByte();
        header.setFileSourceId(byteFsi);

        int byteGE = dis.readByte() + dis.readByte();
        header.setReserved(byteGE);

        long pigd1 = dis.readByte() + dis.readByte() + dis.readByte() + dis.readByte();
        header.setProjectIdGuidData1(pigd1);

        int pigd2 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData2(pigd2);

        int pigd3 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData3(pigd3);

        double pigd4 = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setProjectIdGuidData4(pigd4);

        byte vM = dis.readByte();
        header.setVersionMajor(vM);

        byte vm = dis.readByte();
        header.setVersionMinor(vm);

        char[] si = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setSystemIdentifier(si);

        char[] gs = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setGeneratingSoftware(gs);

        short fcdoy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationDayOfYear(fcdoy);

        short fcy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationYear(fcy);

        short hs = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setHeaderSize(hs);

        long otpd = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setOffsetToPointData(otpd);

        long novlr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfVariableLengthRecords(novlr);

        byte pdfID = dis.readByte();
        header.setPointDataFormatID(pdfID);

        short pdrl = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setPointDataRecordLength(pdrl);

        long nopr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfPointrecords(nopr);

        long nopbyr1 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr2 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr3 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr4 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr5 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});

        long[] nopbyr = new long[]{nopbyr1, nopbyr2, nopbyr3, nopbyr4, nopbyr5};

        header.setNumberOfPointsByReturn(nopbyr);

        double sxf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxScaleFactor(sxf);

        double syf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyScaleFactor(syf);

        double szf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzScaleFactor(szf);

        double xoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxOffset(xoff);

        double yoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyOffset(yoff);

        double zoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzOffset(zoff);

        double maxX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxX(maxX);

        double minX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinX(minX);

        double maxY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxY(maxY);

        double minY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinY(minY);

        double maxZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxZ(maxZ);

        double minZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinZ(minZ);

        return header;
    }

    private LasHeader readHeader12(DataInputStream dis, LasHeader12 header) throws IOException {

        char bytefs1 = (char) dis.readByte();
        char bytefs2 = (char) dis.readByte();
        char bytefs3 = (char) dis.readByte();
        char bytefs4 = (char) dis.readByte();
        char[] fileSignature = new char[]{bytefs1, bytefs2, bytefs3, bytefs4};
        header.setFileSignature(fileSignature);

        int byteFsi = dis.readByte() + dis.readByte();
        header.setFileSourceId(byteFsi);

        int byteGE = dis.readByte() + dis.readByte();
        header.setGlobalEncoding(byteGE);

        long pigd1 = dis.readByte() + dis.readByte() + dis.readByte() + dis.readByte();
        header.setProjectIdGuidData1(pigd1);

        int pigd2 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData2(pigd2);

        int pigd3 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData3(pigd3);

        double pigd4 = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setProjectIdGuidData4(pigd4);

        byte vM = dis.readByte();
        header.setVersionMajor(vM);

        byte vm = dis.readByte();
        header.setVersionMinor(vm);

        char[] si = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setSystemIdentifier(si);

        char[] gs = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setGeneratingSoftware(gs);

        short fcdoy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationDayOfYear(fcdoy);

        short fcy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationYear(fcy);

        short hs = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setHeaderSize(hs);

        long otpd = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setOffsetToPointData(otpd);

        long novlr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfVariableLengthRecords(novlr);

        byte pdfID = dis.readByte();
        header.setPointDataFormatID(pdfID);

        short pdrl = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setPointDataRecordLength(pdrl);

        long nopr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfPointrecords(nopr);

        long nopbyr1 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr2 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr3 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr4 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr5 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});

        long[] nopbyr = new long[]{nopbyr1, nopbyr2, nopbyr3, nopbyr4, nopbyr5};

        header.setNumberOfPointsByReturn(nopbyr);

        double sxf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxScaleFactor(sxf);

        double syf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyScaleFactor(syf);

        double szf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzScaleFactor(szf);

        double xoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxOffset(xoff);

        double yoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyOffset(yoff);

        double zoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzOffset(zoff);

        double maxX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxX(maxX);

        double minX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinX(minX);

        double maxY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxY(maxY);

        double minY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinY(minY);

        double maxZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxZ(maxZ);

        double minZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinZ(minZ);

        return header;
    }

    private LasHeader readHeader13(DataInputStream dis, LasHeader13 header) throws IOException {

        char bytefs1 = (char) dis.readByte();
        char bytefs2 = (char) dis.readByte();
        char bytefs3 = (char) dis.readByte();
        char bytefs4 = (char) dis.readByte();
        char[] fileSignature = new char[]{bytefs1, bytefs2, bytefs3, bytefs4};
        header.setFileSignature(fileSignature);

        int byteFsi = dis.readByte() + dis.readByte();
        header.setFileSourceId(byteFsi);

        int byteGE = dis.readByte() + dis.readByte();
        header.setGlobalEncoding(byteGE);

        long pigd1 = dis.readByte() + dis.readByte() + dis.readByte() + dis.readByte();
        header.setProjectIdGuidData1(pigd1);

        int pigd2 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData2(pigd2);

        int pigd3 = dis.readByte() + dis.readByte();
        header.setProjectIdGuidData3(pigd3);

        double pigd4 = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setProjectIdGuidData4(pigd4);

        byte vM = dis.readByte();
        header.setVersionMajor(vM);

        byte vm = dis.readByte();
        header.setVersionMinor(vm);

        char[] si = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setSystemIdentifier(si);

        char[] gs = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
            (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

        header.setGeneratingSoftware(gs);

        short fcdoy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationDayOfYear(fcdoy);

        short fcy = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setFileCreationYear(fcy);

        short hs = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setHeaderSize(hs);

        long otpd = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setOffsetToPointData(otpd);

        long novlr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfVariableLengthRecords(novlr);

        byte pdfID = dis.readByte();
        header.setPointDataFormatID(pdfID);

        short pdrl = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
        header.setPointDataRecordLength(pdrl);

        long nopr = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        header.setNumberOfPointrecords(nopr);

        long nopbyr1 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr2 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr3 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr4 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});
        long nopbyr5 = LittleEndianUtility.bytesToLong(new byte[]{dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte()});

        long[] nopbyr = new long[]{nopbyr1, nopbyr2, nopbyr3, nopbyr4, nopbyr5};

        header.setNumberOfPointsByReturn(nopbyr);

        double sxf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxScaleFactor(sxf);

        double syf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyScaleFactor(syf);

        double szf = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzScaleFactor(szf);

        double xoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setxOffset(xoff);

        double yoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setyOffset(yoff);

        double zoff = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setzOffset(zoff);

        double maxX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxX(maxX);

        double minX = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinX(minX);

        double maxY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxY(maxY);

        double minY = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinY(minY);

        double maxZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMaxZ(maxZ);

        double minZ = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
        header.setMinZ(minZ);

        BigInteger startOfWaveformDataPacketRecord = LittleEndianUtility.toBigInteger(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());

        header.setStartOfWaveformDataPacketRecord(startOfWaveformDataPacketRecord);

        return header;
    }

    public LasHeader readHeader(File file) throws IOException, UnsupportedOperationException {

        try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

            /**
             * *read the file version at first**
             */
            dis.mark(30);
            dis.skipBytes(24);
            byte vM = dis.readByte();
            byte vm = dis.readByte();
            dis.reset();

            LasHeader header = null;
            String errorMsg = "";

            if (vM == 1) {
                switch (vm) {
                    case 0:
                        header = new LasHeader();
                        header = readHeader10(dis, header);
                        break;
                    case 1:
                        header = new LasHeader11();
                        header = readHeader11(dis, (LasHeader11) header);
                        break;
                    case 2:
                        header = new LasHeader12();
                        header = readHeader12(dis, (LasHeader12) header);
                        break;
                    case 3:
                        header = new LasHeader13();
                        header = readHeader13(dis, (LasHeader13) header);
                        break;
                    case 4:
                        //header = new LasHeader14();
                        errorMsg = "1.4 format not supported yet";
                        //throw new Exception("1.4 format not supported yet");
                        //header = readHeader14(dis, (LasHeader14)header);
                }
            }
            
            if(header == null){
                throw new UnsupportedOperationException(errorMsg);
            }

            return header;

        } catch (IOException ex) {
            throw ex;
        }

    }

    public static ArrayList<VariableLengthRecord> readVariableLengthRecords(File file, int start, long end, long variableNumber) throws IOException {

        ArrayList<VariableLengthRecord> variableLengthRecords = new ArrayList<>();

        if (variableNumber > 0) {

            try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {

                dis.skipBytes(start);

                for (long i = 0; i < variableNumber; i++) {

                    VariableLengthRecord vlr = new VariableLengthRecord();

                    short reserved = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
                    vlr.setReserved(reserved);

                    char[] userID = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

                    vlr.setUserID(userID);

                    short recordID = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
                    vlr.setRecordID(recordID);

                    short rlah = LittleEndianUtility.bytesToShort(dis.readByte(), dis.readByte());
                    vlr.setRecordLengthAfterHeader(rlah);

                    char[] description = new char[]{(char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(),
                        (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte(), (char) dis.readByte()};

                    vlr.setDescription(description);

                    dis.skipBytes(rlah);
                    variableLengthRecords.add(vlr);
                }

            } catch (IOException ex) {
                throw ex;
            } catch (Exception ex) {
                throw ex;
            }
        }

        return variableLengthRecords;
    }
    
    public void open(File file) throws IOException, Exception{
        
        LasReader reader = new LasReader();
        this.file = file;
        header = reader.readHeader(file);
        variableLengthRecords = readVariableLengthRecords(file, header.getHeaderSize(), header.getOffsetToPointData(), header.getNumberOfVariableLengthRecords());
    }
    

    @Override
    public Iterator<PointDataRecordFormat> iterator(){

        final DataInputStream dis;
        final int offset = header.getPointDataRecordLength();
        final long size = header.getNumberOfPointrecords();
        final long start = header.getOffsetToPointData();
        final int pointFormatID = header.getPointDataFormatID();
        Iterator<PointDataRecordFormat> it;
        
        try {
            dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            dis.skip(start);

            it = new Iterator<PointDataRecordFormat>() {
            
            int count = 0;
            
            @Override
            public boolean hasNext() {
                
                return count < size;
            }

            @Override
            public PointDataRecordFormat next(){

                PointDataRecordFormat pdr = null;

                switch (pointFormatID) {

                    case 0:
                        pdr = new PointDataRecordFormat();
                        break;
                    case 1:
                        pdr = new PointDataRecordFormat1();
                        break;
                    case 2:
                        pdr = new PointDataRecordFormat2();
                        break;
                    case 3:
                        pdr = new PointDataRecordFormat3();
                        break;
                }
                int x;
                try {
                    x = LittleEndianUtility.bytesToInt(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                    
                    pdr.setX(x);
                    int y = LittleEndianUtility.bytesToInt(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                    pdr.setY(y);
                    int z = LittleEndianUtility.bytesToInt(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                    pdr.setZ(z);
                    int intensity = LittleEndianUtility.bytesToShortInt(dis.readByte(), dis.readByte());
                    pdr.setIntensity(intensity);
                    byte b = dis.readByte();
                    int bit0 = (b >> 0) & 1;
                    int bit1 = (b >> 1) & 1;
                    int bit2 = (b >> 2) & 1;
                    int bit3 = (b >> 3) & 1;
                    int bit4 = (b >> 4) & 1;
                    int bit5 = (b >> 5) & 1;
                    int bit6 = (b >> 6) & 1;
                    int bit7 = (b >> 7) & 1;
                    int returnNumber = Integer.parseInt(String.valueOf(bit2) + String.valueOf(bit1) + String.valueOf(bit0), 2);
                    pdr.setReturnNumber((short) returnNumber);
                    int numberOfReturns = Integer.parseInt(String.valueOf(bit5) + String.valueOf(bit4) + String.valueOf(bit3), 2);
                    pdr.setNumberOfReturns((short) numberOfReturns);
                    if (bit6 == 0) {
                        pdr.setScanDirectionFlag(false);
                    } else {
                        pdr.setScanDirectionFlag(true);
                    }
                    if (bit7 == 0) {
                        pdr.setEdgeOfFlightLine(false);
                    } else {
                        pdr.setEdgeOfFlightLine(true);
                    }
                    b = dis.readByte();
                    /*classification bits*/
                    bit0 = (b >> 0) & 1;
                    bit1 = (b >> 1) & 1;
                    bit2 = (b >> 2) & 1;
                    bit3 = (b >> 3) & 1;
                    bit4 = (b >> 4) & 1;
                    /*synthetic*/
                    bit5 = (b >> 5) & 1;
                    /*key-point*/
                    bit6 = (b >> 6) & 1;
                    /*Withheld*/
                    bit7 = (b >> 7) & 1;
                    short classification = (short) Integer.parseInt(
                            String.valueOf(bit4)
                            + String.valueOf(bit3)
                            + String.valueOf(bit2)
                            + String.valueOf(bit1)
                            + String.valueOf(bit0), 2);
                    pdr.setClassification(classification);
                    boolean synthetic = (bit5 != 0);
                    pdr.setSynthetic(synthetic);
                    boolean keyPoint = (bit6 != 0);
                    pdr.setKeyPoint(keyPoint);
                    boolean withheld = (bit7 != 0);
                    pdr.setWithheld(withheld);
                    int sar = dis.readByte();
                    pdr.setScanAngleRank(sar);
                    int usrData = dis.readUnsignedByte();
                    pdr.setUserData(usrData);
                    int pointSrcID = dis.readByte() + dis.readByte();
                    pdr.setPointSourceID(pointSrcID);
                    double gpsTime;
                    int red, green, blue;
                    short length = 0;
                    short difference = 0;
                    switch (pointFormatID) {

                        case 1:
                            gpsTime = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                                    dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                            ((PointDataRecordFormat1) pdr).setGpsTime(gpsTime);

                            length = PointDataRecordFormat1.LENGTH;

                            break;

                        case 2:
                            red = LittleEndianUtility.bytesToShortInt(dis.readByte(), dis.readByte());
                            //red = dis.readByte() + dis.readByte();
                            ((PointDataRecordFormat2) pdr).setRed(red);
                            green = LittleEndianUtility.bytesToShortInt(dis.readByte(), dis.readByte());
                            //green = dis.readByte() + dis.readByte();
                            ((PointDataRecordFormat2) pdr).setGreen(green);
                            blue = LittleEndianUtility.bytesToShortInt(dis.readByte(), dis.readByte());
                            //blue = dis.readByte() + dis.readByte();
                            ((PointDataRecordFormat2) pdr).setBlue(blue);

                            length = PointDataRecordFormat2.LENGTH;

                            break;
                        case 3:
                            gpsTime = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                                    dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                            ((PointDataRecordFormat3) pdr).setGpsTime(gpsTime);
                            red = dis.readByte() + dis.readByte();
                            ((PointDataRecordFormat3) pdr).setRed(red);
                            green = dis.readByte() + dis.readByte();
                            ((PointDataRecordFormat3) pdr).setGreen(green);
                            blue = dis.readByte() + dis.readByte();
                            ((PointDataRecordFormat3) pdr).setBlue(blue);

                            length = PointDataRecordFormat3.LENGTH;

                            break;
                    }
                    difference = (short) (offset - length);
                    if (difference != 0) {

                        byte[] bytes = new byte[difference];

                        for (short j = 0; j < difference; j++) {

                            bytes[j] = dis.readByte();
                        }
                        switch (difference) {
                            case 2:
                                pdr.setExtrabytes((Extrabytes) new QLineExtrabytes(bytes));
                                break;
                            case 3:
                                pdr.setExtrabytes((Extrabytes) new VLineExtrabytes(bytes));
                                break;
                        }
                    }
                
                } catch (IOException ex) {
                }
                
                count++;
                
                return pdr;
            }
        };
        
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
        return it;
    }

    public static ArrayList<PointDataRecordFormat> readPointDataRecords(File file, long start, short offset, long pointNumber, int pointFormatID) throws NumberFormatException, IOException {

        ArrayList<PointDataRecordFormat> pointDataRecords = new ArrayList<>();

        try {

            DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
            PointDataRecordFormat pdr = null;

            dis.skip(start);

            for (long i = 0; i < pointNumber; i++) {

                switch (pointFormatID) {

                    case 0:
                        pdr = new PointDataRecordFormat();
                        break;
                    case 1:
                        pdr = new PointDataRecordFormat1();
                        break;
                    case 2:
                        pdr = new PointDataRecordFormat2();
                        break;
                    case 3:
                        pdr = new PointDataRecordFormat3();
                        break;
                }

                int x = LittleEndianUtility.bytesToInt(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                pdr.setX(x);

                int y = LittleEndianUtility.bytesToInt(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                pdr.setY(y);

                int z = LittleEndianUtility.bytesToInt(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                pdr.setZ(z);

                int intensity = LittleEndianUtility.bytesToShortInt(dis.readByte(), dis.readByte());
                pdr.setIntensity(intensity);

                byte b = dis.readByte();

                int bit0 = (b >> 0) & 1;
                int bit1 = (b >> 1) & 1;
                int bit2 = (b >> 2) & 1;
                int bit3 = (b >> 3) & 1;
                int bit4 = (b >> 4) & 1;
                int bit5 = (b >> 5) & 1;
                int bit6 = (b >> 6) & 1;
                int bit7 = (b >> 7) & 1;

                int returnNumber = Integer.parseInt(String.valueOf(bit2) + String.valueOf(bit1) + String.valueOf(bit0), 2);
                pdr.setReturnNumber((short) returnNumber);

                int numberOfReturns = Integer.parseInt(String.valueOf(bit5) + String.valueOf(bit4) + String.valueOf(bit3), 2);
                pdr.setNumberOfReturns((short) numberOfReturns);

                if (bit6 == 0) {
                    pdr.setScanDirectionFlag(false);
                } else {
                    pdr.setScanDirectionFlag(true);
                }

                if (bit7 == 0) {
                    pdr.setEdgeOfFlightLine(false);
                } else {
                    pdr.setEdgeOfFlightLine(true);
                }

                b = dis.readByte();

                /*classification bits*/
                bit0 = (b >> 0) & 1;
                bit1 = (b >> 1) & 1;
                bit2 = (b >> 2) & 1;
                bit3 = (b >> 3) & 1;
                bit4 = (b >> 4) & 1;

                /*synthetic*/
                bit5 = (b >> 5) & 1;

                /*key-point*/
                bit6 = (b >> 6) & 1;

                /*Withheld*/
                bit7 = (b >> 7) & 1;

                short classification = (short) Integer.parseInt(
                        String.valueOf(bit4)
                        + String.valueOf(bit3)
                        + String.valueOf(bit2)
                        + String.valueOf(bit1)
                        + String.valueOf(bit0), 2);

                pdr.setClassification(classification);

                boolean synthetic = (bit5 != 0);
                pdr.setSynthetic(synthetic);

                boolean keyPoint = (bit6 != 0);
                pdr.setKeyPoint(keyPoint);

                boolean withheld = (bit7 != 0);
                pdr.setWithheld(withheld);

                int sar = dis.readByte();
                pdr.setScanAngleRank(sar);

                int usrData = dis.readUnsignedByte();
                pdr.setUserData(usrData);

                int pointSrcID = dis.readByte() + dis.readByte();
                pdr.setPointSourceID(pointSrcID);

                double gpsTime;
                int red, green, blue;

                short length = 0;
                short difference = 0;

                switch (pointFormatID) {

                    case 1:
                        gpsTime = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                        ((PointDataRecordFormat1) pdr).setGpsTime(gpsTime);

                        length = PointDataRecordFormat1.LENGTH;

                        break;

                    case 2:
                        red = dis.readByte() + dis.readByte();
                        ((PointDataRecordFormat2) pdr).setRed(red);
                        green = dis.readByte() + dis.readByte();
                        ((PointDataRecordFormat2) pdr).setGreen(green);
                        blue = dis.readByte() + dis.readByte();
                        ((PointDataRecordFormat2) pdr).setBlue(blue);

                        length = PointDataRecordFormat2.LENGTH;

                        break;
                    case 3:
                        gpsTime = LittleEndianUtility.toDouble(dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte(),
                                dis.readByte(), dis.readByte(), dis.readByte(), dis.readByte());
                        ((PointDataRecordFormat3) pdr).setGpsTime(gpsTime);
                        red = dis.readByte() + dis.readByte();
                        ((PointDataRecordFormat3) pdr).setRed(red);
                        green = dis.readByte() + dis.readByte();
                        ((PointDataRecordFormat3) pdr).setGreen(green);
                        blue = dis.readByte() + dis.readByte();
                        ((PointDataRecordFormat3) pdr).setBlue(blue);

                        length = PointDataRecordFormat3.LENGTH;

                        break;
                }

                difference = (short) (offset - length);

                if (difference != 0) {

                    byte[] bytes = new byte[difference];

                    for (short j = 0; j < difference; j++) {

                        bytes[j] = dis.readByte();
                    }
                    switch (difference) {
                        case 2:
                            pdr.setExtrabytes((Extrabytes) new QLineExtrabytes(bytes));
                            break;
                        case 3:
                            pdr.setExtrabytes((Extrabytes) new VLineExtrabytes(bytes));
                            break;
                    }
                }

                pointDataRecords.add(pdr);
            }

        } catch (IOException | NumberFormatException ex) {
            throw ex;
        }

        return pointDataRecords;
    }

    public static void writeTxt(Las las) {

    }
    
    public static Las read(File file) throws IOException, Exception {

        LasReader reader = new LasReader();
        LasHeader header = reader.readHeader(file);
        ArrayList<VariableLengthRecord> variableLengthRecords = readVariableLengthRecords(file, header.getHeaderSize(), header.getOffsetToPointData(), header.getNumberOfVariableLengthRecords());
        ArrayList<PointDataRecordFormat> pointDataRecords = readPointDataRecords(file, header.getOffsetToPointData(), header.getPointDataRecordLength(), header.getNumberOfPointrecords(), header.getPointDataFormatID());

        Las las = new Las(header, variableLengthRecords, pointDataRecords);

        return las;
    }

    

    private static byte[] getBin(int decimal) {

        byte[] result = new byte[8];

        for (int i = 0; i < 8; i++) {

            if (decimal - Math.pow(2, 7 - i) > 0) {

                result[i] = 1;
                decimal -= Math.pow(2, 7 - i);

            } else if (decimal - Math.pow(2, 7 - i) == 0) {

                result[i] = 1;
                break;

            }
        }

        return result;

    }

}