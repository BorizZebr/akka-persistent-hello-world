package com.zebrosoft

import java.util.UUID

import akka.actor.{ActorLogging, ActorRef, Props, ReceiveTimeout}
import akka.persistence._
import com.zebrosoft.model._

import scala.concurrent.duration._

/**
  * Created by borisbondarenko on 21.02.17.
  */
class CartHandler(manager: ActorRef, cartId: UUID)
  extends PersistentActor
    with CartEventCommandLogic
    with AtLeastOnceDelivery
    with ActorLogging {

  override def persistenceId: String = cartId.toString

  var state: Cart = Cart(cartId)

  context.setReceiveTimeout(10.seconds)

  def withCheckedId(id: UUID)(a: => Unit) = {
    if(id == cartId) a
    else sender ! CartIdMissmatch
  }

  override def receiveCommand: Receive = opened

  def opened: Receive = receiveInfra orElse receiveGet orElse {
    case cmd: CartCommand => withCheckedId(cmd.cartId) {
      persist(commandToEvent(cmd)) { e =>
        state = foldState(state)(e)
        if(state.isClosed) context become closed
      }
      sender ! Ack
    }
  }

  def closed: Receive = receiveInfra orElse receiveGet orElse {
    case c: CartId => withCheckedId(c.cartId)(sender ! CartAlreadyClosed)
  }

  def receiveGet: Receive = {
    case GetCartQuery(id) => withCheckedId(id)(sender ! state)
  }

  def receiveInfra: Receive = {
    case ReceiveTimeout => manager ! CartIdleTimeout(cartId)
    case CartHandlerTerminate => context stop self
  }

  override def receiveRecover: Receive = {
    case evt: CartEvent => state = foldState(state)(evt)
    case SnapshotOffer(_, snapshot: Cart) => state = snapshot
  }
}

object CartHandler {
  def props(manager: ActorRef, cartId: UUID): Props =
    Props(new CartHandler(manager, cartId))
}
