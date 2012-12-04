import java.io.File
import java.lang.String
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import org.fest.assertions.Assertions._
import org.specs2.matcher.{Expectable, Matcher, MatchFailure}
import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test._
import org.apache.commons.io.FileUtils
import play.api.test.TestServer
import scala.Array
import scala.collection.JavaConversions._

object QUnitMatcher extends Matcher[QUnitTestResult] {
  def apply[S <: QUnitTestResult](s: Expectable[S]) = {
    result(s.value.failedCount < 1,
      s.description + " +++++++++++",
      s.value.assertionErrors.mkString("\n"),
      s)
  }
}

case class QUnitTestResult(moduleName: String, testName: String, failedCount: Int, passedCount: Int, assertionErrors: Seq[String])

abstract class QUnitTestsRunner extends Specification {
  import ConsoleColors._

  val TestFileExtension = "test.html"
  val Port = 3333
  val baseUrl: String = "http://localhost:" + Port + "/@qunit"
  val selectorFailedCounter: String = "#qunit-testresult .failed"


  "QUnit tests" should {
    running(TestServer(Port), HTMLUNIT) {
      browser =>
        val testFolder = play.api.Play.current.getFile("/test/")
        val testFiles = FileUtils.listFiles(testFolder, Array(TestFileExtension), true)
        testFiles foreach {
          file =>
            goToQUnitTestPage(browser, file)
            waitUntilJavaScriptTestsFinished(browser)
            val results: Seq[QUnitTestResult] = collectTestResults(browser)
            for (res <- results) {
              res.moduleName in {
                res.testName in {
                    res must QUnitMatcher
                }
              }
            }
        }
        1 === 1
    }
  }

  def collectTestResults(browser: TestBrowser): Seq[QUnitTestResult] = {
    val testCaseSuccessMessages = browser.$("#qunit-tests > li")
    val result = testCaseSuccessMessages map {
      listItem =>
        val element = listItem.find("strong")
        val moduleName = element.find(".module-name").headOption map (_.getText()) getOrElse "default module"
        val testName = element.find(".test-name").headOption map (_.getText()) getOrElse "<no testname>"
        val failedCount = (element.find(".failed").headOption map (_.getText()) getOrElse "<no failed count>").toInt
        val passedCount = (element.find(".passed").headOption map (_.getText()) getOrElse "<no passed count>").toInt
        val assertionErrors =
          if (failedCount > 0) {
            listItem.find("ol > li") map (_.getText)
          } else Seq[String]()
        QUnitTestResult(moduleName, testName, failedCount, passedCount, assertionErrors)
    }
    result
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
