package org.geektimes.projects.user.web.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class TestListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("contextDestroyed");
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("context initialized");
    }
}
