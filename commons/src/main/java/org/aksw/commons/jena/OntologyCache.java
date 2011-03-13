package org.aksw.commons.jena;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.apache.log4j.Logger;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * provides a mapping from remote files to local files
 *
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class OntologyCache {
    private static final Logger logger = Logger.getLogger(OntologyCache.class);
    final private String cacheDir;

    public OntologyCache(String cacheDir) {
        this.cacheDir = cacheDir;
        File f = new File(cacheDir);
        if (!f.exists()) {
            f.mkdir();
        }
    }


    public OntModel loadOntology(String ontologyUri) {
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM, ModelFactory.createDefaultModel());
        try {
            if (!isOntologyCached(ontologyUri)) {

                cache(ontologyUri);
                logger.warn("ontology " + ontologyUri + " was cached. To refresh, delete folder: "
                        + new File(cacheDir).getAbsolutePath());
            }
        } catch (IOException e) {
            logger.warn("caching ontology failed, trying to load from url");
            model.read(ontologyUri);
            return model;
        }


        try {

            model.read(new File(makeFilenameFromOntologyUrl(ontologyUri)).toURI().toURL().toString());
            logger.warn("ontology " + ontologyUri + " was loaded from cache. To refresh, delete folder: "
                    + new File(cacheDir).getAbsolutePath());
             return model;

        } catch (Exception e) {
            logger.error("Could not load cached ontology trying to download", e);
        }

         return null;
    }

    public void clearCache() {

        File[] files = listAllFiles();
        for (File file : files) {
            try {
                if (file.delete()) {
                    logger.info("Deleted file " + file.toString());
                }
            } catch (Exception e) {
                logger.warn("could not delete file");
            }

        }
    }

    private File[] listAllFiles() {
        File f = new File(cacheDir);
        return f.listFiles();
    }

    /**
     * private static void initMapping() {
     * File[] files = listAllFiles();
     * LocationMapper l = FileManager.get().getLocationMapper();
     * for (File f : files) {
     * try {
     * l.altMapping(URLDecoder.decode(f.toString(), "UTF-8"), f.toURI().toURL().toString());
     * } catch (MalformedURLException e) {
     * logger.error("", e);
     * } catch (UnsupportedEncodingException e) {
     * logger.error("", e);
     * }
     * }
     * }
     */

    private void download(String from, String to) throws IOException {

        try {
            URL google = new URL(from);
            ReadableByteChannel rbc = Channels.newChannel(google.openStream());
            FileOutputStream fos = new FileOutputStream(to);
            fos.getChannel().transferFrom(rbc, 0, 1 << 24);
            logger.info("successfully downloaded " + from + " to " + to + " ");
        } catch (IOException e) {
            logger.warn("caching the ontology failed " + from, e);
        }

    }

    //endoces the ontologyUrls
    private String makeFilenameFromOntologyUrl(String ontologyUrl) throws UnsupportedEncodingException {
        return cacheDir + URLEncoder.encode(ontologyUrl, "UTF-8");
    }

    private void cache(String ontologyUrl) throws IOException {
        download(ontologyUrl, makeFilenameFromOntologyUrl(ontologyUrl));
    }


    public static void main(String[] args) throws IOException {
        new OntologyCache("/tmp/").loadOntology("http://nachhalt.sfb632.uni-potsdam.de/owl/stanford.owl")  ;
    }

    public boolean isOntologyCached(String ontologyUri) {
        try {
            return new File(makeFilenameFromOntologyUrl(ontologyUri)).exists();
        } catch (Exception e) {
            logger.error("", e);
        }
        return false;

    }
}
