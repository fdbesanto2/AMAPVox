/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util;

import java.text.DecimalFormat;
import java.text.FieldPosition;

/**
 *
 * @author calcul
 */
    
public class DecimalScientificFormat extends DecimalFormat {
    private static final DecimalFormat df = new DecimalFormat("#0.00##");
    private static final DecimalFormat sf = new DecimalFormat("0.###E0");

    @Override
    public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
        String decimalFormat = df.format(number);
        return (0.0001 != number && df.format(0.0001).equals(decimalFormat)) ? sf.format(number, result, fieldPosition) : result.append(decimalFormat);
    }
}
