object ConsoleColors {
  val Red = "\u001B[31m"
  val Normal = "\u001B[0m";
  val Green = "\u001B[32m";

  def red(o: Any) = Red + o + Normal
  def green(o: Any) = Green + o + Normal
}
