package org.aksw.commons.collections;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Set;

/**
 * Created by Claus Stadler
 * Date: Oct 19, 2010
 * Time: 11:36:20 PM
 */
/**
 * all.negatives may be null, however all other fields must be given!
 * 
 * @param <T>
 */
public class SampleStats<T>
{
    private static final DecimalFormat formatter = new DecimalFormat("0.######");


    private Sample<T> all;
    private Set<T> examples;

    private Sample<T> correctHits;
    private Sample<T> falseHits;

    private SampleStats(Sample<T> all, Set<T> examples)
    {
        this.all = all;
        this.examples = examples;

        correctHits = new Sample<T>(
                Sets.intersection(all.getPositives(), examples),
                all.getNegatives() == null ? null : Sets.difference(all.getNegatives(), examples));

        falseHits = new Sample<T>(Sets.difference(examples, all.getPositives()),
                                   Sets.difference(all.getPositives(), examples));



    }

    public static <T> SampleStats create(Sample<T> all, Set<T> examples)
    {
        return new SampleStats<T>(all, examples);
    }



    /**
     * Statistics for the pool
     * All positives that are not in the pool are removed from the examples
     */
    public static <T> SampleStats create(Sample<T> pool, Set<T> examples, Sample<T> all)
    {
        Set<T> deltaPositives = Sets.difference(all.getPositives(), pool.getPositives());
        Set<T> refinedExamples = Sets.difference(examples, deltaPositives);

        return new SampleStats<T>(pool, refinedExamples);
    }

    public static <T> SampleStats create(Set<T> allPositives, Set<T> allNegatives, Set<T> examples)
    {
        return new SampleStats<T>(new Sample<T>(allPositives, allNegatives), examples);
    }

    public Sample<T> getAll() {
        return all;
    }

    public Set<T> getExamples() {
        return examples;
    }

    public Sample<? extends T> getTrue() {
        return correctHits;
    }

    // Returns a sample with a view on the true negatives and positives
    public Sample<? extends T> getFalse() {
        return falseHits;
    }

    public double getPrecision()
    {
        if(getExamples().isEmpty())
            return 0.0;

        return getTrue().getPositives().size() / (double)getExamples().size();
    }

    public double getRecall()
    {
        if(getAll().getPositives().isEmpty())
            return 0.0;

        return getTrue().getPositives().size() / (double)getAll().getPositives().size();
    }

    public double getFMeasure()
    {
        return fMeasure(getPrecision(), getRecall());
    }

    /*
    public double getAccuracy()
    {
        return null;
    }*/


    public static double fMeasure(double precision, double recall)
    {
        double denominator = precision + recall;
        return (denominator == 0.0)
                ? 0.0
                : 2 * precision * recall / denominator;
    }

    public String toString()
    {
        return "Precision/Recall/FMeasure = " + Joiner.on("/").join(
                formatter.format(getPrecision()), formatter.format(getRecall()), formatter.format(getFMeasure())) + " --- " +
                "False Negatives " + toStringWithSize(getFalse().getNegatives());
    }

    public static String formatHumanReadable(SampleStats<?> stats) {

        DecimalFormat formatter = new DecimalFormat("0.##");

        return "Precision/Recall/FMeasure = " + Joiner.on("/").join(
                "" + formatter.format(stats.getPrecision() * 100.0) + "%",
                "" + formatter.format(stats.getRecall() * 100.0) + "%",
                "" + formatter.format(stats.getFMeasure() * 100.0) + "%") +
                " --- " +
                "False Negatives " + toStringWithSize(stats.getFalse().getNegatives());
    }

    public static String toStringWithSize(Collection<?> collection)
    {
        return collection == null ? "(null)" : "(" + collection.size() + ")" + collection;
    }
}
