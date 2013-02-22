package controllers.qunit

import play.api.mvc._
import play.api._
import play.api.templates._
import play.api.Play.current
import qunit.QUnitTestsRunner._
import ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor
import java.io.{FileReader, StringReader, StringWriter}

object QUnit extends Controller {

  private type TemplateMapping = Map[String, Template0[Result]]

  private lazy val testTemplateNameToClassMap: TemplateMapping = {
    val clazz = Class.forName("controllers.QUnitTemplateMapping")
    val instance = clazz.getConstructor().newInstance()
    val getter = clazz.getMethod("testTemplateNameToClassMap")
    getter.invoke(instance).asInstanceOf[TemplateMapping]
  }
  val MimeJavaScript = "text/javascript"

  def index(templateName: String, testFile: String) = Action { implicit request =>
    if (play.api.Play.isProd) {
      NotFound
    } else if(templateName != "") {
      runTest(templateName)
    } else if (testFile != "") {
      loadTestFile(testFile, request)
    } else {
      Ok(views.html.qunit.index(classUrlPathList, classNameList))
    }
  }

  def loadTestFile(testFile: String, request: Request[AnyContent]) = {
    val necessaryToCompile = testFile.endsWith("precompiled.js")
    val filePathToLoad = if (necessaryToCompile) testFile.replace("precompiled.js", "coffee") else testFile
    val originalFile = Play.current.getFile("test/" + filePathToLoad)
    if (necessaryToCompile) {
      val input = new FileReader(originalFile)
      val writer = new StringWriter()
      (new RhinoCoffeeScriptProcessor).process(input, writer)
      Ok(writer.toString).as(MimeJavaScript)
    } else {
      Ok.sendFile(originalFile).as(MimeJavaScript)
    }
  }

  def runTest(templateName: String) = {
    testTemplateNameToClassMap.get(templateName) match {
      case Some(r: Template0[_]) => Ok(r.render().asInstanceOf[Html])
      case None => NotFound("No test found for " + templateName)
    }
  }
}