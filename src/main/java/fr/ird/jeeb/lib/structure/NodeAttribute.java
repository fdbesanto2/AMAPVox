package fr.ird.jeeb.lib.structure;

import java.awt.Color;
import java.io.Serializable;

import javax.swing.ImageIcon;

import fr.ird.jeeb.lib.util.Check;


public class NodeAttribute<T> implements Cloneable, Serializable,
		Comparable<NodeAttribute<T>> {

	static private final long serialVersionUID = 1L;

	public static class AttributeType implements Serializable, Comparable<AttributeType>{
		
		public final static AttributeType OBJECT = new AttributeType (Object.class, "Object", "Object");
		public final static AttributeType STRING = new AttributeType (String.class, "String", "String");
		public final static AttributeType INTEGER = new AttributeType (Integer.class, "Integer", "Integer");
		public final static AttributeType DOUBLE = new AttributeType (Double.class, "Double", "Double");
		public final static AttributeType BOOLEAN = new AttributeType (Boolean.class, "Boolean", "Boolean");
		public final static AttributeType COLOR = new AttributeType (Color.class, "Color", "Color");		
		public final static AttributeType IMAGE = new AttributeType (ImageIcon.class, "Image", "Image");
						
		
		private final Class valueClass;
		private final String name;
		private final String key;
		
				
		AttributeType (Class c, String name, String key) {
			this.valueClass = c;
			this.name = name;
			this.key = key;
		}
		
		public Class getValueClass() {
			return valueClass;
		}	
		
		
		public String getName () {
			return name;
		}

		@Override
		public int compareTo (AttributeType o) {
			return name.compareTo (o.getName ());
		}
		
		
		@Override
		public String toString () {		
			return getName ();
		}
				
		public static AttributeType findAttributeTypeFromClass(Class valueClass) throws Exception {
			
			
			if (valueClass.equals(Integer.class) || valueClass.equals(Short.class)|| valueClass.equals(Byte.class) || valueClass.equals(Long.class)) {
				return AttributeType.INTEGER;
			} else if (valueClass.equals(Float.class) || valueClass.equals(Double.class)) {
				return AttributeType.DOUBLE;
			} else if (valueClass.equals(Boolean.class)) {
				return AttributeType.BOOLEAN;
			} else  if (valueClass.equals(String.class)) {
				return AttributeType.STRING;
			} else  if (valueClass.equals(Color.class)) {
				return AttributeType.COLOR;
			} else  if (valueClass.equals(ImageIcon.class)) {
				return AttributeType.IMAGE;
			} else 
				return AttributeType.OBJECT;
				
		}
	};
	
	public static class AttributeTypeConvertable extends AttributeType{
		public final static AttributeTypeConvertable METRE = new AttributeTypeConvertable (Double.class, "Metre", "Metre_100", 100);
		public final static AttributeTypeConvertable CENTIMETRE = new AttributeTypeConvertable (Double.class,"Centimetre", "Centimetre#1", 1);
		public final static AttributeTypeConvertable MILLIMETRE = new AttributeTypeConvertable (Double.class, "Millimetre","Millimetre#0.1", 0.1);
		public final static AttributeTypeConvertable METER10EM5 = new AttributeTypeConvertable (Double.class, "10E-5 Metre","10E-5Metre#0.01", 0.01);
		public final static AttributeTypeConvertable CUSTOM = new AttributeTypeConvertable (Double.class, "Custom","Custom#1", 1);
		
		private double ratio;
		
		AttributeTypeConvertable (Class c, String name, String key, double ratio) {
			super(c,name,key);
			this.ratio = ratio;			
		}
		
		
		public void setRatio (double ratio) {
			this.ratio = ratio;
		}
		
		
		public double getRatio () {
			return ratio;
		}
		
		public double convertToCm (double value) {
			return value*ratio;
		}
	}
	
	
	
	
	public static AttributeType [] simpleTypeList = {AttributeType.OBJECT, AttributeType.STRING, AttributeType.INTEGER, AttributeType.DOUBLE, AttributeType.BOOLEAN};
	public static AttributeType [] otherTypeList = {AttributeType.COLOR, AttributeType.IMAGE};
	public static AttributeType [] lengthTypeList = {AttributeTypeConvertable.CENTIMETRE, AttributeTypeConvertable.METRE, AttributeTypeConvertable.MILLIMETRE, AttributeTypeConvertable.METER10EM5, AttributeTypeConvertable.CUSTOM};
	//public static AttributeType [] weightTypeList = {AttributeType.Kilogram, AttributeType.Gram};
	
	protected String key; // current key
	protected T value; // current value
	protected AttributeType type;



	public NodeAttribute(String key, T value) {
		this.key = key;
		this.value = value;
		if(value != null) try {
			this.type = AttributeType.findAttributeTypeFromClass (value.getClass ());
		} catch (Exception e) {			
			e.printStackTrace();
		}
	}
	
	public NodeAttribute(String key, T value, AttributeType type) {
		this.key = key;
		this.value = value;
		this.type = type;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}
	
	
	public void setType (AttributeType type) {
		this.type = type;
	}

	public String toString() {
		if (value == null)
			return "";
		if (value instanceof Color) {
			Color color = (Color) value;
			return "Color " + color.getRed() + " " + color.getGreen() + " "
					+ color.getBlue();
		} else {
			return value.toString();
		}
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}

	public boolean isNumber() {
		if (value instanceof Number)
			return true;
		return false;
	}

	public double getDoubleValue() {
		if (isNumber()) {
			return new Double(((Number)value).doubleValue ());
		}
		return 0;
	}
	
	public float getValueBaseUnit() {
		if (isNumber()) {
			if(type != null && type instanceof AttributeTypeConvertable){
				return new Float(((AttributeTypeConvertable)type).convertToCm (getDoubleValue()));
				
			} else
				return (float)(((Number)value).floatValue ());
		}		
		return 0;
	}

	

	public static Object valueFromString(String value) {

		if (!value.trim ().isEmpty()) {
			if (value.startsWith("Color")) {
				String[] colors = value.split("\\s+");
				if (colors.length == 4) {
					if (Check.isInt(colors[1]) && Check.isInt(colors[2])
							&& Check.isInt(colors[3])) {
						return new Color(Check.intValue(colors[1]), Check
								.intValue(colors[2]), Check.intValue(colors[3]));
					}
				}
			} else {
				if (Check.isInt(value)) {
					return Check.intValue(value);
				} else if (Check.isDouble(value)) {
					return Check.doubleValue(value);
				} else if (Check.isDate(value)) {
					return Check.dateValue(value);
				} else if (Check.isBoolean(value)) {
					return Check.booleanValue(value);
				}
			}
		}
		return value;

	}

	/**
	 * Equality.
	 */

	public boolean equals(NodeAttribute a) {
		return value.equals(a.getValue());
	}

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	@Override
	public int compareTo(NodeAttribute o) {
		if (value instanceof Comparable && o.getValue() instanceof Comparable)
			return ((Comparable) value).compareTo((Comparable) o.getValue());

		return 0;
	}
	
	
	public AttributeType getType () {
		
		if(type != null) {
			return type;
		} else {		
			return AttributeType.OBJECT;
		}
		
	}

}
