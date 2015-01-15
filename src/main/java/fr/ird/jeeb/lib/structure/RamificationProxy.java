package fr.ird.jeeb.lib.structure;

import java.io.Serializable;
import java.util.List;

/**
 *  An adapter to ArchiNode. It manages the ramification ability of an ArchiNode.
 * @author griffon November 2008
 *
 */
public interface RamificationProxy extends Serializable {

	
	
	/** Returns the bearer of the current ArchiNode*/
	ArchiNode getBearer();
	
	/** Returns all the bearers of the current ArchiNode
	 * 	Useful for sub-structural plants. 
	 */
	List<ArchiNode> getBearers();
	

	/** Returns true if the current node has ramifications */
	boolean hasLinkedRamifications();
	
	/** Returns true if the current node has linked bearer */
	boolean hasLinkedBearer();
	
	/** Returns the ramifications of the current ArchiNode*/
	List<ArchiNode> getRamifications();
	
	/** Add a ramification of the current ArchiNode
	 * 
	 * @param son the ramification to add
	 */
	void linkRamification(ArchiNode bearer, ArchiNode son);
	
	/** Add ramifications of the current ArchiNode
	 * 
	 * @param sons the collection of ramifications to add
	 */
	void linkRamifications(ArchiNode bearer, List<ArchiNode> sons);

	/** Remove a ramification of the current ArchiNode
	 * @param son the ramification to remove
	 * @return true if successful, false otherwise
	 */
	void unlinkRamification(ArchiNode bearer, ArchiNode son);
	
	/** Remove ramifications of the current ArchiNode
	 * @param sons the collection of ramifications to remove
	 * @return true if successful, false otherwise
	 */
	void unlinkRamifications(ArchiNode bearer);
	
}
