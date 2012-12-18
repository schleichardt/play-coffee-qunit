import play.api.{Logger, Play, GlobalSettings}
import play.api.mvc.{Handler, RequestHeader}

object Global extends GlobalSettings {

  override def onRouteRequest(request: RequestHeader) = {
    if(Logger.isDebugEnabled && !request.path.startsWith("/assets")) {
      Logger.debug(request.path + "?" + request.rawQueryString)
    }
    super.onRouteRequest(request)
  }
}