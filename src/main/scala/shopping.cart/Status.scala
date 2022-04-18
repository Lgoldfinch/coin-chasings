package shopping.cart

import io.circe.Encoder

sealed trait Status
object Status {
  case object Okay extends Status
  case object Unreachable extends Status

  val _Bool: Iso[Status, Boolean] =
    Iso[Status, Boolean] {
      case Okay         => true
      case Unreachable  => false
    }(if (_) Okay else Unreachable)
  implicit val jsonEncoder: Encoder[Status] =
    Encoder.forProduct1("status")(_.toString)
}
