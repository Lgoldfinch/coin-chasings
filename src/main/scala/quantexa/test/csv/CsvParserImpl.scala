package quantexa.test.csv

import io.chrisdavenport.cormorant
import quantexa.test.models.{AverageValueOfTransactions, StatisticsFromPrevious5Days, TotalTransactionsPerDay, Transaction}

import scala.annotation.tailrec
import scala.io.Source
import quantexa.test.utils.MathsUtils._

object CsvParserImpl extends App {

  private val fileName = "/Users/lg/IdeaProjects/coin-chaser/src/main/scala/quantexa/test/resources/transactions.csv"
  private val transcationParser = new CsvParser[Transaction]{}
  private val totalTransactionsPerDayParser = new CsvParser[TotalTransactionsPerDay] {}
  private val bigModelParser = new CsvParser[AverageValueOfTransactions] {}
  private val previousDaysParser = new CsvParser[StatisticsFromPrevious5Days] {}
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

  def getStatisticsFromPrevious5Days(csv: String): Either[cormorant.Error, List[StatisticsFromPrevious5Days]] =
    transcationParser.readCsv(csv).map {
    transactions =>
      val transactionsByAccountId = transactions.groupBy(_.accountId)
      transactionsByAccountId.map{
                  case (accountId, transactions) =>
                    calculateStatisticsFromPrevious5Days(accountId, transactions)
                }.toList.flatten.sortWith(_.day < _.day)
  }

  @tailrec
  def calculateStatisticsFromPrevious5Days(accountId: String, transactions: List[Transaction], acc: List[StatisticsFromPrevious5Days] = Nil, statsFromPrevious5Days: List[Transaction] = Nil): List[StatisticsFromPrevious5Days] = {
    transactions match {
      case ::(transaction, next) =>
        val currentDay = transaction.transactionDay
        val l5d = statsFromPrevious5Days.dropWhile(_.transactionDay < currentDay - 4)
        val previousDays = StatisticsFromPrevious5Days(transaction, l5d)

        calculateStatisticsFromPrevious5Days(accountId, next, acc ++ List(previousDays), l5d ++ List(transaction))

      case Nil => acc
    }
  }

  val q1 = totalTransactionValueByDay(transactionslines)
  val q2 = averageTransactionValueForAnAccountByCategory(transactionslines)
  val q3 = getStatisticsFromPrevious5Days(transactionslines)

  def provideQuantexaWithSweetSweetAnswers = {
    totalTransactionsPerDayParser.writeToFile("question1", q1)
    bigModelParser.writeToFile("question2", q2)
    previousDaysParser.writeToFile("question3", q3)
  }

  provideQuantexaWithSweetSweetAnswers

  private def groupByTransactionDay(transactions: List[Transaction]): Map[Int, List[Float]] =
    transactions.groupMap(_.transactionDay)(_.transactionAmount)

  private def groupByAccountId(transactions: List[Transaction]): Map[String, List[(String, Float)]] =
    transactions.groupMap(_.accountId)(transaction => (transaction.category, transaction.transactionAmount))

  private def groupByCategory(categoriesWithTransactions: List[(String, Float)]): Map[String, List[Float]] =
    categoriesWithTransactions.groupMap(_._1)(_._2)
}
