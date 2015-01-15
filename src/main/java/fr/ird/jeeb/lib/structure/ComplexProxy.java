/* 
 * Copyright (C) 2006-2009  Sebastien Griffon
 * 
 * This file is part of Jeeb.
 * 
 * Sketch is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Sketch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Sketch.  If not, see <http://www.gnu.org/licenses/>.
 */




package fr.ird.jeeb.lib.structure;

import java.io.Serializable;
import java.util.List;

import fr.ird.jeeb.lib.structure.ArchiNode.ScaleException;

/**
 * An adapter to ArchiNode. It manages the multi-scale ability of an ArchiNode.
 * @author griffon November 2008
 *
 */
public interface ComplexProxy extends Serializable/*, Cloneable*/{

		
	boolean hasLinkedComplex();
	
	boolean hasLinkedComponent();
	
	/** Returns the first sub-level ArchiNode of the current one*/
	ArchiNode getFirstComponent();
	
	/** Returns the last sub-level ArchiNode of the current one
	 * @throws Exception */
	ArchiNode getLastComponent() ;
	
	/** Returns the first sub-level ArchiNode of the current one
	 * @throws Exception */
	List<ArchiNode> getComponents();

	/** Returns the first sub-level ArchiNode of the current one at given position
		@param position Position of the component element (begin at 0) 
		@return ArchiNode or null if not found*/
	ArchiNode getComponentAt (int position)throws ScaleException;
	
	/** Returns the up-level ArchiNode of the current one */
	ArchiNode getComplex();		
	
	/** Remove all the sub-level ArchiNode of the current one 
	 * @throws Exception */
	void unlinkComponents(ArchiNode complex);

	/** Remove the sub-level component from the current one 
	 * @throws Exception */
	void unlinkComponent(ArchiNode complex, ArchiNode component);
	
	/** Sets the first sub-level ArchiNode of the current one
	 * @throws Exception */
	void linkComponent(ArchiNode complex, ArchiNode component) throws ScaleException;	
	
	void linkComponents(ArchiNode complex, List<ArchiNode> components) throws ScaleException;

}
