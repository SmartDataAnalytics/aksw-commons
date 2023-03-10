package org.aksw.commons.algebra.allen;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AllenConstants {

    public final static short BEFORE       =    1; // 0000000000000001
    public final static short AFTER        =    2; // 0000000000000010
    public final static short DURING       =    4; // 0000000000000100
    public final static short CONTAINS     =    8; // 0000000000001000
    public final static short OVERLAPS     =   16; // 0000000000010000
    public final static short OVERLAPPEDBY =   32; // 0000000000100000
    public final static short MEETS        =   64; // 0000000001000000
    public final static short METBY        =  128; // 0000000010000000
    public final static short STARTS       =  256; // 0000000100000000
    public final static short STARTEDBY    =  512; // 0000001000000000
    public final static short FINISHES     = 1024; // 0000010000000000
    public final static short FINISHEDBY   = 2048; // 0000100000000000
    public final static short EQUALS       = 4096; // 0001000000000000
                                                   // 0001111111111111
    public final static short ALL          = (short) (BEFORE | AFTER | DURING | CONTAINS | OVERLAPS | OVERLAPPEDBY | MEETS | METBY | STARTS | STARTEDBY | FINISHES | FINISHEDBY | EQUALS);

    public static final Map<Short, String> DFT_LABELS;

    static {
        DFT_LABELS = new HashMap<>();
        DFT_LABELS.put(BEFORE,       "before");
        DFT_LABELS.put(AFTER,        "after");
        DFT_LABELS.put(DURING,       "during");
        DFT_LABELS.put(CONTAINS,     "contains");
        DFT_LABELS.put(OVERLAPS,     "overlaps");
        DFT_LABELS.put(OVERLAPPEDBY, "overlapped by");
        DFT_LABELS.put(MEETS,        "meets");
        DFT_LABELS.put(METBY,        "met by");
        DFT_LABELS.put(STARTS,       "starts");
        DFT_LABELS.put(STARTEDBY,    "started by");
        DFT_LABELS.put(FINISHES,     "finishes");
        DFT_LABELS.put(FINISHEDBY,   "finished by");
        DFT_LABELS.put(EQUALS,       "equals");
    }

    public static short invert(short c) {
        short result = 0;
        // We could do a for loop over pairs of bits and swap them - but not sure how
        // stable the bit patterns are right now - so doing it explicitly for now
        if ((c & BEFORE)       != 0) result |= AFTER;
        if ((c & AFTER)        != 0) result |= BEFORE;

        if ((c & DURING)       != 0) result |= CONTAINS;
        if ((c & CONTAINS)     != 0) result |= DURING;

        if ((c & OVERLAPS)     != 0) result |= OVERLAPPEDBY;
        if ((c & OVERLAPPEDBY) != 0) result |= OVERLAPS;

        if ((c & MEETS)        != 0) result |= METBY;
        if ((c & METBY)        != 0) result |= MEETS;

        if ((c & STARTS)       != 0) result |= STARTEDBY;
        if ((c & STARTEDBY)    != 0) result |= STARTS;

        if ((c & FINISHES)     != 0) result |= FINISHEDBY;
        if ((c & FINISHEDBY)   != 0) result |= FINISHES;

        if ((c & EQUALS)       != 0) result |= EQUALS;
        return result;
    }

    public static List<String> toLabels(short pattern, Map<Short, String> labelMap) {
        List<String> labels = IntStream.range(0, 13).map(i -> 1 << i)
                .map(mask -> pattern & mask)
                .filter(key -> key != 0)
                .mapToObj(key -> labelMap.get((short)key))
                .collect(Collectors.toList());
        return labels;
    }

    public static List<String> toLabels(short pattern) {
        return toLabels(pattern, DFT_LABELS);
    }

    public static String toString(short pattern) {
        return Objects.toString(toLabels(pattern));
    }

    public final static short[][] transitivityMatrix = {
            // first row before
            {BEFORE,ALL,BEFORE | OVERLAPS | MEETS | DURING | STARTS, BEFORE, BEFORE, BEFORE | OVERLAPS | MEETS | DURING | STARTS, BEFORE, BEFORE | OVERLAPS | MEETS | DURING | STARTS,  BEFORE, BEFORE, BEFORE | OVERLAPS | MEETS | DURING | STARTS, BEFORE, BEFORE},
        //	{"<","< > d di o oi m mi s si f fi e","< o m d s","<","<","< o m d s","<","< o m d s","<","<","< o m d s","<","<"}

            // second row after
            {ALL,AFTER,AFTER | OVERLAPPEDBY | METBY | DURING | FINISHES, AFTER, AFTER | OVERLAPPEDBY | METBY | DURING | FINISHES,AFTER,AFTER | OVERLAPPEDBY | METBY | DURING | FINISHES,AFTER,AFTER | OVERLAPPEDBY | METBY | DURING | FINISHES,AFTER,AFTER,AFTER,AFTER },
        //	{"< > d di o oi m mi s si f fi e",">","> oi mi d f", ">", "> oi mi d f", ">", "> oi mi d f", ">", "> oi mi d f", ">", ">", ">", ">"}
            // third row during
            {BEFORE, AFTER,DURING,ALL,BEFORE | OVERLAPS | MEETS | DURING | STARTS, AFTER | OVERLAPPEDBY | METBY | DURING | FINISHES, BEFORE, AFTER, DURING, AFTER | OVERLAPPEDBY | METBY | DURING | FINISHES, DURING, BEFORE | OVERLAPS | MEETS | DURING | STARTS, DURING},
//    		{"<",">","d", "< > d di o oi m mi s si f fi e", "< o m d s", "> oi mi d f", "<", ">", "d", "> oi mi d f", "d", "< o m d s","d"},
            // fourth row contains
            {BEFORE | OVERLAPS | MEETS | CONTAINS | FINISHEDBY, AFTER | OVERLAPPEDBY | CONTAINS | METBY | STARTEDBY, OVERLAPS | OVERLAPPEDBY | DURING | CONTAINS | EQUALS | STARTS | STARTEDBY | FINISHES | FINISHEDBY, CONTAINS, OVERLAPS | CONTAINS | FINISHEDBY,OVERLAPPEDBY | CONTAINS | STARTEDBY,OVERLAPS | CONTAINS | FINISHEDBY,OVERLAPPEDBY | CONTAINS | STARTEDBY,CONTAINS | FINISHEDBY | OVERLAPS, CONTAINS, CONTAINS | STARTEDBY | OVERLAPPEDBY, CONTAINS,CONTAINS},
//    		{"< o m di fi","> oi mi di si","o oi d di e s si f fi","di","o di fi","oi di si","o di fi","oi di si","di fi o","di","di si oi","di","di"}
            // fifth row overlaps
            {BEFORE,AFTER | OVERLAPPEDBY | CONTAINS | METBY | STARTEDBY,OVERLAPS | DURING | STARTS, BEFORE | OVERLAPS | MEETS | CONTAINS | FINISHEDBY, BEFORE | OVERLAPS | MEETS, OVERLAPS | OVERLAPPEDBY | DURING | CONTAINS | EQUALS | STARTS | STARTEDBY | FINISHES | FINISHEDBY, BEFORE, OVERLAPPEDBY | CONTAINS | STARTEDBY, OVERLAPS, CONTAINS | FINISHEDBY | OVERLAPS, DURING | STARTS | OVERLAPS, BEFORE | OVERLAPS | MEETS, OVERLAPS},
//    		{"<","> oi di mi si","o d s","< o m di fi","< o m", "o oi d di e s si f fi","<","oi di si","o","di fi o","d s o","< o m","o"},
            // six row overlapped by
            {BEFORE | OVERLAPS | MEETS | CONTAINS | FINISHEDBY, AFTER, OVERLAPPEDBY | DURING | FINISHES, AFTER | OVERLAPPEDBY | METBY | CONTAINS | STARTEDBY, OVERLAPS | OVERLAPPEDBY | DURING | CONTAINS | EQUALS | STARTS | STARTEDBY | FINISHES | FINISHEDBY, AFTER | OVERLAPPEDBY | METBY, OVERLAPS | CONTAINS | FINISHEDBY, AFTER, OVERLAPPEDBY | DURING | FINISHES, OVERLAPPEDBY | AFTER | METBY, OVERLAPPEDBY, OVERLAPPEDBY | CONTAINS | STARTEDBY, OVERLAPPEDBY},
//    		{"< o m di fi",">","oi d f","> oi mi di si","o oi d di e s si f fi","> oi mi","o di fi",">","oi d f","oi > mi","oi","oi di si","oi"},
            // seventh row meets
            {BEFORE, AFTER | OVERLAPPEDBY | METBY | CONTAINS | STARTEDBY, OVERLAPS | DURING | STARTS, BEFORE, BEFORE, OVERLAPS | DURING | STARTS, BEFORE, FINISHES | FINISHEDBY | EQUALS, MEETS, MEETS, DURING | STARTS | OVERLAPS, BEFORE, MEETS},
//    		{"<","> oi mi di si","o d s","<","<","o d s","<","f fi e","m","m","d s o","<","m"},
            // eights row metby
            {BEFORE | OVERLAPS | MEETS | CONTAINS | FINISHEDBY, AFTER, OVERLAPPEDBY | DURING | FINISHES, AFTER,OVERLAPPEDBY | DURING | FINISHES, AFTER, STARTS | STARTEDBY | EQUALS, AFTER, DURING | FINISHES | OVERLAPPEDBY, AFTER, METBY, METBY, METBY},
//    		{"< o m di fi",">","oi d f",">","oi d f",">","s si e",">","d f oiX",">","mi","mi","mi"},
            // ninth row starts
            {BEFORE, AFTER, DURING, BEFORE | OVERLAPS | MEETS | CONTAINS | FINISHEDBY,BEFORE | OVERLAPS | MEETS, OVERLAPPEDBY | DURING | FINISHES, BEFORE, METBY, STARTS, STARTS | STARTEDBY | EQUALS, DURING, BEFORE | MEETS | OVERLAPS, STARTS},
//    		{"<",">","d","< o m di fi","< o m","oi d f","<","mi","s","s si e","d","< m o","s"},
            // tenth row startedby
            {BEFORE | OVERLAPS | MEETS | CONTAINS | FINISHEDBY, AFTER, OVERLAPPEDBY | DURING | FINISHES, CONTAINS, OVERLAPS | CONTAINS | FINISHEDBY, OVERLAPPEDBY, OVERLAPS | CONTAINS | FINISHEDBY, METBY, STARTS | STARTEDBY | EQUALS, STARTEDBY, OVERLAPPEDBY, CONTAINS, STARTEDBY},
//    		{"< o m di fi",">","oi d f","di","o di fi","oi","o di fi","mi","s si eX","si","oi","di","si"},
            // eleventh row finishes
            {BEFORE, AFTER, DURING, AFTER | OVERLAPPEDBY | METBY | CONTAINS | STARTEDBY,OVERLAPS | DURING | STARTS, AFTER | OVERLAPPEDBY | METBY , MEETS, AFTER, DURING, AFTER | OVERLAPPEDBY | METBY, FINISHES, FINISHES | FINISHEDBY | EQUALS, FINISHES},
//    		{"<",">","d","> oi mi di si","o d s","> oi mi di","m",">","d","> oi mX","f","f fi e","f"},
            // twelfth row finishedby
            {BEFORE, AFTER | OVERLAPPEDBY | METBY | CONTAINS | STARTEDBY, OVERLAPS | DURING | STARTS, CONTAINS, OVERLAPS, OVERLAPPEDBY | CONTAINS | STARTEDBY, MEETS, STARTEDBY | OVERLAPPEDBY | CONTAINS, OVERLAPS, CONTAINS, FINISHES | FINISHEDBY | EQUALS, FINISHEDBY, FINISHEDBY},
//    		{"<","> oi mi di si","o d s","di","o","oi di si","m","si oi di","o","di","f fi eX","fi","fi"},
            // thirteenth row equals
            {BEFORE,AFTER,DURING,CONTAINS,OVERLAPS,OVERLAPPEDBY,MEETS,METBY,STARTS,STARTEDBY,FINISHES,FINISHEDBY,EQUALS},
//    		{"<",">","d","di","o","oi","m","mi","s","si","f","fi","e"}

        };
}
