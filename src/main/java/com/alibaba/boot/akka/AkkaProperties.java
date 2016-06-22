package com.alibaba.boot.akka;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.typesafe.config.Config;

@ConfigurationProperties(prefix = "spring.akka")
public class AkkaProperties {

    private String systemName;

    private Config config;

    private String actorBeanClass;

    private String actorName;

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public Config getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = com.typesafe.config.ConfigFactory.load(config);
    }

    public String getActorBeanClass() {
        return actorBeanClass;
    }

    public void setActorBeanClass(String actorBeanClass) {
        this.actorBeanClass = actorBeanClass;
    }

    public String getActorName() {
        return actorName;
    }

    public void setActorName(String actorName) {
        this.actorName = actorName;
    }

}
