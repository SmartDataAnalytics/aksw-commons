package org.aksw.commons.jena;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;
import org.aksw.commons.util.ExtendedFile;
import org.aksw.commons.util.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

/**
 *
 * Best to use Claus ModelUtils
 * NOTE, the proper way would be to capture the string right away, this does not always seem to work, especially with
 * Models with reasoning capabilities...
 * 
 *
 *
 *  @author Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 * Time:  20.09.2010 18:23:35
 */

@Deprecated
public class StringConverter {
    private static final Logger logger = LoggerFactory.getLogger(StringConverter.class);

    final private Model model;
    /**
     * if this is set to true, it will use the proper method, else it will use the file based stuff
     */
    private boolean useProperStringSerialization = true;
    /**
     * Does not produce an inferred Model, but deactivates it and provides explicit output only
     */
    private boolean outputInferredModel = false;

    public StringConverter(Model m) {
        this.model = m;
        if(this.model==null){
            logger.warn("inserted Model was null");
        }
        if(model.size()==0){
            logger.warn("inserted Model was empty");
        }
    }

    public void setUseProperStringSerialization(boolean useProperStringSerialization) {
        this.useProperStringSerialization = useProperStringSerialization;
    }

    /**
     * Does not produce an inferred Model, but deactivates it, and provides explicit output only
     * @param outputInferredModel
     */
    public void setOutputInferredModel(boolean outputInferredModel) {
        this.outputInferredModel = outputInferredModel;
    }

    
    /**
     * defaults to turtle/N3
     * @return
     */
    @Override
    public String toString(){
        return toStringAsTurtle();
    }

     public String toStringAsTurtle() {
		return toString(Constants.TURTLE);
	}

    public String toStringAsRDFXML() {
		return toString(Constants.RDFXML);
	}

    public String toStringAsNTriple() {
		return toString(Constants.N_TRIPLE);
	}

    
    public String toString(String format){
        Monitor monitor = MonitorFactory.getTimeMonitor(StringConverter.class.getSimpleName() + "toString").start();
		String ret = useProperStringSerialization ? toStringProper(format) : toStringAlternate(format);
		logger.debug("Conversion of Jena to "+format+" finished ["+ret.length()+" chars] " + Time.neededMs(monitor.stop().getLastValue()));
		return ret;
        
    }



   	/**
	 * Jena workaround which creates a nasty temporary file
	 * @param format
	 * @return
	 */
	public String toStringAlternate( String format) {

        String ret = "";
        String tmpfilename = "";
		try {
			File f = File.createTempFile("JenaTaskstoStringAlternate", null);
            tmpfilename = f.toString();
			model.write(new FileWriter(f), format);
            ret =  new ExtendedFile(f).readContent();
            f.delete();
		} catch (IOException e) {
            logger.error("tmp file was: "+tmpfilename ,e);
		}
		return ret;
	}

	/**
	 * TODO benchmark this function it seems to output the whole inferred
	 * Model!!!
	 *
	 * @param format
	 * @return
	 */
	public String toStringProper( String format) {
        Model convert = model;
        //more workaraounds
        if(model instanceof OntModel && !outputInferredModel){
            convert = ((OntModel) model).getRawModel();
        }

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
        RDFWriter writer = convert.getWriter(format);
		writer.write(convert, baos, "");
		return baos.toString();
	}

	
}
