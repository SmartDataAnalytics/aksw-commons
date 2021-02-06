package org.aksw.jena_sparql_api.io.process.pipe;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.aksw.commons.io.endpoint.FileCreation;
import org.aksw.commons.io.process.pipe.PipeTransform;
import org.aksw.commons.io.process.pipe.PipeTransformRx;
import org.aksw.commons.io.process.pipe.ProcessPipeUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.google.common.base.StandardSystemProperty;
import com.google.common.io.ByteStreams;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

public class ProcessPipesTest {

    /**
     * TODO Convert to test cases
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if(true) {
            String pathStr = StandardSystemProperty.USER_DIR.value() + "/src/test/resources/kill-test.sh";
            Path path = Paths.get(pathStr);
            PipeTransformRx test = PipeTransformRx.fromSysCallStreamToStream("/bin/sh", "-c", "cat /dev/urandom | tr -dc 'a-zA-Z0-9' | fold -w 32 | head -n 1000000 | LC_ALL=C sort -u | LC_ALL=C sort -R | LC_ALL=C sort -u");

            try(InputStream in = Single.just(path)
                .compose(test.mapPathToStream())
                .timeout(10, TimeUnit.SECONDS)
                .blockingGet()) {

                Flowable<String> lines = Flowable.generate(
                        () -> new BufferedReader(new InputStreamReader(in)),
                        (it, e) -> {
                            String line = it.readLine();
                            if(line == null) {
                                e.onComplete();
                            } else {
                                e.onNext(line);
                            }
                        },
                        AutoCloseable::close);

                List<String> strs = lines
                    .timeout(5, TimeUnit.SECONDS)
                    .toList()
                    .blockingGet();

                strs.forEach(System.out::println);
            }

            if(true) {
                return;
            }
                //.timeout(/Timeout)

        }

        if (false) {
            RandomAccessFile f = new RandomAccessFile("/home/raven/tmp/sorttest/dnb-all_lds_20200213.sorted.nt", "r");
            String line;
            while((line = f.readLine()) != null) {
                System.out.println(line);
            }
        }

        if (false) {
            PipeTransform pt = null;
            Path src = null;
            Path tgt = null;

            // This is an example of how the API is still bad:
            // the future.get() blocks the input stream creation and we have no way to cancel it
            // We want a way that immediately returns an object from which the input stream can be obtained once it is ready
            // Obviously, a Single<InputStream> would be much better
            InputStream in = pt.mapPathToPath()
                    .andThen(fc -> {
                        try {
                            return pt.mapPathToStream().apply(fc.future().get());
                        } catch (InterruptedException | ExecutionException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .apply(src, tgt);


        }

        if (true) {
            // This already looks a lot better.

            // But now: how can we delete intermediate files if we chain
            // non-streaming operations?
            // Do we need a close() method on a Pipeline object?
//            PipelineRx
//                .newPipeline()
//                .whatnow();

            //

            Path ntFile = Paths.get("/home/raven/tmp/sorttest/dnb-all_lds_20200213.sorted.nt");

            PipeTransformRx cat = PipeTransformRx.fromSysCallStreamToStream("/bin/cat");
            PipeTransformRx sort = PipeTransformRx.fromSysCallStreamToStream("/usr/bin/sort");
            PipeTransformRx filter = PipeTransformRx.fromSysCallStreamToStream("/bin/grep", "size");

            //InputStream tmp =
            Disposable disposable =
                    Single.just(ntFile)
                        .compose(cat.mapPathToStream())
                        .compose(sort.mapStreamToPath(Paths.get("/tmp/foo.bar")))
                        .compose(filter.mapPathToStream())
                        //.timeout(10, TimeUnit.SECONDS)
                        //.blockingGet();
                        .subscribe();

            disposable.dispose();
//            String line = new BufferedReader(new InputStreamReader(tmp)).readLine();
//            tmp.close();
//            System.out.println(line);

            //ByteStreams.copy(tmp, System.out);;

        }


        Path ntFile = Paths.get("/home/raven/Projects/Eclipse/blank-node-survey-parent/output.nt");
        if (false) {
            Function<InputStream, InputStream> xform = ProcessPipeUtils.createPipedTransformer((in, out) -> {
                Model model = ModelFactory.createDefaultModel();
                RDFDataMgr.read(model, in, Lang.NTRIPLES);
                RDFDataMgr.write(out, model, Lang.TURTLE);
            });
            // OutputStream out = new CloseShieldOutputStream(System.out);
            try(InputStream in = Files.newInputStream(ntFile, StandardOpenOption.READ)) {
                in.close();
                ByteStreams.copy(xform.apply(in), System.out);
            }
        }

        if (false) {
            Function<InputStream, InputStream> xform = ProcessPipeUtils
                    .mapStreamToStream(new String[] { "/usr/bin/sort" })
                    .asStreamTransform();
            try(InputStream in = Files.newInputStream(ntFile, StandardOpenOption.READ)) {
                InputStream in2 = xform.apply(in);

                String line = new BufferedReader(new InputStreamReader(in2)).readLine();
                in2.close();
                System.out.println(line);

                //ByteStreams.copy(in2, System.out);
            }
        }

        if (false) {
            Function<Path, InputStream> xform = ProcessPipeUtils
                    .mapPathToStream(path -> new String[] { "/bin/cat", path.toString() })
                    .asStreamSource();
            try(InputStream in = xform.apply(ntFile)) {
                String line = new BufferedReader(new InputStreamReader(in)).readLine();
                System.out.println(line);

                //ByteStreams.copy(in2, System.out);
            }
        }

        if (false) {
            BiFunction<Path, Path, FileCreation> xform = ProcessPipeUtils.mapPathToPath((src, tgt) -> new String[] { "/bin/cp", src.toString(), tgt.toString() });

//            FileCreation fc = xform.apply(Paths.get("/home/raven/tmp/sorttest/dnb-all_lds_20200213.sorted.nt"), Paths.get("/tmp/copy.nt"));
//            fc.abort();

            FileCreation fc = xform.apply(ntFile, Paths.get("/tmp/copy.nt"));
            Path tmp = fc.future().get();
            System.out.println("File creation done: " + tmp);
        }

    }
}
