package org.example.akka.actor.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import lombok.Value;
import scala.concurrent.duration.FiniteDuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author wangkh
 */
public class ActorSampleTest {

    public static void main(String[] args) throws InterruptedException {
        ActorSystem system = ActorSystem.create("MainActor");
        ActorRef actorRef = system.actorOf(Props.create(MainActor.class));
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        Runnable runnable = () -> actorRef.tell(new Msg(0, list), null);

        system.scheduler().scheduleWithFixedDelay(FiniteDuration.apply(1, TimeUnit.SECONDS),
                FiniteDuration.apply(1, TimeUnit.SECONDS),
                runnable,
                system.dispatcher());
    }

    public static class MainActor extends UntypedAbstractActor {

        LinkedList<Integer> queue = new LinkedList<>();

        @Override
        public void onReceive(Object message) throws Throwable {
            Msg msg = (Msg) message;
            if (msg.code == 0) {
                List<Integer> list = msg.getList();
                ActorRef actorRef1 = getContext().actorOf(Props.create(SubActor.class));
                actorRef1.tell(new Msg(1, list.subList(0, 2)), getSelf());

                ActorRef actorRef2 = getContext().actorOf(Props.create(SubActor.class));
                actorRef2.tell(new Msg(1, list.subList(2, 4)), getSelf());
            } else if (msg.code == 2) {
                if (queue.size() >= 2) {
                    ActorRef actorRef = getContext().actorOf(Props.create(SubActor.class));
                    actorRef.tell(new Msg(3, new ArrayList<>(queue)), getSelf());
                    queue.clear();
                } else {
                    queue.push(msg.getList().get(0));
                }
            } else if (msg.code == 4) {
                System.out.println(msg.getList());
            } else {
                unhandled(message);
            }
        }
    }

    public static class SubActor extends UntypedAbstractActor {
        @Override
        public void onReceive(Object message) throws Throwable {
            Msg msg = (Msg) message;
            if (msg.code == 1) {
                List<Integer> list = msg.getList();
                List<Integer> singleton = Collections.singletonList(list.get(0) + list.get(1));
                getSender().tell(new Msg(2, singleton), getSelf());
            } else if (msg.code == 3) {
                List<Integer> list = msg.getList();
                List<Integer> singleton = Collections.singletonList(list.get(0) + list.get(1));
                getSender().tell(new Msg(4, singleton), getSelf());
            } else {
                unhandled(message);
            }
        }
    }

    @Value
    public static class Msg {
        int code;
        List<Integer> list;
    }
}
