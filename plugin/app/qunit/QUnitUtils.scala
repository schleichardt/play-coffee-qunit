package qunit

import java.io.File
import org.apache.commons.lang3.StringUtils._
import org.specs2.matcher.{Expectable, Matcher}
import org.apache.commons.io.FileUtils
import scala.collection.JavaConversions._

case class QUnitTestResult(moduleName: String, testName: String, failedCount: Int, passedCount: Int, assertionErrors: Seq[String])

object QUnitMatcher extends Matcher[QUnitTestResult] {
  def apply[S <: QUnitTestResult](s: Expectable[S]) = {
    result(s.value.failedCount < 1, s.description, s.value.assertionErrors.mkString("\n"), s)
  }
}

object QUnitUtils {
  lazy val testFolder = play.api.Play.current.getFile("/test/")

  def scalaTemplateFilesInTestFolder = {
    val ScalaTemplateFileExtension = "scala.html"
    val testFilesUnsorted = if (testFolder.exists && testFolder.isDirectory) FileUtils.listFiles(testFolder, Array(ScalaTemplateFileExtension), true).toList else List[File]()
    testFilesUnsorted.sortWith((left, right) => left.getAbsolutePath < right.getAbsolutePath)
  }

  /** converts the location (file path) of a Scala template to the class name of the template */
  def toClassName(path: String): String = {
    val pathElements = path.split("/").toList
    val result = ("views.html." + pathElements.mkString(".")).replace(".scala.html", "").replace("views.html.views.html.", "views.html.")
    result
  }

  /** gets the relative path of a file to the test folder */
  def filepath(file: File) = removeStart(file.getCanonicalPath, testFolder.getCanonicalPath + "/views/")

  /** gets the URL to load the generated HTML of a Scala template file */
  def testFileToUrlPath(relativePath: String): String = {
    val className = toClassName(relativePath)
    val url = controllers.qunit.QUnit.urlForHtml(className)
    url
  }

  def testFileToUrlPath(file: File): String = testFileToUrlPath(filepath(file))

  /** a list of URLs with test cases as HTML */
  def classUrlPathList = scalaTemplateFilesInTestFolder map {
    file => testFileToUrlPath(file)
  }

  /** a list of class names of the Scala templates in the test folder */
  def classNameList = scalaTemplateFilesInTestFolder map {
    file => toClassName(filepath(file))
  }

  /** the number of QUnit test files */
  def testFilesNumber = scalaTemplateFilesInTestFolder.size
}
