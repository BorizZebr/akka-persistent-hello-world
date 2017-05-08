package com.zebrosoft

import com.zebrosoft.model._
import shapeless.{Generic, Poly1, Poly2}

/**
  * Created by borisbondarenko on 08.05.17.
  */
trait CartEventCommandLogic {

  protected val genCartCommand = Generic[CartCommand]

  protected val genCartEvent = Generic[CartEvent]

  protected object cmd2Evnt extends Poly1 {
    implicit def caseCloseCart    = at[CloseCartCommand]   (_ => CloseCartEvent)
    implicit def caseAddItem      = at[AddItemCommand]     (c => AddItemEvent(c.item, c.amount))
    implicit def caseRemoveItem   = at[RemoveItemCommand]  (c => RemoveItemEvent(c.item))
    implicit def caseAmountChange = at[ChangeAmountCommand](c => ChangeAmountEvent(c.item, c.newAmount))
  }

  protected object evnt2Stt extends Poly2 {
    implicit def caseCloseCart     = at[Cart, CloseCartEvent.type]((c, _)  => c.close)
    implicit def caseAddItem       = at[Cart, AddItemEvent]       ((c, a)  => c.addItem(Item(a.itemId, a.amount)))
    implicit def caseRemoveItem    = at[Cart, RemoveItemEvent]    ((c, r)  => c.removeItem(r.itemId))
    implicit def caseAmountChanged = at[Cart, ChangeAmountEvent]  ((c, ac) => c.changeAmount(ac.itemId, ac.amount))
  }

  def foldState(c: Cart)(e: => CartEvent): Cart =
    genCartEvent.to(e).foldLeft(c)(evnt2Stt)

  val commandToEvent: CartCommand => CartEvent = cmd =>
    (genCartCommand.to(cmd) map cmd2Evnt).unify

}
