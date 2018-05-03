/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import java.io.File;
import java.nio.file.Files;
import javafx.collections.ObservableList;
import javafx.scene.control.Control;
import javax.vecmath.Point3d;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

/**
 *
 * @author calcul
 */
public class Validators {

    /**
     * Determines if the field is a decimal
     */
    public static Validator fieldDoubleValidator = new Validator<String>() {
        @Override
        public ValidationResult apply(Control t, String s) {

            if (s.isEmpty()) {
                return ValidationResult.fromErrorIf(t, "A value is required", s.isEmpty());
            } else {
                boolean valid = false;
                try {
                    Double.valueOf(s);
                    valid = true;
                } catch (NumberFormatException ex) {
                }

                return ValidationResult.fromErrorIf(t, "The value has to be a decimal", !valid);
            }
        }
    };
    
    /**
     * Determines if the field is an integer
     */
    public static Validator fieldIntegerValidator = new Validator<String>() {
        @Override
        public ValidationResult apply(Control t, String s) {

            if (s.isEmpty()) {
                return ValidationResult.fromErrorIf(t, "A value is required", s.isEmpty());
            } else {
                boolean valid = false;
                try {
                    Integer.valueOf(s);
                    valid = true;
                } catch (NumberFormatException ex) {
                }

                return ValidationResult.fromErrorIf(t, "The value has to be an integer", !valid);
            }
        }
    };

    /**
     * determines if the file exists
     */
    public static Validator fileExistValidator = new Validator<String>() {
        @Override
        public ValidationResult apply(Control t, String s) {

            if (s.isEmpty()) {
                return ValidationResult.fromErrorIf(t, "A value is required", s.isEmpty());
            } else {
                return ValidationResult.fromErrorIf(t, "Invalid value", !Files.exists(new File(s).toPath()));
            }
        }
    };

    /**
     * determines if the specified file can be written
     */
    public static Validator fileValidityValidator = new Validator<String>() {
        @Override
        public ValidationResult apply(Control t, String s) {
            if (s.isEmpty()) {
                return ValidationResult.fromErrorIf(t, "A value is required", s.isEmpty());
            } else {

                File file = new File(s);
                if (!file.isDirectory()) {
                    file = file.getParentFile();
                    return ValidationResult.fromErrorIf(t, "Invalid value", !file.exists());
                } else {
                    return ValidationResult.fromError(t, "The given file is a directory.");
                }

            }
        }
    };

    /**
     * determines if the directory exists
     */
    public static Validator directoryValidator = new Validator<String>() {
        @Override
        public ValidationResult apply(Control t, String s) {
            if (s.isEmpty()) {
                return ValidationResult.fromErrorIf(t, "A value is required", s.isEmpty());
            } else {

                File file = new File(s);
                if (!file.isDirectory()) {
                    return ValidationResult.fromError(t, "The given file is not a directory.");
                } else {
                    return ValidationResult.fromErrorIf(t, "Invalid value", !Files.exists(new File(s).toPath()));
                }

            }
        }
    };

    /**
     * determines if the list is empty
     */
    public static Validator emptyListValidator = new Validator<ObservableList<Point3d>>() {
        @Override
        public ValidationResult apply(Control t, ObservableList<Point3d> u) {
            return ValidationResult.fromErrorIf(t, "The list is empty", u.isEmpty());
        }
    };

    /**
     * determines if a table view is empty
     */
    public static Validator emptyTableValidator = new Validator<ObservableList>() {
        @Override
        public ValidationResult apply(Control t, ObservableList u) {
            return ValidationResult.fromErrorIf(t, "The list is empty", u.isEmpty());
        }
    };

    /**
     * unregister validator (patch because unregister doesn't exist yet)
     */
    public static Validator unregisterValidator = new Validator<Object>() {
        @Override
        public ValidationResult apply(Control t, Object u) {
            return ValidationResult.fromErrorIf(t, "", false);
        }
    };
}
