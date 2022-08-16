package org.aksw.commons.io.buffer.array;

import java.nio.ByteBuffer;

import org.aksw.commons.io.buffer.plain.Buffer;

/** */
public interface ArraySink<A> {
    void put(A input, int offset, int length);
    int remaining();
    int limit();


    public static class ArraySinkArray<A>
        implements ArraySink<A>
    {
        protected ArrayOps<A> arrayOps;
        protected A array;
        protected int offsetInArray;
        protected int limit;

        public ArraySinkArray(ArrayOps<A> arrayOps, A array, int offsetInArray, int limit) {
            super();
            this.arrayOps = arrayOps;
            this.array = array;
            this.offsetInArray = offsetInArray;
            this.limit = limit;
        }

        public A getArray() {
            return array;
        }

        @Override
        public void put(A input, int offset, int length) {
            arrayOps.copy(input, offset, array, offsetInArray, length);
            offsetInArray += length;
        }

        @Override
        public int remaining() {
            return limit() - offsetInArray;
        }

        @Override
        public int limit() {
            return limit;
        }
    }



    public static class ArraySinkBuffer<A>
        implements ArraySink<A>
    {
        protected Buffer<A> buffer;
        protected long offsetInBuffer;


        public ArraySinkBuffer(Buffer<A> buffer, int offsetInBuffer) {
            super();
            this.buffer = buffer;
            this.offsetInBuffer = offsetInBuffer;
        }

        public Buffer<A> getBuffer() {
            return buffer;
        }

        @Override
        public void put(A input, int offset, int length) {
//            try {
//                buffer.write(offsetInBuffer, buffer, offset, length);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
        }

        @Override
        public int remaining() {
            return (int)(buffer.getCapacity() - offsetInBuffer);
        }

        @Override
        public int limit() {
            return (int)buffer.getCapacity();
        }
    }


    public static class ArraySinkByteBuffer
        implements ArraySink<byte[]>
    {
        protected ByteBuffer byteBuffer;

        public ArraySinkByteBuffer(ByteBuffer byteBuffer) {
            super();
            this.byteBuffer = byteBuffer;
        }

        public ByteBuffer getByteBuffer() {
            return byteBuffer;
        }

        @Override
        public void put(byte[] input, int offset, int length) {
            byteBuffer.put(input, offset, length);
        }

        @Override
        public int remaining() {
            return byteBuffer.remaining();
        }

        @Override
        public int limit() {
            return byteBuffer.limit();
        }
    }

    public static ArraySink<byte[]> forByteBuffer(ByteBuffer byteBuffer) {
        return new ArraySinkByteBuffer(byteBuffer);
    }
}
