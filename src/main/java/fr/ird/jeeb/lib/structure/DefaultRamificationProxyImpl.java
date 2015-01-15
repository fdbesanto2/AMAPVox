package fr.ird.jeeb.lib.structure;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A simple ramification adapter with a single bearer and a collection
 * @author griffon November 2008
 *
 */
public class DefaultRamificationProxyImpl implements RamificationProxy {

	static private final long serialVersionUID = 1L;
	
	private ArchiNode bearer;
	private List<ArchiNode> ramifications;
	public DefaultRamificationProxyImpl() {
		
		
	}
	
	@Override
	public boolean hasLinkedRamifications() {		
		return ramifications != null && !ramifications.isEmpty();
	}

	@Override
	public ArchiNode getBearer() {		
		return bearer;
	}

	@Override
	public List<ArchiNode> getBearers() {		
		return Arrays.asList(bearer);
	}


	@Override
	public boolean hasLinkedBearer() {		
		return bearer != null;
	}

	@Override
	public List<ArchiNode> getRamifications() {
		return ramifications;
	}

	@Override
	public void linkRamification(ArchiNode bearer, ArchiNode son) {
		
		if(bearer == null || son == null) return;
			
		if(ramifications == null) ramifications= new ArrayList<ArchiNode> ();
		ramifications.add(son);
		this.bearer = bearer;
		son.addBearerProxy(this);
		
	}

	@Override
	public void linkRamifications(ArchiNode bearer, List<ArchiNode> sons) {
		if(bearer == null || sons == null) return;
		
		if(ramifications == null) ramifications= new ArrayList<ArchiNode> ();
		ramifications.addAll(sons);
		this.bearer = bearer;
		for(ArchiNode son : sons) {
			son.addBearerProxy(this);
		}
		
	}

	@Override
	public void unlinkRamification(ArchiNode bearer, ArchiNode son) {
		if(bearer == null || ramifications == null) return;
		ramifications.remove(son);		
		if(ramifications.isEmpty()) {
			bearer.setRamificationProxy(null);
		}
		son.removeBearerProxy(this);		
	}

	@Override
	public void unlinkRamifications(ArchiNode bearer) {
		if(bearer == null || ramifications == null) return;
		
		for(ArchiNode son : ramifications) {
			son.removeBearerProxy(this);
		}		
		bearer.setRamificationProxy(null);	
		
	}
	
}
