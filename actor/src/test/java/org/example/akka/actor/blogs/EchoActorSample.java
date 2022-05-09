package org.example.akka.actor.blogs;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.dispatch.OnComplete;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.pattern.Patterns;
import scala.concurrent.Future;

/**
 * @author wangkh
 */
public class EchoActorSample {

    public static void main(String[] args) {
        ActorSystem system = ActorSystem.create("app");
        ActorRef echoActor = system.actorOf(Props.create(EchoActor.class), "echoActor");
        System.out.println(echoActor.path());

        echoActor.tell("Hello Kiana!", null);

        Future<Object> future = Patterns.ask(echoActor, "echo me", 200);
        future.onComplete(new OnComplete<Object>() {
            @Override
            public void onComplete(Throwable failure, Object success) throws Throwable {
                System.out.println(success);
            }
        }, system.dispatcher());
    }

    public static class EchoActor extends AbstractActor {

        private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

        @Override
        public Receive createReceive() {
            return receiveBuilder()
                    .match(String.class, s -> {
                        log.info("Received String message: {}", s);
                        ActorRef sender = getSender();
                        if (sender != null && !sender.isTerminated()) {
                            sender.tell("Receive: " + s, getSelf());
                        }
                    })
                    .matchAny(o -> log.info("Received unknown message"))
                    .build();
        }
    }
}
