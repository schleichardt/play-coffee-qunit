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

class QUnitTestsRunner extends Specification {

  val TestFileExtension = "test.html"
  val Port = 3333
  val baseUrl: String = "http://localhost:" + Port + "/@qunit"
  val selectorFailedCounter: String = "#qunit-testresult .failed"

  "console qunit tests" should {
    "run with selenium" in {
      running(TestServer(Port), HTMLUNIT) {
        browser =>
          val testFolder = play.api.Play.current.getFile("/test/")
          val testFiles = FileUtils.listFiles(testFolder, Array(TestFileExtension), true)
          testFiles foreach {
            file =>
              browser.goTo(baseUrl + "?htmlFile=" + urlEncode(file.getPath))
              browser.await.atMost(4, TimeUnit.SECONDS).until(selectorFailedCounter).hasSize(1)
              val debugOutput = browser.$("#qunit-testresult", 0).getText + "\n" + browser.$("li.fail").getTexts.mkString("\n")
              assertThat(browser.$(selectorFailedCounter, 0).getText).overridingErrorMessage("some qunit tests had failed:\n" + debugOutput).isEqualTo("0")
          }
          1 === 1
      }
    }
  }

  def urlEncode(s: String): String = {
    URLEncoder.encode(s, "utf-8")
  }
}
