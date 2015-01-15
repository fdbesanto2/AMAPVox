package fr.ird.jeeb.lib.structure;

import java.util.ArrayList;
import java.util.List;

import fr.ird.jeeb.lib.structure.ArchiNode.ScaleException;

/**
 * A multi-scale adapter for individu rchiNode. 
 * Adds scale information only on each decomposition element and his container.
 * @author griffon November 2008
 *
 */
public class NoSuccesionComplexProxyImpl implements ComplexProxy {

	static private final long serialVersionUID = 1L;
	
	private ArchiNode complex; //the container ArchiNode
	private ArrayList<ArchiNode> components; //the components collection

	@Override
	public boolean hasLinkedComplex() {

		return complex != null;
	}

	@Override
	public boolean hasLinkedComponent() {
		return components != null && !components.isEmpty();
	}

	@Override
	public ArchiNode getFirstComponent() {
		return components.get(0);
	}
	
	@Override
	public ArchiNode getLastComponent() {
		return components.get(components.size()-1);
		
	}
	@Override
	public List<ArchiNode> getComponents() {		
		return components;
	}
	@Override
	public ArchiNode getComponentAt(int position) throws ScaleException {
		if(position>=0 && position < components.size())
			return components.get(position);
		return null;
	}
	@Override
	public ArchiNode getComplex() {

		return complex;
	}
	
	@Override
	public void unlinkComponents(ArchiNode complex) {
		
		for(ArchiNode c: components){
			c.setComplexProxy(null);
		}				
		complex.setCompositionProxy(null);	
	}
	@Override
	public void unlinkComponent(ArchiNode complex, ArchiNode component) {
		
		if (complex== null || component == null)
			return;
		
		components.remove(component);
		component.setComplexProxy(null);
		
		if(components.isEmpty())
			complex.setCompositionProxy(null);
	}
	@Override
	public void linkComponent(ArchiNode complex, ArchiNode component) throws ScaleException {
		if (complex== null || component == null)
			return;
		
		this.complex = complex;
		if(components == null) components= new ArrayList<ArchiNode> ();
		this.components.add(component);
		component.setComplexProxy(this);
		
	}
	@Override
	public void linkComponents(ArchiNode complex, List<ArchiNode> components) {
		if (complex== null || components == null)
			return;
		
		this.complex = complex;
		this.components = (ArrayList<ArchiNode>) components;
		
		for(ArchiNode c: components){
			c.setComplexProxy(this);
		}	
		
	}
	
	
	
	

}
