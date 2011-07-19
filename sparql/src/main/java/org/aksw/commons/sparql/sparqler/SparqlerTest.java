package org.aksw.commons.sparql.sparqler;

import org.apache.log4j.PropertyConfigurator;

public class SparqlerTest {
	public static void main(String[] args) {
		PropertyConfigurator.configure("log4j.properties");
		
		Sparqler sparqler = new SparqlerHttp("http://live.dbpedia.org/sparql");
		sparqler = new SparqlerDelayer(sparqler, 10000l);
		
		//SelectPaginated it = new SelectPaginated(sparqler, "Select * From <http://dbpedia.org> {?s a <http://dbpedia.org/ontology/Person> . } offset 30 limit 10", 1);
		ConstructPaginated it = new ConstructPaginated(sparqler, "Construct { ?s a <http://dbpedia.org/ontology/Person> . } From <http://dbpedia.org> {?s a <http://dbpedia.org/ontology/Person> . } offset 30 limit 10", 1);
		
		
		while(it.hasNext()) {
			System.out.println(it.next());
		}	
	}
}
