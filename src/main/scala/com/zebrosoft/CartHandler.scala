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
    with AtLeastOnceDelivery
    with ActorLogging {

  override def persistenceId: String = cartId.toString

  var state: Cart = Cart(cartId)

  context.setReceiveTimeout(10 seconds)

  def foldState[A <: CartEvent]: A => Unit = e =>
    if(updateFunction.isDefinedAt(e)) {
      state = updateFunction(e)
    }

  val updateFunction: PartialFunction[CartEvent, Cart] = {
    case CartItemAddEvent(item, am) => state.addItem(Item(item, am))
    case CartItemRemoveEvent(item) => state.removeItem(item)
    case CartItemChangeAmountEvent(item, am) => state.changeAmount(item, am)
    case CartClosedEvent() =>
      context become closed
      state.close
  }

  def withCheckedId(id: UUID)(a: => Unit) = {
    if(id == cartId) a
    else sender ! CartIdMissmatch
  }

  override def receiveCommand: Receive = opened

  def opened: Receive = receiveInfra orElse receiveGet orElse {
    case CloseCartCommand(id) => withCheckedId(id) {
      persist(CartClosedEvent())(foldState)
      sender ! Ack
    }

    case AddItemCommand(id, itm, am) => withCheckedId(id) {
      persist(CartItemAddEvent(itm, am))(foldState)
      sender ! Ack
    }

    case RemoveItemCommand(id, itm) => withCheckedId(id) {
      persist(CartItemRemoveEvent(itm))(foldState)
      sender ! Ack
    }

    case ChangeAmountCommand(id, itm, am) => withCheckedId(id) {
      persist(CartItemChangeAmountEvent(itm, am))(foldState)
      sender ! Ack
    }
  }

  def closed: Receive = receiveInfra orElse receiveGet orElse {
    case c: CartCommand => withCheckedId(c.cartId)(sender ! CartAlreadyClosed)
  }

  def receiveGet: Receive = {
    case GetCartQuery(id) => withCheckedId(id)(sender ! state)
  }

  def receiveInfra: Receive = {
    case ReceiveTimeout => manager ! CartIdleTimeout(cartId)
    case CartHandlerTerminate => context stop self
  }

  override def receiveRecover: Receive = {
    case evt: CartEvent => foldState(evt)
    case SnapshotOffer(_, snapshot: Cart) => state = snapshot
  }
}

object CartHandler {
  def props(manager: ActorRef, cartId: UUID): Props =
    Props(new CartHandler(manager, cartId))
}
