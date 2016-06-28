package fr.amap.commons.util;

import org.joda.time.DateTime;

/**
 * A time class, handle year, day of the year, hour, minutes.
 *
 * @author J. Dauzat - May 2012, J. Heurtebize (refactoring)
 */
public class Time {

    private int year;
    private int doy;
    private int hours;
    private int minutes;
    private int seconds;
    private double decimalHour;

    public Time() {
    }
    
    /**
     * Constructs a time object from the year, the day of month, the month and the decimal hour.
     * @param year The year.
     * @param day
     * @param month
     * @param decimalHours The decimal hour
     */
    public Time(int year, int month, int day, double decimalHours) {
        
        this.decimalHour = decimalHours;
        
        setDecimalHour(decimalHours);
        
        this.year = year;
        
        this.doy = new DateTime(year, month, day, hours, minutes).getDayOfYear();
    }
    
    public Time(int year, int month, int day, int hour, int minutes) {
        
        this.year = year;
        
        this.doy = new DateTime(year, month, day, hour, minutes).getDayOfYear();
        
        this.hours = hour;
        this.minutes = minutes;

        updateDecimalHour();
    }
    
    public Time(int year, int month, int day, int hour, int minutes, int seconds) {
        
        this.year = year;
        
        this.doy = new DateTime(year, month, day, hour, minutes).getDayOfYear();
        
        this.hours = hour;
        this.minutes = minutes;
        this.seconds = seconds;

        updateDecimalHour();
    }

    /**
     * Constructs a time object from the year, the day of the year and the decimal hour.
     * @param year The year.
     * @param doy The day of the year.
     * @param decimalHour The decimal hour
     */
    public Time(int year, int doy, double decimalHour) {
        
        this.year = year;
        this.doy = doy;
        
        setDecimalHour(decimalHour);
    }

    /**
     * Constructs a time object from the year, the day of the year, the hour and the minutes.
     *
     * @param year The year.
     * @param doy The day of the year.
     * @param hour The hour
     * @param minutes The minutes
     */
    public Time(int year, int doy, int hour, int minutes) {

        this.year = year;
        this.doy = doy;
        this.hours = hour;
        this.minutes = minutes;

        updateDecimalHour();
    }
    
    private void updateDecimalHour(){
        
        decimalHour = (float) getDecimalHour(hours, minutes, seconds);
    }
    
    /**
     * Get the decimal hour from hour, minutes and seconds.
     * @param hour The hour
     * @param minutes The minutes
     * @param seconds The seconds
     * @return The decimal hour.
     */
    public static double getDecimalHour(int hour, int minutes, int seconds) {
        return hour + (minutes / 60.0) + (seconds / 3600.0);
    }
    
    public static double getDecimalHour(DateTime time){
        return getDecimalHour(time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());
    }
    
    /**
     * Set the decimal hour.
     * @param decimalHour 
     */
    public final void setDecimalHour(double decimalHour) {
        
        hours = (int) decimalHour;
        minutes = (int) (decimalHour * 60) % 60;
        seconds = (int) Math.round(decimalHour * (3600)) % 60;

        updateDecimalHour();
    }

    /**
     * Set the seconds and update the decimal hour.
     * @param seconds 
     */
    public void setSeconds(int seconds) {
        this.seconds = seconds;
        updateDecimalHour();
    }
    
    /**
     * Set the minutes and update the decimal hour.
     * @param minutes 
     */
    public void setMinutes(int minutes) {
        this.minutes = minutes;
        updateDecimalHour();
    }

    /**
     * Set the hour and update the decimal hour.
     * @param hours 
     */
    public void setHours(int hours) {
        this.hours = hours;
        updateDecimalHour();
    }

    /**
     * Set the year
     * @param year 
     */
    public void setYear(int year) {
        this.year = year;
    }

    /**
     * Set the day of the year
     * @param doy 
     */
    public void setDoy(int doy) {
        this.doy = doy;
    }

    /**
     * Get the year.
     * @return 
     */
    public int getYear() {
        return year;
    }

    /**
     * Get the day of the year.
     * @return 
     */
    public int getDoy() {
        return doy;
    }

    /**
     * Get the hour.
     * @return 
     */
    public int getHours() {
        return hours;
    }

    /**
     * Get the minutes.
     * @return 
     */
    public int getMinutes() {
        return minutes;
    }

    /**
     * Get the seconds.
     * @return 
     */
    public int getSeconds() {
        return seconds;
    }

    /**
     * Get the decimal hour.
     * @return 
     */
    public double getDecimalHour() {
        return decimalHour;
    }
    
    /**
     * Get a time representation in the following format : "year/doy/hour/minutes"
     * @return 
     */
    @Override
    public String toString() {
        return year+"/"+doy+"/"+hours+"/"+minutes;
    }
    
    /**
     * 
     * @param year
     * @param day From 1 to 31
     * @param month From 1 to 12
     * @return 
     */
    public static int computeDoy(int year, int month, int day) {
        
        return new DateTime(year, month, day, 12, 0).getDayOfYear();
    }
}
