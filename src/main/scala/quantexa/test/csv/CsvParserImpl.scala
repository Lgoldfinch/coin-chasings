package quantexa.test.csv

import io.chrisdavenport.cormorant
import quantexa.test.csv.CsvParserImpl.getStatisticsFromPrevious5Days
import quantexa.test.models.{BigModel, StatisticsFromPrevious5Days, TotalTransactionsPerDay, Transaction}

import scala.annotation.tailrec
import scala.io.Source
import quantexa.test.utils.MathsUtils._
object CsvParserImpl extends App {

  private val fileName = "/Users/lg/IdeaProjects/coin-chaser/src/main/scala/quantexa/test/resources/transactions.csv"
  private val transcationParser = new CsvParser[Transaction]{}
  private val totalTransactionsPerDayParser = new CsvParser[TotalTransactionsPerDay] {}
  private val bigModelParser = new CsvParser[BigModel] {}
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

  def averageTransactionValueForAnAccountByCategory(csv: String): Either[cormorant.Error, List[BigModel]] =
    transcationParser.readCsv(csv).map {
    transactions =>
      val categoryAndTransactionValueByAccountId: Map[String, List[(String, Float)]] = groupByAccountId(transactions)

      categoryAndTransactionValueByAccountId.map { case (accountId, categoriesWithTransactions) =>
        groupByCategory(categoriesWithTransactions).map {
          case (category, transactionValues) =>
          val transactionValuesMean = mean(transactionValues).get

          BigModel(accountId, transactionValuesMean, category)
        }.toList
      }.toList.flatten
  }

  def getStatisticsFromPrevious5Days(csv: String): Either[cormorant.Error, List[StatisticsFromPrevious5Days]] =
    transcationParser.readCsv(csv).map {
    transactions =>
      val transactionsByAccountId = transactions.groupBy(_.accountId)

      transactionsByAccountId.map{
                  case (accountId, transactions) =>
                    calculateStatisticsFromPast5Days(accountId, transactions)
                }.toList.flatten.sortWith(_.day < _.day)
  }

  @tailrec
  def calculateStatisticsFromPast5Days(accountId: String, transactions: List[Transaction], acc: List[StatisticsFromPrevious5Days] = Nil, statsFromPrevious5Days: List[Transaction] = Nil): List[StatisticsFromPrevious5Days] = {
    transactions match {
      case ::(transaction, next) =>
        val currentDay = transaction.transactionDay
        val l5d = statsFromPrevious5Days.dropWhile(_.transactionDay < currentDay - 4)
        val previousDays = create(transaction, l5d)

        calculateStatisticsFromPast5Days(accountId, next, acc ++ List(previousDays), l5d ++ List(transaction))

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

  private def create(transaction: Transaction, last5DaysTransactions: List[Transaction]) = {

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

  private def groupByTransactionDay(transactions: List[Transaction]): Map[Int, List[Float]] =
    transactions.groupMap(_.transactionDay)(_.transactionAmount)

  private def groupByAccountId(transactions: List[Transaction]): Map[String, List[(String, Float)]] =
    transactions.groupMap(_.accountId)(transaction => (transaction.category, transaction.transactionAmount))

  private def groupByCategory(categoriesWithTransactions: List[(String, Float)]): Map[String, List[Float]] =
    categoriesWithTransactions.groupMap(_._1)(_._2)
}

object AssumedIncorrectAttempts {
//  def question3(csv: String): Either[cormorant.Error, List[PreviousDays]] = transcationParser.readCsv(csv).map {
//    transactions =>
//      @tailrec
//      def yes(transactions: List[Transaction], acc: List[PreviousDays] = Nil, currentDay: Int = 0, last5Days: List[Transaction] = Nil): List[PreviousDays] = {
//
//        transactions match {
//          case ::(head, next) =>
//            val lastDays = if(last5Days.size == 5) {
//              last5Days.tail ++ List(head)
//            } else
//              last5Days ++ List(head)
//
//            val last5DaysTransactionAmounts = lastDays.map(_.transactionAmount)
//            val maximumTransactionValue = last5DaysTransactionAmounts.maxOption.getOrElse(0f)
//            val averageTransactionValue = mean(last5DaysTransactionAmounts).getOrElse(0f)
//            val aaTotalValue = lastDays.filter(_.category == "AA").map(_.transactionAmount).sum
//            val ccTotalValue = lastDays.filter(_.category == "CC").map(_.transactionAmount).sum
//            val ffTotalValue = lastDays.filter(_.category == "FF").map(_.transactionAmount).sum
//
//            val previousDays = PreviousDays(head.transactionDay, head.accountId, maximumTransactionValue, averageTransactionValue, aaTotalValue, ccTotalValue, ffTotalValue)
//            yes(next, acc ++ List(previousDays), currentDay + 1, lastDays)
//
//          case Nil => acc
//        }
//      }
//      yes(transactions)
//  }
//
//  private def mean(a: List[Float]): Option[Float] = {
//    a.length match {
//      case 0 => None
//      case positiveInt => Some(a.sum / positiveInt)
//    }
//  }
}
