package org.aksw.commons.io.util;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.aksw.commons.util.strings.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

public class UriToPathUtils {

	public static String[] toPathSegments(URI uri) {
		List<String> parts = new ArrayList<>();
		
		String host = uri.getHost();
		if (host != null) {
			parts.addAll(Arrays.asList(javafyHostnameSegments(host)));
		}
		
		int port = uri.getPort();
		if (port != -1) {
			parts.add(Integer.toString(port));
		}
		
		String path = uri.getPath();
		if (path != null) {
			// Replace ~ (tilde) with _ because otherwise jena IRI validation will fail
			// on file:// urls with SCHEME_PATTERN_MATCH_FAILED
			// Tilde is common symbol with e.g. the Apache Web server's userdir mod
			String tmp = path.replaceAll("~", "_");
			parts.addAll(Arrays.asList(PathUtils.splitBySlash(tmp)));
		}

		String query = uri.getQuery();
		if (query != null) {
			parts.addAll(Arrays.asList(PathUtils.splitBySlash(query)));
		}

		String[] result = parts.toArray(new String[0]);		return result;
		
	}
	
	public static String[] toPathSegments(String uri) {
		URI u = UriUtils.newURI(uri);
		String[] result = toPathSegments(u);
		return result;
	}

	
	/**
	 * Convert a host name to a java package name.
	 * Splits the given argument string by '.', then reverses the order and joins the strings using '/'.  
	 * 
	 * For example, subdomain.example.org becomes org/example/subdomain
	 * 
	 * @param hostName
	 * @return
	 */
	public static String[] javafyHostnameSegments(String hostName) {
		String[] result = hostName.split("\\.+"); // Treat consecutive dots as one 
		ArrayUtils.reverse(result);
		return result;
	}

	
	public static String javafyHostname(String hostName) {
		String[] parts = javafyHostnameSegments(hostName);
		String result = Arrays.asList(parts).stream().collect(Collectors.joining("/"));
		return result;
	}
	
	
	
	/**
	 * Default mapping of URIs to relative paths
	 * The scheme is omitted.
	 * 
	 * scheme://host:port/path?query becomes
	 * host/port/path/query
	 * 
	 * @param uri
	 * @return
	 */
	@Deprecated // Use toPathSegments
	public static Path resolvePath(URI uri) {
		String a = Optional.ofNullable(uri.getHost()).map(UriToPathUtils::javafyHostname).orElse("");
		String b = uri.getPort() == -1 ? "" : Integer.toString(uri.getPort());
		
		// Replace ~ (tilde) with _ because otherwise jena IRI validation will fail
		// on file:// urls with SCHEME_PATTERN_MATCH_FAILED
		// Tilde is common symbol with e.g. the Apache Web server's userdir mod
		String pathStr =  Optional.ofNullable(uri.getPath()).orElse("")
				.replaceAll("~", "_");
		
		Path result = Paths.get(".")
		.resolve(a)
		.resolve(b)
		.resolve((a.isEmpty() && b.isEmpty() ? "" : ".") + pathStr)
		.resolve(Optional.ofNullable(uri.getQuery()).orElse(""))
		.normalize();
		
		return result;
	}
	
		
	/**
	 * Attempt to parse the argument as a URI and convert it to a {@link Path}.
	 * If parsing as URI fails for any reason then the result is the URL encoded argument instead.
	 * 
	 * @param uri
	 * @return
	 */
	@Deprecated // Use toPathSegments
	public static Path resolvePath(String uri)  {
		URI u = UriUtils.newURI(uri);
		
		Path tmp = u == null ?
			Paths.get(StringUtils.urlEncode(uri))
			: UriToPathUtils.resolvePath(u);
			
		// Make absolute paths relative (i.e. remove leading slashes)
		Path result;
		if(tmp.isAbsolute()) {
			Path root = tmp.getRoot();
			result = root.relativize(tmp);
		} else {
			result = tmp;
		}
			
		//logger.info("Resolved: " + uri + "\n  to: " + result + "\n  via: " + u);
		return result;
	}
}
