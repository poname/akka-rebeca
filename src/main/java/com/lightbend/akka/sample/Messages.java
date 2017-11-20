package com.lightbend.akka.sample;

import akka.actor.ActorRef;

public class Messages {
    private Messages() {}

    public static class Run {}
    public static class BridgeControllerArrive {}
    public static class BridgeControllerLeave {}
    public static class TrainYouMayPass {}
    public static class TrainPassed {}
    public static class TrainReachBridge {}

    public static void log(ActorRef in, ActorRef from, String message) {
        System.out.println("in\t" + in.path().name() + "\t" + message + "\tfrom\t" + from.path().name());

    }
}
