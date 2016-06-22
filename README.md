# spring-boot-starter-akka

基于spring-boot和akka的集成
1. 将ActorSystem纳入spring的管辖范围，能够在代码中自动注入ActorSystem
2. 支持Remote Actor的自动创建

##如何使用
* 在Spring Boot项目的pom.xml中添加以下依赖:
```
 <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-akka</artifactId>
         <version>1.3.1.RELEASE</version>
 </dependency>
 ```
* 在application.properties添加Akka Actor的配置如下:
```
spring.akka.systemName=ClientActorSystem
spring.akka.config=client.conf
spring.akka.actorBeanClass=com.alibaba.akka.TestActor
spring.akka.actorName=ClientHandler
```
* spring boot启动及编写Remote Actor
```
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(Application.class);
        application.run(args);
    }
    
package com.alibaba.akka;
import com.alibaba.boot.akka.ActorBean;
import akka.actor.UntypedActor;
@ActorBean
public class TestActor extends UntypedActor {

    public void onReceive(Object arg0) throws Exception {
        System.out.println(arg0);

    }

}
启动容器后，就能够使用spring-boot的autoconfig功能，会自动创建ActorSystem及暴露远程Actor（TestActor）
```
* 如何与远程Actor连接

```
public class PingClientSystemMain {

    public static void main(String[] args) throws InterruptedException {

        final ActorSystem system = ActorSystem.create("PingLookupSystem", ConfigFactory.load("pingRemoteLookup"));
        final ActorRef actor = system.actorOf(Props.create(PingLookupActor.class,
                                                           "akka.tcp://ClientActorSystem@127.0.0.1:2552/user/ClientHandler"),
                                              "PingLookupActor");

        TimeUnit.SECONDS.sleep(5);
        for (int i = 0; i < 1000000; i++) {
            actor.tell(TaskProtos.Ping.newBuilder().setId(UUID.randomUUID().toString()).setNow(System.currentTimeMillis()).build(),
                       ActorRef.noSender());
        }

    }

}


public class PingLookupActor extends UntypedActor {

    private final String path;
    private ActorRef     calculator = null;

    public PingLookupActor(String path){
        this.path = path;
        sendIdentifyRequest();
    }

    private void sendIdentifyRequest() {
        getContext().actorSelection(path).tell(new Identify(path), getSelf());
        getContext().system().scheduler().scheduleOnce(Duration.create(3, TimeUnit.SECONDS), getSelf(),
                                                       ReceiveTimeout.getInstance(), getContext().dispatcher(),
                                                       getSelf());
    }

    @Override
    public void onReceive(Object message) throws Exception {
        if (message instanceof ActorIdentity) {
            calculator = ((ActorIdentity) message).getRef();
            if (calculator == null) {
                System.out.println("Remote actor not available: " + path);
            } else {
                getContext().watch(calculator);
                getContext().become(active, true);
            }

        } else if (message instanceof ReceiveTimeout) {
            sendIdentifyRequest();
        } else {
            System.out.println("Not ready yet");

        }
    }

    Procedure<Object> active = message -> {
        if (message instanceof TaskProtos.Ping) {
            TaskProtos.Ping request = (TaskProtos.Ping) message;
            calculator.tell(message, getSelf());
        } else if (message instanceof TaskProtos.PingResponse) {
            TaskProtos.PingResponse result = (TaskProtos.PingResponse) message;
            System.out.println(result.toBuilder().toString());
        } else if (message instanceof Terminated) {
            System.out.println("Calculator terminated");
            sendIdentifyRequest();
            getContext().unbecome();
        } else if (message instanceof ReceiveTimeout) {

        } else {
            unhandled(message);
        }

    };
}

```


##关于spring-boot开发知识
<a href ="http://www.jianshu.com/users/aa6df7dd83ec/latest_articles">spring-boot学习笔记</a>
