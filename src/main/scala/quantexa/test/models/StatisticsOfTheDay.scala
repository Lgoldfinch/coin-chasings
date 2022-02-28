package quantexa.test.models

import io.chrisdavenport.cormorant.LabelledWrite
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.{deriveDecoder, deriveEncoder}
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.implicits._
import quantexa.test.utils.MathsUtils.mean
import quantexa.test.utils.floatFromListWithDefault

final case class StatisticsOfTheDay(day: Int, accountId: String, maximum: Float, average: Float, aaTotalValue: Float, ccTotalValue: Float, ffTotalValue: Float)

object StatisticsOfTheDay {
  def calculatesStatsOfTheDayUsingPast(current: StatisticsOfTheDay, last5Days: List[StatisticsOfTheDay]): StatisticsOfTheDay = {
    val maximumTransactionValue = floatFromListWithDefault(last5Days)(_.maximum, _.maxOption)
    val averageTransactionValue = floatFromListWithDefault(last5Days)(_.average, mean)

    val aaTotalValue = last5Days.map(_.aaTotalValue).sum
    val ccTotalValue = last5Days.map(_.ccTotalValue).sum
    val ffTotalValue = last5Days.map(_.ffTotalValue).sum

    StatisticsOfTheDay(current.day, current.accountId, maximumTransactionValue, averageTransactionValue, aaTotalValue, ccTotalValue, ffTotalValue)
  }

  def apply(day: Int, accountId: String, currentDayTransactions: List[Transaction]): StatisticsOfTheDay = {
    val maximumTransactionValue = floatFromListWithDefault(currentDayTransactions)(_.transactionAmount, _.maxOption)
    val averageTransactionValue = floatFromListWithDefault(currentDayTransactions)(_.transactionAmount, mean)

    val aaTotalValue = sumByTransactionCategory(currentDayTransactions, "AA")
    val ccTotalValue = sumByTransactionCategory(currentDayTransactions, "CC")
    val ffTotalValue = sumByTransactionCategory(currentDayTransactions, "FF")

    StatisticsOfTheDay(day, accountId, maximumTransactionValue, averageTransactionValue, aaTotalValue, ccTotalValue, ffTotalValue)
  }

  private def sumByTransactionCategory(last5DaysOfTransactions: List[Transaction], category: String): Float =
    last5DaysOfTransactions.filter(_.category == category).map(_.transactionAmount).sum

  implicit val encoder: Encoder[StatisticsOfTheDay] = deriveEncoder[StatisticsOfTheDay]
  implicit val decoder: Decoder[StatisticsOfTheDay] = deriveDecoder[StatisticsOfTheDay]

  implicit val lw: LabelledWrite[StatisticsOfTheDay] = deriveLabelledWrite
}
