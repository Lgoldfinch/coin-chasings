package quantexa.test.models

import io.chrisdavenport.cormorant.LabelledWrite
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.implicits._

import quantexa.test.utils.MathsUtils.mean

final case class StatisticsOfTheDay(day: Int, accountId: String, maximum: Float, average: Float, aaTotalValue: Float, ccTotalValue: Float, ffTotalValue: Float)

object StatisticsOfTheDay {

  def apply(transaction: Transaction, previousDaysOfTransactions: List[Transaction]): StatisticsOfTheDay = {
    val previousDaysTransactionAmounts = previousDaysOfTransactions.map(_.transactionAmount)
    val maximumTransactionValue = previousDaysTransactionAmounts.maxOption.getOrElse(0f)
    val averageTransactionValue = mean(previousDaysTransactionAmounts).getOrElse(0f)
    val aaTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "AA")
    val ccTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "CC")
    val ffTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "FF")

    StatisticsOfTheDay(
      transaction.transactionDay,
      transaction.accountId,
      maximumTransactionValue,
      averageTransactionValue,
      aaTotalValue,
      ccTotalValue,
      ffTotalValue)

  }

  def defaultStatisticsOfTheDay(day: Int, accountId: String): StatisticsOfTheDay = {
    StatisticsOfTheDay(day, accountId, 0, 0, 0, 0, 0)
  }
// def apply(transaction: Transaction, previousDaysOfTransactions: List[Transaction]): StatisticsOfTheDay = {
//    val previousDaysTransactionAmounts = previousDaysOfTransactions.map(_.transactionAmount)
//    val maximumTransactionValue = previousDaysTransactionAmounts.maxOption.getOrElse(0f)
//    val averageTransactionValue = mean(previousDaysTransactionAmounts).getOrElse(0f)
//    val aaTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "AA")
//    val ccTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "CC")
//    val ffTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "FF")
//
//    StatisticsOfTheDay(
//      transaction.transactionDay,
//      transaction.accountId,
//      maximumTransactionValue,
//      averageTransactionValue,
//      aaTotalValue,
//      ccTotalValue,
//      ffTotalValue)
//
//  }

  private def sumByTransactionCategory(last5DaysOfTransactions: List[Transaction], category: String): Float =
    last5DaysOfTransactions.filter(_.category == category).map(_.transactionAmount).sum

  implicit val encoder: Encoder[StatisticsOfTheDay] = deriveEncoder[StatisticsOfTheDay]
  implicit val decoder: Decoder[StatisticsOfTheDay] = deriveDecoder[StatisticsOfTheDay]

  implicit val lw: LabelledWrite[StatisticsOfTheDay] = deriveLabelledWrite
}
