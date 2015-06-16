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

package fr.ird.voxelidar.transmittance;

import fr.ird.voxelidar.util.Period;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author calcul
 */


public class SimulationPeriod {
    
    private final SimpleStringProperty period;
    private final SimpleStringProperty clearnessCoefficient;

    public SimulationPeriod(Period period, float clearnessCoefficient) {
        
        this.period = new SimpleStringProperty(period.toString());
        this.clearnessCoefficient = new SimpleStringProperty(String.valueOf(clearnessCoefficient));
    }

    public SimpleStringProperty getPeriod() {
        return period;
    }

    public SimpleStringProperty getClearnessCoefficient() {
        return clearnessCoefficient;
    }
    
}
