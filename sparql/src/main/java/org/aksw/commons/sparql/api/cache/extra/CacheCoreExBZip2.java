package org.aksw.commons.sparql.api.cache.extra;

import com.hp.hpl.jena.rdf.model.Model;
import org.aksw.commons.sparql.api.cache.core.QueryExecutionFactoryCacheEx;
import org.aksw.commons.sparql.api.core.QueryExecutionFactory;
import org.aksw.commons.sparql.api.dereference.QueryExecutionFactoryDereference;
import org.aksw.commons.util.StreamUtils;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.*;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 11/28/11
 *         Time: 11:26 PM
 */
public class CacheCoreExBZip2
    implements CacheCoreEx
{
    private CacheCoreEx decoratee;

    private final CompressorStreamFactory streamFactory = new CompressorStreamFactory();
    private String compression = CompressorStreamFactory.BZIP2;

    public CacheCoreExBZip2(CacheCoreEx decoratee) {
        this.decoratee = decoratee;
    }

    public static CacheCoreExBZip2 wrap(CacheCoreEx decoratee) {
        return new CacheCoreExBZip2(decoratee);
    }


    @Override
    public CacheEntry lookup(String service, String queryString) {
        CacheEntry raw = decoratee.lookup(service, queryString);

        return raw == null
            ? null
            : new CacheEntry(raw.getTimestamp(), raw.getLifespan(),
                new InputStreamProviderBZip2(raw.getInputStreamProvider(), streamFactory, compression));
    }

    @Override
    public void write(String service, String queryString, InputStream in) {
        try {
            _write(service, queryString, in);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    // FIXME We are storing data in memory here - will break with huge amounts of data
    public void _write(String service, String queryString, InputStream in)
            throws Exception
    {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();

        OutputStream out = streamFactory.createCompressorOutputStream(compression, tmp);

        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        PrintWriter writer = new PrintWriter(out);

        /*
        String line;
        while((line = reader.readLine()) != null) {
            writer.println(line);
        }
        writer.flush();
        writer.close();
        */


        final byte[] buffer = new byte[1024];
        int n = 0;
        while (-1 != (n = in.read(buffer))) {
            out.write(buffer, 0, n);
        }
        out.flush();
        out.close();



        //InputStream test = streamFactory.createCompressorInputStream(compression, new ByteArrayInputStream(tmp.toByteArray()));
        //System.out.println(StreamUtils.toString(test));


        decoratee.write(service, queryString, new ByteArrayInputStream(tmp.toByteArray()));
    }



    public static void main(String[] args)
            throws Exception
    {

        QueryExecutionFactory<?> factory = new QueryExecutionFactoryDereference("LATC QA tool <cstadler@informatik.uni-leipzig.de>");

        // Create a cache using a database called 'cache'
        CacheCoreEx cacheBackend = CacheCoreH2.create("cache", 15000000, true);
        CacheEx cacheFrontend = new CacheExImpl(cacheBackend);

        // The following caching query execution factory associates all cache entries
        // with 'the-internet'
        factory = new QueryExecutionFactoryCacheEx(factory, "http://the-internet.org", cacheFrontend);

        Model result = factory.createQueryExecution("DESCRIBE <http://dbpedia.org/resource/London>").execDescribe();
        result.write(System.out, "N-TRIPLES", null);

    }
}
