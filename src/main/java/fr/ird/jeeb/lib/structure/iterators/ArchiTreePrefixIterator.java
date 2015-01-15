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
import fr.ird.jeeb.lib.structure.conditions.ArchiCondition;

/**
 * 
 * A prefix iterator on an ArchiTree.
 * 
 * @author griffon
 *
 */
public class ArchiTreePrefixIterator {
	
	
	
	private LinkedList<ArchiNode> fifo;
	
	private ArchiCondition condition;

	
	public ArchiTreePrefixIterator (ArchiNode startNode) {
		
		fifo = new LinkedList<ArchiNode>();
		fifo.add(startNode);
	}
	
	public void setCondition (ArchiCondition condition) {
		this.condition = condition;
	}
	
	
	public boolean hasNext() {
        return !fifo.isEmpty();  
	}
	
	public ArchiNode next() throws NoSuchElementException {
		 if (hasNext()) {
			ArchiNode c = fifo.pollFirst();
			try {
				if(c != null) {
					Collection<ArchiNode> ln = c.getForwardLinkedNodes();
					if(ln != null && !ln.isEmpty())
						fifo.addAll (0, ln);
				}
			} catch (Exception e) {				
				e.printStackTrace();
			}
			
			if(condition == null || (condition!= null && condition.isCorrect(c)))		    
				return c;
			else
				return next();
			
			
	     } else
	    	throw new NoSuchElementException();
	}
	    
	
}
