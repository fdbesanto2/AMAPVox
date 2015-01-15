package fr.ird.jeeb.lib.structure.geometry.spline;

/**
 * An abstract class defining a general spline object.
 *
 */   
abstract class Spline
{
  protected double controlPoints_[];
  protected int    nParts_;

  abstract double[] generate();
}

