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
package fr.amap.lidar.amapvox.update;

import com.dropbox.core.DbxAppInfo;
import com.dropbox.core.DbxClient;
import com.dropbox.core.DbxEntry;
import com.dropbox.core.DbxException;
import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.DbxWebAuthNoRedirect;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Updater {

    private final static Logger logger = Logger.getLogger(Updater.class);
    
    
    //those keys are retrieved in dropbox account
    private final static String APP_KEY = "yv02kehoorehcli";
    private final static String APP_SECRET = "i8z195pbw5gfei3";
    private final static String ACCESS_TOKEN = "PTDN3PzvDt4AAAAAAAAAFyC_4eXTNHpP7XU56ticl1WiKogxDCCMkIdQoRlUloe4";

    private DbxClient client;
    private Map<Date, File> fileList;
    
    public void connect() throws DbxException{
        
        DbxAppInfo appInfo = new DbxAppInfo(APP_KEY, APP_SECRET);
            
        DbxRequestConfig config = new DbxRequestConfig("AMAPVox User",
                Locale.getDefault().toString());
        DbxWebAuthNoRedirect webAuth = new DbxWebAuthNoRedirect(config, appInfo);

        webAuth.start();

        client = new DbxClient(config, ACCESS_TOKEN);

        logger.info("Linked account: " + client.getAccountInfo().displayName);
    }
    
    public void updateFileList() throws DbxException{
        
        DbxEntry.WithChildren listing = client.getMetadataWithChildren("/");
        
        fileList = new TreeMap<>();
            
        for (DbxEntry child : listing.children) {
            if(child.isFile()){
                DbxEntry.File f = child.asFile();
                if(f.name.contains(".zip")){
                    fileList.put(f.lastModified, new File(f.path));
                }
            }
        }
    }

    public Map<Date, File> getFileList() throws DbxException{
        
        if(fileList == null){
            updateFileList();
        }
        
        return fileList;
    }
    
    public File getLastFile() throws DbxException{
        
        if(fileList == null){
            updateFileList();
        }
        
        File lastFile = null;

        for(Entry entry: fileList.entrySet()){
            lastFile = (File) entry.getValue();
        }
        
        return lastFile;
    }
    
    public void update(File file){
        
        try {
                    
            URL myURL = getClass().getProtectionDomain().getCodeSource().getLocation();
            java.net.URI myURI;
            try {
                myURI = myURL.toURI();
            }catch (URISyntaxException e1){
                logger.error("Cannot get current jar file directory", e1);
                return;
            }

            File workingDirectoryFile = new File(Paths.get(myURI).toFile().toString());
            File workingDirectory = new File(workingDirectoryFile.getParent());
            //String workingDirectory = Paths.get(".").toAbsolutePath().normalize().toString();
            File tempZipFile = new File(workingDirectory+"/"+file.getName());
            logger.info("Saving archive file: " + tempZipFile.getAbsolutePath());
            try (FileOutputStream outputStream = new FileOutputStream(tempZipFile)) {
                String filePath = file.getPath().replaceAll("\\\\", "/");
                client.getFile(filePath, null, outputStream);
            }catch(Exception e){
                logger.error("Cannot get file on server", e);
                return;
            }

            logger.info("Extracting archive");
            Charset charset = Charset.forName("ISO-8859-1");

            ZipInputStream  zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(tempZipFile)), charset);
            ZipEntry entry;
            int BUFFER = 2048;
            BufferedOutputStream dest = null;

            while ((entry = zis.getNextEntry()) != null) {

                File entryFile = new File(workingDirectory+"/"+entry.getName());

                if(!entry.isDirectory()){
                    logger.info("Extracting: " + entry);
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
                        logger.error("Cannot write file : "+entryFile.getAbsolutePath(), ex);
                    }catch(Exception ex){
                        logger.error("Cannot write file : "+entryFile.getAbsolutePath(), ex);
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
                logger.info("Removing archive file: " + tempZipFile.getAbsolutePath());
                tempZipFile.delete();
            }catch(SecurityException ex){
                logger.warn("Saving archive file: " + tempZipFile.getAbsolutePath(), ex);
            }
        }catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }catch(Exception ex){
            logger.error(ex);
        }

        
    }
}
