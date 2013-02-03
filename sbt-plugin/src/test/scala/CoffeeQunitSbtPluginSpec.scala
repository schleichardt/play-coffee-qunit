import org.specs2.mutable._

class CoffeeQunitSbtPluginSpec extends Specification {

  "CoffeeQunitSbtPlugin.templatePathoToClassName" should {
    import info.schleichardt.playcoffeequnit.sbt.CoffeeQunitSbtPlugin.templatePathoToClassName
    "work without subpackages" in {
      templatePathoToClassName("testInCoffeeScript.scala.html") === "views.html.testInCoffeeScript"
    }
    "work in direkt subpackages'" in {
      templatePathoToClassName("subfolder/testInJavaScript.scala.html") === "views.html.subfolder.testInJavaScript"
    }
    "work in deeper subpackages" in {
      templatePathoToClassName("subfolder/subsubfolder/testInJavaScript.scala.html") === "views.html.subfolder.subsubfolder.testInJavaScript"
    }
  }
}