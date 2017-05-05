package com.zebrosoft.model

import java.util.UUID

/**
  * Created by borisbondarenko on 06.03.17.
  */
trait CartEvent {
  val time: Long = System.currentTimeMillis
}

case object CartClosedEvent extends CartEvent

case class CartItemAddEvent(itemId: UUID, amount: Int) extends CartEvent

case class CartItemRemoveEvent(itemId: UUID) extends CartEvent

case class CartItemChangeAmountEvent(itemId: UUID, amount: Int) extends CartEvent
