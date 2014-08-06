package controllers

import play.api.libs.json.Json
import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import akka.actor.Props
import akka.actor.Actor
import scala.collection.mutable
import scala.collection.immutable
import org.joda.time.DateTime
import actors._
import scala.concurrent.Future
import play.api.libs.concurrent._
import akka.actor._
import play.api.Play.current

object Application extends Controller {
  val dispatcher = Akka.system.actorOf(Props[DispatchActor])
  val usernameToActor = mutable.HashMap[String, ActorRef]()

  def index = Action { request =>
    request.session.get("username").map { username =>
      Ok(views.html.index(username))
    }.getOrElse {
      Ok(views.html.index(""))
    }
  }

  def auth(username: String) = Action {
    Logger.debug(s"auth $username")
    Ok.withSession("username" -> username)
  }

  def register(username: String, eventType: String) = Action {
    Logger.debug(s"register $username, $eventType")

    val clientActor = usernameToActor.get(username)
    clientActor.map { ref =>
      dispatcher ! Register(clientActor.get, eventType)
      Ok("")
    }.getOrElse {
      Status(403)
    }
  }

  def unregister(username: String, eventType: String) = Action {
    Logger.debug(s"unregister $username, $eventType")

    val clientActor = usernameToActor.get(username)
    clientActor.map { ref =>
      dispatcher ! Unregister(clientActor.get, eventType)
      Ok("")
    }.getOrElse {
      Status(403)
    }
  }

  import scala.concurrent.ExecutionContext.Implicits.global
  def connect() = WebSocket.async[String] {
    requestHeader =>
      val username = requestHeader.session.get("username").getOrElse("not found")
      Logger.debug(s"connect $username")

      val wsHandlerFuture = WebSocketProxyActor.handle(dispatcher, username)
      wsHandlerFuture map {
        case WebSocketHandler(in, out, actor) =>
          usernameToActor += username -> actor
          (in, out)
      }
  }

}