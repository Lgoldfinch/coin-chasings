package quantexa.test.models

import io.chrisdavenport.cormorant.LabelledWrite
import io.chrisdavenport.cormorant.generic.semiauto.deriveLabelledWrite
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.implicits._

final case class TotalTransactionsPerDay(day: Int, totalValue: Float)

object TotalTransactionsPerDay {
  implicit val encoder: Encoder[TotalTransactionsPerDay] = deriveEncoder[TotalTransactionsPerDay]
  implicit val decoder: Decoder[TotalTransactionsPerDay] = deriveDecoder[TotalTransactionsPerDay]

  implicit val lw: LabelledWrite[TotalTransactionsPerDay] = deriveLabelledWrite
}