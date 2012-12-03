import java.io.{File, FilenameFilter}
import java.lang.String
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import org.fest.assertions.Assertions._
import org.specs2.mutable.Specification
import play.api.Logger
import play.api.test.FakeApplication
import play.api.test.Helpers._
import play.api.test._
import org.apache.commons.io.FileUtils
import play.api.test.TestServer
import scala.collection.JavaConversions._
import org.fluentlenium.core.domain._

object ConsoleColors {
  val Red = "\u001B[31m"
  val Normal = "\u001B[0m";
  val Green = "\u001B[32m";

  def red(o: Any) = Red + o + Normal
  def green(o: Any) = Green + o + Normal
}

class QUnitTestsRunner extends Specification {
  import ConsoleColors._

  val TestFileExtension = "test.html"
  val Port = 3333
  val baseUrl: String = "http://localhost:" + Port + "/@qunit"
  val selectorFailedCounter: String = "#qunit-testresult .failed"


  "console qunit tests" should {

    "should fail message" in {
      1 === 1

      11 === 2

      22 === 4
    }


    "run with selenium" in {
      running(TestServer(Port), HTMLUNIT) {
        browser =>
          val testFolder = play.api.Play.current.getFile("/test/")
          val testFiles = FileUtils.listFiles(testFolder, Array(TestFileExtension), true)
          testFiles foreach {
            file =>
              browser.goTo(baseUrl + "?htmlFile=" + urlEncode(file.getPath))
              browser.await.atMost(4, TimeUnit.SECONDS).until(selectorFailedCounter).hasSize(1)


              /*
              Hide passed testsCheck for GlobalsNo try-catchModule: < All Modules >module 1module 2
Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)
Tests completed in 117 milliseconds.
0 tests of 5 passed, 5 failed.
test description (1, 0, 1)Rerunassertion message
module 1: test description test 1 module 1 (1, 0, 1)Rerunassertion message test 1 module 1
module 1: test description test 2 module 1 (1, 0, 1)Rerunassertion message test 2 module 1
module 2: test description test 1 module 2 (1, 0, 1)Rerunassertion message test 1 module 2
module 2: test description test 2 module 1 (1, 0, 1)Rerunassertion message test 2 module 1]

               */
//              log(browser.$("body").getTexts())
//              log(browser.$("#qunit-testresult").getTexts());//returns [Tests completed in 117 milliseconds.\n 0 tests of 5 passed, 5 failed.]

              val log = new StringBuilder

              val testCaseSuccessMessages = browser.$("#qunit-tests > li > strong")
              var lastModuleName = ""
              testCaseSuccessMessages foreach {element =>
                val moduleName = element.find(".module-name").headOption map (_.getText()) getOrElse "default module"
                val haveToWriteModuleName = moduleName != "" && lastModuleName != moduleName
                if (haveToWriteModuleName) {
                  log ++= moduleName ++= "\n"
                }
                val testName = element.find(".test-name").headOption map (_.getText()) getOrElse "<no testname>"
                val failedCount = (element.find(".failed").headOption map (_.getText()) getOrElse "<no failed count>").toInt
                val passedCount = (element.find(".passed").headOption map (_.getText()) getOrElse "<no failed count>").toInt
                val sumAssertions = (failedCount + passedCount).toString
                log ++= "    " ++=testName ++= " (" ++= red(failedCount) ++= ", " + green(passedCount) ++= ", "  ++= sumAssertions ++= ")\n"

                lastModuleName = moduleName

              }
              assertThat(browser.$(selectorFailedCounter, 0).getText).overridingErrorMessage("some qunit tests had failed:\n" + log).isEqualTo("0")
          }
          1 === 1
      }
    }
  }

  def urlEncode(s: String): String = {
    URLEncoder.encode(s, "utf-8")
  }
}
