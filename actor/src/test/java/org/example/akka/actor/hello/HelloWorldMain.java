package org.example.akka.actor.hello;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.Value;

/**
 * @author wangkh
 */
public class HelloWorldMain extends AbstractBehavior<HelloWorldMain.SayHello> {

    @Value
    public static class SayHello {
        String name;
    }

    public static Behavior<SayHello> create() {
        return Behaviors.setup(HelloWorldMain::new);
    }

    private final ActorRef<HelloWorld.Greet> greeter;

    public HelloWorldMain(ActorContext<SayHello> context) {
        super(context);
        greeter = context.spawn(HelloWorld.create(), "greeter");
    }

    @Override
    public Receive<SayHello> createReceive() {
        return newReceiveBuilder().onMessage(SayHello.class, this::onStart).build();
    }

    private Behavior<SayHello> onStart(SayHello command) {
        ActorRef<HelloWorld.Greeted> replyTo = getContext().spawn(HelloWorldBot.create(3), command.name);
        greeter.tell(new HelloWorld.Greet(command.name, replyTo));
        return this;
    }

    public static void main(String[] args) {
        ActorSystem<SayHello> system = ActorSystem.create(HelloWorldMain.create(), "hello");
        system.tell(new HelloWorldMain.SayHello("World"));
        system.tell(new HelloWorldMain.SayHello("Akka"));
    }
}
