package org.aksw.commons.index.util;

public class Alt3<V1, V2, V3>
    implements Alt
{
    protected V1 v1;
    protected V2 v2;
    protected V3 v3;

    public Alt3(V1 v1, V2 v2, V3 v3) {
        super();
        this.v1 = v1;
        this.v2 = v2;
        this.v3 = v3;
    }

    public V1 getV1() {
        return v1;
    }

    public V2 getV2() {
        return v2;
    }

    public V3 getV3() {
        return v3;
    }

    @Override
    public Object get(int index) {
        Object result;
        switch (index) {
        case 0: result = v1; break;
        case 1: result = v2; break;
        case 2: result = v3; break;
        default: throw new IndexOutOfBoundsException();
        }
        return result;
    }

    @Override
    public int size() {
        return 3;
    }

    public static <V1, V2, V3> Alt3<V1, V2, V3> create(V1 v1, V2 v2, V3 v3) {
        return new Alt3<>(v1, v2, v3);
    }
}
