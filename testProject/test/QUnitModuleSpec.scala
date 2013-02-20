import org.openqa.selenium.WebDriver
import org.specs2.mutable._
import play.api.test._

import scala.collection.JavaConversions._
import com.google.common.base.Predicate

class QUnitModuleSpec extends Specification {

  implicit def function2Predicate(f: => Boolean) = new Predicate[WebDriver]{
    def apply(webDriver: WebDriver) = f
  }

  "The QUnit module" should {
    "run one test suite in the browser" in new WithBrowser {
      browser.goTo(s"http://localhost:$port/@qunit?templateName=views.html.test.sub1.sub2.test1&notrycatch=true")
      browser.title must contain("QUnit")
      val testResultParagraph = browser.$("#qunit-testresult", 0)
      browser.await().atMost(3000).until(testResultParagraph.getText.contains("Tests completed in "))
      testResultParagraph.find(".failed").getText must be equalTo("0")
    }

    "run all test suites in the browser" in new WithBrowser {
      browser.goTo(s"http://localhost:$port/@qunit?notrycatch=true")
      browser.title must contain("QUnit")
      val testResultParagraph = browser.$("#qunit-testresult", 0)
      browser.await().atMost(3000).until(testResultParagraph.getText.contains("Tests completed in "))
      testResultParagraph.find(".failed").getText must be equalTo("0")
      failure("assert that all suites are included")
    }.pendingUntilFixed("does not work with HTML UNIT")
  }
}