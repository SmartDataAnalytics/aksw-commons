package org.aksw.commons.graph;

public class CacheState
{
	private int full;
	private int partial;
	private int miss;
	
	public CacheState(int full, int partial, int miss)
	{
		super();
		this.full = full;
		this.partial = partial;
		this.miss = miss;
	}
	
	public int getFull()
	{
		return full;
	}
	public int getPartial()
	{
		return partial;
	}
	public int getMiss()
	{
		return miss;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + full;
		result = prime * result + miss;
		result = prime * result + partial;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CacheState other = (CacheState) obj;
		if (full != other.full)
			return false;
		if (miss != other.miss)
			return false;
		if (partial != other.partial)
			return false;
		return true;
	}
}
