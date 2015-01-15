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



package fr.ird.jeeb.lib.structure.iterators;

import java.util.Collection;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import fr.ird.jeeb.lib.structure.ArchiNode;

public class ArchiTreeSuffixIterator {
	
	
	
	private LinkedList<ArchiNode> lifo;

	
	public ArchiTreeSuffixIterator (ArchiNode startNode) {
		
		lifo = new LinkedList<ArchiNode>();
		lifo.add(startNode);
	}
	
	
	boolean hasNext() {
        return !lifo.isEmpty();  
	}
	
	ArchiNode next() {
		 if (hasNext()) {
			ArchiNode c = lifo.pollLast();
			try {
				if(c != null) {
					Collection<ArchiNode> ln = c.getBackwardLinkedNodes();
					if(ln != null && !ln.isEmpty())
						lifo.addAll(ln);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    return c;
	     } else
	    	throw new NoSuchElementException();
	}
	    
	
}
