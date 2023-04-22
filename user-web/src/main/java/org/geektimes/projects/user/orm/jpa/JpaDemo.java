package org.geektimes.projects.user.orm.jpa;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.geektimes.projects.user.domain.User;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

public class JpaDemo {


    @PersistenceContext(name = "emf")
    private EntityManager entityManager;

    @Resource(name = "primaryDataSource")
    private DataSource dataSource;



    public static void main(String[] args) {
        EntityManagerFactory entityManagerFactory = Persistence
                .createEntityManagerFactory("emf",getProperties());
        EntityManager entityManager =   entityManagerFactory.createEntityManager();
        User user = new User();
        user.setName("lzw");
        user.setPassword("******");
        user.setEmail("lzw@gmail.com");
        user.setPhoneNumber("123456789");
        EntityTransaction tx = entityManager.getTransaction();
        tx.begin();
        entityManager.persist( user );
        System.out.println(entityManager.find(User.class,1L));
        tx.commit();
    }

    private static Map<Object,Object> getProperties(){
        Map<Object,Object> properties = new HashMap<>();
        properties.put("hibernate.dialect", "org.hibernate.dialect.DerbyDialect");
        properties.put("hibernate.id.new_generator_mappings", false);
        properties.put("hibernate.connection.datasource", getDataSource());
        return properties;

    }

    private static DataSource getDataSource() {
        EmbeddedDataSource dataSource = new EmbeddedDataSource();
        dataSource.setDatabaseName("db/user-platform");
        dataSource.setCreateDatabase("create");
        return dataSource;
    }



}
