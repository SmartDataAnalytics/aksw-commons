package org.aksw.commons.util.strings;

/**
 * @author Claus Stadler
 *         <p/>
 *         Date: 10/21/11
 *         Time: 2:43 PM
 */

import java.util.Comparator;

/**
 * Identifies sub-strings that correspond to integers and compares those parts
 * as integers. Therefore strings like a1, a10 a2 will be sorted as
 * a1, a2, a10 rather than a1, a10, a2
 *
 * @author raven
 *
 */
// TODO Rename to something like StringWithIntegerComparator
public class StringPrettyComparator
    implements Comparator<String>
{
    public static boolean isDigitPrefix(String s)
    {
        if(s.isEmpty())
            return false;

        boolean result = Character.isDigit(s.charAt(0));
        return result;
    }

    public static String getPrefix(String s, boolean digitMode)
    {
        if(s.isEmpty())
            return "";

        int i;
        for(i = 0; i < s.length(); ++i) {
            if(Character.isDigit(s.charAt(i)) != digitMode) {
                break;
            }
        }
        //while(Character.isDigit(s.charAt(i)) == digitMode && (i < s.length() - 1))
            //++i;

        String part = s.substring(0, i);

        return part;
    }



    public static boolean isDigitSuffix(String s)
    {
        if(s.isEmpty()) {
            return false;
        }

        boolean result = Character.isDigit(s.charAt(s.length() - 1));
        return result;
    }

    public static String getSuffix(String s, boolean digitMode)
    {
        if(s.isEmpty())
            return "";

        int i = s.length() - 1;

        for(; Character.isDigit(s.charAt(i)) == digitMode && i > 0; --i);

        String part = s.substring(i);

        return part;
    }


    public static void main(String[] args) {
        System.out.println(doCompare("yay10a", "yay2a"));
        System.out.println(doCompare("yay10a", "yay20b"));
        System.out.println(doCompare("yay10a", "yay10a"));
        System.out.println(doCompare("yay10a", "yay10"));
        System.out.println(doCompare("yay10", "yay10a"));
    }

    public static int doCompare(String a, String b) {
        int d;

        while(true) {
            if(a.isEmpty() && b.isEmpty()) {
                d = 0;
                break;
            }

            // Sort empty strings before non-empty ones
            int da = a.isEmpty() ? -1 : 0;
            int db = b.isEmpty() ? 1 : 0;

            d = db + da;
            if(d != 0) {
                break;
            }


            // Sort values before strings
            da = isDigitPrefix(a) ? -1 : 0;
            db = isDigitPrefix(b) ? 1 : 0;

            d = db + da;
            if(d != 0) {
                break;
            }

            String sa = getPrefix(a, da != 0);
            String sb = getPrefix(b, db != 0);

            d = (da != 0)
                ? ((Long)Long.parseLong(sa)).compareTo(Long.parseLong(sb))
                : sa.compareTo(sb);

            if(d != 0) {
                break;
            }

            a = a.substring(sa.length());
            b = b.substring(sb.length());
            //a = a.substring(0, a.length() - sa.length());
            //b = b.substring(0, b.length() - sb.length());
        }

        return d;
    }

    @Override
    public int compare(String a, String b)
    {
        int result = doCompare(a, b);
        return result;
    }
}
