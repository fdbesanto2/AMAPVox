package fr.amap.commons.javafx.io;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import fr.amap.commons.util.io.file.CSVFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;

/**
 * FXML Controller class
 *
 * @author Julien
 */
public class TextFileParserFrameController implements Initializable{
    @FXML
    private TextField textfieldSeparator;
    @FXML
    private Spinner<Integer> spinnerSkipLines;
    @FXML
    private CheckBox checkboxKeepAllLines;
    @FXML
    private Spinner<Integer> spinnerNumberOfLines;
    @FXML
    private TableView<ObservableList<String>> tableViewFileContent;
    @FXML
    private CheckBox checkboxExtractScalarFieldNames;
    @FXML
    private ToggleGroup toggleGroupExtractHeaderOptions;
    @FXML
    private RadioButton radioButtonExtractHeaderFromFirstLine;
    @FXML
    private RadioButton radioButtonExtractHeaderFromStartingLine;
    @FXML
    private HBox hboxExtractHeaderOptions;
    @FXML
    private VBox vboxTableViewAndColumnsTypeWrapper;

    private final static int MAX_LINE_NUMBER = 1000;
    private String[] lines;
    private int currentLineNumber;
    private String separator = " ";
    private boolean isInit;
    private Stage stage;
    private File currentViewedFile;
    
    //columns names assignment
    private boolean columnNameAssignmentEnabled;
    private ObservableList<String> columnNamesAssignmentValues;
    private List<Integer> columnNamesAssignmentSelectedIndices;
    private GridPane columnsGridPane;
    private List<ComboBox> gridPaneRowNames;
    
    //columns types assignment
    private boolean columnTypeAssignmentEnabled;
    private ObservableList<String> columnTypesAssignmentValues;
    private List<Integer> columnTypesAssignmentSelectedIndices;
    private List<ComboBox> gridPaneRowTypes;
    
    
    public static TextFileParserFrameController getInstance() throws IOException, Exception{
        
        try {
            FXMLLoader loader = new FXMLLoader(TextFileParserFrameController.class.getResource("/fxml/TextFileParserFrame.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            TextFileParserFrameController textFileParserFrameController = loader.getController();
            stage.setScene(new Scene(root));
            textFileParserFrameController.setStage(stage);
            return textFileParserFrameController;
            
        } catch (IOException ex) {
            throw ex;
        }catch (Exception ex) {
            throw ex;
        }
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
        
        this.stage.setOnShowing(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent event) {
                
            }
        });
    }

    public Stage getStage() {
        return stage;
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
               
        
        spinnerSkipLines.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, MAX_LINE_NUMBER, 0));
        spinnerNumberOfLines.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, Integer.MAX_VALUE, 0));
                
        spinnerSkipLines.valueProperty().addListener(new ChangeListener<Integer>() {

            @Override
            public void changed(ObservableValue<? extends Integer> observable, Integer oldValue, Integer newValue) {
                
                if(checkboxExtractScalarFieldNames.isSelected() && radioButtonExtractHeaderFromStartingLine.isSelected()){
                    updateColumns();
                }
                
                updateTable();
            }
        });
        
        textfieldSeparator.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
                separator = newValue;
                
                if(!newValue.equals(oldValue)){
                    updateColumns();
                    updateTable();
                }
            }
        });
        
        checkboxExtractScalarFieldNames.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                updateColumns();
                updateTable();
            }
        });
        
        radioButtonExtractHeaderFromFirstLine.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                updateColumns();
                updateTable();
            }
        });
        
        hboxExtractHeaderOptions.disableProperty().bind(checkboxExtractScalarFieldNames.selectedProperty().not());
        
        checkboxKeepAllLines.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                spinnerNumberOfLines.setDisable(newValue);
            }
        });
        
        
    }    

    @FXML
    private void onActionButtonSpaceSeparator(ActionEvent event) {
        textfieldSeparator.setText(" ");
    }

    @FXML
    private void onActionButtonTabSeparator(ActionEvent event) {
        
        textfieldSeparator.setText("\\t");
    }

    @FXML
    private void onActionButtonCommaSeparator(ActionEvent event) {
        textfieldSeparator.setText(",");
    }

    @FXML
    private void onActionButtonSemiColonSeparator(ActionEvent event) {
        textfieldSeparator.setText(";");
    }

    @FXML
    private void onActionButtonApply(ActionEvent event) {
        stage.close();
    }
    
    public void setTextFile(File file) throws FileNotFoundException, IOException{
        
        currentViewedFile = file;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {

            lines = new String[MAX_LINE_NUMBER];

            int count = 0;
            String currentLine;

            while ((currentLine = reader.readLine()) != null && count < MAX_LINE_NUMBER) {

                lines[count] = currentLine;
                count++;
            }
            
            currentLineNumber = count;
            
            isInit = true;
            
            updateColumns();
            updateTable();

        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }
    
    private void updateTable(){
        
        int lineSkipNumber = spinnerSkipLines.getValue();
        
        int startIndex = lineSkipNumber;
        
        if(checkboxExtractScalarFieldNames.isSelected()){
            startIndex += 1;
            
            if(radioButtonExtractHeaderFromStartingLine.isSelected()){
                updateColumns();
            }
        }
        
        int max = Math.min(currentLineNumber-startIndex+lineSkipNumber, MAX_LINE_NUMBER);
        
        for(int lineID = startIndex ; lineID < max ; lineID++){
                
            String line = lines[lineID];
            String[] split = line.split(separator);
            
            int tableIndex = lineID-startIndex;

            ObservableList<String> values = FXCollections.observableArrayList();
            values.addAll(Arrays.asList(split));
            
            if(tableIndex < tableViewFileContent.getItems().size()){
                tableViewFileContent.getItems().set(tableIndex, values);
            }else{
                tableViewFileContent.getItems().add(values); //ajout d'une ligne dans la table
            }
        }
        
        
        
        //suppression des lignes en trop
        if(max-startIndex >= 0){
            tableViewFileContent.getItems().remove(max-startIndex, tableViewFileContent.getItems().size());
        }
        
        isInit = false;
    }
    
    private void updateColumns(){
        
        int headerIndex;
        
        //on extrait le nom des colonnes
        if(checkboxExtractScalarFieldNames.isSelected()){            
            
            //on extrait le nom des colonnes depuis la première ligne du fichier
            if(radioButtonExtractHeaderFromFirstLine.isSelected()){
                headerIndex = 0;
                
            }else{ //on extrait le nom des colonnes depus la première ligne lue
                headerIndex = spinnerSkipLines.getValue();
            }
            
        }else{ // on n'extrait pas le nom des colonnes
            headerIndex = 0;
        }
        
        if(headerIndex < currentLineNumber){
            //on extrait le nom des colonnes
            String headerLine = lines[headerIndex];
            String[] splittedHeader = headerLine.split(separator);

            if(tableViewFileContent.getColumns().size() == splittedHeader.length){ //om met à jour les colonnes

                for(int j = 0;j<splittedHeader.length;j++){

                    String columnName;

                    if(checkboxExtractScalarFieldNames.isSelected()){
                        columnName = splittedHeader[j];
                    }else{
                        columnName = String.valueOf(j+1);
                    }

                    tableViewFileContent.getColumns().get(j).setText(columnName);

                }

            }else{ //on crée des nouvelles colonnes
                
                //columns assignment
                
                //columnsHbox = new HBox();
                columnsGridPane = new GridPane();
                gridPaneRowNames = new ArrayList<>();
                gridPaneRowTypes = new ArrayList<>();
                
                tableViewFileContent.getColumns().clear();

                for(int j = 0;j<splittedHeader.length;j++){

                    final int columnIndex = j;

                    String columnName;

                    if(checkboxExtractScalarFieldNames.isSelected()){
                        columnName = splittedHeader[j];
                    }else{
                        columnName = String.valueOf(j+1);
                    }

                    TableColumn column = new TableColumn(columnName);
                    column.setMinWidth(50);
                    column.setSortable(false);

                    column.setCellValueFactory(new Callback<CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){                   
                        @Override
                        public ObservableValue<String> call(CellDataFeatures<ObservableList, String> param) { 

                            return new SimpleStringProperty(param.getValue().get(columnIndex).toString());                       
                         }                   
                     });

                    tableViewFileContent.getColumns().add(column);
                    
                    if(columnNameAssignmentEnabled){
                        ComboBox comboBox = new ComboBox(columnNamesAssignmentValues);
                        comboBox.setMaxWidth(Double.MAX_VALUE);
                        
                        stage.widthProperty().addListener(new ChangeListener<Number>() {

                            @Override
                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                comboBox.setPrefWidth(newValue.doubleValue());
                            }
                        });
                    
                        column.widthProperty().addListener(new ChangeListener<Number>() {

                            @Override
                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                comboBox.setPrefWidth(newValue.doubleValue());
                                
                            }
                        });
                        
                        if(columnNamesAssignmentSelectedIndices != null && j < columnNamesAssignmentSelectedIndices.size()){
                            comboBox.getSelectionModel().select(columnNamesAssignmentSelectedIndices.get(j).intValue());
                        }                        

                        columnsGridPane.add(comboBox, j, 0);
                        gridPaneRowNames.add(comboBox);
                        //columnsHbox.getChildren().add(comboBox);
                        //HBox.setHgrow(comboBox, Priority.ALWAYS);
                    }
                    
                    if(columnTypeAssignmentEnabled){
                        
                        ComboBox comboBox = new ComboBox(columnTypesAssignmentValues);
                        comboBox.setMaxWidth(Double.MAX_VALUE);
                        
                        stage.widthProperty().addListener(new ChangeListener<Number>() {

                            @Override
                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                comboBox.setPrefWidth(newValue.doubleValue());
                            }
                        });
                    
                        column.widthProperty().addListener(new ChangeListener<Number>() {

                            @Override
                            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                                comboBox.setPrefWidth(newValue.doubleValue());
                                
                            }
                        });
                        
                        if(columnTypesAssignmentSelectedIndices != null && j < columnTypesAssignmentSelectedIndices.size()){
                            comboBox.getSelectionModel().select(columnTypesAssignmentSelectedIndices.get(j).intValue());
                        }                        

                        columnsGridPane.add(comboBox, j, 1);
                        gridPaneRowTypes.add(comboBox);
                    }
                }

                if(columnNameAssignmentEnabled){
                    if(isInit && vboxTableViewAndColumnsTypeWrapper.getChildren().size() <= 1){
                        vboxTableViewAndColumnsTypeWrapper.getChildren().add(0, columnsGridPane);
                        
                    }else{
                        vboxTableViewAndColumnsTypeWrapper.getChildren().set(0, columnsGridPane);
                    }
                    
                    tableViewFileContent.widthProperty().addListener(new ChangeListener<Number>() {

                        @Override
                        public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                            columnsGridPane.setPrefWidth(newValue.doubleValue());
                        }
                    });
                }
            }
        }
    }
    
    public void setColumnAssignment(boolean value){
        
        columnNameAssignmentEnabled = value;
    }
    
    public void setColumnAssignmentValues(String... items){
        
        columnNameAssignmentEnabled = true;
        columnNamesAssignmentValues = FXCollections.observableArrayList(items);
        columnNamesAssignmentSelectedIndices = new ArrayList<>(items.length);
        
        for(int i=0;i<items.length;i++){
            columnNamesAssignmentSelectedIndices.add(0);
        }
    }
    
    public void setColumnAssignmentDefaultSelectedIndex(int columnIndex, int selectedIndex){
        
        if(columnNamesAssignmentSelectedIndices != null){
            columnNamesAssignmentSelectedIndices.set(columnIndex, selectedIndex);
        }
    }

    public List<String> getColumnAssignmentValues() {
        return columnNamesAssignmentValues;
    }

    public boolean isColumnAssignmentEnabled() {
        return columnNameAssignmentEnabled;
    }

    public boolean isColumnTypeAssignmentEnabled() {
        return columnTypeAssignmentEnabled;
    }

    public void setColumnTypeAssignmentEnabled(boolean columnTypeAssignmentEnabled) {
        this.columnTypeAssignmentEnabled = columnTypeAssignmentEnabled;
    }

    public ObservableList<String> getColumnTypesAssignmentValues() {
        return columnTypesAssignmentValues;
    }
    
    public void setColumnTypesAssignmentValues(Class... items){
        
        columnTypeAssignmentEnabled = true;
        
        columnTypesAssignmentValues = FXCollections.observableArrayList();
        for(int i=0;i<items.length;i++){
            
            columnTypesAssignmentValues.add(items[i].getTypeName());
        }
        
        columnTypesAssignmentSelectedIndices = new ArrayList<>(items.length);
        
        for(int i=0;i<items.length;i++){
            columnTypesAssignmentSelectedIndices.add(0);
        }
    }

    public List<Integer> getColumnTypesAssignmentSelectedIndices() {
        return columnTypesAssignmentSelectedIndices;
    }
    
    public void setColumnTypesAssignmentDefaultSelectedIndex(int columnIndex, int selectedIndex){
        
        if(columnTypesAssignmentSelectedIndices != null){
            columnTypesAssignmentSelectedIndices.set(columnIndex, selectedIndex);
        }
    }
    
    public List<Integer> getAssignedColumnsIndices(){
        
        ObservableList<Node> childs = columnsGridPane.getChildren();
        List<Integer> list = new ArrayList<>();
        
        for(Node child : childs){
            list.add(((ComboBox)child).getSelectionModel().getSelectedIndex());
        }
        
        return list;
    }
    
    public Map<String, Integer> getAssignedColumnsItemsMap(){
        
        Map<String, Integer> columnAssignation = new HashMap<>();
        
        int index = 0;
        for(ComboBox combobox : gridPaneRowNames){
            columnAssignation.put((String)(combobox.getSelectionModel().getSelectedItem()), index);
            index++;
        }
        
        return columnAssignation;
    }
    
    public Map<String, Class> getAssignedColumnsTypesMap() throws ClassNotFoundException{
        
        Map<String, Class> columnAssignation = new HashMap<>();
        
        int index = 0;
        for(ComboBox combobox : gridPaneRowTypes){
            String selectedItem = (String) combobox.getSelectionModel().getSelectedItem();
            Class<?> c = Class.forName(selectedItem);
            
            String columnName = (String) gridPaneRowNames.get(index).getSelectionModel().getSelectedItem();
            
            columnAssignation.put(columnName, c);
            index++;
        }
        
        return columnAssignation;
    }
    
    /**
     * @deprecated 
     * @return 
     */
    public List<String> getAssignedColumnsItems(){
        
        ObservableList<Node> childs = columnsGridPane.getChildren();
        List<String> list = new ArrayList<>();
        
        
        for(Node child : childs){
            list.add((String)(((ComboBox)child).getSelectionModel().getSelectedItem()));
        }
        
        return list;
    }

    public String getSeparator() {
        return textfieldSeparator.getText();
    }
    
    public int getSkipLinesNumber(){
        return spinnerSkipLines.getValue();
    }
    
    public int getNumberOfLines(){
        
        if(checkboxKeepAllLines.isSelected()){
            return Integer.MAX_VALUE;
        }else{
            return Integer.valueOf(spinnerNumberOfLines.getEditor().getText());
        }
    }
    
    /**
     * Get the {@link CSVFile} object linked to this text file parser.
     * @return The {@link CSVFile} object, null if the current text file is null.
     */
    public CSVFile getCSVFile(){
        
        if(currentViewedFile != null){
            
            CSVFile csvFile = new CSVFile(currentViewedFile);
            csvFile.setColumnSeparator(getSeparator());
            
            csvFile.setColumnAssignment(getAssignedColumnsItemsMap());
            try {
                csvFile.setColumnTypes(getAssignedColumnsTypesMap());
            } catch (ClassNotFoundException ex) {}
            
            csvFile.setNbOfLinesToRead(getNumberOfLines());
            csvFile.setNbOfLinesToSkip(getSkipLinesNumber());
            
            int headerIndex = getHeaderIndex();
            csvFile.setContainsHeader(headerIndex != -1);
            csvFile.setHeaderIndex(headerIndex);
            
            return csvFile;
        }else{
            return null;
        }
    }
    
    public void setHeaderExtractionEnabled(boolean value){
        checkboxExtractScalarFieldNames.setSelected(value);
    }
    
    public void setSeparator(String separator){
        textfieldSeparator.setText(separator);
    }
    
    public int getHeaderIndex(){
        
        if(checkboxExtractScalarFieldNames.isSelected()){            
            
            if(radioButtonExtractHeaderFromFirstLine.isSelected()){
                return 0;
                
            }else{
                return spinnerSkipLines.getValue();
            }
            
        }else{
            return -1;
        }
    }
}
