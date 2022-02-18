package quantexa.test.csv
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.parser._
import io.chrisdavenport.cormorant.implicits._
import cats.implicits._
import io.chrisdavenport.cormorant.LabelledRead

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

trait CsvParser[A] {
  def readCsv(csv: String)(implicit R: LabelledRead[A]): Either[Error, List[A]] = parseComplete(csv).leftWiden[Error].flatMap(_.readLabelled[A].sequence)

  def writeCsv(l: List[A])(implicit W: LabelledWrite[A]) = l.writeComplete.print(Printer.default)


  def writeToFile(fileName: String, csvReadResult: List[A])(implicit W: LabelledWrite[A]): Path = {
    val asCsv = writeCsv(csvReadResult)
    Files.write(Paths.get(s"$fileName.csv"), asCsv.getBytes(StandardCharsets.UTF_8))
  }
}
