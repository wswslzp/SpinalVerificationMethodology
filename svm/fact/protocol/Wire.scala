package fact.protocol

trait Direction
object Input extends Direction
object Output extends Direction

case class Wire(
  val name: String = "null",
  val bitWidth: BigInt = 0,
  val direction: Direction = Input,
) {

}

object Wire {
  def main(args: Array[String]): Unit = {
    val w = Wire()
    val values = w.getClass().getMethods()
    for(v <- values) {
      println(v.toString())
    }
  }
}