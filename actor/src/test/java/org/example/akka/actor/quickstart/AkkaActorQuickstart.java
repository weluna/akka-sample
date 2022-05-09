package org.example.akka.actor.quickstart;

import akka.actor.testkit.typed.javadsl.TestKitJunitResource;
import akka.actor.testkit.typed.javadsl.TestProbe;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import lombok.Value;
import org.junit.ClassRule;
import org.junit.Test;

/**
 * @author wangkh
 */
public class AkkaActorQuickstart {

    @ClassRule
    public static final TestKitJunitResource testKit = new TestKitJunitResource();

    @Test
    public void testGreeterActorSendingOfGreeting() {
        TestProbe<Greeted> testProbe = testKit.createTestProbe();
        ActorRef<Greet> underTest = testKit.spawn(Greeter.create(), "greeter");
        underTest.tell(new Greet("Taylor", testProbe.getRef()));
        testProbe.expectMessage(new Greeted("Taylor", underTest));
    }

    public static void main(String[] args) throws Exception {
        ActorSystem<SayHello> greeterMain = ActorSystem.create(GreeterMain.create(), "hello-akka");

        greeterMain.tell(new SayHello("Taylor"));

        System.out.println(">>> Press ENTER to exit");
        int read = System.in.read();
        greeterMain.terminate();
    }

    public static class GreeterMain extends AbstractBehavior<SayHello> {
        private final ActorRef<Greet> greeter;

        public GreeterMain(ActorContext<SayHello> context) {
            super(context);
            greeter = context.spawn(Greeter.create(), "greeter");
        }

        public static Behavior<SayHello> create() {
            return Behaviors.setup(GreeterMain::new);
        }

        @Override
        public Receive<SayHello> createReceive() {
            return newReceiveBuilder().onMessage(SayHello.class, this::onSayHello).build();
        }

        private Behavior<SayHello> onSayHello(SayHello command) {
            ActorRef<Greeted> replyTo = getContext().spawn(GreeterBot.create(3), command.name);
            greeter.tell(new Greet(command.name, replyTo));
            return this;
        }
    }

    public static class GreeterBot extends AbstractBehavior<Greeted> {

        private final int max;
        private int greetingCounter;

        public GreeterBot(ActorContext<Greeted> context, int max) {
            super(context);
            this.max = max;
        }

        public static Behavior<Greeted> create(int max) {
            return Behaviors.setup(context -> new GreeterBot(context, max));
        }

        @Override
        public Receive<Greeted> createReceive() {
            return newReceiveBuilder().onMessage(Greeted.class, this::onGreeted).build();
        }

        private Behavior<Greeted> onGreeted(Greeted message) {
            greetingCounter++;
            getContext().getLog().info("Greeting {} for {}", greetingCounter, message.whom);
            if (greetingCounter == max) {
                return Behaviors.stopped();
            } else {
                message.from.tell(new Greet(message.whom, getContext().getSelf()));
                return this;
            }
        }
    }

    /**
     * Greeter
     */
    public static class Greeter extends AbstractBehavior<Greet> {

        public Greeter(ActorContext<Greet> context) {
            super(context);
        }

        @Override
        public Receive<Greet> createReceive() {
            return newReceiveBuilder().onMessage(Greet.class, this::onGreet).build();
        }

        private Behavior<Greet> onGreet(Greet command) {
            getContext().getLog().info("Hello {}!", command.whom);
            command.replyTo.tell(new Greeted(command.whom, getContext().getSelf()));
            return this;
        }

        public static Behavior<Greet> create() {
            return Behaviors.setup(Greeter::new);
        }
    }

    @Value
    private static class SayHello {
        String name;
    }

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
}
