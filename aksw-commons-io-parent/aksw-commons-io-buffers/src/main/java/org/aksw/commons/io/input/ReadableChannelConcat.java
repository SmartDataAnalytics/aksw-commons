package org.aksw.commons.io.input;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.aksw.commons.io.buffer.array.ArrayOps;
import org.aksw.commons.util.exception.FinallyRunAll;

public class ReadableChannelConcat<A>
    extends ReadableChannelBase<A>
    implements ReadableChannel<A>
{
    protected ArrayOps<A> arrayOps;
    protected List<ReadableChannel<A>> members;
    protected Iterator<ReadableChannel<A>> it;
    protected ReadableChannel<A> current = null;


    public ReadableChannelConcat(ArrayOps<A> arrayOps, List<ReadableChannel<A>> members) {
        super();
        this.arrayOps = arrayOps;
        this.members = members;
        this.it = members.iterator();
    }

    @Override
    public ArrayOps<A> getArrayOps() {
        return arrayOps;
    }

    @Override
    public void closeActual() {
        FinallyRunAll fra = FinallyRunAll.create();
        for (ReadableChannel<A> member : members) {
            fra.addThrowing(member::close);
        }
        fra.addThrowing(super::closeActual);
        fra.run();
    }

    @Override
    public int read(A array, int position, int length) throws IOException {
        int n = -1;
        while (true) {
            if (current == null) {
                if (it.hasNext()) {
                    current = it.next();
                } else {
                    break;
                }
            }
            if (current != null) {
                n = current.read(array, position, length);
                if (n < 0) {
                    current = null;
                } else {
                    break;
                }
            }
        }
        return n;
    }

}
