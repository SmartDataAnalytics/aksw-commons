package org.aksw.commons.path.core;

import java.util.List;

//class MyPath<T, P extends Path<T>, S> extends PathDelegateBase<T, P> implements PathSys<T, S> {
//
//    public MyPath(PathOpsSys<T, S> pathOps, Path<T> delegate) {
//        super(delegate);
//    }
//
//
//    protected S system;
//
//
////    public MyPath(PathOps<T, P> pathOps, boolean isAbsolute, List<T> segments) {
////        super(pathOps, isAbsolute, segments);
////        // TODO Auto-generated constructor stub
////    }
//
//
//    @Override
//    public S getSystem() {
//        return system;
//    }
//}
//
//public class PathOpsSys<S, T>
//    extends PathOpsWrapperBase<T, Path<T>>
//{
//    protected S system;
//
//    public PathOpsSys(PathOps<T, Path<T>> delegate, S system) {
//        super(delegate);
//        this.system = system;
//    }
//
//    public S getSystem() {
//        return system;
//    }
//
//    @Override
//    public Path<T> newPath(boolean isAbsolute, List<T> segments) {
//        Path<T> base = super.newPath(isAbsolute, segments);
//
//        // return new PathSysBase.of(this, base);
//        return null;
//    }
//
//    public static <S, T> PathOpsSys<S, T> pathOf(S system, Path<T> path) {
//        // path.ops
//        return null;
//    }
//
//}
