package com.zebrosoft

import java.util.UUID

import akka.actor.{ActorRef, ActorSystem}
import akka.pattern.ask
import akka.testkit.{DefaultTimeout, ImplicitSender, TestActor, TestActorRef, TestKit, TestProbe}
import com.zebrosoft.model.{CartHandlerTerminate, CartId, CartIdleTimeout}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpecLike}

/**
  * Created by borisbondarenko on 18.04.17.
  */
class CartManagerSpec
  extends TestKit(ActorSystem("system"))
    with WordSpecLike
    with BeforeAndAfterAll
    with DefaultTimeout
    with ImplicitSender
    with ScalaFutures
    with Matchers {

  override protected def afterAll(): Unit = TestKit.shutdownActorSystem(system)

  "Cart manager actor" should {

    trait scope {
      val testAnswer = 42
      val knownId = UUID.randomUUID
      val unknownId = UUID.randomUUID
      val knownHandler = TestProbe()
      val unknownHandler = TestProbe()

      val pilot = new TestActor.AutoPilot {
        def run(sender: ActorRef, msg: Any): TestActor.AutoPilot = msg match {
          case _ => sender ! testAnswer; TestActor.KeepRunning
        }
      }

      knownHandler.setAutoPilot(pilot)
      unknownHandler.setAutoPilot(pilot)

      def getMsg(id: UUID) = new CartId {
        override def cartId: UUID = id
      }

      val manager = TestActorRef(new CartsManager {
        cmdHandlers = cmdHandlers + (knownId -> knownHandler.ref)
        override def createHandler(id: UUID): ActorRef = unknownHandler.ref
      })
    }

    "handle idle timeout from child handler" in new scope {
      val res = manager ? CartIdleTimeout(knownId)
      whenReady(res) { r =>
        r shouldBe CartHandlerTerminate
        manager.underlyingActor.cmdHandlers should not contain key(knownId)
      }
    }

    "transfer cart query to existing handler" in new scope {
      val msg = getMsg(knownId)
      val res = manager ? msg
      knownHandler expectMsg msg
      whenReady(res)(_ shouldBe testAnswer)
    }

    "transfer cart query for non existing handler" in new scope {
      val msg = getMsg(unknownId)
      val res = manager ? msg
      unknownHandler expectMsg msg
      whenReady(res) { r =>
        r shouldBe testAnswer
        manager.underlyingActor.cmdHandlers should contain key(unknownId)
      }
    }
  }
}
