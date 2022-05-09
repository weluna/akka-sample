package org.example.akka.actor.youtube;

import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.Value;

import java.awt.event.WindowFocusListener;

public class ActorSampleTest {


    public static void main(String[] args) {
        ActorSystem<Command> mySystem = ActorSystem.create(HelloWorld.create(), "MySystem");
        mySystem.tell(SayHello.INSTANTS);
        mySystem.tell(SayHello.INSTANTS);
        mySystem.tell(new ChangeMessage("Hello Actor World!!!"));
        mySystem.tell(SayHello.INSTANTS);
        mySystem.tell(SayHello.INSTANTS);
    }

    public static class HelloWorld extends AbstractBehavior<Command> {

        private String message = "Hello World!!!";

        public static Behavior<Command> create() {
            return Behaviors.setup(HelloWorld::new);
        }

        private HelloWorld(ActorContext<Command> context) {
            super(context);
        }

        @Override
        public Receive<Command> createReceive() {
            return newReceiveBuilder()
                    .onMessageEquals(SayHello.INSTANTS, this::sayHello)
                    .onMessage(ChangeMessage.class, this::changeMessage)
                    .build();
        }

        private Behavior<Command> changeMessage(ChangeMessage command) {
            message = command.newMessage;
            return this;
        }

        private Behavior<Command> sayHello() {
            System.out.println(message);
            return this;
        }
    }

    public interface Command {

    }

    public enum SayHello implements Command {
        INSTANTS;
    }

    @Value
    public static class ChangeMessage implements Command {
        String newMessage;
    }
}
