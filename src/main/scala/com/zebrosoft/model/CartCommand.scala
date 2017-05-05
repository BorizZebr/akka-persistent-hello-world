package com.zebrosoft.model

import java.util.UUID

/**
  * Created by borisbondarenko on 06.03.17.
  */
trait CartId {
  def cartId: UUID
}

sealed trait CartCommand extends CartId
case class CloseCartCommand(cartId: UUID) extends CartCommand
case class AddItemCommand(cartId: UUID, item: UUID, amount: Int) extends CartCommand
case class RemoveItemCommand(cartId: UUID, item: UUID) extends CartCommand
case class ChangeAmountCommand(cartId: UUID, item: UUID, newAmount: Int) extends CartCommand