package org.example.akka.actor.hello;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import org.slf4j.helpers.MessageFormatter;

import javax.swing.*;
import java.awt.event.WindowFocusListener;

/**
 * @author wangkh
 */
public class HelloWorldBot extends AbstractBehavior<HelloWorld.Greeted> {

    public static Behavior<HelloWorld.Greeted> create(int max) {
        return Behaviors.setup(context -> new HelloWorldBot(context, max));
    }

    private final int max;
    private int greetingCounter;

    public HelloWorldBot(ActorContext<HelloWorld.Greeted> context, int max) {
        super(context);
        this.max = max;
    }

    @Override
    public Receive<HelloWorld.Greeted> createReceive() {
        return newReceiveBuilder().onMessage(HelloWorld.Greeted.class, this::onGreeted).build();
    }

    private Behavior<HelloWorld.Greeted> onGreeted(HelloWorld.Greeted message) {
        greetingCounter++;
        String msg = MessageFormatter.format("Greeting {} for {}", greetingCounter, message.getWhom()).getMessage();
        System.out.println(msg);
        getContext().getLog().info(msg);
        if (greetingCounter == max) {
            return Behaviors.stopped();
        } else {
            message.getFrom().tell(new HelloWorld.Greet(message.getWhom(), getContext().getSelf()));
            return this;
        }
    }
}
