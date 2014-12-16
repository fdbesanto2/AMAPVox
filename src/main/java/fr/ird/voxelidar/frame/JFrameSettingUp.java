/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.frame;

import com.jogamp.opengl.util.FPSAnimator;
import fr.ird.voxelidar.graphics2d.image.Projection;
import fr.ird.voxelidar.graphics2d.image.ScaleGradient;
import fr.ird.voxelidar.graphics3d.jogl.JoglListener;
import fr.ird.voxelidar.graphics3d.mesh.Attribut;
import fr.ird.voxelidar.graphics3d.mesh.EventManager;
import fr.ird.voxelidar.graphics3d.object.terrain.Terrain;
import fr.ird.voxelidar.graphics3d.object.terrain.TerrainLoader;
import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace;
import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace.VoxelFormat;
import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpaceAdapter;
import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpaceFormat;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.als.LasReader;
import fr.ird.voxelidar.lidar.format.als.LasToTxt;
import fr.ird.voxelidar.lidar.format.als.LasToTxtAdapter;
import fr.ird.voxelidar.lidar.format.tls.Rsp;
import fr.ird.voxelidar.lidar.format.tls.Rxp;
import fr.ird.voxelidar.lidar.format.tls.Scan;
import fr.ird.voxelidar.listener.InputKeyListener;
import fr.ird.voxelidar.listener.InputMouseListener;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec3D;
import fr.ird.voxelidar.util.ColorGradient;
import fr.ird.voxelidar.util.Misc;
import fr.ird.voxelidar.util.Settings;
import fr.ird.voxelidar.voxelisation.VoxelisationParameters;
import fr.ird.voxelidar.voxelisation.VoxelisationTool;
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
import java.util.Collections;
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
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.log4j.Logger;
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
    private static Set<String> setParameters = new HashSet<>();
    private ArrayList<Attribut> attributsList;
    private Map<String, Attribut> mapAttributs;
    public FPSAnimator animator;
    private Terrain terrain;
    private boolean drawVoxelWhenValueIsNull;
    private Settings settings;
    public UIManager.LookAndFeelInfo[] installedLookAndFeels;
    private String currentLookAndFeel;
    private Rsp rsp;
    private Mat4D vopMatrix;
    private Mat4D popMatrix;
    private Mat4D vopPopMatrix;

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
    
    

    public void setMapAttributs(Map<String, Attribut> mapAttributs) {
        this.mapAttributs = mapAttributs;
        
        fillComboBox();
    }

    public Map<String, Attribut> getMapAttributs() {
        return Collections.unmodifiableMap(mapAttributs);
    }
    
    
    public JFrameSettingUp() {
        
        mapAttributs = new LinkedHashMap<>();
        model = new DefaultListModel();
        
        initComponents();
        
        setDefaultAppeareance();
        
        jListOutputFiles.setModel(model);
        
        
        attributsList = new ArrayList<>();
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
                    
                    int minPointX = Integer.valueOf(jTextFieldMinPointX.getText());
                    int minPointY = Integer.valueOf(jTextFieldMinPointY.getText());
                    int minPointZ = Integer.valueOf(jTextFieldMinPointZ.getText());

                    int maxPointX = Integer.valueOf(jTextFieldMaxPointX.getText());
                    int maxPointY = Integer.valueOf(jTextFieldMaxPointY.getText());
                    int maxPointZ = Integer.valueOf(jTextFieldMaxPointZ.getText());

                    int voxelNumberX = (maxPointX - minPointX);
                    int voxelNumberY = (maxPointY - minPointY);
                    int voxelNumberZ = (maxPointZ - minPointZ);

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
    
    public Mat4D getVopMatrix(){
        
        Vec3D point1 = new Vec3D(Double.valueOf(jTextFieldReferencePoint1X.getText()), Double.valueOf(jTextFieldReferencePoint1Y.getText()), Double.valueOf(jTextFieldReferencePoint1Z.getText()));
        Vec3D point2 = new Vec3D(Double.valueOf(jTextFieldReferencePoint2X.getText()), Double.valueOf(jTextFieldReferencePoint2Y.getText()), Double.valueOf(jTextFieldReferencePoint2Z.getText()));
        return VoxelisationTool.getMatrixTransformation(point1, point2);
    }
    
    private void setDefaultAppeareance(){
        setAppearance(UIManager.getSystemLookAndFeelClassName());
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
                }
            }
            
            setAppearance(prefs.get("lookandfeel", ""));
            
            String modelList = prefs.get("model", "");
            String[] modelArray = modelList.split(";");
            
            model = new DefaultListModel();
            
            for (String modelArray1 : modelArray) {
                model.addElement(modelArray1);
            }
            
            jListOutputFiles.setModel(model);
            if(modelArray.length>0)jListOutputFiles.setSelectedIndex(0);
        }
        
        
    }
    
    public static List<Component> getAllComponents(final Container c){
     
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<Component>();
        for (Component comp : comps) {
          if(comp instanceof JRadioButton || comp instanceof JCheckBox || comp instanceof JTextField){
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
                }
            }


            String modelList = "";
            for(int i=0;i<model.getSize();i++){
                modelList+=model.getElementAt(i)+";";
            }
            prefs.put("model", modelList);
            
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
    
    private boolean isTerrainFile(String path) {
        
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
        
        return true;
    }
    
    public void readTerrain(){
        
        try{
            if(terrain == null || !terrain.getPath().equals(jTextFieldFilePathMnt.getText())){
                terrain = TerrainLoader.readFromFile(jTextFieldFilePathMnt.getText());
            }
            
            jButtonGenerateMap.setEnabled(true);
            jButtonGenerateMap.setToolTipText("");

        }catch(Exception e){
            logger.debug("the file isn't a terrain file", e);
        }
    }
    
    public void initComboBox(){
        
        if(jListOutputFiles.getModel().getSize()>0){
            
            String[] parameters = VoxelSpaceFormat.readAttributs(new File(jListOutputFiles.getSelectedValue().toString()));
            
            for(int i=0 ; i< parameters.length ;i++){
                    
                parameters[i] = parameters[i].replaceAll(" ", "");
                parameters[i] = parameters[i].replaceAll("#", "");
            }
            
            //String[] parameters = FileManager.readHeader(jListOutputFiles.getSelectedValue().toString()).split(" ");
            
            if(parameters.length > 5){
                
                setParameters.addAll(Arrays.asList(parameters));
               

                for(int i=0 ; i< parameters.length ;i++){
                    
                    
                    Attribut attribut = new Attribut(parameters[i], parameters[i], setParameters);
                    mapAttributs.put(parameters[i], attribut);
                    //attributsList.add(new Attribut(parameters[i], parameters[i], setParameters));
                }

                jComboBoxAttributeToVisualize.setModel(new DefaultComboBoxModel(parameters));
                jComboBoxAttributeToVisualize.setSelectedIndex(3);

                jComboBoxDefaultX.setModel(new DefaultComboBoxModel(parameters));
                jComboBoxDefaultX.setSelectedIndex(0);

                jComboBoxDefaultY.setModel(new DefaultComboBoxModel(parameters));
                jComboBoxDefaultY.setSelectedIndex(1);

                jComboBoxDefaultZ.setModel(new DefaultComboBoxModel(parameters));
                jComboBoxDefaultZ.setSelectedIndex(2);
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
    
    public void fillComboBox(){
        
        jComboBoxAttributeToVisualize.setModel(new DefaultComboBoxModel(mapAttributs.keySet().toArray()));
        jComboBoxAttributeToVisualize.setSelectedIndex(3);

        jComboBoxDefaultX.setModel(new DefaultComboBoxModel(mapAttributs.keySet().toArray()));
        jComboBoxDefaultX.setSelectedIndex(0);

        jComboBoxDefaultY.setModel(new DefaultComboBoxModel(mapAttributs.keySet().toArray()));
        jComboBoxDefaultY.setSelectedIndex(1);

        jComboBoxDefaultZ.setModel(new DefaultComboBoxModel(mapAttributs.keySet().toArray()));
        jComboBoxDefaultZ.setSelectedIndex(2);
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
        jSplitPane1 = new javax.swing.JSplitPane();
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
        jButtonExecuteVox1 = new javax.swing.JButton();
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
        jPanel25 = new javax.swing.JPanel();
        jTextFieldVoxelNumberX = new javax.swing.JTextField();
        jTextFieldVoxelNumberY = new javax.swing.JTextField();
        jTextFieldVoxelNumberZ = new javax.swing.JTextField();
        jTextFieldVoxelNumberRes = new javax.swing.JTextField();
        jPanel26 = new javax.swing.JPanel();
        jTextFieldMaxPointX = new javax.swing.JTextField();
        jTextFieldMaxPointY = new javax.swing.JTextField();
        jTextFieldMaxPointZ = new javax.swing.JTextField();
        jPanel27 = new javax.swing.JPanel();
        jTextFieldMinPointX = new javax.swing.JTextField();
        jTextFieldMinPointY = new javax.swing.JTextField();
        jTextFieldMinPointZ = new javax.swing.JTextField();
        jButtonExecuteVox = new javax.swing.JButton();
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
        jPanel34 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabelName7 = new javax.swing.JLabel();
        jLabelPath7 = new javax.swing.JLabel();
        jLabelSize2 = new javax.swing.JLabel();
        jLabelVaryingFileSizeInputLocal = new javax.swing.JLabel();
        jTextFieldFilePathInputLocal = new javax.swing.JTextField();
        jTextFieldFileNameInputLocal = new javax.swing.JTextField();
        jButtonOpenInputFileVoxelisation1 = new javax.swing.JButton();
        jPanel22 = new javax.swing.JPanel();
        jLabelName8 = new javax.swing.JLabel();
        jLabelPath8 = new javax.swing.JLabel();
        jTextFieldFilePathCoordLocal = new javax.swing.JTextField();
        jTextFieldFileNameCoordLocal = new javax.swing.JTextField();
        jButtonOpenCoordinatesFile1 = new javax.swing.JButton();
        jPanel23 = new javax.swing.JPanel();
        jLabelName9 = new javax.swing.JLabel();
        jLabelPath9 = new javax.swing.JLabel();
        jTextFieldFilePathSaveLocal = new javax.swing.JTextField();
        jTextFieldFileNameSaveLocal = new javax.swing.JTextField();
        jButtonChooseOutputDirectoryVox1 = new javax.swing.JButton();
        jPanelDisplayParametersTab = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jPanel2 = new javax.swing.JPanel();
        jTextField3 = new javax.swing.JTextField();
        jTextField4 = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jTextField7 = new javax.swing.JTextField();
        jTextField5 = new javax.swing.JTextField();
        jTextField6 = new javax.swing.JTextField();
        jTextField8 = new javax.swing.JTextField();
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
        jRadioButtonVegetationLayer = new javax.swing.JRadioButton();
        jRadioButtonTransmittanceMap = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel8 = new javax.swing.JPanel();
        jButtonAddFile = new javax.swing.JButton();
        jButtonRemoveFile = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListOutputFiles = new javax.swing.JList();
        jButtonLoadSelectedFile = new javax.swing.JButton();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new java.awt.Dimension(560, 450));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });

        jSplitPane1.setDividerLocation(420);
        jSplitPane1.setDividerSize(0);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(0.33);
        jSplitPane1.setContinuousLayout(true);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(848, 533));

        jTabbedPane1.setMinimumSize(new java.awt.Dimension(0, 0));

        jTabbedPane2.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        jPanel39.setBorder(javax.swing.BorderFactory.createTitledBorder("Input file"));

        jLabelName6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName6.setText("Name");
        jLabelName6.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath6.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath6.setText("Path");

        jTextFieldFilePathRsp.setEditable(false);
        jTextFieldFilePathRsp.setColumns(38);
        jTextFieldFilePathRsp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathRspActionPerformed(evt);
            }
        });

        jTextFieldFileNameRsp.setEditable(false);
        jTextFieldFileNameRsp.setColumns(50);
        jTextFieldFileNameRsp.setToolTipText("");
        jTextFieldFileNameRsp.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameRsp.setName(""); // NOI18N

        jButtonOpenRspFile.setText("Open");
        jButtonOpenRspFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenRspFileActionPerformed(evt);
            }
        });

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

        buttonGroup1.add(jRadioButtonLightFile);
        jRadioButtonLightFile.setSelected(true);
        jRadioButtonLightFile.setText("Light file");
        jRadioButtonLightFile.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButtonLightFileStateChanged(evt);
            }
        });

        buttonGroup1.add(jRadioButtonComplexFile);
        jRadioButtonComplexFile.setText("Complex file");
        jRadioButtonComplexFile.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jRadioButtonComplexFileStateChanged(evt);
            }
        });

        jScrollPane1.setViewportView(jListRspScans);

        jButtonSopMatrix.setText("SOP matrix");
        jButtonSopMatrix.setEnabled(false);

        jButtonPopMatrix.setText("POP matrix");
        jButtonPopMatrix.setToolTipText("Project orientation and position");
        jButtonPopMatrix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPopMatrixActionPerformed(evt);
            }
        });

        jButtonVopMatrix.setText("VOP matrix");
        jButtonVopMatrix.setToolTipText("Voxel orientation and position");
        jButtonVopMatrix.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonVopMatrixActionPerformed(evt);
            }
        });

        jButton1.setText("POP %*% VOP");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel40.setBorder(javax.swing.BorderFactory.createTitledBorder("Min point"));
        jPanel40.setToolTipText("");

        jTextFieldMinPointX2.setText("-10");
        jTextFieldMinPointX2.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldMinPointX2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMinPointX2KeyTyped(evt);
            }
        });

        jTextFieldMinPointY2.setText("50");
        jTextFieldMinPointY2.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldMinPointY2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMinPointY2KeyTyped(evt);
            }
        });

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

        jPanel41.setBorder(javax.swing.BorderFactory.createTitledBorder("Max point"));
        jPanel41.setToolTipText("");

        jTextFieldMaxPointX2.setText("10");
        jTextFieldMaxPointX2.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldMaxPointX2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMaxPointX2KeyTyped(evt);
            }
        });

        jTextFieldMaxPointY2.setText("150");
        jTextFieldMaxPointY2.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldMaxPointY2.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMaxPointY2KeyTyped(evt);
            }
        });

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

        jPanel42.setBorder(javax.swing.BorderFactory.createTitledBorder("Voxel Number"));
        jPanel42.setToolTipText("");

        jTextFieldVoxelNumberX1.setText("20");
        jTextFieldVoxelNumberX1.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldVoxelNumberX1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberX1KeyTyped(evt);
            }
        });

        jTextFieldVoxelNumberY1.setText("100");
        jTextFieldVoxelNumberY1.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldVoxelNumberY1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberY1KeyTyped(evt);
            }
        });

        jTextFieldVoxelNumberZ1.setText("70");
        jTextFieldVoxelNumberZ1.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));
        jTextFieldVoxelNumberZ1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberZ1KeyTyped(evt);
            }
        });

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

        jPanel19.setBorder(javax.swing.BorderFactory.createTitledBorder("Output path"));

        jLabelPath10.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath10.setText("Path");

        jTextFieldFileOutputPathTlsVox.setEditable(false);
        jTextFieldFileOutputPathTlsVox.setColumns(38);
        jTextFieldFileOutputPathTlsVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFileOutputPathTlsVoxActionPerformed(evt);
            }
        });

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

        jButtonExecuteVox1.setText("Execute");
        jButtonExecuteVox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecuteVox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel36Layout = new javax.swing.GroupLayout(jPanel36);
        jPanel36.setLayout(jPanel36Layout);
        jPanel36Layout.setHorizontalGroup(
            jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel36Layout.createSequentialGroup()
                .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel36Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jRadioButtonLightFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jRadioButtonComplexFile))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 380, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
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
                    .addComponent(jButtonExecuteVox1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE))
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
                        .addComponent(jButtonExecuteVox1))
                    .addGroup(jPanel36Layout.createSequentialGroup()
                        .addComponent(jPanel39, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(14, 14, 14)
                        .addGroup(jPanel36Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButtonLightFile)
                            .addComponent(jRadioButtonComplexFile))))
                .addContainerGap(14, Short.MAX_VALUE))
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

        jPanel16.setBorder(javax.swing.BorderFactory.createTitledBorder("Input file (*.laz, *.las, *.txt)"));

        jLabelName3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName3.setText("Name");
        jLabelName3.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath3.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath3.setText("Path");

        jLabelSize1.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelSize1.setText("Size (bytes)");

        jLabelFileSizeInputVox.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabelFileSizeInputVox.setMaximumSize(new java.awt.Dimension(40000, 40000));
        jLabelFileSizeInputVox.setName(""); // NOI18N
        jLabelFileSizeInputVox.setPreferredSize(new java.awt.Dimension(150, 20));

        jTextFieldFilePathInputVox.setEditable(false);
        jTextFieldFilePathInputVox.setColumns(38);
        jTextFieldFilePathInputVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathInputVoxActionPerformed(evt);
            }
        });

        jTextFieldFileNameInputVox.setEditable(false);
        jTextFieldFileNameInputVox.setColumns(50);
        jTextFieldFileNameInputVox.setToolTipText("");
        jTextFieldFileNameInputVox.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameInputVox.setName(""); // NOI18N

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
                    .addComponent(jButtonOpenInputFileVoxelisation)
                    .addGroup(jPanel16Layout.createSequentialGroup()
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName3)
                            .addComponent(jLabelPath3)
                            .addComponent(jLabelSize1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel16Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFileNameInputVox, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jTextFieldFilePathInputVox, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jLabelFileSizeInputVox, javax.swing.GroupLayout.DEFAULT_SIZE, 232, Short.MAX_VALUE))))
                .addGap(34, 34, 34))
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

        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder("Trajectory file"));

        jLabelName5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName5.setText("Name");
        jLabelName5.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath5.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath5.setText("Path");

        jTextFieldFilePathTrajVox.setEditable(false);
        jTextFieldFilePathTrajVox.setColumns(38);
        jTextFieldFilePathTrajVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathTrajVoxActionPerformed(evt);
            }
        });

        jTextFieldFileNameTrajVox.setEditable(false);
        jTextFieldFileNameTrajVox.setColumns(50);
        jTextFieldFileNameTrajVox.setToolTipText("");
        jTextFieldFileNameTrajVox.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameTrajVox.setName(""); // NOI18N

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
                    .addComponent(jButtonOpenTrajectoryFile)
                    .addGroup(jPanel18Layout.createSequentialGroup()
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName5)
                            .addComponent(jLabelPath5))
                        .addGap(40, 40, 40)
                        .addGroup(jPanel18Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFileNameTrajVox, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jTextFieldFilePathTrajVox, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))))
                .addGap(34, 34, 34))
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

        jPanel17.setBorder(javax.swing.BorderFactory.createTitledBorder("Output file (*.vox)"));

        jLabelName4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName4.setText("Name");
        jLabelName4.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath4.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath4.setText("Path");

        jTextFieldFilePathSaveVox.setEditable(false);
        jTextFieldFilePathSaveVox.setColumns(38);
        jTextFieldFilePathSaveVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathSaveVoxActionPerformed(evt);
            }
        });

        jTextFieldFileNameSaveVox.setEditable(false);
        jTextFieldFileNameSaveVox.setColumns(50);
        jTextFieldFileNameSaveVox.setToolTipText("");
        jTextFieldFileNameSaveVox.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameSaveVox.setName(""); // NOI18N

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
                    .addComponent(jButtonChooseOutputDirectoryVox)
                    .addGroup(jPanel17Layout.createSequentialGroup()
                        .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName4)
                            .addComponent(jLabelPath4))
                        .addGap(39, 39, 39)
                        .addGroup(jPanel17Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFileNameSaveVox, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jTextFieldFilePathSaveVox, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))))
                .addGap(37, 37, 37))
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

        jPanel25.setBorder(javax.swing.BorderFactory.createTitledBorder("Voxel Number"));
        jPanel25.setToolTipText("");

        jTextFieldVoxelNumberX.setText("20");
        jTextFieldVoxelNumberX.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldVoxelNumberX.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberXKeyTyped(evt);
            }
        });

        jTextFieldVoxelNumberY.setText("100");
        jTextFieldVoxelNumberY.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldVoxelNumberY.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberYKeyTyped(evt);
            }
        });

        jTextFieldVoxelNumberZ.setText("70");
        jTextFieldVoxelNumberZ.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));
        jTextFieldVoxelNumberZ.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldVoxelNumberZKeyTyped(evt);
            }
        });

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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTextFieldVoxelNumberRes, javax.swing.GroupLayout.PREFERRED_SIZE, 83, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel25Layout.setVerticalGroup(
            jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel25Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldVoxelNumberX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldVoxelNumberY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldVoxelNumberZ, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldVoxelNumberRes, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel26.setBorder(javax.swing.BorderFactory.createTitledBorder("Max point"));
        jPanel26.setToolTipText("");

        jTextFieldMaxPointX.setText("10");
        jTextFieldMaxPointX.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldMaxPointX.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMaxPointXKeyTyped(evt);
            }
        });

        jTextFieldMaxPointY.setText("150");
        jTextFieldMaxPointY.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldMaxPointY.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMaxPointYKeyTyped(evt);
            }
        });

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

        jPanel27.setBorder(javax.swing.BorderFactory.createTitledBorder("Min point"));
        jPanel27.setToolTipText("");

        jTextFieldMinPointX.setText("-10");
        jTextFieldMinPointX.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));
        jTextFieldMinPointX.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMinPointXKeyTyped(evt);
            }
        });

        jTextFieldMinPointY.setText("50");
        jTextFieldMinPointY.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));
        jTextFieldMinPointY.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                jTextFieldMinPointYKeyTyped(evt);
            }
        });

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

        jButtonExecuteVox.setText("Execute");
        jButtonExecuteVox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExecuteVoxActionPerformed(evt);
            }
        });

        jPanel28.setBorder(javax.swing.BorderFactory.createTitledBorder("Reference points"));

        jPanel29.setBorder(javax.swing.BorderFactory.createTitledBorder("Point 1"));
        jPanel29.setToolTipText("");

        jTextFieldReferencePoint1X.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));

        jTextFieldReferencePoint1Y.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));

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
                .addComponent(jTextFieldReferencePoint1Z, javax.swing.GroupLayout.PREFERRED_SIZE, 49, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel29Layout.setVerticalGroup(
            jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel29Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextFieldReferencePoint1X, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldReferencePoint1Y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextFieldReferencePoint1Z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel30.setBorder(javax.swing.BorderFactory.createTitledBorder("Point 2"));
        jPanel30.setToolTipText("");

        jTextFieldReferencePoint2X.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));

        jTextFieldReferencePoint2Y.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));

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
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel29, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel30, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(37, 37, 37)
                .addComponent(jButtonPopMatrix1)
                .addGap(29, 29, 29))
        );
        jPanel28Layout.setVerticalGroup(
            jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel28Layout.createSequentialGroup()
                .addGroup(jPanel28Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addComponent(jButtonPopMatrix1))
                    .addGroup(jPanel28Layout.createSequentialGroup()
                        .addComponent(jPanel29, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel30, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel33Layout = new javax.swing.GroupLayout(jPanel33);
        jPanel33.setLayout(jPanel33Layout);
        jPanel33Layout.setHorizontalGroup(
            jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel33Layout.createSequentialGroup()
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel18, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel16, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jButtonExecuteVox, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jPanel25, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel33Layout.createSequentialGroup()
                                .addComponent(jPanel27, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jPanel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                    .addComponent(jPanel28, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(67, 67, 67))
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
                        .addComponent(jPanel17, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
                    .addGroup(jPanel33Layout.createSequentialGroup()
                        .addComponent(jPanel28, javax.swing.GroupLayout.PREFERRED_SIZE, 152, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(2, 2, 2)
                        .addGroup(jPanel33Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPanel27, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel26, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel25, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 40, Short.MAX_VALUE)
                        .addComponent(jButtonExecuteVox)))
                .addContainerGap())
        );

        jTabbedPane3.addTab("Voxelisation", jPanel33);

        jPanel6.setMaximumSize(new java.awt.Dimension(100, 32767));
        jPanel6.setPreferredSize(new java.awt.Dimension(100, 273));

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder("Input file (*.laz, *.las)"));

        jLabelName.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName.setText("Name");
        jLabelName.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath.setText("Path");

        jLabelSize.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelSize.setText("Size (bytes)");

        jLabelFileSize.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabelFileSize.setMaximumSize(new java.awt.Dimension(40000, 40000));
        jLabelFileSize.setName(""); // NOI18N
        jLabelFileSize.setPreferredSize(new java.awt.Dimension(150, 20));

        jTextFieldFilePathInputTxt.setEditable(false);
        jTextFieldFilePathInputTxt.setColumns(38);
        jTextFieldFilePathInputTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathInputTxtActionPerformed(evt);
            }
        });

        jTextFieldFileNameInputTxt.setEditable(false);
        jTextFieldFileNameInputTxt.setColumns(50);
        jTextFieldFileNameInputTxt.setToolTipText("");
        jTextFieldFileNameInputTxt.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameInputTxt.setName(""); // NOI18N

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

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Output file (*.txt)"));

        jLabelName2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName2.setText("Name");
        jLabelName2.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath2.setText("Path");

        jTextFieldFilePathOutputTxt.setEditable(false);
        jTextFieldFilePathOutputTxt.setColumns(38);
        jTextFieldFilePathOutputTxt.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathOutputTxtActionPerformed(evt);
            }
        });

        jTextFieldFileNameOutputTxt.setEditable(false);
        jTextFieldFileNameOutputTxt.setColumns(50);
        jTextFieldFileNameOutputTxt.setToolTipText("");
        jTextFieldFileNameOutputTxt.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameOutputTxt.setName(""); // NOI18N

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

        jCheckBoxWriteX.setSelected(true);
        jCheckBoxWriteX.setText("(x)");
        jCheckBoxWriteX.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteXStateChanged(evt);
            }
        });

        jCheckBoxWriteY.setSelected(true);
        jCheckBoxWriteY.setText("(y)");
        jCheckBoxWriteY.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteYStateChanged(evt);
            }
        });

        jCheckBoxWriteZ.setSelected(true);
        jCheckBoxWriteZ.setText("(z)");
        jCheckBoxWriteZ.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteZStateChanged(evt);
            }
        });

        jCheckBoxWriteI.setSelected(true);
        jCheckBoxWriteI.setText("(i)ntensity");
        jCheckBoxWriteI.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteIStateChanged(evt);
            }
        });

        jCheckBoxWriteR.setSelected(true);
        jCheckBoxWriteR.setText("(r)eturn number");
        jCheckBoxWriteR.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteRStateChanged(evt);
            }
        });

        jCheckBoxWriteN.setSelected(true);
        jCheckBoxWriteN.setText("(n)umber of returns");
        jCheckBoxWriteN.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteNStateChanged(evt);
            }
        });

        jCheckBoxWriteD.setText("scan (d)irection");
        jCheckBoxWriteD.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteDStateChanged(evt);
            }
        });

        jCheckBoxWriteE.setText("(e)dge of flight line");
        jCheckBoxWriteE.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteEStateChanged(evt);
            }
        });

        jCheckBoxWriteC.setSelected(true);
        jCheckBoxWriteC.setText("(c)lassification");
        jCheckBoxWriteC.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteCStateChanged(evt);
            }
        });

        jCheckBoxWriteA.setSelected(true);
        jCheckBoxWriteA.setText("scan (a)ngle");
        jCheckBoxWriteA.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteAStateChanged(evt);
            }
        });

        jCheckBoxWriteU.setText("(u)ser data");
        jCheckBoxWriteU.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteUStateChanged(evt);
            }
        });

        jCheckBoxWriteP.setSelected(true);
        jCheckBoxWriteP.setText("(p)oint source ID");
        jCheckBoxWriteP.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWritePStateChanged(evt);
            }
        });

        jCheckBoxWriteT.setSelected(true);
        jCheckBoxWriteT.setText("GPS (t)ime ");
        jCheckBoxWriteT.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteTStateChanged(evt);
            }
        });

        jCheckBoxWriteRGB.setText("(RGB)color");
        jCheckBoxWriteRGB.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteRGBStateChanged(evt);
            }
        });

        jCheckBoxWritew.setText("(w)ave packet index");
        jCheckBoxWritew.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWritewStateChanged(evt);
            }
        });

        jCheckBoxWriteW.setText("(W)ave packet");
        jCheckBoxWriteW.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteWStateChanged(evt);
            }
        });

        jCheckBoxWriteV.setText("wa(V)e form");
        jCheckBoxWriteV.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                jCheckBoxWriteVStateChanged(evt);
            }
        });

        jButtonLoad.setText("Execute");
        jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoadActionPerformed(evt);
            }
        });

        jCheckBoxWriteHeader.setSelected(true);
        jCheckBoxWriteHeader.setText("write header");

        jTextFieldArguments.setBackground(new java.awt.Color(238, 238, 238));
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
                .addContainerGap(84, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel31Layout = new javax.swing.GroupLayout(jPanel31);
        jPanel31.setLayout(jPanel31Layout);
        jPanel31Layout.setHorizontalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 785, Short.MAX_VALUE)
            .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 785, Short.MAX_VALUE))
        );
        jPanel31Layout.setVerticalGroup(
            jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 358, Short.MAX_VALUE)
            .addGroup(jPanel31Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, 358, Short.MAX_VALUE))
        );

        jTabbedPane3.addTab("LAS => TXT", jPanel31);

        jPanel21.setBorder(javax.swing.BorderFactory.createTitledBorder("Input file (*.laz, *.las, *.txt)"));

        jLabelName7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName7.setText("Name");
        jLabelName7.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath7.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath7.setText("Path");

        jLabelSize2.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelSize2.setText("Size (bytes)");

        jLabelVaryingFileSizeInputLocal.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabelVaryingFileSizeInputLocal.setMaximumSize(new java.awt.Dimension(40000, 40000));
        jLabelVaryingFileSizeInputLocal.setName(""); // NOI18N
        jLabelVaryingFileSizeInputLocal.setPreferredSize(new java.awt.Dimension(150, 20));

        jTextFieldFilePathInputLocal.setEditable(false);
        jTextFieldFilePathInputLocal.setColumns(38);
        jTextFieldFilePathInputLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathInputLocalActionPerformed(evt);
            }
        });

        jTextFieldFileNameInputLocal.setEditable(false);
        jTextFieldFileNameInputLocal.setColumns(50);
        jTextFieldFileNameInputLocal.setToolTipText("");
        jTextFieldFileNameInputLocal.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameInputLocal.setName(""); // NOI18N

        jButtonOpenInputFileVoxelisation1.setText("Open");
        jButtonOpenInputFileVoxelisation1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenInputFileVoxelisation1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel21Layout = new javax.swing.GroupLayout(jPanel21);
        jPanel21.setLayout(jPanel21Layout);
        jPanel21Layout.setHorizontalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonOpenInputFileVoxelisation1)
                    .addGroup(jPanel21Layout.createSequentialGroup()
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName7)
                            .addComponent(jLabelPath7)
                            .addComponent(jLabelSize2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFileNameInputLocal, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jTextFieldFilePathInputLocal, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jLabelVaryingFileSizeInputLocal, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE))))
                .addGap(34, 34, 34))
        );
        jPanel21Layout.setVerticalGroup(
            jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel21Layout.createSequentialGroup()
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileNameInputLocal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilePathInputLocal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPath7))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel21Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabelVaryingFileSizeInputLocal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelSize2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpenInputFileVoxelisation1))
        );

        jPanel22.setBorder(javax.swing.BorderFactory.createTitledBorder("Coordinates file"));

        jLabelName8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName8.setText("Name");
        jLabelName8.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath8.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath8.setText("Path");

        jTextFieldFilePathCoordLocal.setEditable(false);
        jTextFieldFilePathCoordLocal.setColumns(38);
        jTextFieldFilePathCoordLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathCoordLocalActionPerformed(evt);
            }
        });

        jTextFieldFileNameCoordLocal.setEditable(false);
        jTextFieldFileNameCoordLocal.setColumns(50);
        jTextFieldFileNameCoordLocal.setToolTipText("");
        jTextFieldFileNameCoordLocal.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameCoordLocal.setName(""); // NOI18N

        jButtonOpenCoordinatesFile1.setText("Open");
        jButtonOpenCoordinatesFile1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenCoordinatesFile1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel22Layout = new javax.swing.GroupLayout(jPanel22);
        jPanel22.setLayout(jPanel22Layout);
        jPanel22Layout.setHorizontalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonOpenCoordinatesFile1)
                    .addGroup(jPanel22Layout.createSequentialGroup()
                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName8)
                            .addComponent(jLabelPath8))
                        .addGap(40, 40, 40)
                        .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFileNameCoordLocal, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jTextFieldFilePathCoordLocal, javax.swing.GroupLayout.DEFAULT_SIZE, 192, Short.MAX_VALUE))))
                .addGap(34, 34, 34))
        );
        jPanel22Layout.setVerticalGroup(
            jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel22Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileNameCoordLocal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel22Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilePathCoordLocal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPath8))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpenCoordinatesFile1))
        );

        jPanel23.setBorder(javax.swing.BorderFactory.createTitledBorder("Output file"));

        jLabelName9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelName9.setText("Name");
        jLabelName9.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        jLabelPath9.setFont(new java.awt.Font("Tahoma", 1, 11)); // NOI18N
        jLabelPath9.setText("Path");

        jTextFieldFilePathSaveLocal.setEditable(false);
        jTextFieldFilePathSaveLocal.setColumns(38);
        jTextFieldFilePathSaveLocal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldFilePathSaveLocalActionPerformed(evt);
            }
        });

        jTextFieldFileNameSaveLocal.setEditable(false);
        jTextFieldFileNameSaveLocal.setColumns(50);
        jTextFieldFileNameSaveLocal.setToolTipText("");
        jTextFieldFileNameSaveLocal.setMinimumSize(new java.awt.Dimension(0, 20));
        jTextFieldFileNameSaveLocal.setName(""); // NOI18N

        jButtonChooseOutputDirectoryVox1.setText("Choose directory");
        jButtonChooseOutputDirectoryVox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonChooseOutputDirectoryVox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel23Layout = new javax.swing.GroupLayout(jPanel23);
        jPanel23.setLayout(jPanel23Layout);
        jPanel23Layout.setHorizontalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addComponent(jButtonChooseOutputDirectoryVox1)
                        .addGap(0, 155, Short.MAX_VALUE))
                    .addGroup(jPanel23Layout.createSequentialGroup()
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelName9)
                            .addComponent(jLabelPath9))
                        .addGap(64, 64, 64)
                        .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextFieldFileNameSaveLocal, javax.swing.GroupLayout.DEFAULT_SIZE, 1, Short.MAX_VALUE)
                            .addComponent(jTextFieldFilePathSaveLocal, javax.swing.GroupLayout.PREFERRED_SIZE, 1, Short.MAX_VALUE))))
                .addContainerGap())
        );
        jPanel23Layout.setVerticalGroup(
            jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel23Layout.createSequentialGroup()
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFileNameSaveLocal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelName9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel23Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldFilePathSaveLocal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelPath9))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonChooseOutputDirectoryVox1))
        );

        javax.swing.GroupLayout jPanel20Layout = new javax.swing.GroupLayout(jPanel20);
        jPanel20.setLayout(jPanel20Layout);
        jPanel20Layout.setHorizontalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(129, Short.MAX_VALUE))
        );
        jPanel20Layout.setVerticalGroup(
            jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel20Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel20Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jPanel21, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(26, 26, 26)
                        .addComponent(jPanel23, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(71, Short.MAX_VALUE))
                    .addGroup(jPanel20Layout.createSequentialGroup()
                        .addComponent(jPanel22, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );

        javax.swing.GroupLayout jPanel34Layout = new javax.swing.GroupLayout(jPanel34);
        jPanel34.setLayout(jPanel34Layout);
        jPanel34Layout.setHorizontalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 785, Short.MAX_VALUE)
            .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jPanel20, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel34Layout.setVerticalGroup(
            jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 358, Short.MAX_VALUE)
            .addGroup(jPanel34Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel34Layout.createSequentialGroup()
                    .addComponent(jPanel20, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGap(0, 23, Short.MAX_VALUE)))
        );

        jTabbedPane3.addTab("LAS => local LAS", jPanel34);

        jTabbedPane2.addTab("ALS", jTabbedPane3);

        javax.swing.GroupLayout jPanelOutputParametersTabLayout = new javax.swing.GroupLayout(jPanelOutputParametersTab);
        jPanelOutputParametersTab.setLayout(jPanelOutputParametersTabLayout);
        jPanelOutputParametersTabLayout.setHorizontalGroup(
            jPanelOutputParametersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2)
        );
        jPanelOutputParametersTabLayout.setVerticalGroup(
            jPanelOutputParametersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jTabbedPane2, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        jTabbedPane1.addTab("File conversion", jPanelOutputParametersTab);

        jPanel7.setBorder(javax.swing.BorderFactory.createTitledBorder("Voxel space"));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));

        jTextField1.setBorder(javax.swing.BorderFactory.createTitledBorder("Min"));

        jTextField2.setBorder(javax.swing.BorderFactory.createTitledBorder("Max"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));

        jTextField3.setBorder(javax.swing.BorderFactory.createTitledBorder("Min"));

        jTextField4.setBorder(javax.swing.BorderFactory.createTitledBorder("Max"));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, 42, javax.swing.GroupLayout.PREFERRED_SIZE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel9.setBorder(javax.swing.BorderFactory.createTitledBorder("Voxel number"));

        jTextField7.setBorder(javax.swing.BorderFactory.createTitledBorder("Z"));
        jTextField7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField7ActionPerformed(evt);
            }
        });

        jTextField5.setBorder(javax.swing.BorderFactory.createTitledBorder("X"));

        jTextField6.setBorder(javax.swing.BorderFactory.createTitledBorder("Y"));

        jTextField8.setBorder(javax.swing.BorderFactory.createTitledBorder("Count"));
        jTextField8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField8ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel9Layout.createSequentialGroup()
                        .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(47, 47, 47)
                        .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, 56, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextField5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextField6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanelDisplayParametersTabLayout = new javax.swing.GroupLayout(jPanelDisplayParametersTab);
        jPanelDisplayParametersTab.setLayout(jPanelDisplayParametersTabLayout);
        jPanelDisplayParametersTabLayout.setHorizontalGroup(
            jPanelDisplayParametersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDisplayParametersTabLayout.createSequentialGroup()
                .addGap(181, 181, 181)
                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(328, Short.MAX_VALUE))
        );
        jPanelDisplayParametersTabLayout.setVerticalGroup(
            jPanelDisplayParametersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelDisplayParametersTabLayout.createSequentialGroup()
                .addGroup(jPanelDisplayParametersTabLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelDisplayParametersTabLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelDisplayParametersTabLayout.createSequentialGroup()
                        .addGap(48, 48, 48)
                        .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Display parameters", jPanelDisplayParametersTab);

        jTabbedPane4.setTabPlacement(javax.swing.JTabbedPane.LEFT);

        jButtonOpen3DDisplay.setText("Open display window");
        jButtonOpen3DDisplay.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonOpen3DDisplay.setEnabled(false);
        jButtonOpen3DDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpen3DDisplayActionPerformed(evt);
            }
        });

        jPanel3DViewDensity.setBorder(javax.swing.BorderFactory.createTitledBorder("Voxel"));

        jPanel14.setBorder(javax.swing.BorderFactory.createTitledBorder("Attribute to visualize"));

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

        jButtonCreateAttribut.setText("Create attribut");
        jButtonCreateAttribut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCreateAttributActionPerformed(evt);
            }
        });

        jLabel1.setText("X = ");

        jLabel3.setText("Y = ");

        jLabel4.setText("Z = ");

        jLabel5.setText("Expression");

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

        jCheckBoxDrawTerrain.setText("Draw DTM");
        jCheckBoxDrawTerrain.setEnabled(false);

        jCheckBoxDrawAxis.setText("Draw axis");

        jCheckBoxDrawNullVoxel.setText("Draw voxel when value is null");
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
                .addContainerGap(131, Short.MAX_VALUE))
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

        jButtonGenerateMap.setText("Generate map");
        jButtonGenerateMap.setToolTipText("Load a terrain first");
        jButtonGenerateMap.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        jButtonGenerateMap.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonGenerateMapActionPerformed(evt);
            }
        });

        buttonGroup2DProjection.add(jRadioButtonPAI);
        jRadioButtonPAI.setSelected(true);
        jRadioButtonPAI.setText("PAI map (plant area index)");

        buttonGroup2DProjection.add(jRadioButtonVegetationLayer);
        jRadioButtonVegetationLayer.setText("Vegetation layer");

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
                    .addComponent(jRadioButtonVegetationLayer)
                    .addComponent(jRadioButtonPAI)
                    .addComponent(jButtonGenerateMap))
                .addContainerGap(522, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonPAI)
                .addGap(15, 15, 15)
                .addComponent(jRadioButtonTransmittanceMap)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonVegetationLayer)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 193, Short.MAX_VALUE)
                .addComponent(jButtonGenerateMap)
                .addGap(42, 42, 42))
        );

        jTabbedPane4.addTab("2D projection", jPanel4);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        jTabbedPane4.addTab("Histogram", jPanel5);

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 687, Short.MAX_VALUE)
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 384, Short.MAX_VALUE)
        );

        jTabbedPane4.addTab("Vegetation profile", jPanel11);

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

        jSplitPane1.setTopComponent(jTabbedPane1);

        jSplitPane2.setDividerLocation(380);
        jSplitPane2.setContinuousLayout(true);
        jSplitPane2.setDoubleBuffered(true);

        jPanel8.setBorder(javax.swing.BorderFactory.createTitledBorder("Working files (*.vox, *.las, *.laz, *.txt)"));
        jPanel8.setPreferredSize(new java.awt.Dimension(200, 138));

        jButtonAddFile.setText("Add");
        jButtonAddFile.setPreferredSize(new java.awt.Dimension(77, 26));
        jButtonAddFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAddFileActionPerformed(evt);
            }
        });

        jButtonRemoveFile.setText("remove");
        jButtonRemoveFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRemoveFileActionPerformed(evt);
            }
        });

        jListOutputFiles.setBorder(javax.swing.BorderFactory.createTitledBorder("Select a file"));
        jListOutputFiles.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jListOutputFiles.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                jListOutputFilesValueChanged(evt);
            }
        });
        jScrollPane2.setViewportView(jListOutputFiles);

        jButtonLoadSelectedFile.setText("Load selected file");
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

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 281, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jButtonLoadSelectedFile, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(jButtonAddFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonRemoveFile, javax.swing.GroupLayout.Alignment.LEADING)))
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonAddFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonRemoveFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonLoadSelectedFile)
                .addContainerGap(22, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
        );

        jSplitPane2.setRightComponent(jPanel8);

        jPanel10.setBorder(javax.swing.BorderFactory.createTitledBorder("DTM"));
        jPanel10.setPreferredSize(new java.awt.Dimension(200, 138));

        jButtonOpenInputFile1.setText("Open");
        jButtonOpenInputFile1.setVerticalAlignment(javax.swing.SwingConstants.TOP);
        jButtonOpenInputFile1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenInputFile1ActionPerformed(evt);
            }
        });

        jTextFieldFileNameMnt.setEditable(false);
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
                            .addComponent(jTextFieldFilePathMnt, javax.swing.GroupLayout.DEFAULT_SIZE, 301, Short.MAX_VALUE)
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

        jSplitPane1.setBottomComponent(jSplitPane2);

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 569, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonOpen3DDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpen3DDisplayActionPerformed

        GLProfile glp = GLProfile.getMaxFixedFunc(true);
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setDoubleBuffered(true);
        
        GLRenderFrame renderFrame = GLRenderFrame.create(caps, 640, 480);
        
        animator = new FPSAnimator(renderFrame, 60);
        settings = new Settings(this);
        
        readTerrain();
        
        JoglListener joglContext = new JoglListener(this, terrain, settings);
        EventManager eventListener = new EventManager(animator, renderFrame, joglContext);
        joglContext.attachEventListener(eventListener);
        
        
        renderFrame.addGLEventListener(joglContext);
        renderFrame.addKeyListener(new InputKeyListener(eventListener));
        renderFrame.addMouseListener(new InputMouseListener(eventListener, animator));
        
        //renderFrame.setPointerVisible(false);
        
        JFrameTools toolsJframe = new JFrameTools(this,joglContext);
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
        
        toolsJframe.addWindowListener(new ToolsJframeWindowListener(renderWindowListener));
        toolsJframe.setVisible(true);
        
        
        
        animator.start();
    }//GEN-LAST:event_jButtonOpen3DDisplayActionPerformed

    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized

        jSplitPane1.setDividerLocation(this.getHeight() - 200);
    }//GEN-LAST:event_formComponentResized

    private void jButtonGenerateMapActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonGenerateMapActionPerformed
        
        if(terrain == null){
            readTerrain();
        }
        
        if(terrain != null){
            
            
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
                    Projection projection = new Projection(voxelSpace, terrain);
                    BufferedImage img = null;
                    
                    if(jRadioButtonPAI.isSelected()){
                        
                        img= projection.generateMap(Projection.PAI);
                        
                    }else if(jRadioButtonVegetationLayer.isSelected()){
                        
                    }else if(jRadioButtonTransmittanceMap.isSelected()){
                        
                        img= projection.generateMap(Projection.TRANSMITTANCE);
                    }
                    
                    BufferedImage colorScale = ScaleGradient.generateScale(ColorGradient.GRADIENT_HEAT, projection.getMinValue(), projection.getMaxValue(), 50, 200);

                    JFrameImageViewer imageViewer = new JFrameImageViewer(img, colorScale);
                    imageViewer.setJLabelMinValue(String.valueOf(projection.getMinValue()+0.0));
                    imageViewer.setJLabelMaxValue(String.valueOf(projection.getMaxValue()+0.0));
                    imageViewer.setVisible(true);

                }
            });

            try {
                voxelSpace.loadFromFile(new File(jListOutputFiles.getSelectedValue().toString()), VoxelFormat.VOXELSPACE_FORMAT1);
                //voxelSpace.loadFromFile(new File(jListOutputFiles.getSelectedValue().toString()));
            } catch (Exception ex) {
                logger.error(null, ex);
            }
        }
    }//GEN-LAST:event_jButtonGenerateMapActionPerformed

    private void jMenuItemLoadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItemLoadActionPerformed
        
        if (jFileChooser3.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            
            openJFrameStateFile(jFileChooser3.getSelectedFile().getAbsolutePath());
            
        }
    }//GEN-LAST:event_jMenuItemLoadActionPerformed

    private void jButtonAddFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAddFileActionPerformed
        
        if (jFileChooser2.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            
            //if(isVoxelFile(jFileChooser2.getSelectedFile().getAbsolutePath())){
                
                model.addElement(jFileChooser2.getSelectedFile().getAbsolutePath());
                jListOutputFiles.setSelectedIndex(0);
                
            //}else{
                //JOptionPane.showMessageDialog(this, "File isn't a voxel file");
            //}
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

    private void jTextField7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField7ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField7ActionPerformed

    private void jTextField8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField8ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField8ActionPerformed

    private void jButtonCreateAttributActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCreateAttributActionPerformed
        
        
        final JFrameAttributCreation jframeAttribut = new JFrameAttributCreation(mapAttributs);
        final JFrameSettingUp parent = this;
        
        jframeAttribut.setVisible(true);
        jframeAttribut.addWindowListener(new java.awt.event.WindowAdapter() {
            
            @Override
            public void windowClosed(java.awt.event.WindowEvent evt) {
                parent.setMapAttributs(jframeAttribut.getMapAttributs());
            }
        });
        
        
    }//GEN-LAST:event_jButtonCreateAttributActionPerformed

    private void jComboBoxAttributeToVisualizePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_jComboBoxAttributeToVisualizePropertyChange
        
        
    }//GEN-LAST:event_jComboBoxAttributeToVisualizePropertyChange

    private void jComboBoxAttributeToVisualizeItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBoxAttributeToVisualizeItemStateChanged
        
        jTextFieldAttributExpression.setText(mapAttributs.get(jComboBoxAttributeToVisualize.getSelectedItem().toString()).getExpressionString());
    }//GEN-LAST:event_jComboBoxAttributeToVisualizeItemStateChanged

    private void jButtonOpenInputFile1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenInputFile1ActionPerformed
        
        if (jFileChooser1.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            
            
            if(isTerrainFile(jFileChooser1.getSelectedFile().getAbsolutePath())){
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

    private void jCheckBoxDrawNullVoxelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDrawNullVoxelActionPerformed
        
        
    }//GEN-LAST:event_jCheckBoxDrawNullVoxelActionPerformed

    private void jCheckBoxDrawNullVoxelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxDrawNullVoxelStateChanged
        
        if(jCheckBoxDrawNullVoxel.isSelected()){
            drawVoxelWhenValueIsNull = true;
        }else{
            drawVoxelWhenValueIsNull = false;
        }
        
    }//GEN-LAST:event_jCheckBoxDrawNullVoxelStateChanged

    private void jCheckBoxDrawUndergroundVoxelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jCheckBoxDrawUndergroundVoxelStateChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxDrawUndergroundVoxelStateChanged

    private void jCheckBoxDrawUndergroundVoxelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDrawUndergroundVoxelActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBoxDrawUndergroundVoxelActionPerformed

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

    private void jButtonChooseOutputDirectoryVox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChooseOutputDirectoryVox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonChooseOutputDirectoryVox1ActionPerformed

    private void jTextFieldFilePathSaveLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathSaveLocalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathSaveLocalActionPerformed

    private void jButtonOpenCoordinatesFile1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenCoordinatesFile1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonOpenCoordinatesFile1ActionPerformed

    private void jTextFieldFilePathCoordLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathCoordLocalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathCoordLocalActionPerformed

    private void jButtonOpenInputFileVoxelisation1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenInputFileVoxelisation1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButtonOpenInputFileVoxelisation1ActionPerformed

    private void jTextFieldFilePathInputLocalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathInputLocalActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathInputLocalActionPerformed

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

            Las las = LasReader.read(inputFilePath);

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
            lasToTxt.writeTxt(las, outputFilePath, arguments, jCheckBoxWriteHeader.isSelected());
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

        }
    }//GEN-LAST:event_jButtonOpenInputFileActionPerformed

    private void jTextFieldFilePathInputTxtActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathInputTxtActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathInputTxtActionPerformed

    private void jButtonExecuteVoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecuteVoxActionPerformed

        final Mat4D mat4x4 = new Mat4D();
        /*
        mat4x4.mat = new double[]{

            Double.valueOf(jTextFieldMat11.getText()),
            Double.valueOf(jTextFieldMat12.getText()),
            Double.valueOf(jTextFieldMat13.getText()),
            Double.valueOf(jTextFieldMat14.getText()),
            Double.valueOf(jTextFieldMat21.getText()),
            Double.valueOf(jTextFieldMat22.getText()),
            Double.valueOf(jTextFieldMat23.getText()),
            Double.valueOf(jTextFieldMat24.getText()),
            Double.valueOf(jTextFieldMat31.getText()),
            Double.valueOf(jTextFieldMat32.getText()),
            Double.valueOf(jTextFieldMat33.getText()),
            Double.valueOf(jTextFieldMat34.getText()),
            Double.valueOf(jTextFieldMat41.getText()),
            Double.valueOf(jTextFieldMat42.getText()),
            Double.valueOf(jTextFieldMat43.getText()),
            Double.valueOf(jTextFieldMat44.getText())
        };
        */
        
        Mat4D vopMatrix = getVopMatrix();
        
        final String inputVoxPath = jTextFieldFilePathInputVox.getText();
        final File trajectoryFile = new File(jTextFieldFilePathTrajVox.getText());
        
        File inputFile = new File(jTextFieldFilePathInputVox.getText());
        String extension = FileManager.getExtension(inputFile);
        
        VoxelisationParameters parameters = new VoxelisationParameters(
                Double.valueOf(jTextFieldMinPointX.getText()), 
                Double.valueOf(jTextFieldMinPointY.getText()),
                Double.valueOf(jTextFieldMinPointZ.getText()),
                Double.valueOf(jTextFieldMaxPointX.getText()),
                Double.valueOf(jTextFieldMaxPointY.getText()),
                Double.valueOf(jTextFieldMaxPointZ.getText()),
                Integer.valueOf(jTextFieldVoxelNumberX.getText()),
                Integer.valueOf(jTextFieldVoxelNumberY.getText()),
                Integer.valueOf(jTextFieldVoxelNumberZ.getText()));
        
        File outputFile = new File(jTextFieldFilePathSaveVox.getText());
        
        final JProgressLoadingFile progressBar = new JProgressLoadingFile(this);
        progressBar.setVisible(true);

        final JFrameSettingUp parent = this;
        
        switch(extension){
            case ".laz":
                
                //decompress file
                
                break;
                
            case ".las":
                
                //generate shoot file
                //call voxelisation program
                VoxelisationTool voxTool = new VoxelisationTool();
                voxTool.generateVoxelFromLas(LasReader.read(inputVoxPath), trajectoryFile, outputFile, vopMatrix, parameters);
                break;
                
            case ".txt":
                
                //?? is it a text shoot file or a text .las file??
                
                //if shoot file
                    //call voxelisation program
                
                //if text .las file
                    //generate shoot file
                    //call voxelisation program
        }

        //final VoxelPreprocessing preprocessVox = new VoxelPreprocessing();

        /*
        preprocessVox.addVoxelPreprocessingListener(new VoxelPreprocessingAdapter() {

            @Override
            public void alsPreprocessingStepProgress(String progress, int ratio){
                progressBar.jLabelProgressStatus.setText(progress);
                progressBar.jProgressBar1.setValue(ratio);
            }

            @Override
            public void alsPreprocessingFinished(){
                progressBar.dispose();

                int result = JOptionPane.showConfirmDialog(parent, "Do you want to add the voxel file to the voxel filel list?");
                if(result == JOptionPane.OK_OPTION){
                    model.addElement("C:/mon_fichier_voxel.vox");
                    jListOutputFiles.setSelectedValue("C:/mon_fichier_voxel.vox", true);
                }
            }
        });
        */
        /*
        Vec3F bottomCorner = new Vec3F(Float.valueOf(jTextFieldMinPointX.getText()), Float.valueOf(jTextFieldMinPointY.getText()), Float.valueOf(jTextFieldMinPointZ.getText()));
        Vec3F topCorner = new Vec3F(Float.valueOf(jTextFieldMaxPointX.getText()), Float.valueOf(jTextFieldMaxPointY.getText()), Float.valueOf(jTextFieldMaxPointZ.getText()));
        int numberX = Integer.valueOf(jTextFieldVoxelNumberX.getText());
        int numberY = Integer.valueOf(jTextFieldVoxelNumberY.getText());
        int numberZ = Integer.valueOf(jTextFieldVoxelNumberZ.getText());

        Voxelisation vox = new Voxelisation();

        vox.addVoxelisationListener(new VoxelisationAdapter() {

            @Override
            public void voxelisationStepProgress(String progress, int ratio){
                progressBar.setText(progress);
                progressBar.jProgressBar1.setValue(ratio);
            }

            @Override
            public void voxelisationFinished(){
                progressBar.dispose();

                int result = JOptionPane.showConfirmDialog(parent, "Do you want to add the voxel file to the voxel filel list?");
                if(result == JOptionPane.OK_OPTION){
                    model.addElement("C:/mon_fichier_voxel.vox");
                    jListOutputFiles.setSelectedValue("C:/mon_fichier_voxel.vox", true);
                }
            }
        });

        vox.voxelise(mat4x4, inputVoxPath, inputTrajectoryPath, bottomCorner, topCorner, numberX, numberY, numberZ);
        */
        /*
        als.generateEchosFile("C:\\Users\\Julien\\Desktop\\Test Als preprocess\\CoordonnéesP15.csv",
            "C:\\Users\\Julien\\Desktop\\Test Als preprocess\\ALSbuf_xyzirncapt.txt",
            "C:\\Users\\Julien\\Desktop\\Test Als preprocess\\sbet_250913_01.txt");

        */
        //als.generateEchosFile(jTextFieldVaryingFilePathCoordVox.getText(),
            //jTextFieldVaryingFilePathInputVox.getText(), jTextFieldVaryingFilePathTrajVox.getText());
    }//GEN-LAST:event_jButtonExecuteVoxActionPerformed

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

    private void jButtonExecuteVox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExecuteVox1ActionPerformed
        
        /*
        VoxelPreprocessingRxp preprocessRxp =
        new VoxelPreprocessingRxp(rsp, vopPopMatrix, jRadioButtonLightFile.isSelected());
        preprocessRxp.execute();
         */
        final ArrayList<Rxp> filteredRxpList = rsp.getFilteredRxpList();
        
        final JProgressLoadingFile progressBar = new JProgressLoadingFile(this);
        progressBar.jProgressBar1.setIndeterminate(true);
        progressBar.jProgressBar1.setStringPainted(false);
        progressBar.setVisible(true);
        
        final Rxp rxp = filteredRxpList.get(0);
        final ArrayList<File> filesList = new ArrayList<>();
        
        final VoxelisationParameters parameters = new VoxelisationParameters(
                Double.valueOf(jTextFieldMinPointX2.getText()), 
                Double.valueOf(jTextFieldMinPointY2.getText()),
                Double.valueOf(jTextFieldMinPointZ2.getText()),
                Double.valueOf(jTextFieldMaxPointX2.getText()),
                Double.valueOf(jTextFieldMaxPointY2.getText()),
                Double.valueOf(jTextFieldMaxPointZ2.getText()),
                Integer.valueOf(jTextFieldVoxelNumberX1.getText()),
                Integer.valueOf(jTextFieldVoxelNumberY1.getText()),
                Integer.valueOf(jTextFieldVoxelNumberZ1.getText()));
        
        final String outputPath = jTextFieldFileOutputPathTlsVox.getText();
        
        SwingWorker sw = new SwingWorker() {

            @Override
            protected Void doInBackground() throws Exception {
                
                try{
                    
                    int compteur = 1;
                    for(Rxp rxp:filteredRxpList){
                        VoxelisationTool voxTool = new VoxelisationTool();
                        
                        progressBar.setText("Voxelisation in progress, file "+compteur+"/"+filteredRxpList.size()+" : "+rxp.getRxpLiteFile().getName());
                        File outputFile = new File(outputPath+"/"+rxp.getRxpLiteFile().getName()+".vox");
                        File f = voxTool.generateVoxelFromRxp(rxp, outputFile, vopPopMatrix, parameters);
                        filesList.add(f);
                        
                        compteur++;
                        
                    }
                }catch(Exception e){
                    logger.error("voxelisation failed", e);
                }
                
                
                return null;
            }
            
            @Override
            protected void done(){
                progressBar.dispose();
                
                for(File file :filesList){
                    model.addElement(file.getAbsolutePath());
                }
                
                jListOutputFiles.setModel(model);
                
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
        
    }//GEN-LAST:event_jButtonExecuteVox1ActionPerformed

    private void jTextFieldFilePathRspActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFilePathRspActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFilePathRspActionPerformed

    private void jButtonOpenRspFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenRspFileActionPerformed
        
        if (jFileChooser9.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {

            File file = jFileChooser9.getSelectedFile();
            String fileName = file.getName();
            String filePath = file.getAbsolutePath();

            if(fileName.endsWith(".rsp") || fileName.endsWith(".rxp") || fileName.endsWith(".txt")){
                
                jTextFieldFileNameRsp.setText(fileName);
                jTextFieldFileNameRsp.setToolTipText(fileName);

                jTextFieldFilePathRsp.setText(filePath);
                jTextFieldFilePathRsp.setToolTipText(filePath);
                    
                if(fileName.endsWith(".rxp")){
                    
                }else if(fileName.endsWith(".rsp")){
                    


                    rsp.read(new File(file.getAbsolutePath()));

                    rspScansListModel = new FilterDefaultListModel();

                    ArrayList<Rxp> rxpList = rsp.getRxpList();

                    for(Rxp rxp :rxpList){
                        Map<Integer, Scan> scanList = rxp.getScanList();

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

    private void jRadioButtonLightFileStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButtonLightFileStateChanged
        
        if(jRadioButtonLightFile.isSelected()){
            rspScansListModel.doFilter(".mon");
            
        }
        
        jListRspScans.setModel(rspScansListModel);
        
    }//GEN-LAST:event_jRadioButtonLightFileStateChanged

    private void jRadioButtonComplexFileStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_jRadioButtonComplexFileStateChanged
        
        if(jRadioButtonComplexFile.isSelected()){
            rspScansListModel.doInverseFilter(".mon");
            
        }
        
        jListRspScans.setModel(rspScansListModel);
    }//GEN-LAST:event_jRadioButtonComplexFileStateChanged

    private void jButtonPopMatrixActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPopMatrixActionPerformed
        
        final JFrameMatrix jFrameMatrix = new JFrameMatrix(rsp.getPopMatrix());
        jFrameMatrix.setVisible(true);
        
    }//GEN-LAST:event_jButtonPopMatrixActionPerformed

    private void jComboBox1ItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_jComboBox1ItemStateChanged
        
        if(jComboBox1.getSelectedIndex() == 0){
            jButtonSopMatrix.setEnabled(false);
        }else{
            jButtonSopMatrix.setEnabled(true);
        }
    }//GEN-LAST:event_jComboBox1ItemStateChanged

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

    private void jTextFieldVoxelNumberXKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberXKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberXKeyTyped

    private void jTextFieldVoxelNumberYKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberYKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberYKeyTyped

    private void jTextFieldVoxelNumberZKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberZKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberZKeyTyped

    private void jButtonPopMatrix1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPopMatrix1ActionPerformed
        
        final JFrameMatrix jFrameMatrix = new JFrameMatrix(getVopMatrix());
        jFrameMatrix.setVisible(true);
        
        jFrameMatrix.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosed(WindowEvent e) {
                //vopMatrix = jFrameMatrix.getMatrix();
            }
        });
    }//GEN-LAST:event_jButtonPopMatrix1ActionPerformed

    private void jTextFieldMinPointXKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointXKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointXKeyTyped

    private void jTextFieldMinPointYKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointYKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointYKeyTyped

    private void jTextFieldMinPointZKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointZKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointZKeyTyped

    private void jTextFieldMaxPointXKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointXKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointXKeyTyped

    private void jTextFieldMaxPointYKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointYKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointYKeyTyped

    private void jTextFieldMaxPointZKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointZKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointZKeyTyped

    private void jTextFieldMinPointX2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointX2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointX2KeyTyped

    private void jTextFieldMinPointY2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointY2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointY2KeyTyped

    private void jTextFieldMinPointZ2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMinPointZ2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMinPointZ2KeyTyped

    private void jTextFieldMaxPointX2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointX2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointX2KeyTyped

    private void jTextFieldMaxPointY2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointY2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointY2KeyTyped

    private void jTextFieldMaxPointZ2KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldMaxPointZ2KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldMaxPointZ2KeyTyped

    private void jTextFieldVoxelNumberX1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberX1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberX1KeyTyped

    private void jTextFieldVoxelNumberY1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberY1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberY1KeyTyped

    private void jTextFieldVoxelNumberZ1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberZ1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberZ1KeyTyped

    private void jTextFieldVoxelNumberRes1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextFieldVoxelNumberRes1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldVoxelNumberRes1KeyTyped

    private void jTextFieldFileOutputPathTlsVoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldFileOutputPathTlsVoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextFieldFileOutputPathTlsVoxActionPerformed

    private void jButtonChooseOutputDirectoryTlsVoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonChooseOutputDirectoryTlsVoxActionPerformed
        
        if (jFileChooser10.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            
            jTextFieldFileOutputPathTlsVox.setText(jFileChooser10.getSelectedFile().getAbsolutePath());
        }
    }//GEN-LAST:event_jButtonChooseOutputDirectoryTlsVoxActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2DProjection;
    private javax.swing.ButtonGroup buttonGroup3DView;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButtonAddFile;
    private javax.swing.JButton jButtonChooseDirectory;
    private javax.swing.JButton jButtonChooseOutputDirectoryTlsVox;
    private javax.swing.JButton jButtonChooseOutputDirectoryVox;
    private javax.swing.JButton jButtonChooseOutputDirectoryVox1;
    private javax.swing.JButton jButtonCreateAttribut;
    private javax.swing.JButton jButtonExecuteVox;
    private javax.swing.JButton jButtonExecuteVox1;
    private javax.swing.JButton jButtonGenerateMap;
    private javax.swing.JButton jButtonLoad;
    private javax.swing.JButton jButtonLoadSelectedFile;
    private javax.swing.JButton jButtonOpen3DDisplay;
    private javax.swing.JButton jButtonOpenCoordinatesFile1;
    private javax.swing.JButton jButtonOpenInputFile;
    private javax.swing.JButton jButtonOpenInputFile1;
    private javax.swing.JButton jButtonOpenInputFileVoxelisation;
    private javax.swing.JButton jButtonOpenInputFileVoxelisation1;
    private javax.swing.JButton jButtonOpenRspFile;
    private javax.swing.JButton jButtonOpenTrajectoryFile;
    private javax.swing.JButton jButtonPopMatrix;
    private javax.swing.JButton jButtonPopMatrix1;
    private javax.swing.JButton jButtonRemoveFile;
    private javax.swing.JButton jButtonSopMatrix;
    private javax.swing.JButton jButtonVopMatrix;
    private javax.swing.JCheckBox jCheckBoxDrawAxis;
    private javax.swing.JCheckBox jCheckBoxDrawNullVoxel;
    private javax.swing.JCheckBox jCheckBoxDrawTerrain;
    private javax.swing.JCheckBox jCheckBoxDrawUndergroundVoxel;
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
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JFileChooser jFileChooser10;
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
    private javax.swing.JLabel jLabelName7;
    private javax.swing.JLabel jLabelName8;
    private javax.swing.JLabel jLabelName9;
    private javax.swing.JLabel jLabelPath;
    private javax.swing.JLabel jLabelPath1;
    private javax.swing.JLabel jLabelPath10;
    private javax.swing.JLabel jLabelPath2;
    private javax.swing.JLabel jLabelPath3;
    private javax.swing.JLabel jLabelPath4;
    private javax.swing.JLabel jLabelPath5;
    private javax.swing.JLabel jLabelPath6;
    private javax.swing.JLabel jLabelPath7;
    private javax.swing.JLabel jLabelPath8;
    private javax.swing.JLabel jLabelPath9;
    private javax.swing.JLabel jLabelSize;
    private javax.swing.JLabel jLabelSize1;
    private javax.swing.JLabel jLabelSize2;
    private javax.swing.JLabel jLabelVaryingFileSizeInputLocal;
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
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
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
    private javax.swing.JPanel jPanel34;
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
    private javax.swing.JPanel jPanelDisplayParametersTab;
    private javax.swing.JPanel jPanelOutputParametersTab;
    private javax.swing.JPanel jPanelVisualizeTab;
    private javax.swing.JRadioButton jRadioButtonComplexFile;
    private javax.swing.JRadioButton jRadioButtonLightFile;
    private javax.swing.JRadioButton jRadioButtonPAI;
    private javax.swing.JRadioButton jRadioButtonTransmittanceMap;
    private javax.swing.JRadioButton jRadioButtonVegetationLayer;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTabbedPane jTabbedPane2;
    private javax.swing.JTabbedPane jTabbedPane3;
    private javax.swing.JTabbedPane jTabbedPane4;
    private javax.swing.JTabbedPane jTabbedPane5;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JTextField jTextField5;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextField8;
    private javax.swing.JTextField jTextFieldArguments;
    private javax.swing.JTextField jTextFieldAttributExpression;
    private javax.swing.JTextField jTextFieldFileNameCoordLocal;
    private javax.swing.JTextField jTextFieldFileNameInputLocal;
    private javax.swing.JTextField jTextFieldFileNameInputTxt;
    private javax.swing.JTextField jTextFieldFileNameInputVox;
    private javax.swing.JTextField jTextFieldFileNameMnt;
    private javax.swing.JTextField jTextFieldFileNameOutputTxt;
    private javax.swing.JTextField jTextFieldFileNameRsp;
    private javax.swing.JTextField jTextFieldFileNameSaveLocal;
    private javax.swing.JTextField jTextFieldFileNameSaveVox;
    private javax.swing.JTextField jTextFieldFileNameTrajVox;
    private javax.swing.JTextField jTextFieldFileOutputPathTlsVox;
    private javax.swing.JTextField jTextFieldFilePathCoordLocal;
    private javax.swing.JTextField jTextFieldFilePathInputLocal;
    private javax.swing.JTextField jTextFieldFilePathInputTxt;
    private javax.swing.JTextField jTextFieldFilePathInputVox;
    private javax.swing.JTextField jTextFieldFilePathMnt;
    private javax.swing.JTextField jTextFieldFilePathOutputTxt;
    private javax.swing.JTextField jTextFieldFilePathRsp;
    private javax.swing.JTextField jTextFieldFilePathSaveLocal;
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
