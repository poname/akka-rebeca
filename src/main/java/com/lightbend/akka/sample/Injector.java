package com.lightbend.akka.sample;

import akka.actor.ActorRef;

public class Injector {
    public static void inject(ActorRef actor, TrainKnownRebecs args) {
        actor.tell(args, ActorRef.noSender());
    }
    public static void inject(ActorRef actor, BridgeControllerKnownRebecs args) {
        actor.tell(args, ActorRef.noSender());
    }
}