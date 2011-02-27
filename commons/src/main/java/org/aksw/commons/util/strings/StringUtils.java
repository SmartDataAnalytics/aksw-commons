package org.aksw.commons.util.strings;

import com.hp.hpl.jena.sparql.pfunction.library.str;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

public class StringUtils
{
    /**
     * Removes prefixes that are also suffixes from a given string
     * e.g. strip('Hi', ') -> Hi
     *
     * @param str
     * @param chars
     * @return
     */
	public static String strip(String str, String ... chars)
	{
		for(String c : chars) {
			if(str.length() < 2)
				return str;

			if(str.startsWith(c) && str.endsWith(c))
				str = str.substring(1, str.length() - 1);
		}

		return str;
	}


    /**
     * Returns first non-null argument
     *
     * @param args
     * @param <T>
     * @return
     */
	public static <T> T coalesce(T ...args)
	{
		for(T arg : args) {
			if(arg != null) {
				return arg;
			}
		}

		return null;
	}

	public static String ucFirst(String str)
	{
		return str.isEmpty()
			? ""
			: str.substring(0,1).toUpperCase() + str.substring(1);
	}

	public static String lcFirst(String str)
	{
		return str.isEmpty()
			? ""
			: str.substring(0,1).toLowerCase() + str.substring(1);
	}


	public static String toLowerCamelCase(String s)
	{
		int offset = 0;
		String result = "";
		for(;;) {
			int newOffset = s.indexOf('_', offset);
			if(newOffset == -1) {
				result += ucFirst(s.substring(offset));
				break;
			}

			result += ucFirst(s.substring(offset + 1));
			offset = newOffset;
		}

		return result;
	}

    /**
     * Cuts a string after nMax bytes - unless the remaining bytes are less
     * than tolerance.
     * In case of a cut appends "... (# more bytes)".
     * (# cannot be less than tolerance)
     *
     * @param str
     * @param nMax
     * @param nTolerance
     * @return
     */
    public static String cropString(String str, int nMax, int nTolerance)
    {
        String result = str;
        int nGiven = str.length();

        if(nGiven > nMax) {
            int tooMany = nGiven - nMax;

            if(tooMany > nTolerance)
                result = str.substring(0, nMax) +
                    "... (" + tooMany + " more bytes)";
        }
        return result;
    }


    /**
     * Returns the common prefix of the given strings
     *
     * @return
     */
    public static String commonPrefix(String sa, String sb)
    {
        char[] a = sa.toCharArray();
        char[] b = sb.toCharArray();
        int n = Math.min(a.length, b.length);
        String result = "";

        for(int i = 0; i < n; i++) {
            if(a[i] != b[i]) {
                break;
            }

            result += a[i];
        }

        return result;
    }


    /**
     * Looks up an element in the given map that is the longest prefix of the given lookup key.
     * 
     * @param lookup
     * @param prefixMap
     * @return
     */
    public static <T> Map.Entry<String, T> longestPrefixLookup(String lookup, NavigableMap<String, T> prefixMap)
    {
        while(true) {
            NavigableMap<String, T> candidates = prefixMap.headMap(lookup, true).descendingMap();
            Map.Entry<String, T> candidate = candidates.firstEntry();

            if(candidate == null) {
                return null;
            }

            String key = candidate.getKey();

            if(key == lookup || lookup.equals(key)) {
                return candidate;
            }

            lookup = StringUtils.commonPrefix(lookup, key);
        }
    }

    /**
     * Helper functions to get rid of that exception.
     * Afaik UTF8 en/de-coding cannot fail (read it somewhere, not confirmed)
     *
     * @param str
     * @return
     */
    public static String encodeUtf8(String str)
    {
        try {
            return URLEncoder.encode(str, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decodeUtf8(String str)
    {
        try {
            return URLDecoder.decode(str, "UTF8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /*
    public static void main(String[] args) {
        NavigableMap<String, String> m = new TreeMap<String, String>();
        m.put("m", "2");
        m.put("malta", "3");
        m.put("mali", "4");
        m.put("malibu", "5");
        m.put("macedonien", "6");

        System.out.println(longestPrefixLookup("malibuu", m));
        System.out.println(longestPrefixLookup("malibu", m));
        System.out.println(longestPrefixLookup("malib", m));
        System.out.println(longestPrefixLookup("mali", m));
        System.out.println(longestPrefixLookup("mal", m));
        System.out.println(longestPrefixLookup("ma", m));
        System.out.println(longestPrefixLookup("m", m));
        System.out.println(longestPrefixLookup("", m));
    }
    */
}
