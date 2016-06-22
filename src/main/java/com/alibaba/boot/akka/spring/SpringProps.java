package com.alibaba.boot.akka.spring;

import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;

public class SpringProps {

    public static Props create(ActorSystem actorSystem, String actorBeanName) {
        return SpringExtension.instance().get(actorSystem).create(actorBeanName);
    }

    public static Props create(ActorSystem actorSystem, Class<? extends UntypedActor> requiredType) {
        return SpringExtension.instance().get(actorSystem).create(requiredType);
    }

    public static Props create(ActorSystem actorSystem, String actorBeanName,
                               Class<? extends UntypedActor> requiredType) {
        return SpringExtension.instance().get(actorSystem).create(actorBeanName, requiredType);
    }
}
