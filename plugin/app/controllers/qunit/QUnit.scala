package controllers.qunit

import play.api.mvc._
import play.api._
import http.MimeTypes
import play.api.templates._
import qunit.QUnitUtils._
import ro.isdc.wro.extensions.processor.js.RhinoCoffeeScriptProcessor
import java.io.{FileReader, StringWriter}
import MimeTypes.JAVASCRIPT

object QUnit extends Controller {

  private type TemplateMapping = Map[String, Template0[Result]]

  private lazy val testTemplateNameToClassMap: TemplateMapping = {
    val clazz = Play.current.classloader.loadClass("controllers.qunit.QUnitTemplateMapping") //Class.forName() does not work here
    val instance = clazz.getConstructor().newInstance()
    val getter = clazz.getMethod("testTemplateNameToClassMap")
    getter.invoke(instance).asInstanceOf[TemplateMapping]
  }

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
    Ok.sendFile(Play.current.getFile("test/" + file)).as(JAVASCRIPT)
  }

  def csAsJs(file: String) = Action { implicit request =>
    val input = new FileReader(Play.current.getFile("test/" + file))
    val writer = new StringWriter()
    (new RhinoCoffeeScriptProcessor).process(input, writer)
    Ok(writer.toString).as(JAVASCRIPT)
  }

  def urlForAsset(file: String) = controllers.qunit.routes.Assets.at(file).url
  def urlForCsAsJs(file: String) = controllers.qunit.routes.QUnit.csAsJs(file).url
  def urlForJavaScript(file: String) = controllers.qunit.routes.QUnit.javascript(file).url
  def urlForHtml(file: String) = controllers.qunit.routes.QUnit.html(file).url
}