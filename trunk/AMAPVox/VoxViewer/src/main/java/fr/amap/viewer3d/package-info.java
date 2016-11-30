/**
 * Provides the classes necessary to create a window containing 3d stuff.<br>
 * The particularity of the 3d render is that the 3d is not calculate in real time,<br>
 * the 3d is recalculate when the user create event by moving the scene in other things.<br><br>
 * It involves that the FPSAnimator (which is used to callback the render function)<br>
 * is set to pause when nothing happens and set to resume when events happens.<br>
 */
package fr.amap.viewer3d;
