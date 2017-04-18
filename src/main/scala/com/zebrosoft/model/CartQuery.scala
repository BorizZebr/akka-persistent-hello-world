package com.zebrosoft.model

import java.util.UUID

/**
  * Created by borisbondarenko on 06.03.17.
  */
trait CartQuery extends CartId

case class GetCartQuery(cartId: UUID) extends CartQuery
