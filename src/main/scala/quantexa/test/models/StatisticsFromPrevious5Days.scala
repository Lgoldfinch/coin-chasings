package quantexa.test.models

import io.chrisdavenport.cormorant.LabelledWrite
import io.chrisdavenport.cormorant.generic.semiauto.deriveLabelledWrite
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.parser._
import io.chrisdavenport.cormorant.implicits._

final case class StatisticsFromPrevious5Days(day: Int, accountId: String, maximum: Float, average: Float, aaTotalValue: Float, ccTotalValue: Float, ffTotalValue: Float)

object StatisticsFromPrevious5Days {
  implicit val encoder: Encoder[StatisticsFromPrevious5Days] = deriveEncoder[StatisticsFromPrevious5Days]
  implicit val decoder: Decoder[StatisticsFromPrevious5Days] = deriveDecoder[StatisticsFromPrevious5Days]

  implicit val lw: LabelledWrite[StatisticsFromPrevious5Days] = deriveLabelledWrite
}
