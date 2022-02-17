package quantexa.test.models

import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.generic.semiauto._
import io.circe._
import io.circe.generic.semiauto._
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.parser._
import io.chrisdavenport.cormorant.implicits._
import cats.implicits._
import java.util.UUID
import java.time.Instant
final case class Transaction(transactionId: String, accountId: String, transactionDay: Int, category: String, transactionAmount: Float)

object Transaction {
  implicit val encoder: Encoder[Transaction] = deriveEncoder[Transaction]
  implicit val decoder: Decoder[Transaction] = deriveDecoder[Transaction]

  implicit val lr: LabelledRead[Transaction] = deriveLabelledRead
  implicit val lw: LabelledWrite[Transaction] = deriveLabelledWrite
}
