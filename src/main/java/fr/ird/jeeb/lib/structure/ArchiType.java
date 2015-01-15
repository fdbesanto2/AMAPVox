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

import fr.ird.jeeb.lib.defaulttype.Type;
import fr.ird.jeeb.lib.util.Translator;

public class ArchiType implements Type,Serializable/*, Cloneable*/ {
	
	static private final long serialVersionUID = 1L;
	
	public final static ArchiType DEFAULT = new ArchiType ("ArchiType.default",Translator.swap("ArchiType.default"), (short)0);
	public final static ArchiType SCENE = new ArchiType ("ArchiType.scene",Translator.swap("ArchiType.scene"), (short)0);
	public final static ArchiType PLANT = new ArchiType ("ArchiType.plant", Translator.swap("ArchiType.plant"), (short)1);
	public final static ArchiType AXIS = new ArchiType ("ArchiType.axis", Translator.swap("ArchiType.axis"),(short) 2);
	public final static ArchiType INTERNODE = new ArchiType ("ArchiType.internode", Translator.swap("ArchiType.internode"),(short) 6);
	public final static ArchiType ANNUALSHOOT = new ArchiType ("ArchiType.ashoot", Translator.swap("ArchiType.ashoot"),(short) 3);
	public final static ArchiType GROWTHUNIT = new ArchiType ("ArchiType.growthunit", Translator.swap("ArchiType.growthunit"),(short) 4);
	public final static ArchiType ZONE = new ArchiType ("ArchiType.zone", Translator.swap("ArchiType.zone"),(short) 5);
	public final static ArchiType LEAF = new ArchiType ("ArchiType.leaf", Translator.swap("ArchiType.leaf"),(short) 2);
	public final static ArchiType FRUIT = new ArchiType ("ArchiType.fruit", Translator.swap("ArchiType.fruit"),(short) 2);
	public final static ArchiType INFLORESCENCE = new ArchiType ("ArchiType.inflo", Translator.swap("ArchiType.inflo"),(short) 4);
	public final static ArchiType METAMER = new ArchiType ("ArchiType.metamer", Translator.swap("ArchiType.metamer"),(short) 3);	
	public final static ArchiType PETIOLE = new ArchiType ("ArchiType.petiole", Translator.swap("ArchiType.petiole"),(short) 6);
	public final static ArchiType BLADE = new ArchiType ("ArchiType.blade", Translator.swap("ArchiType.blade"),(short) 6);
	
	private String key;
	private String name;
	private String info;
	private short scale;
	
	private ArchiTypeProperties properties;
	
	public ArchiType(String key, String name, short scale) {
		this.key = key;
		this.name = name;
		this.scale = scale;
		
	}
	
	public ArchiType(String key, short scale) {
		this.key = key;
		this.name = new String(key);
		this.scale = scale;
		
	}
	
	public ArchiType(String key, int scale) {
		this.key = key;
		this.name = new String(key);
		this.scale = (short)scale;		
	}
	
	public String getName() {	
		return key;
	}
	
	
	// fc-21.8.2009 - see comments in jeeb.lib.util.Type
	public String getTranslatedName () {return name;}
	
	public void setTranslatedName (String name) {this.name = name;}

	
	public String getInfo() {
		return info;
	}
	
	public void setInfo(String info) {
		this.info = info;
	}
	
	public void setName(String key) {
		this.key = key;
		
	}
	
	public short getScale() {
		return scale;
	}
	
	public void setScale(short scale) {
		this.scale = scale;
	}
	
	/**	String representation.
	*/
	public String toString () {
		StringBuffer result = new StringBuffer (scale +"_"+ name);
		return result.toString ();
	}
	
	@Override
	public int compareTo (Object o) {
		if (o == null) {return -1;}
		if (!(o instanceof ArchiType)) {return 0;}	// do not crash
		ArchiType t = (ArchiType) o;
		if(scale<t.scale) return -1;
		if(scale>t.scale) return 1;		
		return key.compareTo (t.key) ;
			
	}
	
	/**	Equality.
	*/
	@Override
	public boolean equals (Object o) {
		if (o == null) {return false;}
		if (!(o instanceof ArchiType)) {return false;}
		ArchiType t = (ArchiType) o;
		return key.equals (t.key) && scale == t.scale;
	}
	
	@Override
	public int hashCode() {	
		return key.hashCode();
	}
	
	public ArchiTypeProperties getProperties() {
		return properties;
	}
	
	public void setProperties(ArchiTypeProperties properties) {
		this.properties = properties;
	}
	
	
	
}
