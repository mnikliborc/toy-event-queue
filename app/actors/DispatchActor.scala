package actors

import akka.actor.Actor
import akka.actor.ActorRef
import org.joda.time.DateTime
import scala.collection.mutable
import scala.collection.immutable
import play.api.Logger

case class Register(actorRef: ActorRef, eventType: String)
case class Unregister(actorRef: ActorRef, eventType: String)
case class Event(username: String, eventType: String, datetime: DateTime)

class DispatchActor extends Actor {
  val eventToUserRefs = mutable.HashMap[String, immutable.Set[ActorRef]]()

  override def receive = {
    case Register(actorRef: ActorRef, eventType: String) =>
      val actors: Set[ActorRef] = eventToUserRefs.getOrElse(eventType, Set())
      Logger.debug("register received")
      eventToUserRefs += eventType -> (actors + actorRef)

    case Unregister(actorRef: ActorRef, eventType: String) =>
      val actors: Set[ActorRef] = eventToUserRefs.getOrElse(eventType, Set())
      Logger.debug("unregister received")
      val alteredActorsSet = (actors - actorRef)

      if (alteredActorsSet.isEmpty) {
        eventToUserRefs.remove(eventType)
      } else {
        eventToUserRefs += eventType -> alteredActorsSet
      }

    case event @ Event(username: String, eventType: String, datetime: DateTime) =>
      Logger.debug(eventToUserRefs.toString)
      val usernames: Set[ActorRef] = eventToUserRefs.getOrElse(eventType, Set())
      usernames.foreach(_ ! event)
  }
}