/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.frame;

import com.jogamp.opengl.util.FPSAnimator;
import fr.ird.voxelidar.graphics2d.ChartFactory;
import fr.ird.voxelidar.graphics2d.VegetationProfile;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelParameters;
import fr.ird.voxelidar.graphics2d.image.Projection;
import fr.ird.voxelidar.graphics2d.image.ScaleGradient;
import fr.ird.voxelidar.graphics3d.jogl.JoglListener;
import fr.ird.voxelidar.graphics3d.mesh.Attribut;
import fr.ird.voxelidar.listener.EventManager;
import fr.ird.voxelidar.graphics3d.object.terrain.Dtm;
import fr.ird.voxelidar.graphics3d.object.terrain.DtmLoader;
import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace;
import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpaceAdapter;
import fr.ird.voxelidar.lidar.format.voxelspace.VoxelSpaceFormat;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.als.LasHeader;
import fr.ird.voxelidar.lidar.format.als.LasReader;
import fr.ird.voxelidar.lidar.format.als.LasToTxt;
import fr.ird.voxelidar.lidar.format.als.LasToTxtAdapter;
import fr.ird.voxelidar.lidar.format.als.PointDataRecordFormat0;
import fr.ird.voxelidar.lidar.format.dart.DartWriter;
import fr.ird.voxelidar.lidar.format.tls.Rsp;
import fr.ird.voxelidar.lidar.format.tls.Scans;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.listener.InputKeyListener;
import fr.ird.voxelidar.listener.InputMouseAdapter;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec3D;
import fr.ird.voxelidar.math.vector.Vec4D;
import fr.ird.voxelidar.util.ColorGradient;
import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.util.Misc;
import fr.ird.voxelidar.util.Settings;
import fr.ird.voxelidar.util.TimeCounter;
import fr.ird.voxelidar.util.VoxelFilter;
import fr.ird.voxelidar.voxelisation.ProcessingListener;
import fr.ird.voxelidar.voxelisation.VoxelisationTool;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import javax.media.nativewindow.util.Point;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.BorderUIResource;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;
/**
 *
 * @author Julien
 */
public class JFrameSettingUp extends javax.swing.JFrame{

    /**
     * Creates new form JFrameSettingUp
     */
    final static Logger logger = Logger.getLogger(JFrameSettingUp.class);
    
    private FilterDefaultListModel rspScansListModel;
    private DefaultListModel<String> model;
    private DefaultListModel<String> filterModel;
    private DefaultComboBoxModel<String> attributeModel;
    private DefaultComboBoxModel<String> valueModel;
    private ListAdapterComboboxModel mainAttributeModelAdapter;
    
    private Dtm terrain;
    private Settings settings;
    public UIManager.LookAndFeelInfo[] installedLookAndFeels;
    private String currentLookAndFeel;
    private Rsp rsp;
    private Mat4D vopMatrix;
    private Mat4D popMatrix;
    private Mat4D vopPopMatrix;
    private final VoxelParameters voxelisationParameters;
    private final Border customBorder;

    public ListAdapterComboboxModel getMainAttributeModelAdapter() {
        return mainAttributeModelAdapter;
    }

    
    public JCheckBox getjCheckBoxDrawAxis() {
        return jCheckBoxDrawAxis;
    }

    public JCheckBox getjCheckBoxDrawNullVoxel() {
        return jCheckBoxDrawNullVoxel;
    }

    public JCheckBox getjCheckBoxDrawTerrain() {
        return jCheckBoxDrawTerrain;
    }

    public JCheckBox getjCheckBoxDrawUndergroundVoxel() {
        return jCheckBoxDrawUndergroundVoxel;
    }

    public JComboBox getjComboBoxAttributeToVisualize() {
        return jComboBoxAttributeToVisualize;
    }

    public JList getjListOutputFiles() {
        return jListOutputFiles;
    }
    
    private void addElementToVoxList(String element){
        
        if (!model.contains(element)) {
            model.addElement(element);
        }
        
        jListOutputFiles.setSelectedValue(element, true);
        
    }
    
    private void addElementToList(DefaultListModel model, JList list, String element){
        
        if (!model.contains(element)) {
            model.addElement(element);
        }
        
        list.setSelectedValue(element, true);
        
    }
    
    
    public JFrameSettingUp() {
        
        model = new DefaultListModel();
        filterModel = new DefaultListModel();
        attributeModel = new DefaultComboBoxModel();
        
        voxelisationParameters = new VoxelParameters();
        customBorder = new BorderUIResource.LineBorderUIResource(new Color(57, 57, 57));
        initComponents();
        initComboBox();
        setDefaultAppeareance();
        
        jListFilters.setModel(filterModel);
        jListOutputFiles.setModel(model);
        
        currentLookAndFeel = UIManager.getLookAndFeel().getClass().getName();
        installedLookAndFeels = UIManager.getInstalledLookAndFeels();
        
        for(int i=0;i<installedLookAndFeels.length;i++){
            
            final JMenuItem jMenuLookFeel = new JMenuItem(installedLookAndFeels[i].getName());
            jMenuLookFeel.setToolTipText(installedLookAndFeels[i].getClassName());
            jMenuAppearance.add(jMenuLookFeel);
            jMenuAppearance.repaint();
            jMenuLookFeel.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {

                    setAppearance(jMenuLookFeel.getToolTipText());
                }
            });
        }
        
        
        
        vopMatrix = Mat4D.identity();
        rsp = new Rsp();
        
        DocumentListener customDocumentListener = new DocumentListener(){

            @Override
            public void insertUpdate(DocumentEvent e) {
                
                try{
                    
                    double minPointX = Double.valueOf(jTextFieldMinPointX.getText());
                    double minPointY = Double.valueOf(jTextFieldMinPointY.getText());
                    double minPointZ = Double.valueOf(jTextFieldMinPointZ.getText());

                    double maxPointX = Double.valueOf(jTextFieldMaxPointX.getText());
                    double maxPointY = Double.valueOf(jTextFieldMaxPointY.getText());
                    double maxPointZ = Double.valueOf(jTextFieldMaxPointZ.getText());

                    int voxelNumberX = (int)(maxPointX - minPointX);
                    int voxelNumberY = (int)(maxPointY - minPointY);
                    int voxelNumberZ = (int)(maxPointZ - minPointZ);

                    jTextFieldVoxelNumberX.setText(String.valueOf(voxelNumberX));
                    jTextFieldVoxelNumberY.setText(String.valueOf(voxelNumberY));
                    jTextFieldVoxelNumberZ.setText(String.valueOf(voxelNumberZ));

                
                    float resolution = Float.valueOf(jTextFieldVoxelNumberRes.getText());

                    voxelNumberX = (int) ((maxPointX - minPointX)/resolution);
                    voxelNumberY = (int) ((maxPointY - minPointY)/resolution);
                    voxelNumberZ = (int) ((maxPointZ - minPointZ)/resolution);

                    jTextFieldVoxelNumberX.setText(String.valueOf(voxelNumberX));
                    jTextFieldVoxelNumberY.setText(String.valueOf(voxelNumberY));
                    jTextFieldVoxelNumberZ.setText(String.valueOf(voxelNumberZ));
                }catch(Exception ex){

                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                
            }
        };
        
        jTextFieldVoxelNumberRes.getDocument().addDocumentListener(customDocumentListener);
        jTextFieldMinPointX.getDocument().addDocumentListener(customDocumentListener);
        jTextFieldMinPointY.getDocument().addDocumentListener(customDocumentListener);
        jTextFieldMinPointZ.getDocument().addDocumentListener(customDocumentListener);
        jTextFieldMaxPointX.getDocument().addDocumentListener(customDocumentListener);
        jTextFieldMaxPointY.getDocument().addDocumentListener(customDocumentListener);
        jTextFieldMaxPointZ.getDocument().addDocumentListener(customDocumentListener);
        
        DocumentListener customDocumentListener2 = new DocumentListener(){

            @Override
            public void insertUpdate(DocumentEvent e) {
                
                try{
                    
                    int minPointX = Integer.valueOf(jTextFieldMinPointX2.getText());
                    int minPointY = Integer.valueOf(jTextFieldMinPointY2.getText());
                    int minPointZ = Integer.valueOf(jTextFieldMinPointZ2.getText());

                    int maxPointX = Integer.valueOf(jTextFieldMaxPointX2.getText());
                    int maxPointY = Integer.valueOf(jTextFieldMaxPointY2.getText());
                    int maxPointZ = Integer.valueOf(jTextFieldMaxPointZ2.getText());

                    int voxelNumberX = (maxPointX - minPointX);
                    int voxelNumberY = (maxPointY - minPointY);
                    int voxelNumberZ = (maxPointZ - minPointZ);

                    jTextFieldVoxelNumberX1.setText(String.valueOf(voxelNumberX));
                    jTextFieldVoxelNumberY1.setText(String.valueOf(voxelNumberY));
                    jTextFieldVoxelNumberZ1.setText(String.valueOf(voxelNumberZ));

                
                    float resolution = Float.valueOf(jTextFieldVoxelNumberRes1.getText());

                    voxelNumberX = (int) ((maxPointX - minPointX)/resolution);
                    voxelNumberY = (int) ((maxPointY - minPointY)/resolution);
                    voxelNumberZ = (int) ((maxPointZ - minPointZ)/resolution);

                    jTextFieldVoxelNumberX1.setText(String.valueOf(voxelNumberX));
                    jTextFieldVoxelNumberY1.setText(String.valueOf(voxelNumberY));
                    jTextFieldVoxelNumberZ1.setText(String.valueOf(voxelNumberZ));
                }catch(Exception ex){

                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                
            }
        };
        
        jTextFieldVoxelNumberRes1.getDocument().addDocumentListener(customDocumentListener2);
        jTextFieldMinPointX2.getDocument().addDocumentListener(customDocumentListener2);
        jTextFieldMinPointY2.getDocument().addDocumentListener(customDocumentListener2);
        jTextFieldMinPointZ2.getDocument().addDocumentListener(customDocumentListener2);
        jTextFieldMaxPointX2.getDocument().addDocumentListener(customDocumentListener2);
        jTextFieldMaxPointY2.getDocument().addDocumentListener(customDocumentListener2);
        jTextFieldMaxPointZ2.getDocument().addDocumentListener(customDocumentListener2);
    }
    
    private void setAppearance(String value){
        
        if(value.isEmpty()){
            setDefaultAppeareance();
            return;
        }
        try {
            
            UIManager.setLookAndFeel(value);
            currentLookAndFeel = value; 
            UIManager.put("TabbedPane.selected", new Color(114, 114, 114));
            UIManager.put("TabbedPane.contentAreaColor", new Color(114, 114, 114));
            UIManager.put("InternalFrame.background", new Color(114, 114, 114));
            
            SwingUtilities.updateComponentTreeUI(this);
            this.pack();
            
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            logger.error("cannot set look and feel: "+value, ex);
        }
    }
    
    private void updateVoxelParameters(){
        
        int minPointX = Integer.valueOf(jTextFieldMinPointX.getText());
        int minPointY = Integer.valueOf(jTextFieldMinPointY.getText());
        int minPointZ = Integer.valueOf(jTextFieldMinPointZ.getText());
        
        int maxPointX = Integer.valueOf(jTextFieldMaxPointX.getText());
        int maxPointY = Integer.valueOf(jTextFieldMaxPointY.getText());
        int maxPointZ = Integer.valueOf(jTextFieldMaxPointZ.getText());
        
        int voxelNumberX = Integer.valueOf(jTextFieldVoxelNumberX.getText());
        int voxelNumberY = Integer.valueOf(jTextFieldVoxelNumberY.getText());
        int voxelNumberZ = Integer.valueOf(jTextFieldVoxelNumberZ.getText());
        
        float resolution = Float.valueOf(jTextFieldVoxelNumberRes.getText());
        
        voxelNumberX = (int) ((maxPointX - minPointX)/resolution);
        voxelNumberY = (int) ((maxPointY - minPointY)/resolution);
        voxelNumberZ = (int) ((maxPointZ - minPointZ)/resolution);
        
        
        
    }
    
    public Mat4D getVopMatrixALS(){
        
        Vec3D point1 = new Vec3D(Double.valueOf(jTextFieldReferencePoint1X.getText()), Double.valueOf(jTextFieldReferencePoint1Y.getText()), Double.valueOf(jTextFieldReferencePoint1Z.getText()));
        Vec3D point2 = new Vec3D(Double.valueOf(jTextFieldReferencePoint2X.getText()), Double.valueOf(jTextFieldReferencePoint2Y.getText()), Double.valueOf(jTextFieldReferencePoint2Z.getText()));
        return VoxelisationTool.getMatrixTransformation(point1, point2);
    }
    
    public Mat4D getVopMatrixTLS(){
        
        return Mat4D.identity();
    }
    
    private void setDefaultAppeareance(){
        setAppearance(UIManager.getCrossPlatformLookAndFeelClassName());
    }
    
    public void openJFrameStateFile(String path){
        
        try {
            Preferences.importPreferences(new FileInputStream(path));
        } catch (FileNotFoundException ex) {
            logger.error(null, ex);
        } catch (IOException | InvalidPreferencesFormatException ex) {
            logger.error(null, ex);
        }
        
        Preferences prefs = Preferences.userRoot();
        
        List<Component> compList = getAllComponents(this);
        
        if(prefs.getInt("COMPONENTS_NUMBERS", 0) == 0 || prefs.getInt("COMPONENTS_NUMBERS", 0) != compList.size()){
            JOptionPane.showMessageDialog(this, "file corrupted: found "+ prefs.getInt("COMPONENTS_NUMBERS", 0)+" components instead of "+compList.size());
        }else{
            for(int i=0;i<compList.size();i++){

                if(compList.get(i) instanceof JTextField){

                    String value = prefs.get(String.valueOf(i), "");
                    ((JTextField)compList.get(i)).setText(value);

                }else if(compList.get(i) instanceof JRadioButton){
                    
                    ((JRadioButton)compList.get(i)).setSelected(prefs.getBoolean(String.valueOf(i), true));
                    
                }else if(compList.get(i) instanceof JCheckBox){
                    
                    ((JCheckBox)compList.get(i)).setSelected(prefs.getBoolean(String.valueOf(i)+"_selected", true));
                    ((JCheckBox)compList.get(i)).setEnabled(prefs.getBoolean(String.valueOf(i)+"_enabled", true));
                    
                }else if(compList.get(i) instanceof JComboBox){
                    
                    try{
                        ((JComboBox)compList.get(i)).setSelectedIndex(prefs.getInt(String.valueOf(i), 0));
                    }catch(Exception e){
                        
                    }
                    
                }else if(compList.get(i) instanceof JList){
                    
                    String allItems = prefs.get(String.valueOf(i), "");
                    
                    if(!allItems.equals("")){
                        String[] items = allItems.split("\n");
                        DefaultListModel model = new DefaultListModel();

                        for(int j=0;j<items.length;j++){
                            model.addElement(items[j]);
                        }

                        ((JList)compList.get(i)).setModel(model);
                    }
                }
            }
            
            setAppearance(prefs.get("lookandfeel", ""));
            
            String modelList = prefs.get("model", "");
            String[] modelArray = modelList.split(";");
            
            model = new DefaultListModel();
            
            for (String modelArray1 : modelArray) {
                if(!modelArray1.isEmpty()){
                    addElementToVoxList(modelArray1);
                }
            }
            
            jListOutputFiles.setModel(model);
            
            modelList = prefs.get("filterModel", "");
            modelArray = modelList.split(";");
            
            filterModel = new DefaultListModel<>();
            
            for (String modelArray1 : modelArray) {
                if(!modelArray1.isEmpty()){
                    addElementToList(filterModel, jListFilters, modelArray1);
                }
            }
            
            jListFilters.setModel(filterModel);
            
            if(modelArray.length>0)jListOutputFiles.setSelectedIndex(0);
        }
        
        
    }
    
    public static List<Component> getAllComponents(final Container c){
     
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
          if(comp instanceof JRadioButton || comp instanceof JCheckBox || comp instanceof JTextField || comp instanceof JComboBox){
              compList.add(comp);
          }else if(comp instanceof JList){
            
            compList.add(comp);
        }else{
              
            if (comp instanceof Container) {
              compList.addAll(getAllComponents((Container) comp));
            }
          }
        }
        return compList;
    }
    
    
    
    public void saveJFrameState(String path){
        
        Preferences prefs = Preferences.userRoot();
        try {
            prefs.clear();
        } catch (BackingStoreException ex) {
            logger.error(null, ex);
        }
        
        List<Component> compList = getAllComponents(this);
        
        
        try {
            for(int i=0;i<compList.size();i++){

                if(compList.get(i) instanceof JTextField){

                    String value = ((JTextField)compList.get(i)).getText();
                    prefs.put(String.valueOf(i), value);
                    
                }else if(compList.get(i) instanceof JRadioButton){
                    
                    prefs.putBoolean(String.valueOf(i), ((JRadioButton)compList.get(i)).isSelected());
                    
                }else if(compList.get(i) instanceof JCheckBox){
                    
                    prefs.putBoolean(String.valueOf(i)+"_enabled", ((JCheckBox)compList.get(i)).isEnabled());
                    prefs.putBoolean(String.valueOf(i)+"_selected", ((JCheckBox)compList.get(i)).isSelected());
                    
                }else if(compList.get(i) instanceof JComboBox){
                    
                    prefs.putInt(String.valueOf(i), ((JComboBox)compList.get(i)).getSelectedIndex());
                    
                }else if(compList.get(i) instanceof JList){
                    ListModel model = ((JList)compList.get(i)).getModel();
                    String allItems = "";
                    for(int j=0;j<model.getSize();j++){
                        allItems += (String)model.getElementAt(j)+"\n";
                    }
                    
                    prefs.put(String.valueOf(i), allItems);
                }
            }


            String modelList = "";
            for(int i=0;i<model.getSize();i++){
                modelList+=model.getElementAt(i)+";";
            }
            prefs.put("model", modelList);
            
            String modelFilterList = "";
            for(int i=0;i<filterModel.getSize();i++){
                modelFilterList+=filterModel.getElementAt(i)+";";
            }
            prefs.put("filterModel", modelFilterList);
            
            prefs.put("lookandfeel", currentLookAndFeel);
            prefs.putInt("COMPONENTS_NUMBERS", compList.size());
            
            prefs.exportNode(new FileOutputStream(new File(path)));
            
        } catch (IOException | BackingStoreException ex) {
            logger.error(null, ex);
        }        
        
    }
    
    private boolean isVoxelFile(String path){
        
        try{
            
            String[] headerAttributes = FileManager.readHeader(path).split(" ");
            String[] firstVoxel = FileManager.readSpecificLine(path, 3).split(" ");

            if(firstVoxel.length != headerAttributes.length){
                return false;
            }
            
            String[] voxelSpaceParameters = FileManager.readSpecificLine(path, 2).split(" ");
            
            for (String parameter : voxelSpaceParameters) {
                Float.valueOf(parameter);
            }
            
            if(voxelSpaceParameters.length != 7){
                
                return false;
            }
            
        
        }catch(Exception e){

            return false;
        }
        
        return true;
    }
    
    private boolean isTerrainFile(File file) {
        
        String path = file.getAbsolutePath();
        
        if(path.endsWith(".asc")){
            
            return true;
            
        }else if(path.endsWith(".txt")){
            
            try{
            
                String[] headerAttributes = FileManager.readHeader(path).split(" ");

                if(headerAttributes.length != 3){
                    return false;
                }
                String[] firstPoint = FileManager.readSpecificLine(path, 2).split(" ");

                if(firstPoint.length != 3){
                    return false;
                }

                for (String point : firstPoint) {
                    Float.valueOf(point);
                }


            }catch(Exception e){

                return false;
            }
        }
        
        return true;
    }
    
    public void readTerrain(){
        
        try{
            if(terrain == null || !terrain.getPath().equals(jTextFieldFilePathMnt.getText())){
                if(jTextFieldFilePathMnt.getText().endsWith(".asc")){
                    terrain = DtmLoader.readFromAscFile(new File(jTextFieldFilePathMnt.getText()));
                }else if(jTextFieldFilePathMnt.getText().endsWith(".txt")){
                    terrain = DtmLoader.readFromFile(jTextFieldFilePathMnt.getText());
                }
                
            }
            
            jButtonGenerateMap.setEnabled(true);
            jButtonGenerateMap.setToolTipText("");

        }catch(Exception e){
            logger.debug("the file isn't a terrain file", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private void initComboBox(){
        
        if(jListOutputFiles.getModel().getSize()>0){
            
            String[] parameters = VoxelSpaceFormat.readAttributs(new File(jListOutputFiles.getSelectedValue().toString()));
            
            for(int i=0 ; i< parameters.length ;i++){
                    
                parameters[i] = parameters[i].replaceAll(" ", "");
                parameters[i] = parameters[i].replaceAll("#", "");
            }
            
            //String[] parameters = FileManager.readHeader(jListOutputFiles.getSelectedValue().toString()).split(" ");
            
            if(parameters.length > 5){
                
                attributeModel = new DefaultComboBoxModel<>(parameters);
                valueModel = new DefaultComboBoxModel<>(parameters);
                
                ListAdapterComboboxModel attributeModelAdapterXAxis = new ListAdapterComboboxModel(attributeModel, valueModel);
                jComboBoxHorizontalAxisValue.setModel(attributeModelAdapterXAxis);
                attributeModelAdapterXAxis.setSelectedItem(attributeModelAdapterXAxis.getElementAt(3));

                ListAdapterComboboxModel attributeModelAdapterYAxis = new ListAdapterComboboxModel(attributeModel, valueModel);
                jComboBoxverticalAxisValue.setModel(attributeModelAdapterYAxis);
                attributeModelAdapterYAxis.setSelectedItem(attributeModelAdapterYAxis.getElementAt(3));

                mainAttributeModelAdapter = new ListAdapterComboboxModel(attributeModel, valueModel);
                jComboBoxAttributeToVisualize.setModel(mainAttributeModelAdapter);
                mainAttributeModelAdapter.setSelectedItem(mainAttributeModelAdapter.getElementAt(3));
                
                ListAdapterComboboxModel attributeModelAdapterX = new ListAdapterComboboxModel(attributeModel, valueModel);
                jComboBoxDefaultX.setModel(attributeModelAdapterX);
                attributeModelAdapterX.setSelectedItem(attributeModelAdapterX.getElementAt(0));
                
                ListAdapterComboboxModel attributeModelAdapterY = new ListAdapterComboboxModel(attributeModel, valueModel);
                jComboBoxDefaultY.setModel(attributeModelAdapterY);
                attributeModelAdapterY.setSelectedItem(attributeModelAdapterY.getElementAt(1));
                
                ListAdapterComboboxModel attributeModelAdapterZ = new ListAdapterComboboxModel(attributeModel, valueModel);
                jComboBoxDefaultZ.setModel(attributeModelAdapterZ);
                attributeModelAdapterZ.setSelectedItem(attributeModelAdapterZ.getElementAt(2));
            }

            
            
        }else{
            jComboBoxAttributeToVisualize.setModel(new DefaultComboBoxModel());
        }
    }
    
    private void fillAttributes(){
        
        char[] attributes = new char[]{'x', 'y', 'z', 'i', 'r', 'n', 'd', 'e', 'c', 'a', 'u', 'p', 't', 'w', 'W', 'V'};
        String attributs = "";
        
        if(jCheckBoxWriteX.isSelected()){
            attributs += 'x';
        }
        if(jCheckBoxWriteY.isSelected()){
            attributs += 'y';
        }
        if(jCheckBoxWriteZ.isSelected()){
            attributs += 'z';
        }
        if(jCheckBoxWriteI.isSelected()){
            attributs += 'i';
        }
        if(jCheckBoxWriteR.isSelected()){
            attributs += 'r';
        }
        if(jCheckBoxWriteN.isSelected()){
            attributs += 'n';
        }
        if(jCheckBoxWriteD.isSelected()){
            attributs += 'd';
        }
        if(jCheckBoxWriteE.isSelected()){
            attributs += 'e';
        }
        if(jCheckBoxWriteC.isSelected()){
            attributs += 'c';
        }
        if(jCheckBoxWriteA.isSelected()){
            attributs += 'a';
        }
        if(jCheckBoxWriteU.isSelected()){
            attributs += 'u';
        }
        if(jCheckBoxWriteT.isSelected()){
            attributs += 't';
        }
        if(jCheckBoxWriteRGB.isSelected()){
            attributs += "RGB";
        }
        if(jCheckBoxWritew.isSelected()){
            attributs += 'w';
        }
        if(jCheckBoxWriteW.isSelected()){
            attributs += 'W';
        }
        if(jCheckBoxWriteV.isSelected()){
            attributs += 'V';
        }
        
        jTextFieldArguments.setText(attributs);
        
    }
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jFileChooser2 = new javax.swing.JFileChooser();
        buttonGroup3DView = new javax.swing.ButtonGroup();
        buttonGroup2DProjection = new javax.swing.ButtonGroup();
        jFileChooserSave = new javax.swing.JFileChooser();
        jFileChooser3 = new javax.swing.JFileChooser();
        jFileChooser4 = new javax.swing.JFileChooser();
        jFileChooser5 = new javax.swing.JFileChooser();
        jFileChooser6 = new javax.swing.JFileChooser();
        jFileChooser7 = new javax.swing.JFileChooser();
        jFileChooser8 = new javax.swing.JFileChooser();
        jFileChooserSave1 = new javax.swing.JFileChooser();
        jFileChooser9 = new javax.swing.JFileChooser();
        buttonGroup1 = new javax.swing.ButtonGroup();
        jFileChooser10 = new javax.swing.JFileChooser();
        jFileChooserSave2 = new javax.swing.JFileChooser();
        jFileChooser11 = new javax.swing.JFileChooser();
        buttonGroup2 = new javax.swing.ButtonGroup();
        jPanel7 = new javax.swing.JPanel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelOutputParametersTab = new javax.swing.JPanel();
        jTabbedPane2 = new javax.swing.JTabbedPane();
        jTabbedPane5 = new javax.swing.JTabbedPane();
        jPanel35 = new javax.swing.JPanel();
        jPanel36 = new javax.swing.JPanel();
        jPanel39 = new javax.swing.JPanel();
        jLabelName6 = new javax.swing.JLabel();
        jLabelPath6 = new javax.swing.JLabel();
        jTextFieldFilePathRsp = new javax.swing.JTextField();
        jTextFieldFileNameRsp = new javax.swing.JTextField();
        jButtonOpenRspFile = new javax.swing.JButton();
        jComboBox1 = new javax.swing.JComboBox();
        jRadioButtonLightFile = new javax.swing.JRadioButton();
        jRadioButtonComplexFile = new javax.swing.JRadioButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListRspScans = new javax.swing.JList();
        jButtonSopMatrix = new javax.swing.JButton();
        jButtonPopMatrix = new javax.swing.JButton();
        jButtonVopMatrix = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jPanel40 = new javax.swing.JPanel();
        jTextFieldMinPointX2 = new javax.swing.JTextField();
        jTextFieldMinPointY2 = new javax.swing.JTextField();
        jTextFieldMinPointZ2 = new javax.swing.JTextField();
        jPanel41 = new javax.swing.JPanel();
        jTextFieldMaxPointX2 = new javax.swing.JTextField();
        jTextFieldMaxPointY2 = new javax.swing.JTextField();
        jTextFieldMaxPointZ2 = new javax.swing.JTextField();
        jPanel42 = new javax.swing.JPanel();
        jTextFieldVoxelNumberX1 = new javax.swing.JTextField();
        jTextFieldVoxelNumberY1 = new javax.swing.JTextField();
        jTextFieldVoxelNumberZ1 = new javax.swing.JTextField();
        jTextFieldVoxelNumberRes1 = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        jLabelPath10 = new javax.swing.JLabel();
        jTextFieldFileOutputPathTlsVox = new javax.swing.JTextField();
        jButtonChooseOutputDirectoryTlsVox = new javax.swing.JButton();
        jButtonExecuteVoxTls = new javax.swing.JButton();
        jCheckBoxMergeOutputFiles = new javax.swing.JCheckBox();
        jTextFieldOutputMergedFile = new javax.swing.JTextField();
        jTabbedPane3 = new javax.swing.JTabbedPane();
        jPanel33 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabelName3 = new javax.swing.JLabel();
        jLabelPath3 = new javax.swing.JLabel();
        jLabelSize1 = new javax.swing.JLabel();
        jLabelFileSizeInputVox = new javax.swing.JLabel();
        jTextFieldFilePathInputVox = new javax.swing.JTextField();
        jTextFieldFileNameInputVox = new javax.swing.JTextField();
        jButtonOpenInputFileVoxelisation = new javax.swing.JButton();
        jPanel18 = new javax.swing.JPanel();
        jLabelName5 = new javax.swing.JLabel();
        jLabelPath5 = new javax.swing.JLabel();
        jTextFieldFilePathTrajVox = new javax.swing.JTextField();
        jTextFieldFileNameTrajVox = new javax.swing.JTextField();
        jButtonOpenTrajectoryFile = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        jLabelName4 = new javax.swing.JLabel();
        jLabelPath4 = new javax.swing.JLabel();
        jTextFieldFilePathSaveVox = new javax.swing.JTextField();
        jTextFieldFileNameSaveVox = new javax.swing.JTextField();
        jButtonChooseOutputDirectoryVox = new javax.swing.JButton();
        jButtonExecuteVoxAls = new javax.swing.JButton();
        jPanel28 = new javax.swing.JPanel();
        jPanel29 = new javax.swing.JPanel();
        jTextFieldReferencePoint1X = new javax.swing.JTextField();
        jTextFieldReferencePoint1Y = new javax.swing.JTextField();
        jTextFieldReferencePoint1Z = new javax.swing.JTextField();
        jPanel30 = new javax.swing.JPanel();
        jTextFieldReferencePoint2X = new javax.swing.JTextField();
        jTextFieldReferencePoint2Y = new javax.swing.JTextField();
        jTextFieldReferencePoint2Z = new javax.swing.JTextField();
        jButtonPopMatrix1 = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jComboBoxWeighting = new javax.swing.JComboBox();
        jButtonOpenWeightingFile = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jPanel26 = new javax.swing.JPanel();
        jTextFieldMaxPointX = new javax.swing.JTextField();
        jTextFieldMaxPointY = new javax.swing.JTextField();
        jTextFieldMaxPointZ = new javax.swing.JTextField();
        jPanel27 = new javax.swing.JPanel();
        jTextFieldMinPointX = new javax.swing.JTextField();
        jTextFieldMinPointY = new javax.swing.JTextField();
        jTextFieldMinPointZ = new javax.swing.JTextField();
        jPanel25 = new javax.swing.JPanel();
        jTextFieldVoxelNumberX = new javax.swing.JTextField();
        jTextFieldVoxelNumberY = new javax.swing.JTextField();
        jTextFieldVoxelNumberZ = new javax.swing.JTextField();
        jTextFieldVoxelNumberRes = new javax.swing.JTextField();
        jButtonCalculateBoundingBox = new javax.swing.JButton();
        jCheckBoxUseDTM = new javax.swing.JCheckBox();
        jPanel31 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabelName = new javax.swing.JLabel();
        jLabelPath = new javax.swing.JLabel();
        jLabelSize = new javax.swing.JLabel();
        jLabelFileSize = new javax.swing.JLabel();
        jTextFieldFilePathInputTxt = new javax.swing.JTextField();
        jTextFieldFileNameInputTxt = new javax.swing.JTextField();
        jButtonOpenInputFile = new javax.swing.JButton();
        jPanel15 = new javax.swing.JPanel();
        jLabelName2 = new javax.swing.JLabel();
        jLabelPath2 = new javax.swing.JLabel();
        jTextFieldFilePathOutputTxt = new javax.swing.JTextField();
        jTextFieldFileNameOutputTxt = new javax.swing.JTextField();
        jButtonChooseDirectory = new javax.swing.JButton();
        jCheckBoxWriteX = new javax.swing.JCheckBox();
        jCheckBoxWriteY = new javax.swing.JCheckBox();
        jCheckBoxWriteZ = new javax.swing.JCheckBox();
        jCheckBoxWriteI = new javax.swing.JCheckBox();
        jCheckBoxWriteR = new javax.swing.JCheckBox();
        jCheckBoxWriteN = new javax.swing.JCheckBox();
        jCheckBoxWriteD = new javax.swing.JCheckBox();
        jCheckBoxWriteE = new javax.swing.JCheckBox();
        jCheckBoxWriteC = new javax.swing.JCheckBox();
        jCheckBoxWriteA = new javax.swing.JCheckBox();
        jCheckBoxWriteU = new javax.swing.JCheckBox();
        jCheckBoxWriteP = new javax.swing.JCheckBox();
        jCheckBoxWriteT = new javax.swing.JCheckBox();
        jCheckBoxWriteRGB = new javax.swing.JCheckBox();
        jCheckBoxWritew = new javax.swing.JCheckBox();
        jCheckBoxWriteW = new javax.swing.JCheckBox();
        jCheckBoxWriteV = new javax.swing.JCheckBox();
        jPanel24 = new javax.swing.JPanel();
        jButtonLoad = new javax.swing.JButton();
        jCheckBoxWriteHeader = new javax.swing.JCheckBox();
        jTextFieldArguments = new javax.swing.JTextField();
        jPanelVisualizeTab = new javax.swing.JPanel();
        jTabbedPane4 = new javax.swing.JTabbedPane();
        jPanel3 = new javax.swing.JPanel();
        jButtonOpen3DDisplay = new javax.swing.JButton();
        jPanel3DViewDensity = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jComboBoxAttributeToVisualize = new javax.swing.JComboBox();
        jButtonCreateAttribut = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jComboBoxDefaultX = new javax.swing.JComboBox();
        jLabel3 = new javax.swing.JLabel();
        jComboBoxDefaultY = new javax.swing.JComboBox();
        jLabel4 = new javax.swing.JLabel();
        jComboBoxDefaultZ = new javax.swing.JComboBox();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldAttributExpression = new javax.swing.JTextField();
        jCheckBoxDrawTerrain = new javax.swing.JCheckBox();
        jCheckBoxDrawAxis = new javax.swing.JCheckBox();
        jCheckBoxDrawNullVoxel = new javax.swing.JCheckBox();
        jCheckBoxDrawUndergroundVoxel = new javax.swing.JCheckBox();
        jPanel4 = new javax.swing.JPanel();
        jButtonGenerateMap = new javax.swing.JButton();
        jRadioButtonPAI = new javax.swing.JRadioButton();
        jRadioButtonTransmittanceMap = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jPanelVegetationProfile = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jButtonGenerateProfile = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jComboBoxHorizontalAxisValue = new javax.swing.JComboBox();
        jPanel20 = new javax.swing.JPanel();
        jComboBoxverticalAxisValue = new javax.swing.JComboBox();
        jPanel12 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        jListFilters = new javax.swing.JList();
        jButtonAddFilter = new javax.swing.JButton();
        jButtonRemoveFilter = new javax.swing.JButton();
        jButtonDrawChart = new javax.swing.JButton();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel8 = new javax.swing.JPanel();
        jButtonAddFile = new javax.swing.JButton();
        jButtonRemoveFile = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListOutputFiles = new javax.swing.JList();
        jButtonLoadSelectedFile = new javax.swing.JButton();
        jButtonExportSelection = new javax.swing.JButton();
        jPanel10 = new javax.swing.JPanel();
        jButtonOpenInputFile1 = new javax.swing.JButton();
        jTextFieldFileNameMnt = new javax.swing.JTextField();
        jLabelName1 = new javax.swing.JLabel();
        jLabelPath1 = new javax.swing.JLabel();
        jTextFieldFilePathMnt = new javax.swing.JTextField();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenu3 = new javax.swing.JMenu();
        jMenuItemLoad = new javax.swing.JMenuItem();
        jMenuItemSave = new javax.swing.JMenuItem();
        jMenuItemSaveAs = new javax.swing.JMenuItem();
        jMenuSettings = new javax.swing.JMenu();
        jMenuAppearance = new javax.swing.JMenu();
        jMenu2 = new javax.swing.JMenu();

        jFileChooser1.addChoosableFileFilter(new FileNameExtensionFilter(null, "jpg", "png", "gif", "bmp"));

        FileFilter docFilter = new FileNameExtensionFilter("tls", "Terrestrial laser scanning");
        FileFilter pdfFilter = new FileNameExtensionFilter("als", "Airborne laser scanning");
        FileFilter xlsFilter = new FileNameExtensionFilter("TXT", "Text document");

        jFileChooser1.addChoosableFileFilter(docFilter);
        jFileChooser1.addChoosableFileFilter(pdfFilter);
        jFileChooser1.addChoosableFileFilter(xlsFilter);
        jFileChooser1.setCurrentDirectory(null);
        jFileChooser1.setDialogTitle("");

        jFileChooserSave.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        jFileChooserSave.setCurrentDirectory(new java.io.File("C:\\Program Files\\NetBeans 8.0.1"));

        jFileChooser6.setApproveButtonToolTipText("");
        jFileChooser6.setDialogTitle("");
        jFileChooser6.setFileFilter(null);

        jFileChooser7.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);

        jFileChooser8.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        jFileChooser8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jFileChooser8ActionPerformed(evt);
            }
        });

        jFileChooserSave1.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        jFileChooserSave1.setCurrentDirectory(new java.io.File("C:\\Program Files\\NetBeans 8.0.1"));

        jFileChooser9.setCurrentDirectory(null);
        jFileChooser9.setDialogTitle("");
        jFileChooser9.setFileFilter(null);

        jFileChooser10.setCurrentDirectory(null);
        jFileChooser10.setDialogTitle("");
        jFileChooser10.setFileFilter(null);
        jFileChooser10.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);

        jFileChooserSave2.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        jFileChooserSave2.setCurrentDirectory(new java.io.File("C:\\Program Files\\NetBeans 8.0.1"));

        jFileChooser11.setCurrentDirectory(null);
        jFileChooser11.setDialogTitle("");
        jFileChooser11.setFileFilter(null);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(new java.awt.Color(114, 114, 114));
        setMinimumSize(new java.awt.Dimension(560, 450));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jPanel7.setBackground(new java.awt.Color(114, 114, 114));
        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.Y_AXIS));

        jTabbedPane1.setBackground(new java.awt.Color(83, 83, 83));
        jTabbedPane1.setMinimumSize(new java.awt.Dimension(0, 0));

        jPanelOutputParametersTab.setBackground(new java.awt.Color(114, 114, 114));

        jTabbedPane2.setBackground(new java.awt.Color(83, 83, 83));
        jTabbedPane2.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        jTabbedPane5.setBackground(new java.awt.Color(114, 114, 114));

        jPanel35.setBackground(new java.awt.Color(114, 114, 114));

        jPanel36.setBackground(new java.awt.Color(114, 114, 114));

        jPanel39.setBackground(new java.awt.Color(114, 114, 114));
        jPanel39.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Input file", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), java.awt.Color.black)); // NOI18N

        jLabelName6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName6.setText("Name");
        jLabelName6.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath6.setText("Path");

        jTextFieldFilePathRsp.setEditable(false);
        jTextFieldFilePathRsp.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFilePathRsp.setColumns(38);
        jTextFieldFilePathRsp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathRspActionPerformed(evt);
            }
        });

        jTextFieldFileNameRsp.setEditable(false);
        jTextFieldFileNameRsp.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFileNameRsp.setColumns(50);
        jTextFieldFileNameRsp.setToolTipText("");
        jTextFieldFileNameRsp.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameRsp.setName(""); // NOI18N

        jButtonOpenRspFile.setBackground(new java.awt.Color(85, 85, 85));
        jButtonOpenRspFile.setForeground(new java.awt.Color(255, 255, 255));
        jButtonOpenRspFile.setText("Open");
        jButtonOpenRspFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenRspFileActionPerformed(evt);
            }
        });

        jComboBox1.setBackground(new java.awt.Color(180, 180, 180));
        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Riscan project (*.rsp)", "Riegl file, txt file (*.rxp, *.txt)" }));
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBox1ItemStateChanged(evt);
            }
        });

        javax.swing.GroupLayout jPanel39Layout = new javax.swing.GroupLayout(jPanel39);
        jPanel39.setLayout(jPanel39Layout);
        jPanel39Layout.setHorizontalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel39Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel39Layout.createSequentialGroup()
                        .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(158, Short.MAX_VALUE))
                    .addGroup(jPanel39Layout.createSequentialGroup()
                        .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButtonOpenRspFile)
                            .addGroup(jPanel39Layout.createSequentialGroup()
                                .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelName6)
                                    .addComponent(jLabelPath6))
                                .addGap(48, 48, 48)
                                .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jTextFieldFileNameRsp, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                                    .addComponent(jTextFieldFilePathRsp, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))))
                        .addGap(34, 34, 34))))
        );
        jPanel39Layout.setVerticalGroup(
            jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel39Layout.createSequentialGroup()
                .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileNameRsp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel39Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilePathRsp, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPath6))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpenRspFile))
        );

        jRadioButtonLightFile.setBackground(new java.awt.Color(114, 114, 114));
        buttonGroup1.add(jRadioButtonLightFile);
        jRadioButtonLightFile.setSelected(true);
        jRadioButtonLightFile.setText("Light file");
        jRadioButtonLightFile.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButtonLightFileStateChanged(evt);
            }
        });

        jRadioButtonComplexFile.setBackground(new java.awt.Color(114, 114, 114));
        buttonGroup1.add(jRadioButtonComplexFile);
        jRadioButtonComplexFile.setText("Complex file");
        jRadioButtonComplexFile.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButtonComplexFileStateChanged(evt);
            }
        });

        jListRspScans.setBackground(new java.awt.Color(180, 180, 180));
        jScrollPane1.setViewportView(jListRspScans);

        jButtonSopMatrix.setBackground(new java.awt.Color(85, 85, 85));
        jButtonSopMatrix.setForeground(new java.awt.Color(255, 255, 255));
        jButtonSopMatrix.setText("SOP matrix");
        jButtonSopMatrix.setEnabled(false);

        jButtonPopMatrix.setBackground(new java.awt.Color(85, 85, 85));
        jButtonPopMatrix.setForeground(new java.awt.Color(255, 255, 255));
        jButtonPopMatrix.setText("POP matrix");
        jButtonPopMatrix.setToolTipText("Project orientation and position");
        jButtonPopMatrix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPopMatrixActionPerformed(evt);
            }
        });

        jButtonVopMatrix.setBackground(new java.awt.Color(85, 85, 85));
        jButtonVopMatrix.setForeground(new java.awt.Color(255, 255, 255));
        jButtonVopMatrix.setText("VOP matrix");
        jButtonVopMatrix.setToolTipText("Voxel orientation and position");
        jButtonVopMatrix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVopMatrixActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(85, 85, 85));
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("POP %*% VOP");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel40.setBackground(new java.awt.Color(114, 114, 114));
        jPanel40.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Min point", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 12), new java.awt.Color(39, 39, 39))); // NOI18N
        jPanel40.setToolTipText("");

        jTextFieldMinPointX2.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMinPointX2.setText("-10");
        jTextFieldMinPointX2.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldMinPointX2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMinPointX2KeyTyped(evt);
            }
        });

        jTextFieldMinPointY2.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMinPointY2.setText("50");
        jTextFieldMinPointY2.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldMinPointY2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMinPointY2KeyTyped(evt);
            }
        });

        jTextFieldMinPointZ2.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMinPointZ2.setText("0");
        jTextFieldMinPointZ2.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));
        jTextFieldMinPointZ2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMinPointZ2KeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel40Layout = new javax.swing.GroupLayout(jPanel40);
        jPanel40.setLayout(jPanel40Layout);
        jPanel40Layout.setHorizontalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel40Layout.createSequentialGroup()
                .addComponent(jTextFieldMinPointX2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMinPointY2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMinPointZ2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel40Layout.setVerticalGroup(
            jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel40Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldMinPointX2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldMinPointY2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldMinPointZ2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel41.setBackground(new java.awt.Color(114, 114, 114));
        jPanel41.setBorder(javax.swing.BorderFactory.createTitledBorder("Max point"));
        jPanel41.setToolTipText("");

        jTextFieldMaxPointX2.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMaxPointX2.setText("10");
        jTextFieldMaxPointX2.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldMaxPointX2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMaxPointX2KeyTyped(evt);
            }
        });

        jTextFieldMaxPointY2.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMaxPointY2.setText("150");
        jTextFieldMaxPointY2.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldMaxPointY2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMaxPointY2KeyTyped(evt);
            }
        });

        jTextFieldMaxPointZ2.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMaxPointZ2.setText("70");
        jTextFieldMaxPointZ2.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));
        jTextFieldMaxPointZ2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMaxPointZ2KeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel41Layout = new javax.swing.GroupLayout(jPanel41);
        jPanel41.setLayout(jPanel41Layout);
        jPanel41Layout.setHorizontalGroup(
            jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel41Layout.createSequentialGroup()
                .addComponent(jTextFieldMaxPointX2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMaxPointY2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMaxPointZ2, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel41Layout.setVerticalGroup(
            jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel41Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldMaxPointX2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldMaxPointY2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldMaxPointZ2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel42.setBackground(new java.awt.Color(114, 114, 114));
        jPanel42.setBorder(javax.swing.BorderFactory.createTitledBorder("Voxel Number"));
        jPanel42.setToolTipText("");

        jTextFieldVoxelNumberX1.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldVoxelNumberX1.setText("20");
        jTextFieldVoxelNumberX1.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldVoxelNumberX1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberX1KeyTyped(evt);
            }
        });

        jTextFieldVoxelNumberY1.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldVoxelNumberY1.setText("100");
        jTextFieldVoxelNumberY1.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldVoxelNumberY1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberY1KeyTyped(evt);
            }
        });

        jTextFieldVoxelNumberZ1.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldVoxelNumberZ1.setText("70");
        jTextFieldVoxelNumberZ1.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));
        jTextFieldVoxelNumberZ1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberZ1KeyTyped(evt);
            }
        });

        jTextFieldVoxelNumberRes1.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldVoxelNumberRes1.setText("1");
        jTextFieldVoxelNumberRes1.setBorder(javax.swing.BorderFactory.createTitledBorder("Resolution"));
        jTextFieldVoxelNumberRes1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberRes1KeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel42Layout = new javax.swing.GroupLayout(jPanel42);
        jPanel42.setLayout(jPanel42Layout);
        jPanel42Layout.setHorizontalGroup(
            jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel42Layout.createSequentialGroup()
                .addComponent(jTextFieldVoxelNumberX1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldVoxelNumberY1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldVoxelNumberZ1, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTextFieldVoxelNumberRes1, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel42Layout.setVerticalGroup(
            jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel42Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldVoxelNumberX1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldVoxelNumberY1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldVoxelNumberZ1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldVoxelNumberRes1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel19.setBackground(new java.awt.Color(114, 114, 114));
        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder("Output path"));

        jLabelPath10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath10.setText("Path");

        jTextFieldFileOutputPathTlsVox.setEditable(false);
        jTextFieldFileOutputPathTlsVox.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFileOutputPathTlsVox.setColumns(38);
        jTextFieldFileOutputPathTlsVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFileOutputPathTlsVoxActionPerformed(evt);
            }
        });

        jButtonChooseOutputDirectoryTlsVox.setBackground(new java.awt.Color(85, 85, 85));
        jButtonChooseOutputDirectoryTlsVox.setForeground(new java.awt.Color(255, 255, 255));
        jButtonChooseOutputDirectoryTlsVox.setText("Choose directory");
        jButtonChooseOutputDirectoryTlsVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChooseOutputDirectoryTlsVoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel19Layout = new javax.swing.GroupLayout(jPanel19);
        jPanel19.setLayout(jPanel19Layout);
        jPanel19Layout.setHorizontalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addComponent(jLabelPath10)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldFileOutputPathTlsVox, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel19Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(jButtonChooseOutputDirectoryTlsVox))
        );
        jPanel19Layout.setVerticalGroup(
            jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel19Layout.createSequentialGroup()
                .addGroup(jPanel19Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileOutputPathTlsVox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPath10))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonChooseOutputDirectoryTlsVox)
                .addContainerGap())
        );

        jButtonExecuteVoxTls.setBackground(new java.awt.Color(85, 85, 85));
        jButtonExecuteVoxTls.setForeground(new java.awt.Color(255, 255, 255));
        jButtonExecuteVoxTls.setText("Execute");
        jButtonExecuteVoxTls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecuteVoxTlsActionPerformed(evt);
            }
        });

        jCheckBoxMergeOutputFiles.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxMergeOutputFiles.setText("Merge output files");

        jTextFieldOutputMergedFile.setBackground(new java.awt.Color(180, 180, 180));

        javax.swing.GroupLayout jPanel36Layout = new javax.swing.GroupLayout(jPanel36);
        jPanel36.setLayout(jPanel36Layout);
        jPanel36Layout.setHorizontalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel36Layout.createSequentialGroup()
                .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel36Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jRadioButtonLightFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButtonComplexFile)
                        .addGap(32, 32, 32)
                        .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel36Layout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addComponent(jTextFieldOutputMergedFile))
                            .addComponent(jCheckBoxMergeOutputFiles))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(jButtonSopMatrix)
                        .addGroup(jPanel36Layout.createSequentialGroup()
                            .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(jButtonPopMatrix)
                                .addComponent(jButtonVopMatrix))
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jButton1))
                        .addComponent(jPanel42, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel36Layout.createSequentialGroup()
                            .addComponent(jPanel40, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(jPanel41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addComponent(jPanel19, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jButtonExecuteVoxTls, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(21, 21, 21))
        );
        jPanel36Layout.setVerticalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel36Layout.createSequentialGroup()
                .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel36Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButtonSopMatrix)
                        .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel36Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonPopMatrix)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonVopMatrix))
                            .addGroup(jPanel36Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(jButton1)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel40, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel41, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel42, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel19, javax.swing.GroupLayout.PREFERRED_SIZE, 72, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonExecuteVoxTls))
                    .addGroup(jPanel36Layout.createSequentialGroup()
                        .addComponent(jPanel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButtonLightFile)
                            .addComponent(jRadioButtonComplexFile)
                            .addComponent(jCheckBoxMergeOutputFiles))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldOutputMergedFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel35Layout = new javax.swing.GroupLayout(jPanel35);
        jPanel35.setLayout(jPanel35Layout);
        jPanel35Layout.setHorizontalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel35Layout.setVerticalGroup(
            jPanel35Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel36, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jTabbedPane5.addTab("TLS => VOXELS ", jPanel35);

        jTabbedPane2.addTab("TLS", jTabbedPane5);

        jTabbedPane3.setBackground(new java.awt.Color(114, 114, 114));

        jPanel33.setBackground(new java.awt.Color(114, 114, 114));

        jPanel16.setBackground(new java.awt.Color(114, 114, 114));
        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("Input file (*.laz, *.las, *.txt)"));

        jLabelName3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName3.setText("Name");
        jLabelName3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath3.setText("Path");

        jLabelSize1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelSize1.setText("Size (bytes)");

        jLabelFileSizeInputVox.setBackground(new java.awt.Color(180, 180, 180));
        jLabelFileSizeInputVox.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabelFileSizeInputVox.setMaximumSize(new java.awt.Dimension(40000, 40000));
        jLabelFileSizeInputVox.setName(""); // NOI18N
        jLabelFileSizeInputVox.setPreferredSize(new java.awt.Dimension(150, 20));

        jTextFieldFilePathInputVox.setEditable(false);
        jTextFieldFilePathInputVox.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFilePathInputVox.setColumns(38);
        jTextFieldFilePathInputVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathInputVoxActionPerformed(evt);
            }
        });

        jTextFieldFileNameInputVox.setEditable(false);
        jTextFieldFileNameInputVox.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFileNameInputVox.setColumns(50);
        jTextFieldFileNameInputVox.setToolTipText("");
        jTextFieldFileNameInputVox.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameInputVox.setName(""); // NOI18N

        jButtonOpenInputFileVoxelisation.setBackground(new java.awt.Color(85, 85, 85));
        jButtonOpenInputFileVoxelisation.setForeground(new java.awt.Color(255, 255, 255));
        jButtonOpenInputFileVoxelisation.setText("Open");
        jButtonOpenInputFileVoxelisation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenInputFileVoxelisationActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel16Layout = new javax.swing.GroupLayout(jPanel16);
        jPanel16.setLayout(jPanel16Layout);
        jPanel16Layout.setHorizontalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addComponent(jButtonOpenInputFileVoxelisation)
                        .addGap(34, 34, 34))
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName3)
                            .addComponent(jLabelPath3)
                            .addComponent(jLabelSize1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFilePathInputVox, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jLabelFileSizeInputVox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jTextFieldFileNameInputVox, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)))))
        );
        jPanel16Layout.setVerticalGroup(
            jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel16Layout.createSequentialGroup()
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileNameInputVox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilePathInputVox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPath3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelFileSizeInputVox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelSize1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpenInputFileVoxelisation)
                .addContainerGap())
        );

        jPanel18.setBackground(new java.awt.Color(114, 114, 114));
        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder("Trajectory file"));

        jLabelName5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName5.setText("Name");
        jLabelName5.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath5.setText("Path");

        jTextFieldFilePathTrajVox.setEditable(false);
        jTextFieldFilePathTrajVox.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFilePathTrajVox.setColumns(38);
        jTextFieldFilePathTrajVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathTrajVoxActionPerformed(evt);
            }
        });

        jTextFieldFileNameTrajVox.setEditable(false);
        jTextFieldFileNameTrajVox.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFileNameTrajVox.setColumns(50);
        jTextFieldFileNameTrajVox.setToolTipText("");
        jTextFieldFileNameTrajVox.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameTrajVox.setName(""); // NOI18N

        jButtonOpenTrajectoryFile.setBackground(new java.awt.Color(85, 85, 85));
        jButtonOpenTrajectoryFile.setForeground(new java.awt.Color(255, 255, 255));
        jButtonOpenTrajectoryFile.setText("Open");
        jButtonOpenTrajectoryFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenTrajectoryFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel18Layout = new javax.swing.GroupLayout(jPanel18);
        jPanel18.setLayout(jPanel18Layout);
        jPanel18Layout.setHorizontalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addComponent(jButtonOpenTrajectoryFile)
                        .addGap(34, 34, 34))
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName5)
                            .addComponent(jLabelPath5))
                        .addGap(40, 40, 40)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFilePathTrajVox, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jTextFieldFileNameTrajVox, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)))))
        );
        jPanel18Layout.setVerticalGroup(
            jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel18Layout.createSequentialGroup()
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileNameTrajVox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilePathTrajVox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPath5))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpenTrajectoryFile)
                .addContainerGap())
        );

        jPanel17.setBackground(new java.awt.Color(114, 114, 114));
        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder("Output file (*.vox)"));

        jLabelName4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName4.setText("Name");
        jLabelName4.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath4.setText("Path");

        jTextFieldFilePathSaveVox.setEditable(false);
        jTextFieldFilePathSaveVox.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFilePathSaveVox.setColumns(38);
        jTextFieldFilePathSaveVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathSaveVoxActionPerformed(evt);
            }
        });

        jTextFieldFileNameSaveVox.setEditable(false);
        jTextFieldFileNameSaveVox.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFileNameSaveVox.setColumns(50);
        jTextFieldFileNameSaveVox.setToolTipText("");
        jTextFieldFileNameSaveVox.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameSaveVox.setName(""); // NOI18N

        jButtonChooseOutputDirectoryVox.setBackground(new java.awt.Color(85, 85, 85));
        jButtonChooseOutputDirectoryVox.setForeground(new java.awt.Color(255, 255, 255));
        jButtonChooseOutputDirectoryVox.setText("Choose directory");
        jButtonChooseOutputDirectoryVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChooseOutputDirectoryVoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel17Layout = new javax.swing.GroupLayout(jPanel17);
        jPanel17.setLayout(jPanel17Layout);
        jPanel17Layout.setHorizontalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addComponent(jButtonChooseOutputDirectoryVox)
                        .addGap(37, 37, 37))
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName4)
                            .addComponent(jLabelPath4))
                        .addGap(39, 39, 39)
                        .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFilePathSaveVox, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                            .addComponent(jTextFieldFileNameSaveVox, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)))))
        );
        jPanel17Layout.setVerticalGroup(
            jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel17Layout.createSequentialGroup()
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileNameSaveVox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilePathSaveVox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPath4))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonChooseOutputDirectoryVox)
                .addContainerGap())
        );

        jButtonExecuteVoxAls.setBackground(new java.awt.Color(85, 85, 85));
        jButtonExecuteVoxAls.setForeground(new java.awt.Color(255, 255, 255));
        jButtonExecuteVoxAls.setText("Execute");
        jButtonExecuteVoxAls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecuteVoxAlsActionPerformed(evt);
            }
        });

        jPanel28.setBackground(new java.awt.Color(114, 114, 114));
        jPanel28.setBorder(javax.swing.BorderFactory.createTitledBorder("Reference points"));

        jPanel29.setBackground(new java.awt.Color(114, 114, 114));
        jPanel29.setBorder(javax.swing.BorderFactory.createTitledBorder("Point 1"));
        jPanel29.setToolTipText("");

        jTextFieldReferencePoint1X.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldReferencePoint1X.setText("0");
        jTextFieldReferencePoint1X.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));

        jTextFieldReferencePoint1Y.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldReferencePoint1Y.setText("0");
        jTextFieldReferencePoint1Y.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));

        jTextFieldReferencePoint1Z.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldReferencePoint1Z.setText("0");
        jTextFieldReferencePoint1Z.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));

        javax.swing.GroupLayout jPanel29Layout = new javax.swing.GroupLayout(jPanel29);
        jPanel29.setLayout(jPanel29Layout);
        jPanel29Layout.setHorizontalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createSequentialGroup()
                .addComponent(jTextFieldReferencePoint1X, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldReferencePoint1Y, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldReferencePoint1Z, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel29Layout.setVerticalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldReferencePoint1X, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldReferencePoint1Y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldReferencePoint1Z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel30.setBackground(new java.awt.Color(114, 114, 114));
        jPanel30.setBorder(javax.swing.BorderFactory.createTitledBorder("Point 2"));
        jPanel30.setToolTipText("");

        jTextFieldReferencePoint2X.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldReferencePoint2X.setText("0");
        jTextFieldReferencePoint2X.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));

        jTextFieldReferencePoint2Y.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldReferencePoint2Y.setText("0");
        jTextFieldReferencePoint2Y.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));

        jTextFieldReferencePoint2Z.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldReferencePoint2Z.setText("0");
        jTextFieldReferencePoint2Z.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));

        javax.swing.GroupLayout jPanel30Layout = new javax.swing.GroupLayout(jPanel30);
        jPanel30.setLayout(jPanel30Layout);
        jPanel30Layout.setHorizontalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createSequentialGroup()
                .addComponent(jTextFieldReferencePoint2X, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldReferencePoint2Y, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldReferencePoint2Z, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel30Layout.setVerticalGroup(
            jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel30Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldReferencePoint2X, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldReferencePoint2Y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldReferencePoint2Z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jButtonPopMatrix1.setBackground(new java.awt.Color(85, 85, 85));
        jButtonPopMatrix1.setForeground(new java.awt.Color(255, 255, 255));
        jButtonPopMatrix1.setText("VOP matrix");
        jButtonPopMatrix1.setToolTipText("Project orientation and position");
        jButtonPopMatrix1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPopMatrix1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel28Layout = new javax.swing.GroupLayout(jPanel28);
        jPanel28.setLayout(jPanel28Layout);
        jPanel28Layout.setHorizontalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addComponent(jButtonPopMatrix1)
                .addContainerGap())
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addComponent(jPanel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addGap(51, 51, 51)
                        .addComponent(jButtonPopMatrix1)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1.setBackground(new java.awt.Color(114, 114, 114));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Weighting"));

        jComboBoxWeighting.setBackground(new java.awt.Color(180, 180, 180));
        jComboBoxWeighting.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No weighting", "From the echo number", "From a parameter file", "Local recalculation " }));
        jComboBoxWeighting.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxWeightingItemStateChanged(evt);
            }
        });

        jButtonOpenWeightingFile.setBackground(new java.awt.Color(85, 85, 85));
        jButtonOpenWeightingFile.setForeground(new java.awt.Color(255, 255, 255));
        jButtonOpenWeightingFile.setText("Open file");
        jButtonOpenWeightingFile.setEnabled(false);
        jButtonOpenWeightingFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenWeightingFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jComboBoxWeighting, javax.swing.GroupLayout.PREFERRED_SIZE, 135, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpenWeightingFile)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBoxWeighting, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonOpenWeightingFile))
                .addGap(0, 13, Short.MAX_VALUE))
        );

        jPanel2.setBackground(new java.awt.Color(114, 114, 114));
        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Bounding box"));

        jPanel26.setBackground(new java.awt.Color(114, 114, 114));
        jPanel26.setBorder(javax.swing.BorderFactory.createTitledBorder("Max point"));
        jPanel26.setToolTipText("");

        jTextFieldMaxPointX.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMaxPointX.setText("10");
        jTextFieldMaxPointX.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldMaxPointX.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMaxPointXKeyTyped(evt);
            }
        });

        jTextFieldMaxPointY.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMaxPointY.setText("150");
        jTextFieldMaxPointY.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldMaxPointY.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMaxPointYKeyTyped(evt);
            }
        });

        jTextFieldMaxPointZ.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMaxPointZ.setText("70");
        jTextFieldMaxPointZ.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));
        jTextFieldMaxPointZ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMaxPointZKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel26Layout = new javax.swing.GroupLayout(jPanel26);
        jPanel26.setLayout(jPanel26Layout);
        jPanel26Layout.setHorizontalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createSequentialGroup()
                .addComponent(jTextFieldMaxPointX, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMaxPointY, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMaxPointZ, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel26Layout.setVerticalGroup(
            jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel26Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldMaxPointX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldMaxPointY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldMaxPointZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel27.setBackground(new java.awt.Color(114, 114, 114));
        jPanel27.setBorder(javax.swing.BorderFactory.createTitledBorder("Min point"));
        jPanel27.setToolTipText("");

        jTextFieldMinPointX.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMinPointX.setText("-10");
        jTextFieldMinPointX.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldMinPointX.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMinPointXKeyTyped(evt);
            }
        });

        jTextFieldMinPointY.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMinPointY.setText("50");
        jTextFieldMinPointY.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldMinPointY.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMinPointYKeyTyped(evt);
            }
        });

        jTextFieldMinPointZ.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldMinPointZ.setText("0");
        jTextFieldMinPointZ.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));
        jTextFieldMinPointZ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMinPointZKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel27Layout = new javax.swing.GroupLayout(jPanel27);
        jPanel27.setLayout(jPanel27Layout);
        jPanel27Layout.setHorizontalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createSequentialGroup()
                .addComponent(jTextFieldMinPointX, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMinPointY, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldMinPointZ, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel27Layout.setVerticalGroup(
            jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel27Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldMinPointX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldMinPointY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldMinPointZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel25.setBackground(new java.awt.Color(114, 114, 114));
        jPanel25.setBorder(javax.swing.BorderFactory.createTitledBorder("Voxel Number"));
        jPanel25.setToolTipText("");

        jTextFieldVoxelNumberX.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldVoxelNumberX.setText("20");
        jTextFieldVoxelNumberX.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldVoxelNumberX.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberXKeyTyped(evt);
            }
        });

        jTextFieldVoxelNumberY.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldVoxelNumberY.setText("100");
        jTextFieldVoxelNumberY.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldVoxelNumberY.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberYKeyTyped(evt);
            }
        });

        jTextFieldVoxelNumberZ.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldVoxelNumberZ.setText("70");
        jTextFieldVoxelNumberZ.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));
        jTextFieldVoxelNumberZ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberZKeyTyped(evt);
            }
        });

        jTextFieldVoxelNumberRes.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldVoxelNumberRes.setText("1");
        jTextFieldVoxelNumberRes.setBorder(javax.swing.BorderFactory.createTitledBorder("Resolution"));

        javax.swing.GroupLayout jPanel25Layout = new javax.swing.GroupLayout(jPanel25);
        jPanel25.setLayout(jPanel25Layout);
        jPanel25Layout.setHorizontalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createSequentialGroup()
                .addComponent(jTextFieldVoxelNumberX, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldVoxelNumberY, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldVoxelNumberZ, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextFieldVoxelNumberRes, javax.swing.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE))
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldVoxelNumberX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldVoxelNumberY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldVoxelNumberZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldVoxelNumberRes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jButtonCalculateBoundingBox.setBackground(new java.awt.Color(85, 85, 85));
        jButtonCalculateBoundingBox.setForeground(new java.awt.Color(255, 255, 255));
        jButtonCalculateBoundingBox.setText("Automatic");
        jButtonCalculateBoundingBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCalculateBoundingBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jPanel25, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonCalculateBoundingBox)
                        .addGap(3, 3, 3)))
                .addGap(72, 72, 72))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel26, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jButtonCalculateBoundingBox)
                        .addContainerGap())))
        );

        jCheckBoxUseDTM.setBackground(new java.awt.Color(85, 85, 85));
        jCheckBoxUseDTM.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBoxUseDTM.setSelected(true);
        jCheckBoxUseDTM.setText("Use DTM");

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel33Layout.createSequentialGroup()
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel33Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxUseDTM)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButtonExecuteVoxAls, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel33Layout.setVerticalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel33Layout.createSequentialGroup()
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel33Layout.createSequentialGroup()
                        .addComponent(jPanel16, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel18, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel33Layout.createSequentialGroup()
                        .addComponent(jPanel28, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel33Layout.createSequentialGroup()
                                .addGap(18, 18, 18)
                                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(jPanel33Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                    .addGroup(jPanel33Layout.createSequentialGroup()
                                        .addComponent(jButtonExecuteVoxAls)
                                        .addGap(0, 0, Short.MAX_VALUE))))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel33Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jCheckBoxUseDTM)
                                .addGap(19, 19, 19)))))
                .addContainerGap())
        );

        jTabbedPane3.addTab("Voxelisation", jPanel33);

        jPanel31.setBackground(new java.awt.Color(114, 114, 114));

        jPanel6.setBackground(new java.awt.Color(114, 114, 114));
        jPanel6.setMaximumSize(new java.awt.Dimension(100, 32767));
        jPanel6.setPreferredSize(new java.awt.Dimension(100, 273));

        jPanel13.setBackground(new java.awt.Color(114, 114, 114));
        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Input file (*.laz, *.las)"));

        jLabelName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName.setText("Name");
        jLabelName.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath.setText("Path");

        jLabelSize.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelSize.setText("Size (bytes)");

        jLabelFileSize.setBackground(new java.awt.Color(180, 180, 180));
        jLabelFileSize.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabelFileSize.setMaximumSize(new java.awt.Dimension(40000, 40000));
        jLabelFileSize.setName(""); // NOI18N
        jLabelFileSize.setPreferredSize(new java.awt.Dimension(150, 20));

        jTextFieldFilePathInputTxt.setEditable(false);
        jTextFieldFilePathInputTxt.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFilePathInputTxt.setColumns(38);
        jTextFieldFilePathInputTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathInputTxtActionPerformed(evt);
            }
        });

        jTextFieldFileNameInputTxt.setEditable(false);
        jTextFieldFileNameInputTxt.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFileNameInputTxt.setColumns(50);
        jTextFieldFileNameInputTxt.setToolTipText("");
        jTextFieldFileNameInputTxt.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameInputTxt.setName(""); // NOI18N

        jButtonOpenInputFile.setBackground(new java.awt.Color(85, 85, 85));
        jButtonOpenInputFile.setForeground(new java.awt.Color(255, 255, 255));
        jButtonOpenInputFile.setText("Open");
        jButtonOpenInputFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenInputFileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel13Layout = new javax.swing.GroupLayout(jPanel13);
        jPanel13.setLayout(jPanel13Layout);
        jPanel13Layout.setHorizontalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonOpenInputFile)
                    .addGroup(jPanel13Layout.createSequentialGroup()
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName)
                            .addComponent(jLabelPath)
                            .addComponent(jLabelSize))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFileNameInputTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jTextFieldFilePathInputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jLabelFileSize, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addGap(34, 34, 34))
        );
        jPanel13Layout.setVerticalGroup(
            jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel13Layout.createSequentialGroup()
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileNameInputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilePathInputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPath))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel13Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelFileSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelSize))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpenInputFile))
        );

        jPanel15.setBackground(new java.awt.Color(114, 114, 114));
        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Output file (*.txt)"));

        jLabelName2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName2.setText("Name");
        jLabelName2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath2.setText("Path");

        jTextFieldFilePathOutputTxt.setEditable(false);
        jTextFieldFilePathOutputTxt.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFilePathOutputTxt.setColumns(38);
        jTextFieldFilePathOutputTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathOutputTxtActionPerformed(evt);
            }
        });

        jTextFieldFileNameOutputTxt.setEditable(false);
        jTextFieldFileNameOutputTxt.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFileNameOutputTxt.setColumns(50);
        jTextFieldFileNameOutputTxt.setToolTipText("");
        jTextFieldFileNameOutputTxt.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameOutputTxt.setName(""); // NOI18N

        jButtonChooseDirectory.setBackground(new java.awt.Color(85, 85, 85));
        jButtonChooseDirectory.setForeground(new java.awt.Color(255, 255, 255));
        jButtonChooseDirectory.setText("Choose directory");
        jButtonChooseDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChooseDirectoryActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel15Layout = new javax.swing.GroupLayout(jPanel15);
        jPanel15.setLayout(jPanel15Layout);
        jPanel15Layout.setHorizontalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addComponent(jButtonChooseDirectory)
                        .addGap(0, 157, Short.MAX_VALUE))
                    .addGroup(jPanel15Layout.createSequentialGroup()
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName2)
                            .addComponent(jLabelPath2))
                        .addGap(64, 64, 64)
                        .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFileNameOutputTxt, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jTextFieldFilePathOutputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel15Layout.setVerticalGroup(
            jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel15Layout.createSequentialGroup()
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileNameOutputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel15Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilePathOutputTxt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPath2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonChooseDirectory))
        );

        jCheckBoxWriteX.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteX.setSelected(true);
        jCheckBoxWriteX.setText("(x)");
        jCheckBoxWriteX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteXStateChanged(evt);
            }
        });

        jCheckBoxWriteY.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteY.setSelected(true);
        jCheckBoxWriteY.setText("(y)");
        jCheckBoxWriteY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteYStateChanged(evt);
            }
        });

        jCheckBoxWriteZ.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteZ.setSelected(true);
        jCheckBoxWriteZ.setText("(z)");
        jCheckBoxWriteZ.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteZStateChanged(evt);
            }
        });

        jCheckBoxWriteI.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteI.setSelected(true);
        jCheckBoxWriteI.setText("(i)ntensity");
        jCheckBoxWriteI.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteIStateChanged(evt);
            }
        });

        jCheckBoxWriteR.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteR.setSelected(true);
        jCheckBoxWriteR.setText("(r)eturn number");
        jCheckBoxWriteR.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteRStateChanged(evt);
            }
        });

        jCheckBoxWriteN.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteN.setSelected(true);
        jCheckBoxWriteN.setText("(n)umber of returns");
        jCheckBoxWriteN.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteNStateChanged(evt);
            }
        });

        jCheckBoxWriteD.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteD.setText("scan (d)irection");
        jCheckBoxWriteD.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteDStateChanged(evt);
            }
        });

        jCheckBoxWriteE.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteE.setText("(e)dge of flight line");
        jCheckBoxWriteE.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteEStateChanged(evt);
            }
        });

        jCheckBoxWriteC.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteC.setSelected(true);
        jCheckBoxWriteC.setText("(c)lassification");
        jCheckBoxWriteC.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteCStateChanged(evt);
            }
        });

        jCheckBoxWriteA.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteA.setSelected(true);
        jCheckBoxWriteA.setText("scan (a)ngle");
        jCheckBoxWriteA.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteAStateChanged(evt);
            }
        });

        jCheckBoxWriteU.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteU.setText("(u)ser data");
        jCheckBoxWriteU.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteUStateChanged(evt);
            }
        });

        jCheckBoxWriteP.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteP.setSelected(true);
        jCheckBoxWriteP.setText("(p)oint source ID");
        jCheckBoxWriteP.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWritePStateChanged(evt);
            }
        });

        jCheckBoxWriteT.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteT.setSelected(true);
        jCheckBoxWriteT.setText("GPS (t)ime ");
        jCheckBoxWriteT.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteTStateChanged(evt);
            }
        });

        jCheckBoxWriteRGB.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteRGB.setText("(RGB)color");
        jCheckBoxWriteRGB.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteRGBStateChanged(evt);
            }
        });

        jCheckBoxWritew.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWritew.setText("(w)ave packet index");
        jCheckBoxWritew.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWritewStateChanged(evt);
            }
        });

        jCheckBoxWriteW.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteW.setText("(W)ave packet");
        jCheckBoxWriteW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteWStateChanged(evt);
            }
        });

        jCheckBoxWriteV.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteV.setText("wa(V)e form");
        jCheckBoxWriteV.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteVStateChanged(evt);
            }
        });

        jPanel24.setBackground(new java.awt.Color(114, 114, 114));

        jButtonLoad.setBackground(new java.awt.Color(85, 85, 85));
        jButtonLoad.setForeground(new java.awt.Color(255, 255, 255));
        jButtonLoad.setText("Execute");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });

        jCheckBoxWriteHeader.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxWriteHeader.setSelected(true);
        jCheckBoxWriteHeader.setText("write header");

        jTextFieldArguments.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldArguments.setBorder(javax.swing.BorderFactory.createTitledBorder("Arguments"));

        javax.swing.GroupLayout jPanel24Layout = new javax.swing.GroupLayout(jPanel24);
        jPanel24.setLayout(jPanel24Layout);
        jPanel24Layout.setHorizontalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonLoad, javax.swing.GroupLayout.PREFERRED_SIZE, 87, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTextFieldArguments, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jCheckBoxWriteHeader)
                .addGap(42, 42, 42))
        );
        jPanel24Layout.setVerticalGroup(
            jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel24Layout.createSequentialGroup()
                .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel24Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonLoad)
                            .addComponent(jTextFieldArguments, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel24Layout.createSequentialGroup()
                        .addGap(16, 16, 16)
                        .addComponent(jCheckBoxWriteHeader)))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel24, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(265, 265, 265))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(100, 100, 100))
                    .addGroup(jPanel6Layout.createSequentialGroup()
                        .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jCheckBoxWriteX)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteY)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteZ)
                                .addGap(6, 6, 6)
                                .addComponent(jCheckBoxWriteI)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteR)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteN)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteD)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteE))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jCheckBoxWriteC)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteA)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteU)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteP)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteT)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteRGB))
                            .addGroup(jPanel6Layout.createSequentialGroup()
                                .addComponent(jCheckBoxWritew)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteW)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jCheckBoxWriteV)))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel15, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel13, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxWriteX)
                    .addComponent(jCheckBoxWriteY)
                    .addComponent(jCheckBoxWriteZ)
                    .addComponent(jCheckBoxWriteI)
                    .addComponent(jCheckBoxWriteN)
                    .addComponent(jCheckBoxWriteR)
                    .addComponent(jCheckBoxWriteD)
                    .addComponent(jCheckBoxWriteE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxWriteC)
                    .addComponent(jCheckBoxWriteA)
                    .addComponent(jCheckBoxWriteU)
                    .addComponent(jCheckBoxWriteP)
                    .addComponent(jCheckBoxWriteT)
                    .addComponent(jCheckBoxWriteRGB))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxWritew)
                    .addComponent(jCheckBoxWriteW)
                    .addComponent(jCheckBoxWriteV))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel24, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(120, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 748, Short.MAX_VALUE)
            .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 748, Short.MAX_VALUE))
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 394, Short.MAX_VALUE)
            .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("LAS => TXT", jPanel31);

        jTabbedPane2.addTab("ALS", jTabbedPane3);

        javax.swing.GroupLayout jPanelOutputParametersTabLayout = new javax.swing.GroupLayout(jPanelOutputParametersTab);
        jPanelOutputParametersTab.setLayout(jPanelOutputParametersTabLayout);
        jPanelOutputParametersTabLayout.setHorizontalGroup(
            jPanelOutputParametersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOutputParametersTabLayout.createSequentialGroup()
                .addComponent(jTabbedPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 804, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanelOutputParametersTabLayout.setVerticalGroup(
            jPanelOutputParametersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jTabbedPane1.addTab("File conversion", jPanelOutputParametersTab);

        jPanelVisualizeTab.setBackground(new java.awt.Color(114, 114, 114));

        jTabbedPane4.setBackground(new java.awt.Color(83, 83, 83));
        jTabbedPane4.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        jPanel3.setBackground(new java.awt.Color(114, 114, 114));

        jButtonOpen3DDisplay.setBackground(new java.awt.Color(85, 85, 85));
        jButtonOpen3DDisplay.setForeground(new java.awt.Color(255, 255, 255));
        jButtonOpen3DDisplay.setText("Open display window");
        jButtonOpen3DDisplay.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonOpen3DDisplay.setEnabled(false);
        jButtonOpen3DDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpen3DDisplayActionPerformed(evt);
            }
        });

        jPanel3DViewDensity.setBackground(new java.awt.Color(114, 114, 114));
        jPanel3DViewDensity.setBorder(javax.swing.BorderFactory.createTitledBorder(customBorder, "Input file", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, java.awt.Color.black));

        jPanel14.setBackground(new java.awt.Color(114, 114, 114));
        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Attribute to visualize"));

        jComboBoxAttributeToVisualize.setBackground(new java.awt.Color(180, 180, 180));
        jComboBoxAttributeToVisualize.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxAttributeToVisualizeItemStateChanged(evt);
            }
        });
        jComboBoxAttributeToVisualize.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jComboBoxAttributeToVisualizePropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel14Layout = new javax.swing.GroupLayout(jPanel14);
        jPanel14.setLayout(jPanel14Layout);
        jPanel14Layout.setHorizontalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBoxAttributeToVisualize, javax.swing.GroupLayout.PREFERRED_SIZE, 126, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel14Layout.setVerticalGroup(
            jPanel14Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel14Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jComboBoxAttributeToVisualize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jButtonCreateAttribut.setBackground(new java.awt.Color(85, 85, 85));
        jButtonCreateAttribut.setForeground(new java.awt.Color(255, 255, 255));
        jButtonCreateAttribut.setText("Create attribut");
        jButtonCreateAttribut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateAttributActionPerformed(evt);
            }
        });

        jLabel1.setText("X = ");

        jComboBoxDefaultX.setBackground(new java.awt.Color(180, 180, 180));

        jLabel3.setText("Y = ");

        jComboBoxDefaultY.setBackground(new java.awt.Color(180, 180, 180));

        jLabel4.setText("Z = ");

        jComboBoxDefaultZ.setBackground(new java.awt.Color(180, 180, 180));

        jLabel5.setText("Expression");

        jTextFieldAttributExpression.setBackground(new java.awt.Color(180, 180, 180));

        javax.swing.GroupLayout jPanel3DViewDensityLayout = new javax.swing.GroupLayout(jPanel3DViewDensity);
        jPanel3DViewDensity.setLayout(jPanel3DViewDensityLayout);
        jPanel3DViewDensityLayout.setHorizontalGroup(
            jPanel3DViewDensityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3DViewDensityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                        .addComponent(jPanel14, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3DViewDensityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                                .addGap(22, 22, 22)
                                .addComponent(jLabel5))
                            .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldAttributExpression, javax.swing.GroupLayout.PREFERRED_SIZE, 178, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(55, 55, 55))
                    .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                        .addGroup(jPanel3DViewDensityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxDefaultX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                                .addComponent(jLabel3)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxDefaultY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jComboBoxDefaultZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                        .addComponent(jButtonCreateAttribut)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel3DViewDensityLayout.setVerticalGroup(
            jPanel3DViewDensityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                .addGroup(jPanel3DViewDensityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                        .addGap(19, 19, 19)
                        .addComponent(jPanel14, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel3DViewDensityLayout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jTextFieldAttributExpression, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 18, Short.MAX_VALUE)
                .addComponent(jButtonCreateAttribut)
                .addGap(48, 48, 48)
                .addGroup(jPanel3DViewDensityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jComboBoxDefaultX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3DViewDensityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jComboBoxDefaultY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3DViewDensityLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jComboBoxDefaultZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jCheckBoxDrawTerrain.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxDrawTerrain.setText("Draw DTM");
        jCheckBoxDrawTerrain.setEnabled(false);

        jCheckBoxDrawAxis.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxDrawAxis.setText("Draw axis");

        jCheckBoxDrawNullVoxel.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxDrawNullVoxel.setText("Draw voxel when null value");
        jCheckBoxDrawNullVoxel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxDrawNullVoxelStateChanged(evt);
            }
        });
        jCheckBoxDrawNullVoxel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDrawNullVoxelActionPerformed(evt);
            }
        });

        jCheckBoxDrawUndergroundVoxel.setBackground(new java.awt.Color(114, 114, 114));
        jCheckBoxDrawUndergroundVoxel.setText("Draw voxel under dtm ground");
        jCheckBoxDrawUndergroundVoxel.setEnabled(false);
        jCheckBoxDrawUndergroundVoxel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxDrawUndergroundVoxelStateChanged(evt);
            }
        });
        jCheckBoxDrawUndergroundVoxel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDrawUndergroundVoxelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jButtonOpen3DDisplay))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel3DViewDensity, javax.swing.GroupLayout.PREFERRED_SIZE, 388, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBoxDrawNullVoxel)
                            .addComponent(jCheckBoxDrawUndergroundVoxel)
                            .addComponent(jCheckBoxDrawAxis)
                            .addComponent(jCheckBoxDrawTerrain))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jCheckBoxDrawNullVoxel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxDrawUndergroundVoxel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxDrawAxis)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBoxDrawTerrain))
                    .addComponent(jPanel3DViewDensity, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButtonOpen3DDisplay)
                .addGap(24, 24, 24))
        );

        jTabbedPane4.addTab("3D view", jPanel3);

        jPanel4.setBackground(new java.awt.Color(114, 114, 114));

        jButtonGenerateMap.setBackground(new java.awt.Color(85, 85, 85));
        jButtonGenerateMap.setForeground(new java.awt.Color(255, 255, 255));
        jButtonGenerateMap.setText("Generate map");
        jButtonGenerateMap.setToolTipText("Load a terrain first");
        jButtonGenerateMap.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonGenerateMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenerateMapActionPerformed(evt);
            }
        });

        jRadioButtonPAI.setBackground(new java.awt.Color(114, 114, 114));
        buttonGroup2DProjection.add(jRadioButtonPAI);
        jRadioButtonPAI.setSelected(true);
        jRadioButtonPAI.setText("PAI map (plant area index)");

        jRadioButtonTransmittanceMap.setBackground(new java.awt.Color(114, 114, 114));
        buttonGroup2DProjection.add(jRadioButtonTransmittanceMap);
        jRadioButtonTransmittanceMap.setText("Transmittance map");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonTransmittanceMap)
                    .addComponent(jRadioButtonPAI)
                    .addComponent(jButtonGenerateMap))
                .addContainerGap(502, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonPAI)
                .addGap(15, 15, 15)
                .addComponent(jRadioButtonTransmittanceMap)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 278, Short.MAX_VALUE)
                .addComponent(jButtonGenerateMap)
                .addGap(42, 42, 42))
        );

        jTabbedPane4.addTab("2D projection", jPanel4);

        jPanel5.setBackground(new java.awt.Color(114, 114, 114));

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 688, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jTabbedPane4.addTab("Histogram", jPanel5);

        jPanelVegetationProfile.setBackground(new java.awt.Color(114, 114, 114));

        jRadioButton1.setBackground(new java.awt.Color(114, 114, 114));
        buttonGroup2.add(jRadioButton1);
        jRadioButton1.setText("X, Height");

        jRadioButton2.setBackground(new java.awt.Color(114, 114, 114));
        buttonGroup2.add(jRadioButton2);
        jRadioButton2.setText("Y, Height");

        jButtonGenerateProfile.setBackground(new java.awt.Color(85, 85, 85));
        jButtonGenerateProfile.setForeground(new java.awt.Color(255, 255, 255));
        jButtonGenerateProfile.setText("Generate profile");
        jButtonGenerateProfile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenerateProfileActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelVegetationProfileLayout = new javax.swing.GroupLayout(jPanelVegetationProfile);
        jPanelVegetationProfile.setLayout(jPanelVegetationProfileLayout);
        jPanelVegetationProfileLayout.setHorizontalGroup(
            jPanelVegetationProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelVegetationProfileLayout.createSequentialGroup()
                .addGap(127, 127, 127)
                .addGroup(jPanelVegetationProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonGenerateProfile)
                    .addGroup(jPanelVegetationProfileLayout.createSequentialGroup()
                        .addComponent(jRadioButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jRadioButton2)))
                .addContainerGap(408, Short.MAX_VALUE))
        );
        jPanelVegetationProfileLayout.setVerticalGroup(
            jPanelVegetationProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelVegetationProfileLayout.createSequentialGroup()
                .addGap(49, 49, 49)
                .addGroup(jPanelVegetationProfileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2))
                .addGap(18, 18, 18)
                .addComponent(jButtonGenerateProfile)
                .addContainerGap(310, Short.MAX_VALUE))
        );

        jTabbedPane4.addTab("Vegetation profile", jPanelVegetationProfile);

        jPanel9.setBackground(new java.awt.Color(114, 114, 114));

        jPanel11.setBackground(new java.awt.Color(114, 114, 114));
        jPanel11.setBorder(javax.swing.BorderFactory.createTitledBorder("Horizontal axis"));

        jComboBoxHorizontalAxisValue.setBackground(new java.awt.Color(180, 180, 180));
        jComboBoxHorizontalAxisValue.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jComboBoxHorizontalAxisValue.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxHorizontalAxisValueItemStateChanged(evt);
            }
        });
        jComboBoxHorizontalAxisValue.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jComboBoxHorizontalAxisValuePropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jComboBoxHorizontalAxisValue, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jComboBoxHorizontalAxisValue)
        );

        jPanel20.setBackground(new java.awt.Color(114, 114, 114));
        jPanel20.setBorder(javax.swing.BorderFactory.createTitledBorder("Vertical axis"));

        jComboBoxverticalAxisValue.setBackground(new java.awt.Color(180, 180, 180));
        jComboBoxverticalAxisValue.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jComboBoxverticalAxisValue.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                jComboBoxverticalAxisValueItemStateChanged(evt);
            }
        });
        jComboBoxverticalAxisValue.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                jComboBoxverticalAxisValuePropertyChange(evt);
            }
        });

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jComboBoxverticalAxisValue, javax.swing.GroupLayout.PREFERRED_SIZE, 165, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addComponent(jComboBoxverticalAxisValue)
                .addGap(0, 0, 0))
        );

        jPanel12.setBackground(new java.awt.Color(114, 114, 114));
        jPanel12.setBorder(javax.swing.BorderFactory.createTitledBorder("Filters"));

        jListFilters.setBackground(new java.awt.Color(180, 180, 180));
        jScrollPane3.setViewportView(jListFilters);

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 240, Short.MAX_VALUE)
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        jButtonAddFilter.setBackground(new java.awt.Color(85, 85, 85));
        jButtonAddFilter.setForeground(new java.awt.Color(255, 255, 255));
        jButtonAddFilter.setText("Add");
        jButtonAddFilter.setPreferredSize(new java.awt.Dimension(77, 26));
        jButtonAddFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddFilterActionPerformed(evt);
            }
        });

        jButtonRemoveFilter.setBackground(new java.awt.Color(85, 85, 85));
        jButtonRemoveFilter.setForeground(new java.awt.Color(255, 255, 255));
        jButtonRemoveFilter.setText("remove");
        jButtonRemoveFilter.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveFilterActionPerformed(evt);
            }
        });

        jButtonDrawChart.setBackground(new java.awt.Color(85, 85, 85));
        jButtonDrawChart.setForeground(new java.awt.Color(255, 255, 255));
        jButtonDrawChart.setText("Draw chart");
        jButtonDrawChart.setPreferredSize(new java.awt.Dimension(77, 26));
        jButtonDrawChart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonDrawChartActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonDrawChart, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18)
                        .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButtonRemoveFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonAddFilter, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(150, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel9Layout.createSequentialGroup()
                                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addGap(30, 30, 30)
                        .addComponent(jButtonAddFilter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveFilter)))
                .addGap(51, 51, 51)
                .addComponent(jButtonDrawChart, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(232, Short.MAX_VALUE))
        );

        jTabbedPane4.addTab("Chart", jPanel9);

        javax.swing.GroupLayout jPanelVisualizeTabLayout = new javax.swing.GroupLayout(jPanelVisualizeTab);
        jPanelVisualizeTab.setLayout(jPanelVisualizeTabLayout);
        jPanelVisualizeTabLayout.setHorizontalGroup(
            jPanelVisualizeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane4)
        );
        jPanelVisualizeTabLayout.setVerticalGroup(
            jPanelVisualizeTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane4)
        );

        jTabbedPane1.addTab("Visualize", jPanelVisualizeTab);

        jPanel7.add(jTabbedPane1);

        jSplitPane2.setBackground(new java.awt.Color(114, 114, 114));
        jSplitPane2.setDividerLocation(340);
        jSplitPane2.setContinuousLayout(true);
        jSplitPane2.setDoubleBuffered(true);

        jPanel8.setBackground(new java.awt.Color(114, 114, 114));
        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Voxel files"));
        jPanel8.setPreferredSize(new java.awt.Dimension(200, 138));

        jButtonAddFile.setBackground(new java.awt.Color(85, 85, 85));
        jButtonAddFile.setForeground(new java.awt.Color(255, 255, 255));
        jButtonAddFile.setText("Add");
        jButtonAddFile.setPreferredSize(new java.awt.Dimension(77, 26));
        jButtonAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddFileActionPerformed(evt);
            }
        });

        jButtonRemoveFile.setBackground(new java.awt.Color(85, 85, 85));
        jButtonRemoveFile.setForeground(new java.awt.Color(255, 255, 255));
        jButtonRemoveFile.setText("remove");
        jButtonRemoveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveFileActionPerformed(evt);
            }
        });

        jListOutputFiles.setBackground(new java.awt.Color(180, 180, 180));
        jListOutputFiles.setBorder(javax.swing.BorderFactory.createTitledBorder("Select a file"));
        jListOutputFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListOutputFiles.setSelectedIndex(0);
        jListOutputFiles.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListOutputFilesValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListOutputFiles);

        jButtonLoadSelectedFile.setBackground(new java.awt.Color(85, 85, 85));
        jButtonLoadSelectedFile.setForeground(new java.awt.Color(255, 255, 255));
        jButtonLoadSelectedFile.setText("Load selection");
        jButtonLoadSelectedFile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonLoadSelectedFileMouseClicked(evt);
            }
        });
        jButtonLoadSelectedFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadSelectedFileActionPerformed(evt);
            }
        });

        jButtonExportSelection.setBackground(new java.awt.Color(85, 85, 85));
        jButtonExportSelection.setForeground(new java.awt.Color(255, 255, 255));
        jButtonExportSelection.setText("Export selection");
        jButtonExportSelection.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jButtonExportSelectionMouseClicked(evt);
            }
        });
        jButtonExportSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExportSelectionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonExportSelection)
                    .addComponent(jButtonLoadSelectedFile)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addComponent(jButtonAddFile, javax.swing.GroupLayout.PREFERRED_SIZE, 58, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonRemoveFile)))
                .addGap(5, 5, 5))
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonAddFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonRemoveFile))
                        .addGap(26, 26, 26)
                        .addComponent(jButtonLoadSelectedFile)
                        .addGap(8, 8, 8)
                        .addComponent(jButtonExportSelection)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel8Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jSplitPane2.setRightComponent(jPanel8);

        jPanel10.setBackground(new java.awt.Color(114, 114, 114));
        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("DTM"));
        jPanel10.setPreferredSize(new java.awt.Dimension(200, 138));

        jButtonOpenInputFile1.setBackground(new java.awt.Color(85, 85, 85));
        jButtonOpenInputFile1.setForeground(new java.awt.Color(255, 255, 255));
        jButtonOpenInputFile1.setText("Open");
        jButtonOpenInputFile1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jButtonOpenInputFile1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenInputFile1ActionPerformed(evt);
            }
        });

        jTextFieldFileNameMnt.setEditable(false);
        jTextFieldFileNameMnt.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFileNameMnt.setColumns(50);
        jTextFieldFileNameMnt.setToolTipText("");
        jTextFieldFileNameMnt.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameMnt.setName(""); // NOI18N

        jLabelName1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName1.setText("Name");
        jLabelName1.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath1.setText("Path");

        jTextFieldFilePathMnt.setEditable(false);
        jTextFieldFilePathMnt.setBackground(new java.awt.Color(180, 180, 180));
        jTextFieldFilePathMnt.setColumns(38);
        jTextFieldFilePathMnt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathMntActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel10Layout = new javax.swing.GroupLayout(jPanel10);
        jPanel10.setLayout(jPanel10Layout);
        jPanel10Layout.setHorizontalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel10Layout.createSequentialGroup()
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName1)
                            .addComponent(jLabelPath1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFilePathMnt, javax.swing.GroupLayout.DEFAULT_SIZE, 261, Short.MAX_VALUE)
                            .addComponent(jTextFieldFileNameMnt, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel10Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jButtonOpenInputFile1)))
                .addContainerGap())
        );
        jPanel10Layout.setVerticalGroup(
            jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel10Layout.createSequentialGroup()
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileNameMnt, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel10Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelPath1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jTextFieldFilePathMnt, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpenInputFile1)
                .addGap(42, 42, 42))
        );

        jSplitPane2.setLeftComponent(jPanel10);

        jPanel7.add(jSplitPane2);

        getContentPane().add(jPanel7, java.awt.BorderLayout.CENTER);

        jMenuBar1.setBackground(new java.awt.Color(114, 114, 114));

        jMenu1.setText("File");

        jMenu3.setText("Parameters");

        jMenuItemLoad.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemLoad.setText("Load");
        jMenuItemLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemLoadActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemLoad);

        jMenuItemSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSave.setText("Save");
        jMenu3.add(jMenuItemSave);

        jMenuItemSaveAs.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItemSaveAs.setText("Save as");
        jMenuItemSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItemSaveAsActionPerformed(evt);
            }
        });
        jMenu3.add(jMenuItemSaveAs);

        jMenu1.add(jMenu3);

        jMenuBar1.add(jMenu1);

        jMenuSettings.setText("Settings");

        jMenuAppearance.setText("Appearance");
        jMenuSettings.add(jMenuAppearance);

        jMenuBar1.add(jMenuSettings);

        jMenu2.setText("About");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized

        
    }//GEN-LAST:event_formComponentResized

    private void jMenuItemLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadActionPerformed
        
        if (jFileChooser3.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            
            openJFrameStateFile(jFileChooser3.getSelectedFile().getAbsolutePath());
            
        }
    }//GEN-LAST:event_jMenuItemLoadActionPerformed

    private void jButtonAddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddFileActionPerformed
        
        if (jFileChooser2.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            addElementToVoxList(jFileChooser2.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonAddFileActionPerformed

    private void jButtonRemoveFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveFileActionPerformed
        
        try{
            int index = jListOutputFiles.getSelectedIndex();
            model.remove(index);
            
            //jListOutputFiles.setModel(model);
            
        }catch(Exception e){
            logger.error("cannot remove file from list", e);
        }
        
    }//GEN-LAST:event_jButtonRemoveFileActionPerformed

    private void jMenuItemSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemSaveAsActionPerformed
        
        /*ici on sauvegarde les valeurs des paramètres tels que le contenu des jtextfield et
        *la sélection ou non des radiobuttons
        */
        if (jFileChooser3.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            
            saveJFrameState(jFileChooser3.getSelectedFile().getAbsolutePath());

        }
        
        
       
    }//GEN-LAST:event_jMenuItemSaveAsActionPerformed

    private void jButtonOpenInputFile1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenInputFile1ActionPerformed
        
        if (jFileChooser1.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            
            
            if(isTerrainFile(jFileChooser1.getSelectedFile())){
                jTextFieldFileNameMnt.setText(jFileChooser1.getSelectedFile().getName());
                jTextFieldFileNameMnt.setToolTipText(jFileChooser1.getSelectedFile().getName());

                jTextFieldFilePathMnt.setText(jFileChooser1.getSelectedFile().getAbsolutePath());
                jTextFieldFilePathMnt.setToolTipText(jFileChooser1.getSelectedFile().getAbsolutePath());

                jCheckBoxDrawTerrain.setEnabled(true);
                jCheckBoxDrawUndergroundVoxel.setEnabled(true);
                
            }else{
                JOptionPane.showMessageDialog(this, "File isn't a terrain file");
            }
            /*
            try{
                terrain = TerrainLoader.readFromFile(jFileChooser1.getSelectedFile().getAbsolutePath());
                
                jCheckBoxDrawTerrain.setEnabled(true);
                jButtonGenerateMap.setEnabled(true);
                jButtonGenerateMap.setToolTipText("");
                
            }catch(Exception e){
                System.err.println("the file isn't a terrain file");
            }
            */
        }
    }//GEN-LAST:event_jButtonOpenInputFile1ActionPerformed

    private void jTextFieldFilePathMntActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathMntActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathMntActionPerformed

    private void jListOutputFilesValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_jListOutputFilesValueChanged
        
        
    }//GEN-LAST:event_jListOutputFilesValueChanged

    private void jButtonLoadSelectedFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadSelectedFileActionPerformed
        
        String filePath = jListOutputFiles.getSelectedValue().toString();
        
        if(filePath.endsWith(".las") || filePath.endsWith(".laz") || filePath.endsWith(".txt") || filePath.endsWith(".vox")){
            
            if(filePath.endsWith(".las")){
                
                /*
                String coordinatesFilePath ="";
                String trajectoryFilePath ="";
                
                final VoxelPreprocessing preprocessVox = new VoxelPreprocessing();
        
                final JProgressLoadingFile progressBar = new JProgressLoadingFile(this);
                progressBar.setVisible(true);

                final JFrameSettingUp parent = this;

                preprocessVox.addVoxelPreprocessingListener(new VoxelPreprocessingAdapter() {

                    @Override
                    public void voxelPreprocessingStepProgress(String progress, int ratio){
                        progressBar.setText(progress);
                        progressBar.jProgressBar1.setValue(ratio);
                    }

                    @Override
                    public void voxelPreprocessingFinished(){
                        
                        progressBar.dispose();
                        
                        //Voxelisation vox = new Voxelisation();
                        
                        //retournera la structure du fichier voxelisation
                        //vox.voxelise(preprocessVox.echos);
                        
                        int answer = JOptionPane.showConfirmDialog(parent, "Do you want to write the voxel file?");
                        
                        if(answer == JOptionPane.YES_OPTION){
                            if(jFileChooserSave1.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
                                
                                File file = jFileChooser1.getSelectedFile();
                                
                                //vox.writeFile(file.getAbsolutePath());
                                
                                model.addElement(file.getAbsolutePath());
                            }
                        }
                    }
                });
                */
                //preprocessVox.generateEchosFile(coordinatesFilePath, filePath, trajectoryFilePath);
                
            }else if(filePath.endsWith(".laz")){
                
                JOptionPane.showMessageDialog(this, "not supported yet");
                
            }else if(filePath.endsWith(".txt")){
                
                initComboBox();
                jButtonOpen3DDisplay.setEnabled(true);
                
            }else if(filePath.endsWith(".vox")){
                
                initComboBox();
                jButtonOpen3DDisplay.setEnabled(true);
                jTabbedPane1.setSelectedIndex(1);
                jTabbedPane4.setSelectedIndex(0);
            }
            
        }else{
            JOptionPane.showMessageDialog(this, "File must be  in following format: *.laz, *.las, *.txt, *.vox");
        }
    }//GEN-LAST:event_jButtonLoadSelectedFileActionPerformed

    private void jButtonLoadSelectedFileMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonLoadSelectedFileMouseClicked
        
        
    }//GEN-LAST:event_jButtonLoadSelectedFileMouseClicked

    private void jFileChooser8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jFileChooser8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jFileChooser8ActionPerformed

    private void jButtonGenerateMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenerateMapActionPerformed

        if(terrain == null){
            readTerrain();
        }
        

        //chargement de l'espace voxel
        final VoxelSpace voxelSpace = new VoxelSpace(new Settings(this));
        final JProgressLoadingFile progress = new JProgressLoadingFile(this);
        progress.setVisible(true);

        voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

            @Override
            public void voxelSpaceCreationProgress(int progression){

                progress.jProgressBar1.setValue(progression);
            }

            @Override
            public void voxelSpaceCreationFinished(){
                progress.dispose();
                Projection projection = new Projection(voxelSpace.voxelSpaceFormat, terrain);
                BufferedImage img = null;

                if(jRadioButtonPAI.isSelected()){

                    img= projection.generateMap(Projection.PAI);

                }else if(jRadioButtonTransmittanceMap.isSelected()){

                    img= projection.generateMap(Projection.TRANSMITTANCE);
                }

                BufferedImage colorScale = ScaleGradient.generateScale(ColorGradient.GRADIENT_HEAT, projection.getMinValue(), projection.getMaxValue(), 50, 200, ScaleGradient.VERTICAL);

                JFrameImageViewer imageViewer = new JFrameImageViewer(img, colorScale);
                imageViewer.setJLabelMinValue(String.valueOf(projection.getMinValue()+0.0));
                imageViewer.setJLabelMaxValue(String.valueOf(projection.getMaxValue()+0.0));
                imageViewer.setVisible(true);

            }
        });

        try {
            voxelSpace.loadFromFile(new File(jListOutputFiles.getSelectedValue().toString()));
            //voxelSpace.loadFromFile(new File(jListOutputFiles.getSelectedValue().toString()));
        } catch (Exception ex) {
            logger.error(null, ex);
        }
    }//GEN-LAST:event_jButtonGenerateMapActionPerformed

    private void jCheckBoxDrawUndergroundVoxelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDrawUndergroundVoxelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxDrawUndergroundVoxelActionPerformed

    private void jCheckBoxDrawUndergroundVoxelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxDrawUndergroundVoxelStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxDrawUndergroundVoxelStateChanged

    private void jCheckBoxDrawNullVoxelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDrawNullVoxelActionPerformed

    }//GEN-LAST:event_jCheckBoxDrawNullVoxelActionPerformed

    private void jCheckBoxDrawNullVoxelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxDrawNullVoxelStateChanged

        

    }//GEN-LAST:event_jCheckBoxDrawNullVoxelStateChanged

    private void jButtonCreateAttributActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateAttributActionPerformed
        
        ListAdapterComboboxModel attributeModelAdapter = new ListAdapterComboboxModel(attributeModel, valueModel);
        final JFrameAttributCreation jframeAttribut = new JFrameAttributCreation(attributeModelAdapter);
        final JFrameSettingUp parent = this;

        jframeAttribut.setVisible(true);
        jframeAttribut.addWindowListener(new java.awt.event.WindowAdapter() {

            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                //parent.setMapAttributs(jframeAttribut.getMapAttributs());
            }
        });

    }//GEN-LAST:event_jButtonCreateAttributActionPerformed

    private void jComboBoxAttributeToVisualizePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBoxAttributeToVisualizePropertyChange

    }//GEN-LAST:event_jComboBoxAttributeToVisualizePropertyChange

    private void jComboBoxAttributeToVisualizeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxAttributeToVisualizeItemStateChanged
        
        jTextFieldAttributExpression.setText(((ListAdapterComboboxModel)jComboBoxAttributeToVisualize.getModel()).getValue(jComboBoxAttributeToVisualize.getSelectedIndex()));
        
        //jTextFieldAttributExpression.setText(mapAttributs.get(jComboBoxAttributeToVisualize.getSelectedItem().toString()).getExpressionString());
    }//GEN-LAST:event_jComboBoxAttributeToVisualizeItemStateChanged

    private void jButtonOpen3DDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpen3DDisplayActionPerformed

        GLProfile glp = GLProfile.getMaxFixedFunc(true);
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setDoubleBuffered(true);

        GLRenderFrame renderFrame = GLRenderFrame.create(caps, 640, 480, jListOutputFiles.getSelectedValue().toString());

        FPSAnimator animator = new FPSAnimator(renderFrame, 60);
        
        settings = new Settings(this);

        readTerrain();

        JoglListener joglContext = new JoglListener(this, terrain, settings, animator);
        EventManager eventListener = new EventManager(animator, renderFrame, joglContext);
        joglContext.attachEventListener(eventListener);
        
        renderFrame.addGLEventListener(joglContext);
        renderFrame.addKeyListener(new InputKeyListener(eventListener, animator));
        renderFrame.addMouseListener(new InputMouseAdapter(eventListener, animator));

        //renderFrame.setPointerVisible(false);
        
        ListAdapterComboboxModel attributeModelAdapterTools = new ListAdapterComboboxModel(attributeModel, valueModel);
        JFrameTools toolsJframe = new JFrameTools(this, joglContext, attributeModelAdapterTools);
        joglContext.attachToolBox(toolsJframe);

        toolsJframe.setTitle("Toolbox");
        Point locationOnScreen = renderFrame.getLocationOnScreen(null);
        toolsJframe.setLocation(new java.awt.Point(locationOnScreen.getX()-275, locationOnScreen.getY()-20));

        //set default camera orthographic parameters
        toolsJframe.jTextFieldLeftOrthoCamera.setText(String.valueOf(-renderFrame.width/2));
        toolsJframe.jTextFieldRightOrthoCamera.setText(String.valueOf(renderFrame.width/2));
        toolsJframe.jTextFieldBottomOrthoCamera.setText(String.valueOf(-renderFrame.height/2));
        toolsJframe.jTextFieldTopOrthoCamera.setText(String.valueOf(renderFrame.height/2));
        toolsJframe.jTextFieldNearOrthoCamera.setText(String.valueOf(-1000.0f));
        toolsJframe.jTextFieldFarOrthoCamera.setText(String.valueOf(1000.0f));

        //set default camera perspective parameters
        toolsJframe.jTextFieldFovPerspectiveCamera.setText(String.valueOf(70.0f));
        toolsJframe.jTextFieldAspectPerspectiveCamera.setText(String.valueOf((1.0f*renderFrame.width)/renderFrame.height));
        toolsJframe.jTextFieldNearPerspectiveCamera.setText(String.valueOf(1.0f));
        toolsJframe.jTextFieldFarPerspectiveCamera.setText(String.valueOf(10000.0f));

        GLRenderWindowListener renderWindowListener = new GLRenderWindowListener(renderFrame, toolsJframe, animator);
        renderFrame.addWindowListener(renderWindowListener);
        
        toolsJframe.setVisible(true);

        animator.start();
    }//GEN-LAST:event_jButtonOpen3DDisplayActionPerformed

    private void jButtonLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoadActionPerformed

        String inputFilePath = jTextFieldFilePathInputTxt.getText();
        String outputFilePath = jTextFieldFilePathOutputTxt.getText();
        String arguments = jTextFieldArguments.getText();

        if(inputFilePath.isEmpty() || outputFilePath.isEmpty() || arguments.isEmpty()){

            if(inputFilePath.isEmpty()){
                JOptionPane.showMessageDialog(this, "select input file");
            }
            if(outputFilePath.isEmpty()){
                JOptionPane.showMessageDialog(this, "select output file");
            }

        }else{

            Las las = LasReader.read(new File(inputFilePath));

            LasToTxt lasToTxt = new LasToTxt();
            final JProgressLoadingFile progressBar = new JProgressLoadingFile(this);

            lasToTxt.addLasToTxtListener(new LasToTxtAdapter() {

                @Override
                public void LasToTxtProgress(int progress) {
                    progressBar.jProgressBar1.setValue(progress);
                }

                @Override
                public void LasToTxtFinished(){
                    progressBar.dispose();
                }
            });

            progressBar.setVisible(true);
            lasToTxt.writeTxt(getVopMatrixALS(), las, outputFilePath, arguments, jCheckBoxWriteHeader.isSelected());
        }
    }//GEN-LAST:event_jButtonLoadActionPerformed

    private void jCheckBoxWriteVStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteVStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteVStateChanged

    private void jCheckBoxWriteWStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteWStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteWStateChanged

    private void jCheckBoxWritewStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWritewStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWritewStateChanged

    private void jCheckBoxWriteRGBStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteRGBStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteRGBStateChanged

    private void jCheckBoxWriteTStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteTStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteTStateChanged

    private void jCheckBoxWritePStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWritePStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWritePStateChanged

    private void jCheckBoxWriteUStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteUStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteUStateChanged

    private void jCheckBoxWriteAStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteAStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteAStateChanged

    private void jCheckBoxWriteCStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteCStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteCStateChanged

    private void jCheckBoxWriteEStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteEStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteEStateChanged

    private void jCheckBoxWriteDStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteDStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteDStateChanged

    private void jCheckBoxWriteNStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteNStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteNStateChanged

    private void jCheckBoxWriteRStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteRStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteRStateChanged

    private void jCheckBoxWriteIStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteIStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteIStateChanged

    private void jCheckBoxWriteZStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteZStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteZStateChanged

    private void jCheckBoxWriteYStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteYStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteYStateChanged

    private void jCheckBoxWriteXStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxWriteXStateChanged
        fillAttributes();
    }//GEN-LAST:event_jCheckBoxWriteXStateChanged

    private void jButtonChooseDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChooseDirectoryActionPerformed

        if (jFileChooser8.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = jFileChooser8.getSelectedFile();
            String fileName = file.getName();
            String filePath = file.getAbsolutePath();

            if(!fileName.endsWith(".txt")){
                fileName += ".txt";
                filePath += ".txt";
            }

            jTextFieldFileNameOutputTxt.setText(fileName);
            jTextFieldFileNameOutputTxt.setToolTipText(fileName);

            jTextFieldFilePathOutputTxt.setText(filePath);
            jTextFieldFilePathOutputTxt.setToolTipText(filePath);
        }
    }//GEN-LAST:event_jButtonChooseDirectoryActionPerformed

    private void jTextFieldFilePathOutputTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathOutputTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathOutputTxtActionPerformed

    private void jButtonOpenInputFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenInputFileActionPerformed

        jFileChooser1.changeToParentDirectory();

        if (jFileChooser1.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            Misc.enableContentInComponent(true, jPanel6);
            Misc.enableContentInComponent(true,jPanel8);

            jTextFieldFileNameInputTxt.setText(jFileChooser1.getSelectedFile().getName());
            jTextFieldFileNameInputTxt.setToolTipText(jFileChooser1.getSelectedFile().getName());

            jTextFieldFilePathInputTxt.setText(jFileChooser1.getSelectedFile().getAbsolutePath());
            jTextFieldFilePathInputTxt.setToolTipText(jFileChooser1.getSelectedFile().getAbsolutePath());

            jLabelFileSize.setText(String.valueOf(jFileChooser1.getSelectedFile().length()));
            
            //LasReader.

        }
    }//GEN-LAST:event_jButtonOpenInputFileActionPerformed

    private void jTextFieldFilePathInputTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathInputTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathInputTxtActionPerformed

    private void jButtonPopMatrix1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPopMatrix1ActionPerformed

        final JFrameMatrix jFrameMatrix = new JFrameMatrix(getVopMatrixALS());
        jFrameMatrix.setVisible(true);

        jFrameMatrix.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                //vopMatrix = jFrameMatrix.getMatrix();
            }
        });
    }//GEN-LAST:event_jButtonPopMatrix1ActionPerformed

    private void jButtonExecuteVoxAlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecuteVoxAlsActionPerformed

        
        vopMatrix = getVopMatrixALS();

        final String inputVoxPath = jTextFieldFilePathInputVox.getText();
        final File trajectoryFile = new File(jTextFieldFilePathTrajVox.getText());

        File inputFile = new File(jTextFieldFilePathInputVox.getText());
        String extension = FileManager.getExtension(inputFile);
        final File dtmFile = new File(jTextFieldFilePathMnt.getText());
        
        voxelisationParameters.setBottomCorner(new Point3d(
                            Double.valueOf(jTextFieldMinPointX.getText()), 
                            Double.valueOf(jTextFieldMinPointY.getText()), 
                            Double.valueOf(jTextFieldMinPointZ.getText())));
        
        voxelisationParameters.setTopCorner(new Point3d(
                            Double.valueOf(jTextFieldMaxPointX.getText()), 
                            Double.valueOf(jTextFieldMaxPointY.getText()), 
                            Double.valueOf(jTextFieldMaxPointZ.getText())));
        
        voxelisationParameters.setSplit(new Point3i(
                            Integer.valueOf(jTextFieldVoxelNumberX.getText()), 
                            Integer.valueOf(jTextFieldVoxelNumberY.getText()), 
                            Integer.valueOf(jTextFieldVoxelNumberZ.getText())));
        
        voxelisationParameters.setResolution(Double.valueOf(jTextFieldVoxelNumberRes.getText()));
        voxelisationParameters.setWeighting(jComboBoxWeighting.getSelectedIndex());
        voxelisationParameters.setUseDTMCorrection(jCheckBoxUseDTM.isSelected());
        
        final VoxelParameters parameters = voxelisationParameters;
        
        
        final File outputFile = new File(jTextFieldFilePathSaveVox.getText());

        final JProgressLoadingFile progressBar = new JProgressLoadingFile(this);
        
        final VoxelisationTool voxTool = new VoxelisationTool();
        final long start_time = System.currentTimeMillis();
        
        voxTool.addProcessingListener(new ProcessingListener() {

            @Override
            public void processingStepProgress(String progress, int ratio) {
                progressBar.setText(progress);
                progressBar.jProgressBar1.setValue(ratio);
            }

            @Override
            public void processingFinished() {
                progressBar.dispose();
                
                logger.info("las extraction is finished ( "+TimeCounter.getElapsedTimeInSeconds(start_time)+" )");
            }
        });
        

        switch(extension){
            case ".laz":

            //decompress file

            break;

            case ".las":
                
                progressBar.setVisible(true);
                
                SwingWorker sw = new SwingWorker() {

                @Override
                protected Void doInBackground() throws Exception {

                    voxTool.generateVoxelSpaceFromLas(new File(inputVoxPath), trajectoryFile, outputFile, vopMatrix, parameters, dtmFile);
            
                    return null;
                }

                @Override
                protected void done(){
                    
                    progressBar.dispose();
                    
                    addElementToVoxList(outputFile.getAbsolutePath());

                    jListOutputFiles.setModel(model);
                    jListOutputFiles.setSelectedIndex(0);
                }
            };

            sw.execute();
            
            break;

            case ".txt":
        }

        
    }//GEN-LAST:event_jButtonExecuteVoxAlsActionPerformed

    private void jTextFieldMinPointZKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointZKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointZKeyTyped

    private void jTextFieldMinPointYKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointYKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointYKeyTyped

    private void jTextFieldMinPointXKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointXKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointXKeyTyped

    private void jTextFieldMaxPointZKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointZKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointZKeyTyped

    private void jTextFieldMaxPointYKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointYKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointYKeyTyped

    private void jTextFieldMaxPointXKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointXKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointXKeyTyped

    private void jTextFieldVoxelNumberZKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberZKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberZKeyTyped

    private void jTextFieldVoxelNumberYKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberYKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberYKeyTyped

    private void jTextFieldVoxelNumberXKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberXKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberXKeyTyped

    private void jButtonChooseOutputDirectoryVoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChooseOutputDirectoryVoxActionPerformed

        if (jFileChooser7.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = jFileChooser7.getSelectedFile();
            String fileName = file.getName();
            String filePath = file.getAbsolutePath();

            if(!fileName.endsWith(".vox")){
                fileName += ".vox";
                filePath += ".vox";
            }

            jTextFieldFileNameSaveVox.setText(fileName);
            jTextFieldFileNameSaveVox.setToolTipText(fileName);

            jTextFieldFilePathSaveVox.setText(filePath);
            jTextFieldFilePathSaveVox.setToolTipText(filePath);
        }
    }//GEN-LAST:event_jButtonChooseOutputDirectoryVoxActionPerformed

    private void jTextFieldFilePathSaveVoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathSaveVoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathSaveVoxActionPerformed

    private void jButtonOpenTrajectoryFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenTrajectoryFileActionPerformed

        if (jFileChooser5.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            jTextFieldFileNameTrajVox.setText(jFileChooser5.getSelectedFile().getName());
            jTextFieldFileNameTrajVox.setToolTipText(jFileChooser5.getSelectedFile().getName());

            jTextFieldFilePathTrajVox.setText(jFileChooser5.getSelectedFile().getAbsolutePath());
            jTextFieldFilePathTrajVox.setToolTipText(jFileChooser5.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonOpenTrajectoryFileActionPerformed

    private void jTextFieldFilePathTrajVoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathTrajVoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathTrajVoxActionPerformed

    private void jButtonOpenInputFileVoxelisationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenInputFileVoxelisationActionPerformed

        if (jFileChooser4.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = jFileChooser4.getSelectedFile();
            String fileName = file.getName();
            String filePath = file.getAbsolutePath();

            if(fileName.endsWith(".las") || fileName.endsWith(".laz") || fileName.endsWith(".txt")){
                
                jTextFieldFileNameInputVox.setText(fileName);
                jTextFieldFileNameInputVox.setToolTipText(fileName);

                jTextFieldFilePathInputVox.setText(filePath);
                jTextFieldFilePathInputVox.setToolTipText(filePath);

                jLabelFileSizeInputVox.setText(String.valueOf(file.getTotalSpace()));
                
                

            }else{
                JOptionPane.showMessageDialog(this, "File isn't a *.laz or *.las file");
            }
        }
    }//GEN-LAST:event_jButtonOpenInputFileVoxelisationActionPerformed

    private void jTextFieldFilePathInputVoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathInputVoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathInputVoxActionPerformed

    private void jButtonExecuteVoxTlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecuteVoxTlsActionPerformed

        /*
        VoxelPreprocessingRxp preprocessRxp =
        new VoxelPreprocessingRxp(rsp, vopPopMatrix, jRadioButtonLightFile.isSelected());
        preprocessRxp.execute();
        */
        final boolean isLight = jRadioButtonLightFile.isSelected();
        final boolean mergeOutput = jCheckBoxMergeOutputFiles.isSelected();
        
        ArrayList<Scans> filteredRxpList = null;
        

        switch(jComboBox1.getSelectedIndex()){
            case 0:
                filteredRxpList = rsp.getFilteredRxpList();

                break;
            case 1:

                Scans scans = new Scans();
                
                RxpScan scan = new RxpScan();
                scan.setFile(new File(jListRspScans.getSelectedValue().toString()));

                if(isLight){
                    scans.setScanLite(scan);
                }else{
                    scans.setScanFull(scan);
                }

                filteredRxpList = new ArrayList<>();
                
                filteredRxpList.add(scans);
                
                vopPopMatrix = getVopMatrixTLS();

                break;
        }
        

        final ArrayList<Scans> scanList = filteredRxpList;

        final JProgressLoadingFile progressBar = new JProgressLoadingFile(this);
        progressBar.jProgressBar1.setIndeterminate(true);
        progressBar.jProgressBar1.setStringPainted(false);
        progressBar.setVisible(true);

        final ArrayList<File> filesList = new ArrayList<>();
        
        voxelisationParameters.setBottomCorner(new Point3d(
                            Float.valueOf(jTextFieldMinPointX2.getText()), 
                            Float.valueOf(jTextFieldMinPointY2.getText()), 
                            Float.valueOf(jTextFieldMinPointZ2.getText())));
        
        voxelisationParameters.setTopCorner(new Point3d(
                            Float.valueOf(jTextFieldMaxPointX2.getText()), 
                            Float.valueOf(jTextFieldMaxPointY2.getText()), 
                            Float.valueOf(jTextFieldMaxPointZ2.getText())));
        
        voxelisationParameters.setSplit(new Point3i(
                            Integer.valueOf(jTextFieldVoxelNumberX1.getText()), 
                            Integer.valueOf(jTextFieldVoxelNumberY1.getText()), 
                            Integer.valueOf(jTextFieldVoxelNumberZ1.getText())));
        
        voxelisationParameters.setResolution(Float.valueOf(jTextFieldVoxelNumberRes1.getText()));
        voxelisationParameters.setWeighting(jComboBoxWeighting.getSelectedIndex());
        
        final VoxelParameters parameters = voxelisationParameters;

        final String outputPath = jTextFieldFileOutputPathTlsVox.getText();
                
        final VoxelisationTool voxTool = new VoxelisationTool();

        SwingWorker sw = new SwingWorker() {

            @Override
            protected Void doInBackground() throws Exception {

                try{

                    int compteur = 1;
                    
                    
                    for(Scans rxp:scanList){
                        
                        final RxpScan scan;
                        
                        if(isLight){
                            scan = rxp.getScanLite();
                        }else{
                            scan = rxp.getScanFull();
                        }
                        progressBar.setText("Voxelisation in progress, file "+compteur+"/"+scanList.size()+" : "+scan.getFile().getName());
                        progressBar.pack();
                        
                        final File outputFile = new File(outputPath+"/"+scan.getFile().getName()+".vox");
                        
                        long start_time = System.nanoTime();
                        
                        voxTool.generateVoxelSpaceFromRxp(scan, outputFile, vopPopMatrix, parameters);
 
                        
                        long end_time = System.nanoTime();
                        double difference = (end_time - start_time)*(Math.pow(10, -9));
                        System.out.println("time: "+Math.round(difference*100)/100);
                        
                        filesList.add(outputFile);
                        //System.out.println(compteur);
                        compteur++;

                    }
                    
                }catch(Exception e){
                    logger.error("voxelisation failed", e);
                }
                //this.get();
                
                return null;
            }

            @Override
            protected void done(){
                progressBar.dispose();
                
                if(!mergeOutput){
                    
                    for(File file :filesList){
                        addElementToVoxList(file.getAbsolutePath());
                    }
                    
                }else{
                    //merge
                    File outputFile = VoxelisationTool.mergeVoxelsFile(filesList, new File("test.vox"));
                    addElementToVoxList(outputFile.getAbsolutePath());
                }
                
                jListOutputFiles.setModel(model);
                jListOutputFiles.setSelectedIndex(0);
            }
        };
        
        sw.execute();

        /*
        for(int i=0;i<rspScansListModel.getSize();i++){

            //here we call Frédéric program to generate echos files
            //for each file we pass vop, pop, sop matrices
            Mat4D popMatrix = rsp.getPopMatrix();

            //rspScansListModel.get(i).
        }
        */

    }//GEN-LAST:event_jButtonExecuteVoxTlsActionPerformed

    private void jButtonChooseOutputDirectoryTlsVoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChooseOutputDirectoryTlsVoxActionPerformed

        if (jFileChooser10.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            jTextFieldFileOutputPathTlsVox.setText(jFileChooser10.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonChooseOutputDirectoryTlsVoxActionPerformed

    private void jTextFieldFileOutputPathTlsVoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFileOutputPathTlsVoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFileOutputPathTlsVoxActionPerformed

    private void jTextFieldVoxelNumberRes1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberRes1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberRes1KeyTyped

    private void jTextFieldVoxelNumberZ1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberZ1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberZ1KeyTyped

    private void jTextFieldVoxelNumberY1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberY1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberY1KeyTyped

    private void jTextFieldVoxelNumberX1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberX1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberX1KeyTyped

    private void jTextFieldMaxPointZ2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointZ2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointZ2KeyTyped

    private void jTextFieldMaxPointY2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointY2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointY2KeyTyped

    private void jTextFieldMaxPointX2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointX2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointX2KeyTyped

    private void jTextFieldMinPointZ2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointZ2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointZ2KeyTyped

    private void jTextFieldMinPointY2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointY2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointY2KeyTyped

    private void jTextFieldMinPointX2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointX2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointX2KeyTyped

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        final JFrameMatrix jFrameMatrix = new JFrameMatrix(vopPopMatrix);
        jFrameMatrix.setVisible(true);

        jFrameMatrix.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                vopPopMatrix = jFrameMatrix.getMatrix();
            }
        });
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButtonVopMatrixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonVopMatrixActionPerformed

        final JFrameMatrix jFrameMatrix = new JFrameMatrix(Mat4D.identity());
        jFrameMatrix.setVisible(true);

        jFrameMatrix.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                vopMatrix = jFrameMatrix.getMatrix();
            }
        });
    }//GEN-LAST:event_jButtonVopMatrixActionPerformed

    private void jButtonPopMatrixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPopMatrixActionPerformed

        final JFrameMatrix jFrameMatrix = new JFrameMatrix(rsp.getPopMatrix());
        jFrameMatrix.setVisible(true);

    }//GEN-LAST:event_jButtonPopMatrixActionPerformed

    private void jRadioButtonComplexFileStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButtonComplexFileStateChanged

        if(jRadioButtonComplexFile.isSelected()){
            rspScansListModel.doInverseFilter(".mon");

        }

        jListRspScans.setModel(rspScansListModel);
    }//GEN-LAST:event_jRadioButtonComplexFileStateChanged

    private void jRadioButtonLightFileStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButtonLightFileStateChanged

        if(jRadioButtonLightFile.isSelected()){
            rspScansListModel.doFilter(".mon");

        }

        jListRspScans.setModel(rspScansListModel);

    }//GEN-LAST:event_jRadioButtonLightFileStateChanged

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged

        if(jComboBox1.getSelectedIndex() == 0){
            jButtonSopMatrix.setEnabled(false);
        }else{
            jButtonSopMatrix.setEnabled(true);
        }
    }//GEN-LAST:event_jComboBox1ItemStateChanged

    private void jButtonOpenRspFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenRspFileActionPerformed
        
        if(!jTextFieldFilePathRsp.getText().equals("")){
            jFileChooser9.setCurrentDirectory(new File(jTextFieldFilePathRsp.getText()));
        }
        
        if (jFileChooser9.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = jFileChooser9.getSelectedFile();
            String fileName = file.getName();
            String filePath = file.getAbsolutePath();

            if(fileName.endsWith(".rsp") || fileName.endsWith(".rxp") || fileName.endsWith(".txt")){

                jTextFieldFileNameRsp.setText(fileName);
                jTextFieldFileNameRsp.setToolTipText(fileName);

                jTextFieldFilePathRsp.setText(filePath);
                jTextFieldFilePathRsp.setToolTipText(filePath);

                if(fileName.endsWith(".rxp") && jComboBox1.getSelectedIndex() == 1){

                    rspScansListModel = new FilterDefaultListModel();
                    rspScansListModel.addElement(filePath);
                    jListRspScans.setModel(rspScansListModel);
                    jListRspScans.setSelectedIndex(0);

                }else if(fileName.endsWith(".rsp")){

                    rsp.read(new File(file.getAbsolutePath()));

                    rspScansListModel = new FilterDefaultListModel();

                    ArrayList<Scans> rxpList = rsp.getRxpList();

                    for(Scans rxp :rxpList){
                        Map<Integer, RxpScan> scanList = rxp.getScanList();

                        for(Entry scan:scanList.entrySet()){

                            rspScansListModel.addElement(scanList.get((int)scan.getKey()).getAbsolutePath());
                        }
                    }

                    if(jRadioButtonLightFile.isSelected()){
                        rspScansListModel.doFilter(".mon");
                    }else{
                        rspScansListModel.doInverseFilter(".mon");
                    }

                    jListRspScans.setModel(rspScansListModel);

                    popMatrix = rsp.getPopMatrix();
                    vopPopMatrix = Mat4D.multiply(vopMatrix, popMatrix);
                }

            }else{
                JOptionPane.showMessageDialog(this, "File extension not supported");
            }
        }
    }//GEN-LAST:event_jButtonOpenRspFileActionPerformed

    private void jTextFieldFilePathRspActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathRspActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathRspActionPerformed

    private void jButtonCalculateBoundingBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCalculateBoundingBoxActionPerformed
        
        if(!jTextFieldFilePathInputVox.getText().equals( "")){
            
            SwingWorker sw = new SwingWorker() {

            @Override
            protected Object doInBackground() throws Exception {
                
                Las read = LasReader.read(new File(jTextFieldFilePathInputVox.getText()));
                ArrayList<? extends PointDataRecordFormat0> pointDataRecords = read.getPointDataRecords();
                LasHeader header = read.getHeader();

                int count =0;
                double xMin=0, yMin=0, zMin=0;
                double xMax=0, yMax=0, zMax=0;

                Mat4D mat = getVopMatrixALS();

                for(PointDataRecordFormat0 point:pointDataRecords){

                    Vec4D pt = new Vec4D(((point.getX()*header.getxScaleFactor())+header.getxOffset()),
                                (point.getY()*header.getyScaleFactor())+header.getyOffset(),
                                (point.getZ()*header.getzScaleFactor())+header.getzOffset(),
                                1);


                    pt = Mat4D.multiply(mat, pt);

                    if(count != 0){

                        if(pt.x < xMin){
                            xMin = pt.x;
                        }else if(pt.x > xMax){
                            xMax = pt.x;
                        }

                        if(pt.y < yMin){
                            yMin = pt.y;
                        }else if(pt.y > yMax){
                            yMax = pt.y;
                        }

                        if(pt.z < zMin){
                            zMin = pt.z;
                        }else if(pt.z > zMax){
                            zMax = pt.z;
                        }

                    }else{

                        xMin = pt.x;
                        yMin = pt.y;
                        zMin = pt.z;

                        xMax = pt.x;
                        yMax = pt.y;
                        zMax = pt.z;

                        count++;
                    }

                }
                
                jTextFieldMinPointX.setText(String.valueOf(xMin));
                jTextFieldMinPointX.setCaretPosition(0);
                jTextFieldMinPointY.setText(String.valueOf(yMin));
                jTextFieldMinPointY.setCaretPosition(0);
                jTextFieldMinPointZ.setText(String.valueOf(zMin));
                jTextFieldMinPointZ.setCaretPosition(0);

                jTextFieldMaxPointX.setText(String.valueOf(xMax));
                jTextFieldMaxPointX.setCaretPosition(0);
                jTextFieldMaxPointY.setText(String.valueOf(yMax));
                jTextFieldMaxPointY.setCaretPosition(0);
                jTextFieldMaxPointZ.setText(String.valueOf(zMax));
                jTextFieldMaxPointZ.setCaretPosition(0);
                
                return null;
            }
        };
        
        sw.execute();
        }        
        
    }//GEN-LAST:event_jButtonCalculateBoundingBoxActionPerformed

    private void jButtonExportSelectionMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jButtonExportSelectionMouseClicked
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonExportSelectionMouseClicked

    private void jButtonExportSelectionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExportSelectionActionPerformed
        
        String filePath = jListOutputFiles.getSelectedValue().toString();
        
        if(jFileChooserSave2.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                                
            final File outputFile = jFileChooserSave2.getSelectedFile();
            
            final VoxelSpace voxelSpace = new VoxelSpace(settings);
            
            voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

                @Override
                public void voxelSpaceCreationFinished() {
                    DartWriter.writeFromVoxelSpace(voxelSpace.voxelSpaceFormat, outputFile);
                }
            });
            
            voxelSpace.loadFromFile(new File(filePath));
            
        }
        
    }//GEN-LAST:event_jButtonExportSelectionActionPerformed

    private void jComboBoxWeightingItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxWeightingItemStateChanged
        
        if(jComboBoxWeighting.getSelectedIndex() == 2){
            jButtonOpenWeightingFile.setEnabled(true);
        }else{
            jButtonOpenWeightingFile.setEnabled(false);
        }
    }//GEN-LAST:event_jComboBoxWeightingItemStateChanged

    private void jButtonOpenWeightingFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenWeightingFileActionPerformed
        
        if (jFileChooser11.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            
            this.voxelisationParameters.setWeightingFile(jFileChooser11.getSelectedFile());
        }
    }//GEN-LAST:event_jButtonOpenWeightingFileActionPerformed

    private void jButtonGenerateProfileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenerateProfileActionPerformed
        
        if(terrain == null){
            readTerrain();
        }

        //chargement de l'espace voxel
        final VoxelSpace voxelSpace = new VoxelSpace(new Settings(this));
        final JProgressLoadingFile progress = new JProgressLoadingFile(this);
        progress.setVisible(true);

        voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

            @Override
            public void voxelSpaceCreationProgress(int progression){

                progress.jProgressBar1.setValue(progression);
            }

            @Override
            public void voxelSpaceCreationFinished(){
                
                
                VoxelFilter voxelFilter = new VoxelFilter();

                for(int i=0;i<filterModel.getSize();i++){
                    Filter filter = Filter.getFilterFromString(filterModel.getElementAt(i));

                    if(filter != null){
                        voxelFilter.addFilter(filter);
                    }
                }
                
                //progress.jProgressBar1.setIndeterminate(true);
                XYSeries data = VegetationProfile.getData(voxelSpace.widthZ, voxelFilter, voxelSpace.voxelSpaceFormat);
                ChartJFrame chartJFrame = new ChartJFrame(data, "Vegetation profile","PAD", "height");
                
                progress.dispose();
                
                chartJFrame.setVisible(true);

            }
        });

        try {
            voxelSpace.loadFromFile(new File(jListOutputFiles.getSelectedValue().toString()));
            //voxelSpace.loadFromFile(new File(jListOutputFiles.getSelectedValue().toString()));
        } catch (Exception ex) {
            logger.error(null, ex);
        }
    }//GEN-LAST:event_jButtonGenerateProfileActionPerformed

    private void jComboBoxHorizontalAxisValueItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxHorizontalAxisValueItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxHorizontalAxisValueItemStateChanged

    private void jComboBoxHorizontalAxisValuePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBoxHorizontalAxisValuePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxHorizontalAxisValuePropertyChange

    private void jComboBoxverticalAxisValueItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxverticalAxisValueItemStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxverticalAxisValueItemStateChanged

    private void jComboBoxverticalAxisValuePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBoxverticalAxisValuePropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBoxverticalAxisValuePropertyChange

    private void jButtonAddFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddFilterActionPerformed
        
        ListAdapterComboboxModel attributeModelAdapter = new ListAdapterComboboxModel(attributeModel, valueModel);
        final FilterJFrame filterJFrame = new FilterJFrame(attributeModelAdapter);
        filterJFrame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                Filter filter = filterJFrame.getFilter();
                if(filter != null){
                    filterModel.addElement(filter.getVariable()+" "+filter.getConditionString()+" "+filter.getValue());
                }
            }
        });
        
        filterJFrame.setVisible(true);
    }//GEN-LAST:event_jButtonAddFilterActionPerformed

    private void jButtonRemoveFilterActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRemoveFilterActionPerformed
        
        if(jListFilters.getSelectedIndex()>=0){
            filterModel.removeElement(jListFilters.getSelectedValue().toString());
            jListFilters.setModel(filterModel);
        }
    }//GEN-LAST:event_jButtonRemoveFilterActionPerformed

    private void jButtonDrawChartActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonDrawChartActionPerformed
        
        if(terrain == null){
            readTerrain();
        }

        //chargement de l'espace voxel
        final VoxelSpace voxelSpace = new VoxelSpace(new Settings(this));
        final JProgressLoadingFile progress = new JProgressLoadingFile(this);
        progress.setVisible(true);

        voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

            @Override
            public void voxelSpaceCreationProgress(int progression){

                progress.jProgressBar1.setValue(progression);
            }

            @Override
            public void voxelSpaceCreationFinished(){
                progress.dispose();
                
                String horizontal = jComboBoxHorizontalAxisValue.getSelectedItem().toString();
                String vertical = jComboBoxverticalAxisValue.getSelectedItem().toString();

                VoxelFilter voxelFilter = new VoxelFilter();

                for(int i=0;i<filterModel.getSize();i++){
                    Filter filter = Filter.getFilterFromString(filterModel.getElementAt(i));

                    if(filter != null){
                        voxelFilter.addFilter(filter);
                    }
                }
                XYSeries series = ChartFactory.generateChartWithFilters(voxelSpace.voxelSpaceFormat, horizontal, vertical, voxelFilter);

                ChartJFrame chartJFrame = new ChartJFrame(series, horizontal+"~"+vertical, horizontal, vertical);
                chartJFrame.setVisible(true);

            }
        });

        try {
            voxelSpace.loadFromFile(new File(jListOutputFiles.getSelectedValue().toString()));
            //voxelSpace.loadFromFile(new File(jListOutputFiles.getSelectedValue().toString()));
        } catch (Exception ex) {
            logger.error(null, ex);
        }
        
        
    }//GEN-LAST:event_jButtonDrawChartActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup2DProjection;
    private javax.swing.ButtonGroup buttonGroup3DView;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAddFile;
    private javax.swing.JButton jButtonAddFilter;
    private javax.swing.JButton jButtonCalculateBoundingBox;
    private javax.swing.JButton jButtonChooseDirectory;
    private javax.swing.JButton jButtonChooseOutputDirectoryTlsVox;
    private javax.swing.JButton jButtonChooseOutputDirectoryVox;
    private javax.swing.JButton jButtonCreateAttribut;
    private javax.swing.JButton jButtonDrawChart;
    private javax.swing.JButton jButtonExecuteVoxAls;
    private javax.swing.JButton jButtonExecuteVoxTls;
    private javax.swing.JButton jButtonExportSelection;
    private javax.swing.JButton jButtonGenerateMap;
    private javax.swing.JButton jButtonGenerateProfile;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonLoadSelectedFile;
    private javax.swing.JButton jButtonOpen3DDisplay;
    private javax.swing.JButton jButtonOpenInputFile;
    private javax.swing.JButton jButtonOpenInputFile1;
    private javax.swing.JButton jButtonOpenInputFileVoxelisation;
    private javax.swing.JButton jButtonOpenRspFile;
    private javax.swing.JButton jButtonOpenTrajectoryFile;
    private javax.swing.JButton jButtonOpenWeightingFile;
    private javax.swing.JButton jButtonPopMatrix;
    private javax.swing.JButton jButtonPopMatrix1;
    private javax.swing.JButton jButtonRemoveFile;
    private javax.swing.JButton jButtonRemoveFilter;
    private javax.swing.JButton jButtonSopMatrix;
    private javax.swing.JButton jButtonVopMatrix;
    private javax.swing.JCheckBox jCheckBoxDrawAxis;
    private javax.swing.JCheckBox jCheckBoxDrawNullVoxel;
    private javax.swing.JCheckBox jCheckBoxDrawTerrain;
    private javax.swing.JCheckBox jCheckBoxDrawUndergroundVoxel;
    private javax.swing.JCheckBox jCheckBoxMergeOutputFiles;
    private javax.swing.JCheckBox jCheckBoxUseDTM;
    private javax.swing.JCheckBox jCheckBoxWriteA;
    private javax.swing.JCheckBox jCheckBoxWriteC;
    private javax.swing.JCheckBox jCheckBoxWriteD;
    private javax.swing.JCheckBox jCheckBoxWriteE;
    private javax.swing.JCheckBox jCheckBoxWriteHeader;
    private javax.swing.JCheckBox jCheckBoxWriteI;
    private javax.swing.JCheckBox jCheckBoxWriteN;
    private javax.swing.JCheckBox jCheckBoxWriteP;
    private javax.swing.JCheckBox jCheckBoxWriteR;
    private javax.swing.JCheckBox jCheckBoxWriteRGB;
    private javax.swing.JCheckBox jCheckBoxWriteT;
    private javax.swing.JCheckBox jCheckBoxWriteU;
    private javax.swing.JCheckBox jCheckBoxWriteV;
    private javax.swing.JCheckBox jCheckBoxWriteW;
    private javax.swing.JCheckBox jCheckBoxWriteX;
    private javax.swing.JCheckBox jCheckBoxWriteY;
    private javax.swing.JCheckBox jCheckBoxWriteZ;
    private javax.swing.JCheckBox jCheckBoxWritew;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBoxAttributeToVisualize;
    private javax.swing.JComboBox jComboBoxDefaultX;
    private javax.swing.JComboBox jComboBoxDefaultY;
    private javax.swing.JComboBox jComboBoxDefaultZ;
    private javax.swing.JComboBox jComboBoxHorizontalAxisValue;
    private javax.swing.JComboBox jComboBoxWeighting;
    private javax.swing.JComboBox jComboBoxverticalAxisValue;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JFileChooser jFileChooser10;
    private javax.swing.JFileChooser jFileChooser11;
    private javax.swing.JFileChooser jFileChooser2;
    private javax.swing.JFileChooser jFileChooser3;
    private javax.swing.JFileChooser jFileChooser4;
    private javax.swing.JFileChooser jFileChooser5;
    private javax.swing.JFileChooser jFileChooser6;
    private javax.swing.JFileChooser jFileChooser7;
    private javax.swing.JFileChooser jFileChooser8;
    private javax.swing.JFileChooser jFileChooser9;
    private javax.swing.JFileChooser jFileChooserSave;
    private javax.swing.JFileChooser jFileChooserSave1;
    private javax.swing.JFileChooser jFileChooserSave2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelFileSize;
    private javax.swing.JLabel jLabelFileSizeInputVox;
    private javax.swing.JLabel jLabelName;
    private javax.swing.JLabel jLabelName1;
    private javax.swing.JLabel jLabelName2;
    private javax.swing.JLabel jLabelName3;
    private javax.swing.JLabel jLabelName4;
    private javax.swing.JLabel jLabelName5;
    private javax.swing.JLabel jLabelName6;
    private javax.swing.JLabel jLabelPath;
    private javax.swing.JLabel jLabelPath1;
    private javax.swing.JLabel jLabelPath10;
    private javax.swing.JLabel jLabelPath2;
    private javax.swing.JLabel jLabelPath3;
    private javax.swing.JLabel jLabelPath4;
    private javax.swing.JLabel jLabelPath5;
    private javax.swing.JLabel jLabelPath6;
    private javax.swing.JLabel jLabelSize;
    private javax.swing.JLabel jLabelSize1;
    private javax.swing.JList jListFilters;
    private javax.swing.JList jListOutputFiles;
    private javax.swing.JList jListRspScans;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenu jMenuAppearance;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItemLoad;
    private javax.swing.JMenuItem jMenuItemSave;
    private javax.swing.JMenuItem jMenuItemSaveAs;
    private javax.swing.JMenu jMenuSettings;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel3DViewDensity;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelOutputParametersTab;
    private javax.swing.JPanel jPanelVegetationProfile;
    private javax.swing.JPanel jPanelVisualizeTab;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButtonComplexFile;
    private javax.swing.JRadioButton jRadioButtonLightFile;
    private javax.swing.JRadioButton jRadioButtonPAI;
    private javax.swing.JRadioButton jRadioButtonTransmittanceMap;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JTabbedPane jTabbedPane5;
    private javax.swing.JTextField jTextFieldArguments;
    private javax.swing.JTextField jTextFieldAttributExpression;
    private javax.swing.JTextField jTextFieldFileNameInputTxt;
    private javax.swing.JTextField jTextFieldFileNameInputVox;
    private javax.swing.JTextField jTextFieldFileNameMnt;
    private javax.swing.JTextField jTextFieldFileNameOutputTxt;
    private javax.swing.JTextField jTextFieldFileNameRsp;
    private javax.swing.JTextField jTextFieldFileNameSaveVox;
    private javax.swing.JTextField jTextFieldFileNameTrajVox;
    private javax.swing.JTextField jTextFieldFileOutputPathTlsVox;
    private javax.swing.JTextField jTextFieldFilePathInputTxt;
    private javax.swing.JTextField jTextFieldFilePathInputVox;
    private javax.swing.JTextField jTextFieldFilePathMnt;
    private javax.swing.JTextField jTextFieldFilePathOutputTxt;
    private javax.swing.JTextField jTextFieldFilePathRsp;
    private javax.swing.JTextField jTextFieldFilePathSaveVox;
    private javax.swing.JTextField jTextFieldFilePathTrajVox;
    private javax.swing.JTextField jTextFieldMaxPointX;
    private javax.swing.JTextField jTextFieldMaxPointX2;
    private javax.swing.JTextField jTextFieldMaxPointY;
    private javax.swing.JTextField jTextFieldMaxPointY2;
    private javax.swing.JTextField jTextFieldMaxPointZ;
    private javax.swing.JTextField jTextFieldMaxPointZ2;
    private javax.swing.JTextField jTextFieldMinPointX;
    private javax.swing.JTextField jTextFieldMinPointX2;
    private javax.swing.JTextField jTextFieldMinPointY;
    private javax.swing.JTextField jTextFieldMinPointY2;
    private javax.swing.JTextField jTextFieldMinPointZ;
    private javax.swing.JTextField jTextFieldMinPointZ2;
    private javax.swing.JTextField jTextFieldOutputMergedFile;
    private javax.swing.JTextField jTextFieldReferencePoint1X;
    private javax.swing.JTextField jTextFieldReferencePoint1Y;
    private javax.swing.JTextField jTextFieldReferencePoint1Z;
    private javax.swing.JTextField jTextFieldReferencePoint2X;
    private javax.swing.JTextField jTextFieldReferencePoint2Y;
    private javax.swing.JTextField jTextFieldReferencePoint2Z;
    private javax.swing.JTextField jTextFieldVoxelNumberRes;
    private javax.swing.JTextField jTextFieldVoxelNumberRes1;
    private javax.swing.JTextField jTextFieldVoxelNumberX;
    private javax.swing.JTextField jTextFieldVoxelNumberX1;
    private javax.swing.JTextField jTextFieldVoxelNumberY;
    private javax.swing.JTextField jTextFieldVoxelNumberY1;
    private javax.swing.JTextField jTextFieldVoxelNumberZ;
    private javax.swing.JTextField jTextFieldVoxelNumberZ1;
    // End of variables declaration//GEN-END:variables

    
}
