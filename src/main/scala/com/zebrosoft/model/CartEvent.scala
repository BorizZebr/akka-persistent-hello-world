package com.zebrosoft.model

import java.util.UUID

/**
  * Created by borisbondarenko on 06.03.17.
  */
sealed trait CartEvent {
  val time: Long = System.currentTimeMillis
}

case object CloseCartEvent extends CartEvent
case class AddItemEvent(itemId: UUID, amount: Int) extends CartEvent
case class RemoveItemEvent(itemId: UUID) extends CartEvent
case class ChangeAmountEvent(itemId: UUID, amount: Int) extends CartEvent
