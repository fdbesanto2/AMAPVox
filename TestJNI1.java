/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Julien
 */
public class TestJNI1 {
    
    public native void afficherBonjour();
    
    static {
        System.loadLibrary("JNITEST");
    }
    
    public static void main(String[] args) {
        new TestJNI1().afficherBonjour();
    }
}
