/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.commons.javafx.io.FileChooserContext;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize
 */
public class VoxelSpaceCroppingFrameController implements Initializable {

    private File voxelFile;
    
    @FXML
    private Spinner<Integer> spinnerEnterIMin;
    @FXML
    private Spinner<Integer> spinnerEnterIMax;
    @FXML
    private Spinner<Integer> spinnerEnterJMin;
    @FXML
    private Spinner<Integer> spinnerEnterJMax;
    @FXML
    private Spinner<Integer> spinnerEnterKMin;
    @FXML
    private Spinner<Integer> spinnerEnterKMax;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        
    }
    
    public void setVoxelFile(final File voxelFile) throws Exception{
        
        try {
            VoxelFileReader reader = new VoxelFileReader(voxelFile);
            VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();

            spinnerEnterIMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, infos.getSplit().x-1, 0, 1));
            spinnerEnterJMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, infos.getSplit().y-1, 0, 1));
            spinnerEnterKMin.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, infos.getSplit().z-1, 0, 1));

            spinnerEnterIMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, infos.getSplit().x-1, infos.getSplit().x-1, 1));
            spinnerEnterJMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, infos.getSplit().y-1, infos.getSplit().y-1, 1));
            spinnerEnterKMax.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, infos.getSplit().z-1, infos.getSplit().z-1, 1));

            this.voxelFile = voxelFile;
            
        } catch (Exception ex) {
            throw ex;
        }
    }

    @FXML
    private void onActionButtonAreaExtractingWriteNewFile(ActionEvent event) {
        
        try {
            VoxelFileReader reader = new VoxelFileReader(voxelFile, true);
            VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();

            int iMin = Integer.valueOf(spinnerEnterIMin.getEditor().getText());
            int jMin = Integer.valueOf(spinnerEnterJMin.getEditor().getText());
            int kMin = Integer.valueOf(spinnerEnterKMin.getEditor().getText());

            int iMax = Integer.valueOf(spinnerEnterIMax.getEditor().getText());
            int jMax = Integer.valueOf(spinnerEnterJMax.getEditor().getText());
            int kMax = Integer.valueOf(spinnerEnterKMax.getEditor().getText());

            int iSplit = iMax - iMin + 1;
            int jSplit = jMax - jMin + 1;
            int kSplit = kMax - kMin + 1;

            infos.setMinCorner(new Point3d(
                    infos.getMinCorner().x + iMin * infos.getResolution(),
                    infos.getMinCorner().y + jMin * infos.getResolution(),
                    infos.getMinCorner().z + kMin * infos.getResolution()));

            infos.setMaxCorner(new Point3d(
                    infos.getMaxCorner().x - ((infos.getSplit().x - iMax - 1)*infos.getResolution()),
                    infos.getMaxCorner().y - ((infos.getSplit().y - jMax - 1)*infos.getResolution()),
                    infos.getMaxCorner().z - ((infos.getSplit().z - kMax - 1)*infos.getResolution())));

            infos.setSplit(new Point3i(iSplit, jSplit, kSplit));

            FileChooserContext fc = new FileChooserContext();
            fc.fc.setInitialDirectory(voxelFile.getParentFile());
            
            File selectedFile = fc.showSaveDialog(null);
            
            if(selectedFile != null){
                
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                    writer.write(infos.toString()+"\n");

                    Iterator<Voxel> iterator = reader.iterator();

                    while(iterator.hasNext()){

                        Voxel voxel = iterator.next();

                        if(voxel.$i >= iMin && voxel.$i <= iMax &&
                                voxel.$j >= jMin && voxel.$j <= jMax &&
                                voxel.$k >= kMin && voxel.$k <= kMax){

                            voxel.$i -= iMin;
                            voxel.$j -= jMin;
                            voxel.$k -= kMin;

                            writer.write(voxel+"\n");
                        }
                    }
                }catch(IOException ex){
                    DialogHelper.showErrorDialog(null, ex);
                }
            }

        } catch (Exception ex) {
            DialogHelper.showErrorDialog(null, ex);
        }
    }
    
}
