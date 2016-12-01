/*
 This software is distributed WITHOUT ANY WARRANTY and without even the
 implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

 This program is open-source LGPL 3 (see copying.txt).
 Authors:
 Gregoire Vincent    gregoire.vincent@ird.fr
 Julien Heurtebize   julienhtbe@gmail.com
 Jean Dauzat         jean.dauzat@cirad.fr
 Rémi Cresson        cresson.r@gmail.com

 For further information, please contact Gregoire Vincent.
 */
package fr.amap.lidar.amapvox.gui.update;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Updater {

    private final static Logger LOGGER = Logger.getLogger(Updater.class);
    
    private Map<Date, ProgramDetail> fileList;
    private List<URL> changeLogs;
        
    public void updateFileList() {
        
        fileList = new TreeMap<>();
        changeLogs = new ArrayList<>();
        
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy KK:mm aa");
                
        Document doc;
        try {
            doc = Jsoup.connect("http://amap-dev.cirad.fr/projects/voxelidar/files").get();
            Element listFilesElement = doc.getElementsByClass("list files").get(0);
            
            Elements elements = listFilesElement.getElementsByTag("tbody").get(0).getElementsByTag("tr");
            
            for(int i = 0;i<elements.size();i++){
                
                Element element = elements.get(i);
                Element fileNameElement = element.getElementsByClass("filename").get(0);
                Element aElement = fileNameElement.getElementsByTag("a").get(0);
                String name = aElement.ownText();
                
                String link = "http://amap-dev.cirad.fr"+aElement.attr("href");
                
                Date creationDate = sdf.parse(element.getElementsByClass("created_on").get(0).ownText());
                
                if(name.endsWith(".zip")){
                    fileList.put(creationDate, new ProgramDetail(creationDate, new URL(link), ""));
                }else if(name.endsWith(".changes")){
                    changeLogs.add(new URL(link));
                }
            }
            
        } catch (IOException | ParseException ex) {
            LOGGER.error(ex);
        }
        
        Iterator<Entry<Date, ProgramDetail>> iterator = fileList.entrySet().iterator();
        
        while(iterator.hasNext()){
            Entry<Date, ProgramDetail> entry = iterator.next();
            
            String fileName = getFilenameFromURL(entry.getValue().getUrl());
            String fileNameWithoutExt = fileName.substring(0, fileName.length()-4);
            
            for(URL changeLog : changeLogs){
                if(getFilenameFromURL(changeLog).equals(fileNameWithoutExt+".changes")){
                    
                    try {
                        //download change log
                        File tempFile = File.createTempFile("amapvox_", "_changelog");
                        
                        downloadFile(changeLog, tempFile);
                        
                        StringBuilder builder;
                        try (BufferedReader reader = new BufferedReader(new FileReader(tempFile))) {
                            String line;
                            builder = new StringBuilder();
                            while((line = reader.readLine()) != null){
                                builder.append(line).append("\n");
                            }
                        }
                        
                        entry.getValue().setChangeLog(builder.toString());
                       
                    } catch (IOException ex) {
                        LOGGER.error(ex);
                    }
                }
            }
        }
    }
    
    public static String getFilenameFromURL(URL url){
        String path = url.getPath();
        return url.getPath().substring(path.lastIndexOf("/")+1);
    }
    
    private void downloadFile(URL url, File outputFile){
        
        try {
            ReadableByteChannel rbc = Channels.newChannel(url.openStream());
            FileOutputStream fos = new FileOutputStream(outputFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        } catch (MalformedURLException ex) {
            java.util.logging.Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Updater.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Map<Date, ProgramDetail> getFileList() throws DbxException{
        
        updateFileList();
        
        return fileList;
    }
    
    public void update(URL url){
        
        try {
                    
            URL myURL = getClass().getProtectionDomain().getCodeSource().getLocation();
            java.net.URI myURI;
            try {
                myURI = myURL.toURI();
            }catch (URISyntaxException e1){
                LOGGER.error("Cannot get current jar file directory", e1);
                return;
            }

            File workingDirectoryFile = new File(Paths.get(myURI).toFile().toString());
            File workingDirectory = new File(workingDirectoryFile.getParent());
            //String workingDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
            
            File tempZipFile = new File(workingDirectory+"/"+getFilenameFromURL(url));
            
            LOGGER.info("Saving archive file: " + tempZipFile.getAbsolutePath());
            
            downloadFile(url, tempZipFile);

            LOGGER.info("Extracting archive");
            Charset charset = Charset.forName("ISO-8859-1");

            ZipInputStream  zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(tempZipFile)), charset);
            ZipEntry entry;
            int BUFFER = 2048;
            BufferedOutputStream dest = null;

            while ((entry = zis.getNextEntry()) != null) {

                File entryFile = new File(workingDirectory+"/"+entry.getName());

                if(!entry.isDirectory()){
                    LOGGER.info("Extracting: " + entry);
                    int count;
                    byte data[] = new byte[BUFFER];
                    // write the files to the disk
                    try(FileOutputStream fos = new FileOutputStream(entryFile)){

                        dest = new BufferedOutputStream(fos, BUFFER);

                        while ((count = zis.read(data, 0, BUFFER)) != -1) {
                            dest.write(data, 0, count);
                        }

                        dest.flush();


                    }catch(IOException ex){
                        LOGGER.error("Cannot write file : "+entryFile.getAbsolutePath(), ex);
                    }catch(Exception ex){
                        LOGGER.error("Cannot write file : "+entryFile.getAbsolutePath(), ex);
                    }finally{
                        if(dest != null){
                            dest.close();
                        }
                    }

                }else{
                    entryFile.mkdirs();
                }

            }

            zis.close();

            try{
                LOGGER.info("Removing archive file: " + tempZipFile.getAbsolutePath());
                tempZipFile.delete();
            }catch(SecurityException ex){
                LOGGER.warn("Saving archive file: " + tempZipFile.getAbsolutePath(), ex);
            }
        }catch (FileNotFoundException ex) {
            LOGGER.error(ex);
        } catch (IOException ex) {
            LOGGER.error(ex);
        }catch(Exception ex){
            LOGGER.error(ex);
        }

        
    }
}
