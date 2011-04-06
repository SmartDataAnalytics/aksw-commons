package org.aksw.commons.sparql.core;


import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.slf4j.LoggerFactory;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Sebastian Hellmann - http://bis.informatik.uni-leipzig.de/SebastianHellmann
 * Date: 04.04.11
 */

public class SparqlTemplate {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(SparqlTemplate.class);

    private static VelocityEngine ve = null;

    public static VelocityEngine getClassPathEngine() {
        if (ve == null) {
            try {
                ve = new VelocityEngine();
                ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
                ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
                ve.init();
            } catch (Exception e) {
                logger.error("", e);
            }
        }
        return ve;
    }

    public static SparqlTemplate getInstance(String classpathFile) throws Exception {
        return new SparqlTemplate(getClassPathEngine().getTemplate(classpathFile));
    }


    private Template template;
    private VelocityContext velocityContext = new VelocityContext();
    //TODO this needs to be implemented better
    private boolean usePrefixes = true;
    private int limit = 1000;
    private Set<String> from = new HashSet<String>();
    private Set<String> filter = new HashSet<String>();

    public SparqlTemplate(Template template) {
        this.template = template;
    }


    public void addFrom(String s) {
        from.add(s);
    }

    public void addFrom(Collection<String> c) {
        from.addAll(c);
    }

    public void addFilter(String s) {
        filter.add(s);
    }

    public void addFilter(Collection<String> c) {
        filter.addAll(c);
    }


    public VelocityContext getVelocityContext() {
        return velocityContext;
    }


    public boolean isUsePrefixes() {
        return usePrefixes;
    }

    public void setUsePrefixes(boolean usePrefixes) {
        this.usePrefixes = usePrefixes;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public String getQuery() {
        velocityContext.put("limit", limit);

        if (!from.isEmpty()) {
            velocityContext.put("fromList", new ArrayList<String>(from));
        }
        if (!filter.isEmpty()) {
            velocityContext.put("filterList", new ArrayList<String>(filter));
        }

        if (usePrefixes) {
            velocityContext.put("prefix", usePrefixes);
        }

        StringWriter writer = new StringWriter();
        try {
            template.merge(velocityContext, writer);
        } catch (Exception e) {
            logger.error("", e);
        }
        return writer.toString();
    }

}

