package quantexa.test.models

import io.chrisdavenport.cormorant.LabelledWrite
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.implicits._

import quantexa.test.utils.MathsUtils.mean

final case class StatisticsFromPreviousDays(day: Int, accountId: String, maximum: Float, average: Float, aaTotalValue: Float, ccTotalValue: Float, ffTotalValue: Float)

object StatisticsFromPreviousDays {

  def apply(transaction: Transaction, previousDaysOfTransactions: List[Transaction]): StatisticsFromPreviousDays = {
    val previousDaysTransactionAmounts = previousDaysOfTransactions.map(_.transactionAmount)
    val maximumTransactionValue = previousDaysTransactionAmounts.maxOption.getOrElse(0f)
    val averageTransactionValue = mean(previousDaysTransactionAmounts).getOrElse(0f)
    val aaTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "AA")
    val ccTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "CC")
    val ffTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "FF")

    StatisticsFromPreviousDays(
      transaction.transactionDay,
      transaction.accountId,
      maximumTransactionValue,
      averageTransactionValue,
      aaTotalValue,
      ccTotalValue,
      ffTotalValue)

  }

  private def sumByTransactionCategory(last5DaysOfTransactions: List[Transaction], category: String): Float =
    last5DaysOfTransactions.filter(_.category == category).map(_.transactionAmount).sum

  implicit val encoder: Encoder[StatisticsFromPreviousDays] = deriveEncoder[StatisticsFromPreviousDays]
  implicit val decoder: Decoder[StatisticsFromPreviousDays] = deriveDecoder[StatisticsFromPreviousDays]

  implicit val lw: LabelledWrite[StatisticsFromPreviousDays] = deriveLabelledWrite
}
