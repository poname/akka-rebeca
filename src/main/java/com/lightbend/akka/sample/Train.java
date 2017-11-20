package com.lightbend.akka.sample;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class Train extends AbstractActor{
    static public Props props(String message) {
        return Props.create(Train.class, () -> new Train(message));
    }
    private final String message;

    /* known rebecs */
    private ActorRef controller;

    /* state vars */
    boolean onTheBridge;

    private Train(String message) {
        this.message = message;
    }

    private void run() {
        onTheBridge = false;
        this.Passed();
    }

    private void YouMayPass() {
        Messages.log(getSelf(), getSender(), "YouMayPass ");
        onTheBridge = true;
        this.Passed();
    }

    private void Passed() {
//        Messages.log(getSelf(), getSender(), "Passed     ");
        onTheBridge = false;
//        controller.Leave();
        controller.tell(new Messages.BridgeControllerLeave(), getSelf());
        this.ReachBridge();
    }

    private void ReachBridge() {
//        Messages.log(getSelf(), getSender(), "ReachBridge");
//        controller.Arrive();
        controller.tell(new Messages.BridgeControllerArrive(), getSelf());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(TrainKnownRebecs.class, y -> {
                    this.controller = y.getController();
                })
                .match(Messages.Run.class, m -> run())
                .match(Messages.TrainYouMayPass.class, m -> YouMayPass())
//                .match(Messages.TrainPassed.class, m -> Passed())
//                .match(Messages.TrainReachBridge.class, m -> ReachBridge())
                .build();
    }
}

class TrainKnownRebecs {
    private ActorRef controller;

    public TrainKnownRebecs(ActorRef controller) {
        this.controller = controller;
    }

    public ActorRef getController() {
        return this.controller;
    }
}