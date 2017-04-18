package com.zebrosoft.model

import java.util.UUID

/**
  * Created by borisbondarenko on 06.03.17.
  */
case class Item(id: UUID, amount: Int) {
}

case class Cart(id: UUID, items: Map[UUID, Item] = Map.empty, isClosed: Boolean = false) {

  val isEmpty = items.isEmpty

  def close = this.copy(isClosed = true)

  def addItem(item: Item) = this.copy(items = items + (item.id -> item))

  def removeItem(itemId: UUID) = this.copy(items = items - itemId)

  def changeAmount(itemId: UUID, amount: Int) = {
    val newItems = items - itemId + (itemId -> Item(itemId, amount))
    this.copy(items = newItems)
  }
}
