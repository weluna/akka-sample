package org.example.akka.cluster;

import akka.actor.ActorSystem;
import akka.actor.Props;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author wangkh
 */
public class App {

    public static void main(String[] args) {
        String[] ports = {"2551", "2552", "0"};
        startup(ports);
    }

    public static void startup(String[] ports) {
        ExecutorService pool = Executors.newFixedThreadPool(ports.length);

        for (String port : ports) {
            pool.submit(() -> {
                // Using input port to start multiple instances
                String configStr = "akka.remote.netty.tcp.port=" + port + "\n" +
                        "akka.remote.artery.canonical.port=" + port;

                Config config = ConfigFactory.parseString(configStr)
                        .withFallback(ConfigFactory.load());

                // Create an akka system
                ActorSystem system = ActorSystem.create("ClusterSystem", config);

                // Create an
                system.actorOf(Props.create(SimpleClusterListener.class), "ClusterListener");
            });
        }
    }
}
