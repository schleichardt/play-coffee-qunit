package qunit

import java.io.File
import java.net.URLEncoder
import java.util.concurrent.TimeUnit
import org.apache.commons.lang3.StringUtils._
import org.specs2.matcher.{Expectable, Matcher}
import org.specs2.mutable.Specification
import play.api.test.Helpers._
import play.api.test._
import org.apache.commons.io.FileUtils
import play.api.test.TestServer
import scala.Array
import scala.collection.JavaConversions._
import scala.Predef._

object QUnitMatcher extends Matcher[QUnitTestResult] {
  def apply[S <: QUnitTestResult](s: Expectable[S]) = {
    result(s.value.failedCount < 1, s.description, s.value.assertionErrors.mkString("\n"), s)
  }
}

case class QUnitTestResult(moduleName: String, testName: String, failedCount: Int, passedCount: Int, assertionErrors: Seq[String])

object QUnitTestsRunner {
  lazy val testFolder = play.api.Play.current.getFile("/test/")
  def scalaTemplateFilesInTestFolder = {
    val ScalaTemplateFileExtension = "scala.html"
    val testFilesUnsorted = if(testFolder.exists && testFolder.isDirectory) FileUtils.listFiles(testFolder, Array(ScalaTemplateFileExtension), true).toList else List[File]()
    testFilesUnsorted.sortWith((left, right) => left.getAbsolutePath < right.getAbsolutePath)
  }

  /** converts the location (file path) of a Scala template to the class name of the template*/
  def toClassName(path: String): String = {
    val pathElements = path.split("/").toList
    val result = ("views.html." + pathElements.mkString(".")).replace(".scala.html", "").replace("views.html.views.html.", "views.html.")
    result
  }

  /** gets the relative path of a file to the test folder */
  def filepath(file: File) =  removeStart(file.getCanonicalPath, testFolder.getCanonicalPath + "/views/")

  /** gets the URL to load the generated HTML of a Scala template file */
  def testFileToUrlPath(relativePath: String): String = {
    val className = toClassName(relativePath)
    val url = controllers.qunit.QUnit.urlForHtml(className)
    url
  }

  def testFileToUrlPath(file: File): String = testFileToUrlPath(filepath(file))

  /** a list of URLs with test cases as HTML */
  def classUrlPathList = scalaTemplateFilesInTestFolder map {file => testFileToUrlPath(file)}

  /** a list of class names of the Scala templates in the test folder */
  def classNameList = scalaTemplateFilesInTestFolder map {file => toClassName(filepath(file))}

  /** the number of QUnit test files */
  def testFilesNumber = scalaTemplateFilesInTestFolder.size
}

abstract class QUnitTestsRunner extends Specification {
  import QUnitTestsRunner._

  lazy val Port = 3333
  val baseUrl = "http://localhost:" + Port
  val selectorFailedCounter = "#qunit-testresult .failed"

  running(TestServer(Port), HTMLUNIT) {
    browser =>
      classUrlPathList foreach {
        clazzUrlPath =>
          browser.goTo(baseUrl + clazzUrlPath)
          waitUntilJavaScriptTestsFinished(browser)
          val results: Seq[QUnitTestResult] = collectTestResults(browser)
          val modulesInOrder = collection.SortedSet.empty[String] ++ results.map(_.moduleName)
          val groupedByModuleName = results.groupBy(_.moduleName)
          for (module <- modulesInOrder) {
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

  def waitUntilJavaScriptTestsFinished(browser: TestBrowser) {
    browser.await.atMost(4, TimeUnit.SECONDS).until(selectorFailedCounter).hasSize(1)
  }

  def urlEncode(s: String): String = URLEncoder.encode(s, "utf-8")
}
