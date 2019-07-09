package com.nasa.bt.server.data;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class ConfigurationInstance {

    private static ConfigurationInstance instance;
    private Configuration configuration;
    private SessionFactory sessionFactory;

    private ConfigurationInstance(){
        configuration=new Configuration();
        configuration.configure();
        sessionFactory=configuration.buildSessionFactory();
    }

    public static Session openSession(){
        if(instance==null)
            instance=new ConfigurationInstance();
        return instance.sessionFactory.openSession();
    }

}
