package org.aksw.commons.collections;

/**
 * Created by IntelliJ IDEA.
 * User: raven
 * Date: 4/22/11
 * Time: 1:47 PM
 * To change this template use File | Settings | File Templates.
 */

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Claus Stadler
 * Date: Oct 12, 2010
 * Time: 5:51:59 PM
 *
 * A pair of sets of positives and negative examples.
 * This class is intended to be used e.g. for training sets, test sets, in information retrieval / machine learning scenarios and such.
 * But also for keeping track of the sets of constant values a variable in e.g. jena expression may have.
 *
 */
public class Sample<T>
{

    public static <T> Sample<T> create()
    {
        return new Sample<T>(new HashSet<T>(), new HashSet<T>());
    }

    public static <T> Sample<T> createCopy(Sample<T> other)
    {
        return createCopy(other.getPositives(), other.getNegatives());
    }

    public static <T> Sample<T> createCopy(Collection<T> positives, Collection<T> negatives)
    {
        return new Sample<T>(new HashSet<T>(positives), negatives == null ? null : new HashSet<T>(negatives));
    }

    public static <T> Sample<T> create(Sample<T> other)
    {
        return create(other.getPositives(), other.getNegatives());
    }

    public static <T> Sample<T> create(Set<T> positives, Set<T> negatives)
    {
        return new Sample<T>(positives, negatives);
    }


    private Set<T> positives;
    private Set<T> negatives;

    public Sample(Set<T> positives, Set<T> negatives) {
        this.positives = positives;
        this.negatives = negatives;
    }

    public Set<T> getPositives() {
        return positives;
    }

    public Set<T> getNegatives() {
        return negatives;
    }

    public void addAll(Sample<T> other)
    {
        addAll(other.getPositives(), other.getNegatives());
    }

    public void addAll(Collection<? extends T> positives, Collection<? extends T> negatives)
    {
        this.positives.addAll(positives);
        this.negatives.addAll(negatives);
    }

    public void clear()
    {
        positives.clear();
        negatives.clear();
    }

    public boolean isEmpty()
    {
        return positives.isEmpty() && negatives.isEmpty();
    }

    public String toString()
    {
        return "Positives: " + positives + "\nNegatives: " + negatives;
    }
}
