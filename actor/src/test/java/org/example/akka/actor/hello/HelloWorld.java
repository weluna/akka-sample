package org.example.akka.actor.hello;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.Value;
import org.slf4j.helpers.MessageFormatter;

/**
 * @author wangkh
 */
public class HelloWorld extends AbstractBehavior<HelloWorld.Greet> {

    @Value
    public static class Greet {
        String whom;
        ActorRef<Greeted> replyTo;
    }

    @Value
    public static class Greeted {
        String whom;
        ActorRef<Greet> from;
    }

    public static Behavior<Greet> create() {
        return Behaviors.setup(HelloWorld::new);
    }

    public HelloWorld(ActorContext<Greet> context) {
        super(context);
    }

    @Override
    public Receive<Greet> createReceive() {
        return newReceiveBuilder().onMessage(Greet.class, this::onGreet).build();
    }

    private Behavior<Greet> onGreet(Greet command) {
        String message = MessageFormatter.format("Hello {}!", command.whom).getMessage();
        System.out.println(message);
        getContext().getLog().info(message);
        command.replyTo.tell(new Greeted(command.whom, getContext().getSelf()));
        return this;
    }
}
