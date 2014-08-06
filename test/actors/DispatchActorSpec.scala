package actors

import akka.actor.ActorSystem
import akka.testkit.TestKit
import akka.testkit.TestActorRef
import org.specs2.mutable.Specification
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import akka.testkit.ImplicitSender
import akka.actor.Props
import akka.testkit.TestProbe
import org.joda.time.DateTime
import org.specs2.mutable.After

@RunWith(classOf[JUnitRunner])
class DispatchActorSpec extends TestKit(ActorSystem("testSystem")) with Specification {

  "DispatchActor" should {
    val eventType = "eventType"

    "register an actor for an event" in {
      val (actorRef, actor) = createTestActor

      actorRef ! Register(actorRef, eventType)
      assert(actor.eventToUserRefs.keySet.contains(eventType))
      assert(actor.eventToUserRefs.get(eventType).get.contains(actorRef))
    }

    "unregister an actor from an event" in {
      val (actorRef, actor) = createTestActor

      actorRef ! Register(actorRef, eventType)
      assert(actor.eventToUserRefs.keySet.contains(eventType))
      assert(actor.eventToUserRefs.get(eventType).get.contains(actorRef))

      actorRef ! Unregister(actorRef, eventType)
      assert(actor.eventToUserRefs.isEmpty)
    }

    def createTestActor = {
      val actorRef = TestActorRef(new DispatchActor)
      (actorRef, actorRef.underlyingActor)
    }

    "dispatches an event" in {
      val actorRef = system.actorOf(Props[DispatchActor])
      val receiverProbe = TestProbe()

      actorRef ! Register(receiverProbe.ref, eventType)
      val event = Event("username", eventType, new DateTime)
      actorRef ! event
      receiverProbe.expectMsg(event)
      ()
    }
  }

  //step(TestKit.shutdownActorSystem(system))
}