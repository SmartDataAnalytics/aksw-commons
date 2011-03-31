package org.aksw.commons.experiments;

/**
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 */
public class Static {


    public enum Units {
        MILLISECONDS, SECONDS, COUNT, DOUBLE, PERCENTAGE
    }


    public static String getUnitString(Units unit){
        switch(unit){
            case MILLISECONDS:return "ms.";
            case SECONDS:return "sec.";
            case COUNT:return "count";
            case DOUBLE:return "double";
            case PERCENTAGE:return "%";
           default: return "hm?";

        }

    }
    

    enum Formats {
        LATEX, GNUPLOT
    }

    ;
}
