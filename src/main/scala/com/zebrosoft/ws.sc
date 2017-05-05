import shapeless._

sealed trait A
case object B extends A
case object C extends A
case class D(d: String) extends A

val gen = Generic[A]

object function extends Poly1 {
  implicit def caseB = at[B.type](_ => "That is B")
  implicit def caseC = at[C.type](_ => "That is C")
  implicit def caseD = at[D](_.d)
}

val akaka: A = B
val value = gen.to(akaka)
val text: String = (value map function).unify

def something(a: A) = {
  val value = gen.to(a)
  (value map function).unify
}

val text2 = something(D("azaza"))

