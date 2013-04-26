package com.cah;

import java.util.Arrays;

public abstract class Delta {
	/* Print all fields for sub-classes */
	public String toString() {
		StringBuilder result = new StringBuilder();
		String newLine = System.getProperty("line.separator");

		result.append( this.getClass().getName() );
		result.append( " Object {" );
		result.append(newLine);

		//determine fields declared in this class only (no fields of superclass)
		java.lang.reflect.Field[] fields = this.getClass().getDeclaredFields();

		//print field names paired with their values
		for ( java.lang.reflect.Field field : fields  ) {
			result.append("  ");
			try {
				result.append( field.getName() );
				result.append(": ");
				//requires access to private field:
				if(field.get(this).getClass() == String[].class) {
					result.append(Arrays.toString((String[]) field.get(this)));
				} else {
					result.append( field.get(this) );
				}
			} catch ( IllegalAccessException ex ) {
				System.out.println(ex);
			} catch ( NullPointerException ex ) {
				result.append("<null>");
			}
			result.append(newLine);
		}
		result.append("}");

		return result.toString();
	}
}