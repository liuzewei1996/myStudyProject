package org.geektimes.projects.user.web.listener;

import org.geektimes.projects.user.context.ComponentContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

/**
 * {@link ComponentContext} 初始化器
 * ContextLoaderListener
 */
public class ComponentContextInitializerListener implements ServletContextListener {

    private Logger logger = Logger.getLogger(this.getClass().getName());

    private ServletContext servletContext;


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        this.servletContext = sce.getServletContext();
        ComponentContext componentContext = new ComponentContext();
        componentContext.initalize(servletContext);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        //todo
        System.out.println("ComponentContextInitializerListener contextDestroyed ");
//        ComponentContext context = new ComponentContext();
//        context.destroy();

    }
}
