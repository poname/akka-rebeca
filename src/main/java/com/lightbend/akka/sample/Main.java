package com.lightbend.akka.sample;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;

import java.io.IOException;

public class Main {
    public static void main(String[] args) {

        final ActorSystem system = ActorSystem.create("Rebeca");

        final ActorRef train1 = system.actorOf(Train.props("train1"), "train____1");
        final ActorRef train2 = system.actorOf(Train.props("train2"), "train____2");
        final ActorRef theController = system.actorOf(BridgeController.props("theController"), "controller");

        Injector.inject(train1, new TrainKnownRebecs(theController));
        Injector.inject(train2, new TrainKnownRebecs(theController));
        Injector.inject(theController, new BridgeControllerKnownRebecs(train1, train2));

        train1.tell(new Messages.Run(), ActorRef.noSender());
        train2.tell(new Messages.Run(), ActorRef.noSender());
        theController.tell(new Messages.Run(), ActorRef.noSender());

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
        system.terminate();
    }
}
