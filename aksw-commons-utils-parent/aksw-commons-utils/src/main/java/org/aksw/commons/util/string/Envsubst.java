package org.aksw.commons.util.string;

import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A basic 'envsubst' implementation.
 * Just uses regex matching for substituting placeholders in a string.
 * Does not precompile templates - we may want to switch to a fully fledged template engine for that.
 *
 */
public class Envsubst {

    /** Regex for matching strings such as '${varname}'
     * variable names cannot include the '}' symbol
     */
    public static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\$(\\{(?<name1>[^}]*)\\}|(?<name2>[a-zA-Z_]\\w*))");

    /**
     * Perform substitution of placeholders of form "${name}"
     *
     * @param input A string in which to replace placeholder patterns
     * @param getReplacement Function that receives the variable name of a match and can return a substitution string.
     *        A result of null leaves the placeholder unchanged in order to allow for future substitution.
     * @return
     */
    public static String envsubst(
            String input,
            Function<String, String> getReplacement) {

        StringBuilder sb = new StringBuilder();
        Matcher m = PLACEHOLDER_PATTERN.matcher(input);
        while (m.find()) {

            String placeholderName = Optional.ofNullable(m.group("name1")).orElse(m.group("name2"));
            String replacement = getReplacement.apply(placeholderName);

            // Leave unmapped placeholders (they may get substituted in a separate pass)
            if (replacement == null) {
                m.appendReplacement(sb, "$0");
            } else {
                m.appendReplacement(sb, ""); // Force flush to the string builder
                sb.append(replacement);
            }
        }
        m.appendTail(sb);

        return sb.toString();
    }

}
