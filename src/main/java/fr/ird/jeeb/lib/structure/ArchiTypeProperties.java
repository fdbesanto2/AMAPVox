package fr.ird.jeeb.lib.structure;

import java.io.Serializable;
import java.util.HashSet;

public class ArchiTypeProperties implements Serializable{

	
	
	private HashSet notEditableAttributeMap;
	
	public ArchiTypeProperties() {
		notEditableAttributeMap = new HashSet();
	}
	
	public void addNotEditableAttribute (String attributeKey) {
		notEditableAttributeMap.add(attributeKey);
	}
	
	public void removeNotEditableAttribute (String attributeKey) {
		notEditableAttributeMap.remove(attributeKey);
	}
	
	public boolean isNotEditableAttribute(String attributeKey) {
		return notEditableAttributeMap.contains(attributeKey);
	}
	
	public boolean isEmpty() {
		return notEditableAttributeMap.isEmpty();
	}
}
