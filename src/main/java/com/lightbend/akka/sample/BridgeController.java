package com.lightbend.akka.sample;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;

public class BridgeController extends AbstractActor {
    static public Props props(String message) {
        return Props.create(BridgeController.class, () -> new BridgeController(message));
    }
    private final String message;

    /* known rebecs */
    private ActorRef t1, t2;

    /* state vars */
    boolean isWaiting1;
    boolean isWaiting2;
    boolean signal1;
    boolean signal2;

    private BridgeController(String message/*, ActorRef printerActor*/) {
        this.message = message;
    }

    private void run() {
        signal1 = false;	/* red */
        signal2 = false;	/* red */
        isWaiting1 = false;
        isWaiting2 = false;
    }

    private void Arrive() {
        Messages.log(getSelf(), getSender(), "Arrive     ");
        if (getSender() == t1){
            if (signal2 == false) {
                signal1 = true;	/* green */
//                t1.YouMayPass();
                t1.tell(new Messages.TrainYouMayPass(), getSelf());
            }
            else {
                isWaiting1 = true;
            }
        }
        else {
            if (signal1 == false){
                signal2 = true;	/* green */
//                t2.YouMayPass();
                t2.tell(new Messages.TrainYouMayPass(), getSelf());
            }
            else{
                isWaiting2 = true;
            }
        }
    }

    private void Leave() {
        Messages.log(getSelf(), getSender(), "Leave      ");
        if (getSender() == t1) {
            signal1 = false;	/* red */
            if (isWaiting2){
                signal2 = true;
//                t2.YouMayPass();
                t2.tell(new Messages.TrainYouMayPass(), getSelf());
                isWaiting2 = false;
            }
        } else {
            signal2 = false;	/* red */
            if (isWaiting1) {
                signal1 = true;
//                t1.YouMayPass();
                t1.tell(new Messages.TrainYouMayPass(), getSelf());
                isWaiting1 = false;
            }
        }
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(BridgeControllerKnownRebecs.class, y -> {
                    this.t1 = y.getT1();
                    this.t2 = y.getT2();
                })
                .match(Messages.Run.class, m -> run())
                .match(Messages.BridgeControllerArrive.class, m -> Arrive())
                .match(Messages.BridgeControllerLeave.class, m -> Leave())
                .build();
    }
}
class BridgeControllerKnownRebecs {
    private ActorRef t1;
    private ActorRef t2;

    public BridgeControllerKnownRebecs(ActorRef t1, ActorRef t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public ActorRef getT1() {
        return this.t1;
    }
    public ActorRef getT2() {
        return this.t2;
    }
}