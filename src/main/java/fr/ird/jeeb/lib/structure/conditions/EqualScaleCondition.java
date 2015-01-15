package fr.ird.jeeb.lib.structure.conditions;

import fr.ird.jeeb.lib.structure.ArchiNode;

public class EqualScaleCondition implements ArchiCondition {

	
	private short scale;

	public EqualScaleCondition (short scale) {
		this.scale = scale;
	}
	
	public EqualScaleCondition (int scale) {
		this.scale = (short) scale;
	} 
	
	@Override
	public boolean isCorrect(ArchiNode node) {
		if(node.getScale() == scale) {
			return true;
		}else {
			return false;
		}
	}
	
	

}
