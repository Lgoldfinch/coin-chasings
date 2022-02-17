package quantexa.test.utils

object MathsUtils {
  def mean(a: List[Float]): Option[Float] = {
    a.length match {
      case 0 => None
      case positiveInt => Some(a.sum / positiveInt)
    }
  }
}
