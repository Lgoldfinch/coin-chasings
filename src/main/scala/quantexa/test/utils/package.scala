package quantexa.test

package object utils {
   def floatFromListWithDefault[A](l: List[A])(f: A => Float, g: List[Float] => Option[Float]): Float = {
    g(l.map(f)).getOrElse(0f)
  }
}
