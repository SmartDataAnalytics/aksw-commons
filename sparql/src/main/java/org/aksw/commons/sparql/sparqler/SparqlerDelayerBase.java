package org.aksw.commons.sparql.sparqler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.update.UpdateRequest;

/**
 * Imposes a request rate limit on a sparqler.
 * 
 * @author raven
 * 
 */
public abstract class SparqlerDelayerBase implements Sparqler {
	private static final Logger logger = LoggerFactory
			.getLogger(SparqlerDelayerBase.class);

	private Sparqler decoratee;

	public abstract void setLastRequestTime(long time);

	public abstract long getLastRequestTime();

	public abstract long getDelay();

	public SparqlerDelayerBase(Sparqler decoratee) {
		this.decoratee = decoratee;
	}

	protected void doDelay() {
		long now = System.currentTimeMillis();

		long remainingDelay = getDelay() - (now - getLastRequestTime());

		if (remainingDelay > 0l) {
			logger.debug("Delaying sparql request by " + remainingDelay + "ms.");
			try {
				Thread.sleep(remainingDelay);
			} catch (Exception e) {

			}
		}

		setLastRequestTime(System.currentTimeMillis());
	}

	@Override
	public boolean executeAsk(String queryString) {
		doDelay();
		return decoratee.executeAsk(queryString);
	}

	@Override
	public Model executeDescribe(Model result, String queryString) {
		doDelay();
		return decoratee.executeDescribe(result, queryString);
	}

	@Override
	public Model executeConstruct(Model result, String queryString) {
		doDelay();
		return decoratee.executeConstruct(result, queryString);
	}

	@Override
	public ResultSet executeSelect(String queryString) {
		doDelay();
		return decoratee.executeSelect(queryString);
	}

	@Override
	public void executeUpdate(String requestString) {
		doDelay();
		decoratee.executeUpdate(requestString);
	}

	@Override
	public Object getId() {
		return decoratee.getId();
	}

	@Override
	public boolean executeAsk(Query query) {
		doDelay();
		return decoratee.executeAsk(query);
	}

	@Override
	public Model executeDescribe(Model result, Query query) {
		doDelay();
		return decoratee.executeDescribe(result, query);
	}

	@Override
	public Model executeConstruct(Model result, Query query) {
		doDelay();
		return decoratee.executeConstruct(result, query);
	}

	@Override
	public ResultSet executeSelect(Query query) {
		doDelay();
		return decoratee.executeSelect(query);
	}

	@Override
	public void executeUpdate(UpdateRequest request) {
		doDelay();
		decoratee.executeUpdate(request);
	}

	@Override
	public void abort() {
		decoratee.abort();
	}
}
