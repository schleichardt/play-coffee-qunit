import java.io.File
import java.lang.String
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import org.fest.assertions.Assertions._
import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test._
import org.apache.commons.io.FileUtils
import play.api.test.TestServer
import scala.Array
import scala.collection.JavaConversions._


abstract class QUnitTestsRunner extends Specification {
  import ConsoleColors._

  val TestFileExtension = "test.html"
  val Port = 3333
  val baseUrl: String = "http://localhost:" + Port + "/@qunit"
  val selectorFailedCounter: String = "#qunit-testresult .failed"


  "QUnit tests" should {
    "run with selenium" in {
      running(TestServer(Port), HTMLUNIT) {
        browser =>
          val testFolder = play.api.Play.current.getFile("/test/")
          val testFiles = FileUtils.listFiles(testFolder, Array(TestFileExtension), true)
          testFiles foreach {
            file =>
              goToQUnitTestPage(browser, file)
              waitUntilJavaScriptTestsFinished(browser)
              val log = collectConsoleOutput(browser)
              assertThat(browser.$(selectorFailedCounter, 0).getText).overridingErrorMessage(log.toString).isEqualTo("0")
          }
          1 === 1
      }
    }
  }


  def collectConsoleOutput(browser: TestBrowser): StringBuilder = {
    val log = new StringBuilder
    val testCaseSuccessMessages = browser.$("#qunit-tests > li")
    var lastModuleName = ""
    testCaseSuccessMessages foreach {
      listItem =>
        val element = listItem.find("strong")
        val moduleName = element.find(".module-name").headOption map (_.getText()) getOrElse "default module"
        val haveToWriteModuleName = moduleName != "" && lastModuleName != moduleName
        if (haveToWriteModuleName) {
          log ++= moduleName ++= "\n"
        }
        val testName = element.find(".test-name").headOption map (_.getText()) getOrElse "<no testname>"
        val failedCount = (element.find(".failed").headOption map (_.getText()) getOrElse "<no failed count>").toInt
        val passedCount = (element.find(".passed").headOption map (_.getText()) getOrElse "<no passed count>").toInt
        val sumAssertions = (failedCount + passedCount).toString
        log ++= "    " ++= testName ++= " (" ++= red(failedCount) ++= ", " + green(passedCount) ++= ", " ++= sumAssertions ++= ")\n"

        if (failedCount > 0) {
          log ++= "        "
          val errorMessages = listItem.find("ol > li") map (_.getText)
          errorMessages.addString(log, "", "\n        ", "\n")
        }
        lastModuleName = moduleName
    }
    log ++= "\n\n"
  }

  def goToQUnitTestPage(browser: TestBrowser, file: File) {
    browser.goTo(baseUrl + "?htmlFile=" + urlEncode(file.getPath))
  }

  def waitUntilJavaScriptTestsFinished(browser: TestBrowser) {
    browser.await.atMost(4, TimeUnit.SECONDS).until(selectorFailedCounter).hasSize(1)
  }

  def urlEncode(s: String): String = {
    URLEncoder.encode(s, "utf-8")
  }
}
