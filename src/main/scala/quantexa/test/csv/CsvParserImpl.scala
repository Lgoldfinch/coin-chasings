package quantexa.test.csv

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import cats.implicits.catsSyntaxTuple3Parallel
import io.chrisdavenport.cormorant
import io.chrisdavenport.cormorant.LabelledWrite
import quantexa.test.models.{AverageValueOfTransactions, StatisticsFromPreviousDays, TotalTransactionsPerDay, Transaction}

import scala.annotation.tailrec
import scala.io.{BufferedSource, Source}
import quantexa.test.utils.MathsUtils._

import java.nio.file.Path

object CsvParserImpl extends App {

  private val fileName = "/Users/lg/IdeaProjects/coin-chaser/src/main/scala/quantexa/test/resources/transactions.csv"
  private val transactionParser = new CsvParser[Transaction]{}
  private implicit val totalTransactionsPerDayParser: CsvParser[TotalTransactionsPerDay] = new CsvParser[TotalTransactionsPerDay] {}
  private implicit val averageValueOfTransactionsParser: CsvParser[AverageValueOfTransactions] = new CsvParser[AverageValueOfTransactions] {}
  private implicit val statisticsFromPreviousDaysParser: CsvParser[StatisticsFromPreviousDays] = new CsvParser[StatisticsFromPreviousDays] {}
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

  def getStatisticsFromPrevious5Days(csv: String): Either[cormorant.Error, List[StatisticsFromPreviousDays]] =
    transactionParser.readCsv(csv).map {
    transactions =>
      val transactionsByAccountId = transactions.groupBy(_.accountId)
      transactionsByAccountId.map{
                  case (accountId, transactions) =>
                    calculateStatisticsFromPrevious5Days(accountId, transactions)
                }.toList.flatten.sortWith(_.day < _.day)
  }

  @tailrec
  def calculateStatisticsFromPrevious5Days(accountId: String, transactions: List[Transaction], acc: List[StatisticsFromPreviousDays] = Nil, statsFromPrevious5Days: List[Transaction] = Nil): List[StatisticsFromPreviousDays] = {
    transactions match {
      case ::(transaction, next) =>
        val currentDay = transaction.transactionDay
        val l5d = statsFromPrevious5Days.dropWhile(_.transactionDay < currentDay - 4)
        val previousDays = StatisticsFromPreviousDays(transaction, l5d)

        calculateStatisticsFromPrevious5Days(accountId, next, acc ++ List(previousDays), l5d ++ List(transaction))

      case Nil => acc
    }
  }

  val finalAnswers = for {
    source <- sourceHandling
    transactionLines = source.mkString("")
    question1 = writeAnswerToFile[TotalTransactionsPerDay]("question1", totalTransactionValueByDay(transactionLines))
    question2 = writeAnswerToFile[AverageValueOfTransactions]("question2", averageTransactionValueForAnAccountByCategory(transactionLines))
    question3 = writeAnswerToFile[StatisticsFromPreviousDays]("question3", getStatisticsFromPrevious5Days(transactionLines))
  } yield (question1, question2, question3).parTupled

  finalAnswers.use(identity).unsafeRunSync()

  private def writeAnswerToFile[A](fileName: String, f: Either[Throwable, List[A]])(implicit csvParser: CsvParser[A], labelledWrite: LabelledWrite[A]): IO[Path] =
    IO.fromEither(f.map(csvParser.writeToFile(fileName, _)))

  private def groupByTransactionDay(transactions: List[Transaction]): Map[Int, List[Float]] =
    transactions.groupMap(_.transactionDay)(_.transactionAmount)

  private def groupByAccountId(transactions: List[Transaction]): Map[String, List[(String, Float)]] =
    transactions.groupMap(_.accountId)(transaction => (transaction.category, transaction.transactionAmount))

  private def groupByCategory(categoriesWithTransactions: List[(String, Float)]): Map[String, List[Float]] =
    categoriesWithTransactions.groupMap(_._1)(_._2)

}
