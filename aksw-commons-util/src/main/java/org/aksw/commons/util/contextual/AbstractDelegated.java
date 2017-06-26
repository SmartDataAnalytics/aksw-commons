package org.aksw.commons.util.contextual;

public class AbstractDelegated<T>
	implements Delegated<T>
{
	protected T delegate;

	public AbstractDelegated() {
		this(null);
	}

	public AbstractDelegated(T delegate) {
		super();
		this.delegate = delegate;
	}

	@Override
	public T delegate() {
		return delegate;
	}

	@Override
	public String toString() {
		return "delegated " + delegate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((delegate == null) ? 0 : delegate.hashCode());
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
		AbstractDelegated<?> other = (AbstractDelegated<?>) obj;
		if (delegate == null) {
			if (other.delegate != null)
				return false;
		} else if (!delegate.equals(other.delegate))
			return false;
		return true;
	}
}
