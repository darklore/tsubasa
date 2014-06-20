package domain.service.server

import models.Server
import scala.collection.mutable.Map

object ServerInitializers {
  val initializers = Map.empty[Server, ServerInitializer]

  def getInitializer(s: Server) : ServerInitializer = {
    initializers.get(s).getOrElse {
      val initializer = new ServerInitializer(s)
      initializers += s -> initializer
      initializer
    }
  }
}
