import org.specs2.mutable._
import qunit.QUnitTestsRunner

class QUnitTestRunnerSpec extends Specification {

  "The QUnitTestsRunner" should {
    "be able to create the url from a template name to the test with it" in {
      QUnitTestsRunner.testFileToUrlPath("views.html.test.sub1.sub2.test1") === "//html/views.html.test.sub1.sub2.test1" //included workaround for play bug
    }
  }
}