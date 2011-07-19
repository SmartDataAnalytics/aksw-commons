package org.aksw.commons.sparql.sparqler;

import org.slf4j.LoggerFactory;

public class SparqlerDelayer
	extends SparqlerDelayerBase
{	
	private long lastRequestTime = 0;
	private long delay = 1000;
	
	public SparqlerDelayer(Sparqler decoratee, long delay)
	{
		super(decoratee);
		this.delay = delay;
	}
	
	@Override
	public void setLastRequestTime(long time) {
		this.lastRequestTime = time;
	}

	@Override
	public long getLastRequestTime() {
		return this.lastRequestTime;
	}

	@Override
	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay)
	{
		this.delay = delay;
	}
}

