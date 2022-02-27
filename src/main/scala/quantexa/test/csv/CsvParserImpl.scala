package quantexa.test.csv

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import cats.implicits.catsSyntaxTuple3Parallel
import io.chrisdavenport.cormorant
import io.chrisdavenport.cormorant.LabelledWrite
import quantexa.test.models.{AverageValueOfTransactions, StatisticsOfTheDay, TotalTransactionsPerDay, Transaction}

import scala.annotation.tailrec
import scala.io.{BufferedSource, Source}
import quantexa.test.utils.MathsUtils._

import java.nio.file.Path

object CsvParserImpl extends App {

  private val fileName = "/Users/lg/IdeaProjects/coin-chaser/src/main/scala/quantexa/test/resources/transactions.csv"
  private val transactionParser = new CsvParser[Transaction]{}
  private implicit val totalTransactionsPerDayParser: CsvParser[TotalTransactionsPerDay] = new CsvParser[TotalTransactionsPerDay] {}
  private implicit val averageValueOfTransactionsParser: CsvParser[AverageValueOfTransactions] = new CsvParser[AverageValueOfTransactions] {}
  private implicit val statisticsFromPreviousDaysParser: CsvParser[StatisticsOfTheDay] = new CsvParser[StatisticsOfTheDay] {}
  private val transactionLines = Source.fromFile(fileName)

  val sourceHandling: Resource[IO, BufferedSource] = Resource.make(IO.blocking(transactionLines))(source => IO.blocking(source.close()))

  def totalTransactionValueByDay(csv: String): Either[cormorant.Error, List[TotalTransactionsPerDay]] =
    transactionParser.readCsv(csv).map {
      groupByTransactionDay(_).map { keyValuePair =>
        val day = keyValuePair._1
        val transactionAmounts = keyValuePair._2

        TotalTransactionsPerDay(day, transactionAmounts.sum)
      }.toList
  }

  def averageTransactionValueForAnAccountByCategory(csv: String): Either[cormorant.Error, List[AverageValueOfTransactions]] =
    transactionParser.readCsv(csv).map {
    transactions =>

      val categoryAndTransactionValueByAccountId: Map[String, List[(String, Float)]] = groupByAccountId(transactions)

      categoryAndTransactionValueByAccountId.map { case (accountId, categoriesWithTransactions) =>
        groupByCategory(categoriesWithTransactions).map {
          case (category, transactionValues) =>
          val transactionValuesMean = mean(transactionValues).getOrElse(0f)

          AverageValueOfTransactions(accountId, transactionValuesMean, category)
        }.toList
      }.toList.flatten
  }

  def getStatisticsFromPrevious5Days(csv: String): Either[cormorant.Error, List[StatisticsOfTheDay]] =
    transactionParser.readCsv(csv).map {
    transactions =>

      val transactionsByAccountIdAndDay = transactions.groupBy(transaction => (transaction.transactionDay, transaction.accountId))

      // we have a bunch of days added together.
     val yes: List[StatisticsOfTheDay] = transactionsByAccountIdAndDay.map{
                  case (dayAndAccountIdTuple, transactions) =>
                    calculateStatisticsForDay(dayAndAccountIdTuple._1, dayAndAccountIdTuple._2, transactions)
                }.toList.sortWith(_.day < _.day)
      // could group by account here? Would give all days alongside an account

      val statisticsForEachDayByGroupId: Map[String, List[StatisticsOfTheDay]] = yes.groupBy(_.accountId)

      // we want to skip the day we're on but carry forward the previous
      statisticsForEachDayByGroupId.map(_._2.foldLeft[(List[StatisticsOfTheDay], List[StatisticsOfTheDay])]((List.empty[StatisticsOfTheDay], List.empty[StatisticsOfTheDay])){
        (acc, statsForDay) =>
          val properAcc = acc._1
          val last5Days = acc._2
          val l5Days: List[StatisticsOfTheDay] = last5Days.dropWhile(_.day < statsForDay.day - 4)
          val acccc: List[StatisticsOfTheDay] = properAcc ++ List(calculateMore(statsForDay, l5Days))
          val lasssssst: List[StatisticsOfTheDay] =  l5Days ++ List(statsForDay)

            (acccc, lasssssst)
        }).toList.flatMap(_._1).sortWith(_.day < _.day) // we just need to calculate the previous day and we're sorted
  }

//@tailrec
//  def churn(difference: Int, latest: StatisticsOfTheDay, acc: List[StatisticsOfTheDay]): List[StatisticsOfTheDay] =
//    if(difference <= 1) acc
//    else {
//      val nextDay = difference - 1 /// hmm
//
//      if(acc.isEmpty) {
//        val defaultStatOfTheDay = List(defaultDay1StatisticOfTheDay(latest.accountId)) ++ acc
//        churn(nextDay, latest, defaultStatOfTheDay)
//      }
//      else {
//        val fillGap = latest.copy(day = latest.day - nextDay)
//        val removeDaysBeforeRollingWindow: List[StatisticsOfTheDay] = acc.dropWhile(_.day < fillGap.day - 4)
//        val newAcc = List(fillGap) ++ removeDaysBeforeRollingWindow
//
////        val calculateNewStatistics = List(calculateMore(fillGap, newAcc))
//        churn(nextDay, latest, )
//      }
//    }

  // we dont want to store the value of the previous days

  def calculateMore(current: StatisticsOfTheDay, last5Days: List[StatisticsOfTheDay]) = {
    val maximumTransactionValue = last5Days.map(_.maximum).maxOption.getOrElse(0f)
    val averageTransactionValue = mean(last5Days.map(_.average)).getOrElse(0f)
    val aaTotalValue = last5Days.map(_.aaTotalValue).sum
    val ccTotalValue = last5Days.map(_.ccTotalValue).sum
    val ffTotalValue = last5Days.map(_.ffTotalValue).sum

    StatisticsOfTheDay(current.day, current.accountId, maximumTransactionValue, averageTransactionValue, aaTotalValue, ccTotalValue, ffTotalValue)
  }


  private def sumByTransactionCategory(last5DaysOfTransactions: List[Transaction], category: String): Float =
    last5DaysOfTransactions.filter(_.category == category).map(_.transactionAmount).sum

  def calculateStatisticsForDay(day: Int, accountId: String, currentDayTransactions: List[Transaction]) = {
    val max = currentDayTransactions.map(_.transactionAmount).maxOption.getOrElse(0f)
    val averageTransactionValue = mean(currentDayTransactions.map(_.transactionAmount)).getOrElse(0f)
    val aaTotalValue = sumByTransactionCategory(currentDayTransactions, "AA")
    val ccTotalValue = sumByTransactionCategory(currentDayTransactions, "CC")
    val ffTotalValue = sumByTransactionCategory(currentDayTransactions, "FF")

    StatisticsOfTheDay(day, accountId, max, averageTransactionValue, aaTotalValue, ccTotalValue, ffTotalValue)
  }

  val finalAnswers = for {
    source <- sourceHandling
    transactionLines = source.mkString("")
    question1 = writeAnswerToFile("question1")( totalTransactionValueByDay(transactionLines))
    question2 = writeAnswerToFile("question2")( averageTransactionValueForAnAccountByCategory(transactionLines))
    question3 = writeAnswerToFile("question3")(getStatisticsFromPrevious5Days(transactionLines))
  } yield (question1, question2, question3).parTupled

  finalAnswers.use(identity).unsafeRunSync()

  private def writeAnswerToFile[A](fileName: String)(f: Either[Throwable, List[A]])(implicit csvParser: CsvParser[A], labelledWrite: LabelledWrite[A]): IO[Path] =
    IO.fromEither(f.map(csvParser.writeToFile(fileName, _)))

  private def groupByTransactionDay(transactions: List[Transaction]): Map[Int, List[Float]] =
    transactions.groupMap(_.transactionDay)(_.transactionAmount)

  private def groupByAccountId(transactions: List[Transaction]): Map[String, List[(String, Float)]] =
    transactions.groupMap(_.accountId)(transaction => (transaction.category, transaction.transactionAmount))

  private def groupByCategory(categoriesWithTransactions: List[(String, Float)]): Map[String, List[Float]] =
    categoriesWithTransactions.groupMap(_._1)(_._2)

}
