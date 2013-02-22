package controllers.qunit

import play.api.mvc._
import play.api._
import play.api.templates._
import qunit.QUnitTestsRunner._
import ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor
import java.io.{FileReader, StringWriter}

object QUnit extends Controller {

  private type TemplateMapping = Map[String, Template0[Result]]

  private lazy val testTemplateNameToClassMap: TemplateMapping = {
    val clazz = Class.forName("controllers.QUnitTemplateMapping")
    val instance = clazz.getConstructor().newInstance()
    val getter = clazz.getMethod("testTemplateNameToClassMap")
    getter.invoke(instance).asInstanceOf[TemplateMapping]
  }
  val MimeJavaScript = "text/javascript"

  def index = Action { implicit request =>
    Ok(views.html.qunit.index(classUrlPathList, classNameList))
  }

  def html(file: String) = Action { implicit request =>
    testTemplateNameToClassMap.get(file) match {
      case Some(r: Template0[_]) => Ok(r.render().asInstanceOf[Html])
      case None => NotFound("No test found for " + file)
    }
  }

  def javascript(file: String) = Action { implicit request =>
    Ok.sendFile(Play.current.getFile("test/" + file)).as(MimeJavaScript)
  }

  def csAsJs(file: String) = Action { implicit request =>
    val input = new FileReader(Play.current.getFile("test/" + file))
    val writer = new StringWriter()
    (new RhinoCoffeeScriptProcessor).process(input, writer)
    Ok(writer.toString).as(MimeJavaScript)
  }
}