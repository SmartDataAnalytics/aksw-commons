package org.aksw.commons.util;

import org.aksw.commons.util.strings.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Loads all key-value pairs from a given file.
 *
 * The "source" keyword can be used for inclusion. (So this is somewhat simlar to bash)
 *
 *
 *
 * @author Claus Stadler
 *         <p/>
 *         Date: 12/2/11
 *         Time: 5:05 PM
 */
public class IniUtils {
    	public static Map<String, String> loadIniFile(File file) throws IOException
	{
		Map<String, String> config = new HashMap<String, String>();

		loadIniFile(file, config);

		return config;
	}

	public static void loadIniFile(File file, Map<String, String> out)
			throws IOException
	{
		loadIniFile(new BufferedReader(new FileReader(file)), out);
	}

	public static void loadIniFile(BufferedReader reader,
			Map<String, String> out) throws IOException
	{
		String SOURCE = "source";
		Pattern pattern = Pattern.compile("\\s*([^=]*)\\s*=\\s*(.*)\\s*");

		String line;
		List<String> loadFileNames = new ArrayList<String>();

		String tmp = "";

		while ((line = reader.readLine()) != null) {
			line.trim();
			if (line.startsWith(SOURCE)) {
				String fileName = line.substring(SOURCE.length()).trim();

				loadFileNames.add(fileName);

			} else {
				Matcher m = pattern.matcher(line);
				if (m.find()) {
					String key = m.group(1);
					String value = m.group(2);

                    value = value.trim();

                    if(value.startsWith("\"")) {
					    value = StringUtils.strip(value, "\"").trim();
                    } else if(value.startsWith("'")) {
                        value = StringUtils.strip(value, "'").trim();
                    }

					out.put(key, value);
				}
			}
		}

		//System.out.println(tmp);
		//System.out.println(loadFileNames);

		for (String loadFileName : loadFileNames) {
			File file = new File(loadFileName);
			loadIniFile(file, out);
		}
	}
}
