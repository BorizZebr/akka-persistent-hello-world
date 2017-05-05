package com.zebrosoft

import java.util.UUID

import akka.actor.{ActorLogging, ActorRef, Props, ReceiveTimeout}
import akka.persistence._
import com.zebrosoft.model._
import shapeless.{Generic, Poly1}

import scala.concurrent.duration._

/**
  * Created by borisbondarenko on 21.02.17.
  */
class CartHandler(manager: ActorRef, cartId: UUID)
  extends PersistentActor
    with AtLeastOnceDelivery
    with ActorLogging {

  val genCartCommand = Generic[CartCommand]

  override def persistenceId: String = cartId.toString

  var state: Cart = Cart(cartId)

  context.setReceiveTimeout(10.seconds)

  def foldState[A <: CartEvent]: A => Unit = e =>
    if(updateFunction.isDefinedAt(e)) {
      state = updateFunction(e)
    }

  val updateFunction: PartialFunction[CartEvent, Cart] = {
    case CartItemAddEvent(item, am) => state.addItem(Item(item, am))
    case CartItemRemoveEvent(item) => state.removeItem(item)
    case CartItemChangeAmountEvent(item, am) => state.changeAmount(item, am)
    case CartClosedEvent => context become closed; state.close
  }

  object commandToEvent extends Poly1 {
    implicit def caseCloseCart    = at[CloseCartCommand]   (_ => CartClosedEvent)
    implicit def caseAddItem      = at[AddItemCommand]     (c => CartItemAddEvent(c.item, c.amount))
    implicit def caseRemoveItem   = at[RemoveItemCommand]  (c => CartItemRemoveEvent(c.item))
    implicit def caseAmountChange = at[ChangeAmountCommand](c => CartItemChangeAmountEvent(c.item, c.newAmount))
  }

  def withCheckedId(id: UUID)(a: => Unit) = {
    if(id == cartId) a
    else sender ! CartIdMissmatch
  }

  override def receiveCommand: Receive = opened

  def opened: Receive = receiveInfra orElse receiveGet orElse {
    case cmd: CartCommand => withCheckedId(cmd.cartId) {
      val cmdCoproduct = genCartCommand.to(cmd)
      val event = (cmdCoproduct map commandToEvent).unify
      persist(event)(foldState)
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
    case evt: CartEvent => foldState(evt)
    case SnapshotOffer(_, snapshot: Cart) => state = snapshot
  }
}

object CartHandler {
  def props(manager: ActorRef, cartId: UUID): Props =
    Props(new CartHandler(manager, cartId))
}
