package org.aksw.commons.collections.diff;

/**
 * @author Claus Stadler
 *
 *         Date: 7/12/11
 *         Time: 10:50 PM
 */

public class Diff<T>
    implements IDiff<T>
{
    private T added;
    private T removed;
    private T retained;


    public Diff(T added, T removed, T retained)
    {
        this.added = added;
        this.removed = removed;
        this.retained = retained;
    }

    @Override
    public T getAdded()
    {
        return added;
    }

    @Override
    public T getRemoved()
    {
        return removed;
    }

    @Override
    public T getRetained()
    {
        return retained;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((added == null) ? 0 : added.hashCode());
        result = prime * result + ((removed == null) ? 0 : removed.hashCode());
        result = prime * result
                + ((retained == null) ? 0 : retained.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Diff other = (Diff) obj;
        if (added == null) {
            if (other.added != null)
                return false;
        } else if (!added.equals(other.added))
            return false;
        if (removed == null) {
            if (other.removed != null)
                return false;
        } else if (!removed.equals(other.removed))
            return false;
        if (retained == null) {
            if (other.retained != null)
                return false;
        } else if (!retained.equals(other.retained))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Diff [added=" + added + ", removed=" + removed + ", retained="
                + retained + "]";
    }
}
