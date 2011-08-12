package org.aksw.commons.util.reflect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/15/11
 *         Time: 5:23 PM
 */
class MethodSignature
{
	private String name;
	private Class<?> returnType;
	private List<Class<?>> parameterTypes;
	private boolean isVarArgs;


	@SuppressWarnings("unchecked")
	public MethodSignature(Method m) {
		this.name = m.getName();
		this.returnType = m.getReturnType();
		this.parameterTypes = Arrays.asList(m.getParameterTypes());
		this.isVarArgs = m.isVarArgs();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (isVarArgs ? 1231 : 1237);
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((parameterTypes == null) ? 0 : parameterTypes.hashCode());
		result = prime * result
				+ ((returnType == null) ? 0 : returnType.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodSignature other = (MethodSignature) obj;
		if (isVarArgs != other.isVarArgs)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (parameterTypes == null) {
			if (other.parameterTypes != null)
				return false;
		} else if (!parameterTypes.equals(other.parameterTypes))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}

}
