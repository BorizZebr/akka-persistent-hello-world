import shapeless._
import syntax.typeable._

val l: Any = List(Vector("foo", "bar", "baz"), Vector("wibble"))

l.cast[List[Vector[String]]]