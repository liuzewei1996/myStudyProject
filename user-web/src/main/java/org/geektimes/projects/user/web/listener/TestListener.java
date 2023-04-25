package org.geektimes.projects.user.web.listener;

import org.geektimes.projects.user.context.ComponentContext;
import org.geektimes.projects.user.domain.User;
import org.geektimes.projects.user.sql.DBConnectionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.logging.Logger;

public class TestListener implements ServletContextListener {

    private Logger logger = Logger.getLogger(this.getClass().getName());


    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ComponentContext context = ComponentContext.getInstance();
        DBConnectionManager dbConnectionManager = context.getComponent("bean/DBConnectionManager");
        dbConnectionManager.getConnection();
        testUser(dbConnectionManager.getEntityManager());
        logger.info("所有的 JNDI 组件名称：[");
        context.getComponentNames().forEach(logger::info);
        logger.info("]");
        System.out.println("context initialized");
    }

    private void testUser(EntityManager entityManager){
        User user = new User();
        user.setName("lzw");
        user.setPassword("324234232");
        user.setEmail("lzw@gmail.com");
        user.setPhoneNumber("123456789");
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        entityManager.persist( user );
        tx.commit();
        System.out.println(entityManager.find(User.class,user.getId()));
        logger.info(entityManager.find(User.class,user.getId()).toString());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("contextDestroyed");
        System.out.println("contextDestroyed");
    }

}
