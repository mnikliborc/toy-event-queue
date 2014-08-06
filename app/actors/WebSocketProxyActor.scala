package actors

import play.api.libs.iteratee.PushEnumerator
import akka.actor.Actor
import play.api.libs.iteratee.Enumerator
import org.joda.time.DateTime
import play.libs.Akka
import akka.actor.Props
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import play.api.libs.iteratee.Iteratee
import scala.collection.mutable
import akka.actor.ActorRef
import play.api.Logger
import org.joda.time.format.DateTimeFormatterBuilder
import play.api.libs.iteratee.Concurrent

case class Start()
case class Connected(out: Enumerator[String], channel: Concurrent.Channel[String])

case class WebSocketHandler(in: Iteratee[String, Unit], out: Enumerator[String], actor: ActorRef)

class WebSocketProxyActor extends Actor {
  val formatter = new DateTimeFormatterBuilder()
    .appendHourOfDay(2)
    .appendLiteral(':')
    .appendMinuteOfHour(2)
    .appendLiteral(':')
    .appendSecondOfMinute(2)
    .appendLiteral('.')
    .appendMillisOfSecond(2).toFormatter()

  override def receive = {
    case Start() =>
      val (out, channel) = Concurrent.broadcast[String]
      context.become(receiveAndPass(channel))

      sender ! Connected(out, channel)
  }

  def receiveAndPass(channel: Concurrent.Channel[String]): Receive = {
    case Event(username: String, eventType: String, datetime: DateTime) =>
      channel.push(s"$username - $eventType - ${formatter.print(datetime)}")
  }
}

object WebSocketProxyActor {
  implicit val timeout = Timeout(1 second)
  import scala.concurrent.ExecutionContext.Implicits.global

  def handle(dispatcher: ActorRef, username: String) = {
    val actor = Akka.system.actorOf(Props[WebSocketProxyActor])
    val p = actor ? Start()

    p map {
      case Connected(out, channel) =>
        val in = Iteratee.foreach[String] {
          msg =>
            Logger.debug(s"ws $msg")
            dispatcher ! Event(username, msg, new DateTime)
        }

        WebSocketHandler(in, out, actor)
    }
  }
}