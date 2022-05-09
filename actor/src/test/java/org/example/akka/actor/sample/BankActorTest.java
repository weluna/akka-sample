package org.example.akka.actor.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import akka.pattern.Patterns;
import lombok.extern.slf4j.Slf4j;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

/**
 * @author wangkh
 */
@Slf4j
public class BankActorTest {

    public static void main(String[] args) throws Exception {
        ActorSystem actorSystem = ActorSystem.create("actor-system");
        ActorRef actorRef = actorSystem.actorOf(Props.create(BankActor.class));

        CountDownLatch addCount = new CountDownLatch(20);
        CountDownLatch minusCount = new CountDownLatch(10);

        Thread addCountT = new Thread(() -> {
            while (addCount.getCount() > 0) {
                actorRef.tell(Command.ADD, null);
                addCount.countDown();
            }
        });

        Thread minusCountT = new Thread(() -> {
            while (minusCount.getCount() > 0) {
                actorRef.tell(Command.MINUS, null);
                minusCount.countDown();
            }
        });

        minusCountT.start();
        addCountT.start();
        minusCount.await();
        addCount.await();

        Future<Object> count = Patterns.ask(actorRef, Command.GET, 1000);

        count.onComplete(o -> {
            log.info("Get result from {}", o);
            return o;
        }, ExecutionContext.global());

        actorSystem.terminate();
        LockSupport.park();
    }

    @Slf4j
    public static class BankActor extends UntypedAbstractActor {
        private int count;

        @Override
        public void preStart() throws Exception {
            super.preStart();
            count = 0;
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message instanceof Command) {
                Command cmd = (Command) message;
                switch (cmd) {
                    case ADD:
                        log.info("Add 1 from {} to {}", count, ++count);
                        break;
                    case MINUS:
                        log.info("Minus 1 from {} to {}", count, --count);
                        break;
                    case GET:
                        log.info("Return current count {}", count);
                        getSender().tell(count, this.getSelf());
                        break;
                }
            }
        }
    }

    public enum Command {
        ADD,
        MINUS,
        GET
    }
}
