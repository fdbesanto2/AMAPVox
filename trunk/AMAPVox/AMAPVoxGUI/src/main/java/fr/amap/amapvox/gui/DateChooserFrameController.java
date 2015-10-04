package fr.amap.amapvox.gui;

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



import fr.amap.amapvox.simulation.transmittance.SimulationPeriod;
import fr.amap.amapvox.simulation.transmittance.util.Period;
import java.net.URL;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author calcul
 */


public class DateChooserFrameController implements Initializable {
    
    private boolean confirmed;
    private Stage stage;
    private int currentStartHour;
    private int currentStartMinute;
    private int currentEndHour;
    private int currentEndMinute;
    
    private NumberFormat numberFormat;
    
    @FXML
    private DatePicker datepickerStartDate;
    @FXML
    private DatePicker datepickerEndDate;
    @FXML
    private TextField textfieldClearnessCoefficient;
    @FXML
    private TextField textfieldEndHour;
    @FXML
    private TextField textfieldEndMinute;
    @FXML
    private TextField textfieldStartHour;
    @FXML
    private TextField textfieldStartMinute;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        confirmed = false;
        
        currentStartHour = Integer.valueOf(textfieldStartHour.getText());
        currentStartMinute = Integer.valueOf(textfieldStartMinute.getText());
        currentEndHour = Integer.valueOf(textfieldEndHour.getText());
        currentEndMinute = Integer.valueOf(textfieldEndMinute.getText());
        
        numberFormat = NumberFormat.getInstance();
        numberFormat.setMinimumIntegerDigits(2);
        
    }    

    @FXML
    private void onActionButtonAcceptDate(ActionEvent event) {
        confirmed = true;
        stage.close();
    }

    
    public void reset(){
        confirmed = false;
    }

    public boolean isConfirmed() {
        return confirmed;
    }
    
    public SimulationPeriod getDateRange(){
        
        try{
            LocalDate startDate = datepickerStartDate.getValue();
            LocalDate endDate = datepickerEndDate.getValue();

            Period period = new Period();
            Date date1 = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
            Date date2 = Date.from(endDate.atStartOfDay(ZoneId.systemDefault()).toInstant());

            Calendar startTime = Calendar.getInstance(TimeZone.getDefault());

            //start time
            startTime.setTime(date1);
            startTime.set(Calendar.HOUR, currentStartHour);
            startTime.set(Calendar.MINUTE, currentStartMinute);
            period.startDate = startTime;

            //end time
            Calendar endTime = Calendar.getInstance(TimeZone.getDefault());
            endTime.setTime(date2);
            endTime.set(Calendar.HOUR, currentEndHour);
            endTime.set(Calendar.MINUTE, currentEndMinute);
            period.endDate = endTime;

            return new SimulationPeriod(period, Float.valueOf(textfieldClearnessCoefficient.getText()));
            
        }catch(Exception e){
            return null;
        }
        
    }
    
    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    private void onActionButtonIncreaseEndHour(ActionEvent event) {
        
        if(currentEndHour < 23){
            currentEndHour++;
        }else{
            currentEndHour = 0;
        }
        
        textfieldEndHour.setText(numberFormat.format(currentEndHour));
    }

    @FXML
    private void onActionButtonDecreaseEndHour(ActionEvent event) {
        
        if(currentEndHour > 0){
            currentEndHour--;
        }else{
            currentEndHour = 23;
        }
        
        textfieldEndHour.setText(numberFormat.format(currentEndHour));
    }

    @FXML
    private void onActionButtonIncreaseEndMinute(ActionEvent event) {
        
        if(currentEndMinute < 59){
            currentEndMinute++;
        }else{
            currentEndMinute = 0;
        }
        
        textfieldEndMinute.setText(numberFormat.format(currentEndMinute));
    }

    @FXML
    private void onActionButtonDecreaseEndMinute(ActionEvent event) {
        
        if(currentEndMinute > 0){
            currentEndMinute--;
        }else{
            currentEndMinute = 59;
        }
        
        textfieldEndMinute.setText(numberFormat.format(currentEndMinute));
    }

    @FXML
    private void onActionButtonIncreaseStartHour(ActionEvent event) {
        
        if(currentStartHour < 23){
            currentStartHour++;
        }else{
            currentStartHour = 0;
        }
        
        textfieldStartHour.setText(numberFormat.format(currentStartHour));
    }

    @FXML
    private void onActionButtonDecreaseStartHour(ActionEvent event) {
        
        if(currentStartHour > 0){
            currentStartHour--;
        }else{
            currentStartHour = 23;
        }
        
        textfieldStartHour.setText(numberFormat.format(currentStartHour));
    }

    @FXML
    private void onActionButtonIncreaseStartMinute(ActionEvent event) {
        
        if(currentStartMinute < 59){
            currentStartMinute++;
        }else{
            currentStartMinute = 0;
        }
        
        textfieldStartMinute.setText(numberFormat.format(currentStartMinute));
    }

    @FXML
    private void onActionButtonDecreaseStartMinute(ActionEvent event) {
        
        if(currentStartMinute > 0){
            currentStartMinute--;
        }else{
            currentStartMinute = 59;
        }
        
        textfieldStartMinute.setText(numberFormat.format(currentStartMinute));
    }

    
    
}
