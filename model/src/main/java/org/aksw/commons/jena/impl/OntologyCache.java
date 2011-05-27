package org.aksw.commons.jena.impl;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.aksw.commons.jena.OntologyLoader;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Loads and caches Ontologies in cacheDir
 * Use loadImports to recursively load any models. They will be included as Jena submodels
 *
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class OntologyCache extends SimpleLoader implements OntologyLoader {
    private static final Logger log = Logger.getLogger(OntologyCache.class);
    final private String cacheDir;


    public OntologyCache(String cacheDir) {
        this.cacheDir = cacheDir;
        File f = new File(cacheDir);
        if (!f.exists()) {
            f.mkdir();
        }
    }


    @Override
    public OntModel loadOntology(String ontologyUri) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
        try {
            if (!isOntologyCached(ontologyUri)) {

                cache(ontologyUri);
                log.info("ontology " + ontologyUri + " was cached. To refresh, delete folder: " + new File(cacheDir).getAbsolutePath());
            }

            model.read(new File(makeFilenameFromOntologyUrl(ontologyUri)).toURI().toURL().toString());
            log.info("ontology " + ontologyUri + " was loaded from cache. To refresh, delete folder: " + new File(cacheDir).getAbsolutePath());

        } catch (IOException e) {
            log.warn("caching ontology failed, trying to load from url");
            model.read(ontologyUri);
        } catch (Exception e) {
            log.error("Could not load cached ontology trying to download", e);
        }

        return model;
    }

    public void clearCache() {

        File[] files = listAllFiles();
        for (File file : files) {
            try {
                if (file.delete()) {
                    log.info("Deleted file " + file.toString());
                }
            } catch (Exception e) {
                log.warn("could not delete file");
            }

        }
    }

    private File[] listAllFiles() {
        File f = new File(cacheDir);
        return f.listFiles();
    }

    /*
    private void initMapping() {
        File[] files = listAllFiles();
        LocationMapper l = FileManager.get().getLocationMapper();
        for (File f : files) {
            try {
                l.altMapping(URLDecoder.decode(f.toString(), "UTF-8"), f.toURI().toURL().toString());
            } catch (MalformedURLException e) {
                log.error("", e);
            } catch (UnsupportedEncodingException e) {
                log.error("", e);
            }
        }
    }
     */

    public void download(String from, String to) throws IOException {

        try {
            URL google = new URL(from);
            ReadableByteChannel rbc = Channels.newChannel(google.openStream());
            FileOutputStream fos = new FileOutputStream(to);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            log.debug("successfully downloaded " + from + " to " + to + " ");
        } catch (IOException e) {
            log.warn("caching the ontology failed " + from, e);
        }

    }

    //encodes the ontologyUrls
    private String makeFilenameFromOntologyUrl(String ontologyUrl) throws UnsupportedEncodingException {
        return cacheDir + URLEncoder.encode(ontologyUrl, "UTF-8");
    }

    private void cache(String ontologyUrl) throws IOException {
        download(ontologyUrl, makeFilenameFromOntologyUrl(ontologyUrl));
    }


    public static void main(String[] args) throws IOException {
        OntologyCache oc = new OntologyCache("/tmp/");
        OntModel m = oc.loadOntology("http://nachhalt.sfb632.uni-potsdam.de/owl/stanford.owl");
        oc.loadImports(m);
    }

    public boolean isOntologyCached(String ontologyUri) {
        try {
            return new File(makeFilenameFromOntologyUrl(ontologyUri)).exists();
        } catch (Exception e) {
            log.error("", e);
        }
        return false;

    }
}
