package fr.ird.jeeb.lib.structure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.ird.jeeb.lib.structure.ArchiNode.ScaleException;

/**
 * A simple multi-scale adapter for ArchiNode. Adds scale information only on
 * the first decomposition element and his container.
 * 
 * @author griffon November 2008
 * 
 */
public class DefaultComplexProxyImpl implements ComplexProxy {

	static private final long serialVersionUID = 1L;

	private ArchiNode complex; // the container ArchiNode
	private ArchiNode firstComponent; // the first decomposition ArchiNode


	@Override
	public boolean hasLinkedComplex() {
		return complex != null;
	}

	@Override
	public boolean hasLinkedComponent() {
		return firstComponent != null;
	}

	@Override
	public ArchiNode getFirstComponent() {
		return firstComponent;
	}

	@Override
	public ArchiNode getLastComponent() {
		ArchiNode currentElement = firstComponent;
		while (currentElement != null && currentElement.getLinkedSuccessor() != null) {
			currentElement = currentElement.getLinkedSuccessor();
		}
		return currentElement;
	}

	@Override
	public List<ArchiNode> getComponents() {
		if (firstComponent != null) {
			Collection<ArchiNode> decompositions = new ArrayList<ArchiNode>();
			ArchiNode currentElement = firstComponent;
			while (currentElement != null) {
				decompositions.add(currentElement);
				currentElement = currentElement.getLinkedSuccessor();
			}
			return (List<ArchiNode>) decompositions;
		}
		return null;
	}

	@Override
	public ArchiNode getComponentAt(int position) throws ScaleException {
		if (firstComponent != null) {
			ArchiNode currentElement = firstComponent;
			int currentPosition = 1;
			while (currentElement != null && currentPosition <= position) {
				currentElement = currentElement.getLinkedSuccessor();
				currentPosition++;
			}
			return currentElement;
		}
		return null;
	}

	@Override
	public ArchiNode getComplex() {		
		return complex;
	}

	@Override
	public void unlinkComponents(ArchiNode complex) {
		
//		List<ArchiNode> components = getComponents();
//		for(ArchiNode c: components){
//			c.unlinkSuccessor();
//		}
		firstComponent.setComplexProxy(null);		
		complex.setCompositionProxy(null);		
	}

	@Override
	public void unlinkComponent(ArchiNode complex, ArchiNode component) {
		if (complex== null || component == null)
			return;
		
		if(component == firstComponent)
			unlinkComponents(complex);
		else {
			ArchiNode predecessor = component.getLinkedPredecessor();
			ArchiNode successor = component.getLinkedSuccessor();
			try {
				predecessor.addSuccessor(successor);
			} catch (Exception e) {				
				e.printStackTrace();
			}
			
		}
			
	}

	@Override
	public void linkComponent(ArchiNode complex, ArchiNode component) throws ScaleException {
		if (complex== null || component == null)
			return;

		if (firstComponent == null) {
			this.firstComponent = component;			
			this.complex = complex;
			component.setComplexProxy(this);	
		} else {
			ArchiNode lastElement = getLastComponent();
			if (lastElement != null) {
				if (lastElement.getScale() < component.getScale()) {
					lastElement.addComponent(component);
				} else {
					lastElement.addSuccessor(component);
				}
			}
		}

	}

	@Override
	public void linkComponents(ArchiNode complex, List<ArchiNode> components) throws ScaleException {
		
		if (complex== null || components == null)
			return;
		
		ArchiNode lastElement = null;
		for(ArchiNode n : components) {			
			if (lastElement == null) {
				this.firstComponent = n;			
				this.complex = complex;
				n.setComplexProxy(this);	
			} else {				
				
				if (lastElement.getScale() < n.getScale()) {
					lastElement.addComponent(n);
				} else {
					lastElement.addSuccessor(n);
				}
				
			}
			lastElement = n;
		}

	}


	
}
