package org.aksw.commons.sparql.api.delay.core;

import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import org.aksw.commons.sparql.api.core.QueryExecutionDecorator;
import org.aksw.commons.sparql.api.delay.extra.Delayer;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 7/26/11
 *         Time: 10:57 AM
 */
public class QueryExecutionDelay
    extends QueryExecutionDecorator

{
    private Delayer delayer;

    public QueryExecutionDelay(QueryExecution decoratee, Delayer delayer) {
        super(decoratee);
        this.delayer = delayer;
    }

    @Override
     public ResultSet execSelect() {
        delayer.doDelay();
        return super.execSelect();
     }

     @Override
     public Model execConstruct() {
         delayer.doDelay();
         return super.execConstruct();
     }

     @Override
     public Model execConstruct(Model model) {
         delayer.doDelay();
         return super.execConstruct(model);
     }

     @Override
     public Model execDescribe() {
         delayer.doDelay();
         return super.execDescribe();
     }

     @Override
     public Model execDescribe(Model model) {
         delayer.doDelay();
         return super.execDescribe(model);
     }

     @Override
     public boolean execAsk() {
         delayer.doDelay();
         return super.execAsk();
     }
}
