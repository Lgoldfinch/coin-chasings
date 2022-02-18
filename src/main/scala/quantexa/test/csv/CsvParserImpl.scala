package quantexa.test.csv

import cats.effect.unsafe.implicits.global
import cats.effect.{Async, ExitCode, IO, IOApp, Resource, Sync}
import cats.implicits.catsSyntaxTuple3Parallel
import io.chrisdavenport.cormorant
import quantexa.test.models.{AverageValueOfTransactions, StatisticsFromPreviousDays, TotalTransactionsPerDay, Transaction}

import scala.annotation.tailrec
import scala.io.Source
import quantexa.test.utils.MathsUtils._

object CsvParserImpl extends App {

  private val fileName = "/Users/lg/IdeaProjects/coin-chaser/src/main/scala/quantexa/test/resources/transactions.csv"
  private val transcationParser = new CsvParser[Transaction]{}
  private val totalTransactionsPerDayParser = new CsvParser[TotalTransactionsPerDay] {}
  private val averageValueOfTransactionsParser = new CsvParser[AverageValueOfTransactions] {}
  private val statisticsFromPreviousDaysParser = new CsvParser[StatisticsFromPreviousDays] {}
  private val transactionslines: String = Source.fromFile(fileName).mkString("")

  def totalTransactionValueByDay(csv: String): Either[cormorant.Error, List[TotalTransactionsPerDay]] =
    transcationParser.readCsv(csv).map {
      groupByTransactionDay(_).map { keyValuePair =>
        val day = keyValuePair._1
        val transactionAmounts = keyValuePair._2

        TotalTransactionsPerDay(day, transactionAmounts.sum)
      }.toList
  }

  def averageTransactionValueForAnAccountByCategory(csv: String): Either[cormorant.Error, List[AverageValueOfTransactions]] =
    transcationParser.readCsv(csv).map {
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
    transcationParser.readCsv(csv).map {
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

  val q1 = totalTransactionValueByDay(transactionslines).toOption.getOrElse(Nil)
  val q2 = averageTransactionValueForAnAccountByCategory(transactionslines).toOption.getOrElse(Nil)
  val q3 = getStatisticsFromPrevious5Days(transactionslines).toOption.getOrElse(Nil)

  def provideQuantexaWithSweetSweetAnswers = {
    (IO(totalTransactionsPerDayParser.writeToFile("question1", q1)),
    IO(averageValueOfTransactionsParser.writeToFile("question2", q2)),
    IO(statisticsFromPreviousDaysParser.writeToFile("question3", q3))).parTupled
  }

  provideQuantexaWithSweetSweetAnswers.unsafeRunSync()

  private def groupByTransactionDay(transactions: List[Transaction]): Map[Int, List[Float]] =
    transactions.groupMap(_.transactionDay)(_.transactionAmount)

  private def groupByAccountId(transactions: List[Transaction]): Map[String, List[(String, Float)]] =
    transactions.groupMap(_.accountId)(transaction => (transaction.category, transaction.transactionAmount))

  private def groupByCategory(categoriesWithTransactions: List[(String, Float)]): Map[String, List[Float]] =
    categoriesWithTransactions.groupMap(_._1)(_._2)

}
