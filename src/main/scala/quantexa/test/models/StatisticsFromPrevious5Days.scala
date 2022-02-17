package quantexa.test.models

import io.chrisdavenport.cormorant.LabelledWrite
import io.chrisdavenport.cormorant.generic.semiauto.deriveLabelledWrite
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.parser._
import io.chrisdavenport.cormorant.implicits._
import quantexa.test.csv.CsvParserImpl.sumByTransactionCategory
import quantexa.test.utils.MathsUtils.mean

final case class StatisticsFromPrevious5Days(day: Int, accountId: String, maximum: Float, average: Float, aaTotalValue: Float, ccTotalValue: Float, ffTotalValue: Float)

object StatisticsFromPrevious5Days {

  def apply(transaction: Transaction, last5DaysTransactions: List[Transaction]): StatisticsFromPrevious5Days = {
    val last5DaysTransactionAmounts = last5DaysTransactions.map(_.transactionAmount)
    val maximumTransactionValue = last5DaysTransactionAmounts.maxOption.getOrElse(0f)
    val averageTransactionValue = mean(last5DaysTransactionAmounts).getOrElse(0f)
    val aaTotalValue = sumByTransactionCategory(last5DaysTransactions, "AA")
    val ccTotalValue = sumByTransactionCategory(last5DaysTransactions, "CC")
    val ffTotalValue = sumByTransactionCategory(last5DaysTransactions, "FF")

    StatisticsFromPrevious5Days(
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

  implicit val encoder: Encoder[StatisticsFromPrevious5Days] = deriveEncoder[StatisticsFromPrevious5Days]
  implicit val decoder: Decoder[StatisticsFromPrevious5Days] = deriveDecoder[StatisticsFromPrevious5Days]

  implicit val lw: LabelledWrite[StatisticsFromPrevious5Days] = deriveLabelledWrite
}
