package fr.ird.voxelidar.gui;

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



import fr.ird.voxelidar.transmittance.SimulationPeriod;
import fr.ird.voxelidar.util.Period;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author calcul
 */


public class DateChooserFrameController implements Initializable {
    @FXML
    private DatePicker datepickerStartDate;
    @FXML
    private TextField textfieldStartTime;
    @FXML
    private Button buttonIncreaseStartTime;
    @FXML
    private Button buttonDecreaseStartTime;
    @FXML
    private DatePicker datepickerEndDate;
    @FXML
    private TextField textfieldEndTime;
    @FXML
    private Button buttonIncreaseEndTime;
    @FXML
    private Button buttonAcceptDate;
    
    private boolean confirmed;
    private Stage stage;
    @FXML
    private TextField textfieldClearnessCoefficient;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        confirmed = false;
    }    

    @FXML
    private void onActionButtonAcceptDate(ActionEvent event) {
        confirmed = true;
        stage.close();
    }

    @FXML
    private void onActionButtonIncreaseStartTime(ActionEvent event) {
    }

    @FXML
    private void onActionButtonDecreaseStartTime(ActionEvent event) {
    }

    @FXML
    private void onActionButtonIncreaseEndTime(ActionEvent event) {
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
            String[] time = textfieldStartTime.getText().replaceAll(" ", "").split(":");
            startTime.set(Calendar.HOUR, Integer.valueOf(time[0]));
            startTime.set(Calendar.MINUTE, Integer.valueOf(time[1]));
            period.startDate = startTime;

            //end time
            Calendar endTime = Calendar.getInstance(TimeZone.getDefault());
            endTime.setTime(date2);
            time = textfieldEndTime.getText().replaceAll(" ", "").split(":");
            endTime.set(Calendar.HOUR, Integer.valueOf(time[0]));
            endTime.set(Calendar.MINUTE, Integer.valueOf(time[1]));
            period.endDate = endTime;

            return new SimulationPeriod(period, Float.valueOf(textfieldClearnessCoefficient.getText()));
            
        }catch(Exception e){
            return null;
        }
        
    }
    
    public void setStage(Stage stage){
        this.stage = stage;
    }
    
}
