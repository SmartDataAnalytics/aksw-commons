package org.aksw.commons.util.jdbc;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 12/6/11
 *         Time: 4:48 PM
 */
public class Column
{
	private String name;
	private String type;
	private int ordinalPosition;
	private Boolean isNullable; // null: unknown

	public Column(int ordinalPosition, String name, String type) {
		this(ordinalPosition, name, type, null);
	}

	public Column(int ordinalPosition, String name, String type, Boolean isNullable) {
		super();
		this.ordinalPosition = ordinalPosition;
		this.name = name;
		this.type = type;
		this.isNullable = isNullable;
	}

	public int getOrdinalPosition() {
		return ordinalPosition;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
	
	public Boolean isNullable() {
		return isNullable;
	}
	
	/**
	 * Return the value with a default assumption
	 * 
	 * @param def
	 * @return
	 */
	public Boolean isNullable(boolean assumption) {
		boolean result = isNullable == null ? assumption : isNullable;
		return result;
	}
}
