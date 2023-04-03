package org.aksw.commons.path.nio;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.util.Iterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.StreamSupport;

public class DirectoryStreams {

    public static <I, O> DirectoryStream<O> map(DirectoryStream<I> stream, Function<? super I, O> mapper) {
        return new DirectoryStreamMap<>(stream, mapper);
    }

    public static <T> DirectoryStream<T> filter(DirectoryStream<T> stream, Filter<? super T> filter) {
        return new DirectoryStreamFilter<>(stream, filter);
    }

    private static class DirectoryStreamMap<I, O>
        implements DirectoryStream<O>
    {
        protected DirectoryStream<I> base;
        protected Function<? super I, O> mapper;

        public DirectoryStreamMap(DirectoryStream<I> base, Function<? super I, O> mapper) {
            super();
            this.base = base;
            this.mapper = mapper;
        }

        @Override
        public Iterator<O> iterator() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(base.iterator(), 0), false)
                .map(mapper)
                .iterator();
        }

        @Override
        public void close() throws IOException {
            base.close();
        }
    };

    private static class DirectoryStreamFilter<T>
        implements DirectoryStream<T>
    {
        protected DirectoryStream<T> base;
        protected Filter<? super T> filter;

        public DirectoryStreamFilter(DirectoryStream<T> base, Filter<? super T> filter) {
            super();
            this.base = base;
            this.filter = filter;
        }

        @Override
        public Iterator<T> iterator() {
            return StreamSupport.stream(Spliterators.spliteratorUnknownSize(base.iterator(), 0), false)
                .filter(path -> {
                    boolean r;
                    try {
                        r = filter.accept(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    return r;
                })
                .iterator();
        }

        @Override
        public void close() throws IOException {
            base.close();
        }
    };
}
