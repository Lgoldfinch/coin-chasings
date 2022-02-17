package quantexa.test.models

import enumeratum._
import io.chrisdavenport.cormorant._
import io.chrisdavenport.cormorant.generic.semiauto._
import io.chrisdavenport.cormorant.parser._
import io.chrisdavenport.cormorant.implicits._

final case class BigModel(accountId: String, averageValue: Float, transactionType: String)

object BigModel {
    implicit val lw: LabelledWrite[BigModel] = deriveLabelledWrite
}

sealed trait TransactionCategory extends EnumEntry

object TransactionCategory extends Enum[TransactionCategory] with CirceEnum[TransactionCategory] {
    case object AA extends TransactionCategory
    case object BB extends TransactionCategory
    case object CC extends TransactionCategory
    case object DD extends TransactionCategory
    case object EE extends TransactionCategory
    case object FF extends TransactionCategory
    case object GG extends TransactionCategory

    implicit val lrAA: LabelledRead[AA.type] = deriveLabelledRead
    implicit val lrBB: LabelledRead[BB.type] = deriveLabelledRead
    implicit val lrCC: LabelledRead[CC.type] = deriveLabelledRead
    implicit val lrDD: LabelledRead[DD.type] = deriveLabelledRead
    implicit val lrEE: LabelledRead[EE.type] = deriveLabelledRead
    implicit val lrFF: LabelledRead[FF.type] = deriveLabelledRead
    implicit val lrGG: LabelledRead[GG.type] = deriveLabelledRead

  override def values: IndexedSeq[TransactionCategory] = findValues
}