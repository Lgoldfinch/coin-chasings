package quantexa.test.csv

import cats.effect.unsafe.implicits.global
import cats.effect.{IO, Resource}
import cats.implicits.catsSyntaxTuple3Parallel
import io.chrisdavenport.cormorant
import io.chrisdavenport.cormorant.LabelledWrite
import quantexa.test.models.StatisticsOfTheDay.sumByTransactionCategory
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
                }.toList
      // could group by account here? Would give all days alongside an account

      yes.groupBy(_.accountId).map(_._2.foldLeft(List.empty[StatisticsOfTheDay]){
        (acc, statsForDay) =>
        val last5Days: List[StatisticsOfTheDay] = acc.dropWhile(_.day < statsForDay.day - 4)

          println(statsForDay)
          println(last5Days + " last 5 days")
          println(calculateMore(statsForDay, last5Days) + " calculate result")

          acc ++ List(calculateMore(statsForDay, last5Days))
      }).toList.flatten.sortWith(_.day < _.day) // we just need to calculate the previous day and we're sorted
  }

  def calculateMore(current: StatisticsOfTheDay, last5Days: List[StatisticsOfTheDay]) = {
    val l = last5Days ++ List(current)
    val maximumTransactionValue = l.map(_.maximum).maxOption.getOrElse(0f)
    val averageTransactionValue = mean(l.map(_.average)).getOrElse(0f)
    val aaTotalValue = l.map(_.aaTotalValue).sum
    val ccTotalValue = l.map(_.ccTotalValue).sum
    val ffTotalValue = l.map(_.ffTotalValue).sum

    StatisticsOfTheDay(current.day, current.accountId, maximumTransactionValue, averageTransactionValue, aaTotalValue, ccTotalValue, ffTotalValue)

  }


  private def sumByTransactionCategory(last5DaysOfTransactions: List[Transaction], category: String): Float =
    last5DaysOfTransactions.filter(_.category == category).map(_.transactionAmount).sum

//def yes(previousDaysOfTransactions: List[Transaction]) = {
//  val previousDaysTransactionAmounts = previousDaysOfTransactions.map(_.transactionAmount)
//  val maximumTransactionValue = previousDaysTransactionAmounts.maxOption.getOrElse(0f)
//  val averageTransactionValue = mean(previousDaysTransactionAmounts).getOrElse(0f)
//  val aaTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "AA")
//  val ccTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "CC")
//  val ffTotalValue = sumByTransactionCategory(previousDaysOfTransactions, "FF")
//
//  StatisticsFromPreviousDays()
//}

  def calculateStatisticsForDay(day: Int, accountId: String, currentDayTransactions: List[Transaction]) = {
  val max = currentDayTransactions.map(_.transactionAmount).maxOption.getOrElse(0f)
  val averageTransactionValue = mean(currentDayTransactions.map(_.transactionAmount)).getOrElse(0f)
    val aaTotalValue = sumByTransactionCategory(currentDayTransactions, "AA")
    val ccTotalValue = sumByTransactionCategory(currentDayTransactions, "CC")
    val ffTotalValue = sumByTransactionCategory(currentDayTransactions, "FF")

  StatisticsOfTheDay(day, accountId, max, averageTransactionValue, aaTotalValue, ccTotalValue, ffTotalValue)
  //  values.foldLeft(StatisticsFromPreviousDays(0, "", 0, 0, 0, 0, 0)) {
//    case (StatisticsFromPreviousDays(_, accountId, maximum, average, aaTotalValue, ccTotalValue, ffTotalValue),
//          Transaction(_, accountId, transactionDay, category, transactionAmount)) =>
//      val maxTransactionValue = maximum.max(transactionAmount)

//
//      StatisticsFromPreviousDays(transactionDay, accountId, maxTransactionValue, _, )
  }
//    case (acc, t @ Transaction(transactionId, accountId, transactionDay, category, transactionAmount)) =>
//      val maximumTransactionValue = transactionAmount.max(a)
//
//      acc ++ List(StatisticsFromPreviousDays(t, values))
//  }

//   @tailrec
//  def calculateStatisticsFromPrevious5Days(accountId: String, transactions: List[Transaction], acc: List[StatisticsFromPreviousDays] = Nil, statsFromPrevious5Days: List[Transaction] = Nil): List[StatisticsFromPreviousDays] = {
//    transactions match {
//      case ::(transaction, next) =>
//        val currentDay = transaction.transactionDay
//        val l5d = statsFromPrevious5Days.dropWhile(_.transactionDay < currentDay - 4)
//        val previousDays = StatisticsFromPreviousDays(transaction, l5d)
//
//        println(currentDay + " current day")
//        println(l5d + "after droppage")
//        println(previousDays + " previous days")
//        println(acc ++ List(previousDays) + " next in acc")
//        println(statsFromPrevious5Days + " prev 5 days")
//
//        calculateStatisticsFromPrevious5Days(accountId = accountId, transactions = next, acc = acc ++ List(previousDays), statsFromPrevious5Days = l5d ++ List(transaction))
//
//      case Nil => acc
//      case Nil => acc
//    }
//  }

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
