package com.zebrosoft

import java.util.UUID

import akka.actor._
import akka.pattern.{Backoff, BackoffSupervisor, ask, pipe}
import akka.util.Timeout
import com.zebrosoft.model.{CartHandlerTerminate, CartId, CartIdleTimeout}

import scala.concurrent.duration._

/**
  * Created by borisbondarenko on 22.02.17.
  */
class CartsManager
  extends Actor
    with ActorLogging {

  import context.dispatcher

  var cmdHandlers: Map[UUID, ActorRef] = Map.empty

  implicit val timeout = Timeout(1.second)

  def createHandler(id: UUID): ActorRef = {
    val childProps = CartHandler.props(self, id)
    val props = BackoffSupervisor.props(
      Backoff.onStop(
        childProps,
        childName = s"cart-$id",
        minBackoff = 3.seconds,
        maxBackoff = 30.seconds,
        randomFactor = 0.2))
    context.actorOf(props, name = s"cart-supervisor-$id")
  }

  override def receive: Receive = {
    case c: CartId => handleCommandOrGetQuery(c)

    case CartIdleTimeout(id) =>
      cmdHandlers = cmdHandlers - id
      sender ! CartHandlerTerminate
  }

  def handleCommandOrGetQuery[A <: CartId](c: A) = {
    val id = c.cartId

    val h = cmdHandlers.getOrElse(id, {
      val n = createHandler(id)
      cmdHandlers = cmdHandlers + (id -> n)
      n
    })

    h ? c pipeTo sender
  }
}

object CartsManager {
  def props: Props = Props(new CartsManager)
}
