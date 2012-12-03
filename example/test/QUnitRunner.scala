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

  "console qunit tests" should {
    "run with selenium" in {
      running(TestServer(Port), HTMLUNIT) { browser =>
        browser.goTo("http://localhost:" + Port + "/@qunit")
        browser.pageSource must equalTo("needle")

        val testFolder: File = play.api.Play.current.getFile("/test/")
        val testFiles =  FileUtils.listFiles(testFolder, Array(TestFileExtension), true)

       testFiles foreach { file =>
          val pathUrlEncoded = URLEncoder.encode(file.getPath, "utf-8")
          browser.goTo("http://localhost:" + Port + "/@qunit" + "?htmlFile=" + pathUrlEncoded)
          val selectorFailedCounter: String = "#qunit-testresult .failed"
          browser.await.atMost(4, TimeUnit.SECONDS).until(selectorFailedCounter).hasSize(1)
          var debugOutput: String = browser.$("#qunit-testresult", 0).getText
          for (item <- browser.$("li.fail").getTexts) debugOutput += item.toString
          assertThat(browser.$(selectorFailedCounter, 0).getText).overridingErrorMessage("some qunit tests had failed: " + debugOutput).isEqualTo("0")
       }
        1 === 1
      }
    }
  }
}
