package org.aksw.commons.path.json;

import java.util.List;
import java.util.Objects;

import org.aksw.commons.path.core.PathBase;
import org.aksw.commons.path.core.PathOps;
import org.aksw.commons.path.json.PathJson.Step;

import com.google.gson.JsonArray;


public class PathJson
    extends PathBase<Step, PathJson>
{
    private static final long serialVersionUID = 1L;

    public PathJson(PathOps<Step, PathJson> pathOps, boolean isAbsolute, List<Step> segments) {
        super(pathOps, isAbsolute, segments);
    }

    public static interface Step extends Comparable<Step> {
        default boolean isKey() { return false; }
        default boolean isIndex() { return false; }
        default String getKey() { throw new UnsupportedOperationException(); }
        default int getIndex() { throw new UnsupportedOperationException(); }

        // Hacky
        @Override default int compareTo(Step that) { return Objects.toString(this).compareTo(Objects.toString(that)); }

        public static StepIndex of(int index) { return new StepIndex(index); }
        public static StepKey of(String key) { return new StepKey(key); }

        // Hack - use a special Step subclass!
        public static StepKey self() { return new StepKey("---self---"); }
        public static StepKey parent() { return new StepKey("---parent---"); }
    }

    public static class StepKey
        implements Step
    {
        protected final String key;
        protected StepKey(String key) { super(); this.key = key; }
        @Override public boolean isKey() { return true; }
        @Override public String getKey() { return key; }
        @Override public String toString() { return key; }
        @Override
        public int hashCode() {
            return Objects.hash(key);
        }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StepKey other = (StepKey) obj;
            return Objects.equals(key, other.key);
        }
    }

    public static class StepIndex
        implements Step
    {
        protected final int index;
        protected StepIndex(int index) { super(); this.index = index; }
        @Override public boolean isIndex() { return true; }
        @Override public int getIndex() { return index; }
        @Override public int hashCode() { return Objects.hash(index); }
        @Override public String toString() { return Integer.toString(index); }
        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            StepIndex other = (StepIndex) obj;
            return index == other.index;
        }
    }

    /* Extra methods */
    public JsonArray toJsonArray() {
        return PathOpsJson.toJsonArray(this);
    }

    /* Static convenience shorthands */

    public static PathJson parse(String str) {
        return PathOpsJson.get().fromString(str);
    }

    public static PathJson newAbsolutePath(Step segment) {
        return PathOpsJson.get().newAbsolutePath(segment);
    }

    public static PathJson newAbsolutePath(Step ... segments) {
        return PathOpsJson.get().newAbsolutePath(segments);
    }

    public static PathJson newAbsolutePath(List<Step> segments) {
        return PathOpsJson.get().newAbsolutePath(segments);
    }

    public static PathJson newRelativePath(Step segment) {
        return PathOpsJson.get().newRelativePath(segment);
    }

    public static PathJson newRelativePath(Step ... segments) {
        return PathOpsJson.get().newRelativePath(segments);
    }

    public static PathJson newRelativePath(List<Step> segments) {
        return PathOpsJson.get().newRelativePath(segments);
    }
}
