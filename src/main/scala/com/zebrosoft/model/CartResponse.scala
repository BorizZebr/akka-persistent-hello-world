package com.zebrosoft.model

/**
  * Created by borisbondarenko on 05.04.17.
  */
sealed trait CartResponse

case object CartIdMissmatch extends CartResponse

case object CartAlreadyClosed extends CartResponse

case object Ack


