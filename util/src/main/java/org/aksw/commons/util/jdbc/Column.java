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

	public Column(String name, String type) {
		super();
		this.name = name;
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}
}
