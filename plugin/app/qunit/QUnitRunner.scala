package qunit
import java.util.concurrent.TimeUnit.SECONDS
import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test._
import scala.collection.JavaConversions._

abstract class QUnitTestsRunner extends Specification {
  import QUnitUtils._
  lazy val Port = 3333

  /* mediating between QUnit and Specs2 */
  running(TestServer(Port), HTMLUNIT) {
    browser =>
      classUrlPathList foreach { clazzUrlPath =>
          browser.goTo("http://localhost:" + Port + clazzUrlPath) //open a HTML file with the QUnit tests
          browser.await.atMost(4, SECONDS).until("#qunit-testresult .failed").hasSize(1)
          val results: Seq[QUnitTestResult] = collectTestResults(browser)
          val modulesInOrder = collection.SortedSet.empty[String] ++ results.map(_.moduleName)
          val groupedByModuleName = results.groupBy(_.moduleName)
          for (module <- modulesInOrder) {
            /* creating pseudo Specs2 tests to give the results to the console */
            module + " in " + clazzUrlPath should {
              val testsInModule = groupedByModuleName.get(module).get
              for (res <- testsInModule) {
                res.testName in {
                  res must QUnitMatcher
                }
              }
            }
          }
      }
  }

  /** views the current HTML page with QUnit test results and extracts them */
  def collectTestResults(browser: TestBrowser): Seq[QUnitTestResult] = {
    val testCaseSuccessMessages = browser.$("#qunit-tests > li")
    testCaseSuccessMessages map {
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
  }
}