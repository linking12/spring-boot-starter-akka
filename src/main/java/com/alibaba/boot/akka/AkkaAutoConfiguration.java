package com.alibaba.boot.akka;

import org.springframework.beans.factory.BeanCreationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.alibaba.boot.akka.spring.SpringExtension;
import com.alibaba.boot.akka.spring.SpringProps;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

@Configuration
@EnableConfigurationProperties(AkkaProperties.class)
public class AkkaAutoConfiguration {

    @Autowired
    private AkkaProperties     akkaProperties;

    @Autowired
    private ApplicationContext ctx;

    @Bean(destroyMethod = "shutdown")
    public ActorSystem getActorSystem() {
        ActorSystem system;
        if (akkaProperties.getSystemName() != null && akkaProperties.getConfig() != null) {
            system = ActorSystem.create(akkaProperties.getSystemName(), akkaProperties.getConfig());
        } else if (akkaProperties.getSystemName() != null) {
            system = ActorSystem.create(akkaProperties.getSystemName());
        } else {
            system = ActorSystem.create();
        }
        SpringExtension.instance().get(system).setApplicationContext(ctx);
        return system;
    }

    @Bean
    @ConditionalOnProperty(prefix = "spring.akka", name = { "actorBeanClass", "actorName" })
    public ActorRef getRemoteActorRef() throws ClassNotFoundException {
        ActorSystem actorSystem = getActorSystem();
        @SuppressWarnings("unchecked")
        Props props = SpringProps.create(actorSystem,
                                         (Class<? extends UntypedActor>) Class.forName(akkaProperties.getActorBeanClass()),
                                         null);
        if (props == null) {
            throw new BeanCreationException("Can not create ActorRef for given parameters, actorBeanClass="
                                            + akkaProperties.getActorBeanClass());
        }
        if (StringUtils.isEmpty(akkaProperties.getActorName())) {
            return actorSystem.actorOf(props);
        } else {
            return actorSystem.actorOf(props, akkaProperties.getActorName());
        }
    }
}
