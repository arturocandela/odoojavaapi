package com.sge.nuestratienda.client.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Class with Singleton Pattern
 */
public class PropertyValues {

    private static Object instanceLock = new Object();
    private static PropertyValues Instance = null;

    /**
     *
     * @return The Default class instance
     */
    public static PropertyValues Instance() {

        synchronized (instanceLock){
                if (Instance == null){
                    Instance = new PropertyValues();
                }
        }
        return Instance;
    }

    private String OdooUrl = null;
    private String DBName = null;
    private String DBUser = null;
    private String DBPassword = null;


    private PropertyValues() {

        InputStream input = PropertyValues.class.getClassLoader().getResourceAsStream("config.properties");

        Properties prop = new Properties();

        if (input == null) {
            System.out.println("Sorry, unable to find config.properties");
            return;
        }

        //load a properties file from class path, inside static method
        try {
            prop.load(input);
        } catch (Exception e){
            e.printStackTrace();
        }

        //get the property value and print it out
        OdooUrl = prop.getProperty("odoo.url","http://localhost:6059");
        DBName = prop.getProperty("db.name","odoo");
        DBUser = prop.getProperty("db.user","admin");
        DBPassword = prop.getProperty("db.password","admin");

    }

    public String getOdooUrl() {
        return OdooUrl;
    }

    public String getDBName() {
        return DBName;
    }

    public String getDBUser() {
        return DBUser;
    }

    public String getDBPassword() {
        return DBPassword;
    }

}
