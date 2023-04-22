package org.geektimes.configuration.microprofile.config.source;

import javax.servlet.ServletContext;
import java.util.Enumeration;
import java.util.Map;

public class ServletContextConfigSource extends MapBasedConfigSource{

    private final ServletContext servletContext;

    public ServletContextConfigSource(ServletContext servletContext) {
        super("ServletContext init Parameters", 500);
        this.servletContext = servletContext;
    }

    @Override
    protected void prepareConfigData(Map configData) throws Throwable {
        Enumeration<String> parameterNames = servletContext.getInitParameterNames();
        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            configData.put(name, servletContext.getInitParameter(name));
        }
    }
}
