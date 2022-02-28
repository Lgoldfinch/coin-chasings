package quantexa.test.csv

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import cats.implicits.catsSyntaxTuple3Parallel
import io.chrisdavenport.cormorant
import io.chrisdavenport.cormorant.LabelledWrite
import quantexa.test.models.{AverageValueOfTransactions, StatisticsOfTheDay, TotalTransactionsPerDay, Transaction}
import quantexa.test.utils.MathsUtils._
import quantexa.test.models.StatisticsOfTheDay._

import java.nio.file.Path
import scala.io.{BufferedSource, Source}

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

        val transactionsToStatisticsOfTheDay: List[StatisticsOfTheDay] = transactionsByAccountIdAndDay.map {
          case ((day, accountId), transactions) =>
            apply(day, accountId, transactions)
        }.toList.sortWith(_.day < _.day)

        val statisticsForEachDayByGroupId: Map[String, List[StatisticsOfTheDay]] = transactionsToStatisticsOfTheDay.groupBy(_.accountId)

      statisticsForEachDayByGroupId.map(_._2.foldLeft[(List[StatisticsOfTheDay], List[StatisticsOfTheDay])]((List.empty[StatisticsOfTheDay], List.empty[StatisticsOfTheDay])){
        case ((rowsForCsvOutput, last5Days), statsForDay) =>
          val daysInRollingWindow: List[StatisticsOfTheDay] = last5Days.dropWhile(_.day < statsForDay.day - 4)
          val nextRowsForCsvOutput: List[StatisticsOfTheDay] = rowsForCsvOutput ++ List(calculatesStatsOfTheDayUsingPast(statsForDay, daysInRollingWindow))
          val nextLast5Days = List(statsForDay) ++ daysInRollingWindow

            (nextRowsForCsvOutput, nextLast5Days)
        }).toList.flatMap(_._1).sortWith(_.day < _.day)
  }

  val finalAnswers = for {
    source <- sourceHandling
    transactionLines = source.mkString("")
    question1 = writeAnswerToFile("question1")(totalTransactionValueByDay(transactionLines))
    question2 = writeAnswerToFile("question2")(averageTransactionValueForAnAccountByCategory(transactionLines))
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
