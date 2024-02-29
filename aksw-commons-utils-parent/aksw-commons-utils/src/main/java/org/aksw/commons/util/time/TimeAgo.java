package org.aksw.commons.util.time;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/** Utility class to parse durations such as 1h5m (1 hour and 5 minutes) */
public class TimeAgo {

    public static final List<Long> times = Arrays.asList(
            TimeUnit.DAYS.toMillis(365),
            TimeUnit.DAYS.toMillis(30),
            TimeUnit.DAYS.toMillis(1),
            TimeUnit.HOURS.toMillis(1),
            TimeUnit.MINUTES.toMillis(1),
            TimeUnit.SECONDS.toMillis(1));

    public static final List<ChronoUnit> timesString = Arrays.asList(
            ChronoUnit.YEARS,
            ChronoUnit.MONTHS,
            ChronoUnit.DAYS,
            ChronoUnit.HOURS,
            ChronoUnit.MINUTES,
            ChronoUnit.SECONDS);

    public static final BiMap<String, ChronoUnit> suffixToUnit = HashBiMap.create();

    static {
        suffixToUnit.put("ns", ChronoUnit.NANOS);
        // suffixToUnit.put("mcs", ChronoUnit.MICROS);
        suffixToUnit.put("ms", ChronoUnit.MILLIS);
        suffixToUnit.put("s", ChronoUnit.SECONDS);
        suffixToUnit.put("m", ChronoUnit.MINUTES);
        suffixToUnit.put("h", ChronoUnit.HOURS);
        suffixToUnit.put("d", ChronoUnit.DAYS);
        suffixToUnit.put("M", ChronoUnit.MONTHS);
        suffixToUnit.put("y", ChronoUnit.YEARS);
    }

    public static final Pattern parsePattern = Pattern.compile("(\\d+(\\.\\d*)?)([^\\d]*)");

    public static Duration parse(String string) {
        Matcher matcher = parsePattern.matcher(string);
        Duration result = Duration.ZERO;
        while (matcher.find()) {
            String numberStr = matcher.group(1);
            String unitStr = matcher.group(3);

            Double number = Double.parseDouble(numberStr);
            ChronoUnit chronoUnit = unitStr.isBlank() ? ChronoUnit.SECONDS : suffixToUnit.get(unitStr);

            TimeUnit timeUnit = TimeUnit.of(chronoUnit);
            long factor = TimeUnit.NANOSECONDS.convert(1, timeUnit);
            long nanos = Math.multiplyExact(number.longValue(), factor);
            Duration contrib = Duration.of(nanos, ChronoUnit.NANOS);

            result = result == null ? contrib : result.plus(contrib);
        }
        return result;
    }

    public static String fmtLong(long value, ChronoUnit chronoUnit) {
        return (value == 0 ? "moments" : value) + " " + chronoUnit.toString().toLowerCase() + " ago";
    }

    public static String fmtCompact(long value, ChronoUnit chronoUnit) {
        String suffix = suffixToUnit.inverse().get(chronoUnit);
        if (suffix == null) {
            suffix = "(unknown time unit)";
        }

        return value == 0
                ? "now"
                : Long.toString(value) + suffix;
    }

    public static String toDuration(long duration, BiFunction<Long, ChronoUnit, String> formatter) {
        Entry<Long, ChronoUnit> pair = toDuration(duration);
        String result = formatter.apply(pair.getKey(), pair.getValue());
        return result;
    }

    public static Entry<Long, ChronoUnit> toDuration(long duration) {

        long value = 0;
        ChronoUnit unit = null;
        for (int i = 0; i < TimeAgo.times.size(); i++) {
            Long current = TimeAgo.times.get(i);
            long temp = duration / current;
            if (temp > 0) {
                value = temp;
                unit = timesString.get(i);
                break;
            }
        }
        if (unit == null) {
            unit = ChronoUnit.SECONDS;
        }


        return new SimpleEntry<>(value, unit);
    }

    public static void main(String args[]) {
        Locale.setDefault(Locale.GERMAN);

        System.out.println(parse("5m5m"));

        BiFunction<Long, ChronoUnit, String> labelizer = TimeAgo::fmtCompact;
        System.out.println(toDuration(123, labelizer));
        System.out.println(toDuration(1230, labelizer));
        System.out.println(toDuration(12300, labelizer));
        System.out.println(toDuration(123000, labelizer));
        System.out.println(toDuration(1230000, labelizer));
        System.out.println(toDuration(12300000, labelizer));
        System.out.println(toDuration(123000000, labelizer));
        System.out.println(toDuration(1230000000, labelizer));
        System.out.println(toDuration(12300000000L, labelizer));
        System.out.println(toDuration(123000000000L, labelizer));
    }
}
