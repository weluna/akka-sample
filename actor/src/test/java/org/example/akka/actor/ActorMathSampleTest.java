package org.example.akka.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedAbstractActor;
import com.typesafe.config.ConfigFactory;
import lombok.Value;
import scala.concurrent.duration.FiniteDuration;

import java.io.Serializable;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author wangkh
 */
public class ActorMathSampleTest {

    public interface MathOp extends Serializable {

    }

    public interface MathResult extends Serializable {

    }

    @Value
    static class Add implements MathOp {
        int n1;
        int n2;
    }

    @Value
    static class AddResult implements MathResult {
        int n1;
        int n2;
        int result;
    }

    @Value
    static class Subtract implements MathOp {
        int n1;
        int n2;
    }

    @Value
    static class SubtractResult implements MathResult {
        int n1;
        int n2;
        int result;
    }

    @Value
    static class Multiply implements MathOp {
        int n1;
        int n2;
    }

    @Value
    static class MultiplicationResult implements MathResult {
        int n1;
        int n2;
        int result;
    }

    @Value
    static class Divide implements MathOp {
        float n1;
        float n2;
    }

    @Value
    static class DivisionResult implements MathResult {
        float n1;
        float n2;
        float result;
    }

    public static class CalculatorActor extends UntypedAbstractActor {

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message instanceof Add) {
                Add add = (Add) message;
                System.out.println("Calculating " + add.getN1() + " + " + add.getN2());
                AddResult result = new AddResult(add.getN1(), add.getN2(), add.getN1() + add.getN2());
                getSender().tell(result, getSelf());
            } else if (message instanceof Subtract) {
                Subtract subtract = (Subtract) message;
                System.out.println("Calculating " + subtract.getN1() + " - " + subtract.getN2());
                SubtractResult result = new SubtractResult(subtract.getN1(), subtract.getN2(), subtract.getN1() - subtract.getN2());
                getSender().tell(result, getSelf());
            } else if (message instanceof Multiply) {
                Multiply multiply = (Multiply) message;
                System.out.println("Calculating " + multiply.getN1() + " * " + multiply.getN2());
                MultiplicationResult result = new MultiplicationResult(multiply.getN1(), multiply.getN2(), multiply.getN1() * multiply.getN2());
                getSender().tell(result, getSelf());
            } else if (message instanceof Divide) {
                Divide divide = (Divide) message;
                System.out.println("Calculating " + divide.getN1() + " / " + divide.getN2());
                DivisionResult result = new DivisionResult(divide.getN1(), divide.getN2(), divide.getN1() / divide.getN2());
                getSender().tell(result, getSelf());
            } else {
                unhandled(message);
            }
        }
    }

    public static class CreationActor extends UntypedAbstractActor {

        String name;

        public CreationActor(String name) {
            this.name = name;
        }

        @Override
        public void onReceive(Object message) throws Throwable {
            if (message instanceof MathOp) {
                ActorRef calculator = getContext().actorOf(Props.create(CalculatorActor.class));
                calculator.tell(message, getSelf());
            } else if (message instanceof MultiplicationResult) {
                MultiplicationResult result = (MultiplicationResult) message;
                String format = String.format("Mul result: %d * %d = %d", result.getN1(), result.getN2(), result.getResult());
                System.out.println(format);
            } else if (message instanceof DivisionResult) {
                DivisionResult result = (DivisionResult) message;
                String format = String.format("Div result: %.2f / %.2f = %.2f", result.getN1(), result.getN2(), result.getResult());
                System.out.println(format);
            } else {
                unhandled(message);
            }
        }
    }

    public static class CreationApplication {

        public void startRemoteWorkerSystem() {
            ActorSystem.create("CalculatorWorkerSystem", ConfigFactory.load("calculator"));
            System.out.println("Start CalculatorWorkerSystem");
        }

        public void startRemoteCreationSystem() {
            ActorSystem system = ActorSystem.create("CreationSystem", ConfigFactory.load("remoteCreation"));
            ActorRef actor = system.actorOf(Props.create(CreationActor.class, "creationActor"));

            System.out.println("Started CalculatorWorkerSystem");

            Random random = new Random();
            Runnable runnable = () -> {
                if (random.nextInt(100) % 2 == 0) {
                    actor.tell(new Multiply(random.nextInt(100), random.nextInt(100)), null);
                } else {
                    actor.tell(new Divide(random.nextInt(10000), random.nextInt(99) + 1), null);
                }
            };

            system.scheduler().scheduleWithFixedDelay(FiniteDuration.apply(1, TimeUnit.SECONDS),
                    FiniteDuration.apply(1, TimeUnit.SECONDS),
                    runnable,
                    system.dispatcher());
        }
    }

    public static void main(String[] args) {
        CreationApplication application = new CreationApplication();
        application.startRemoteCreationSystem();
        application.startRemoteWorkerSystem();
    }
}
