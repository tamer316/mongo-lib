package dev.choppers.mongo

import dev.choppers.config.HasConfig

import scala.collection.JavaConverters._

trait MongoDB extends Mongo {
  lazy val db = MongoDB.servicedb
}

object MongoDB extends HasConfig {
  val database = config.getString("mongodb.database")
  val username = config.getString("mongodb.username")
  val password = config.getString("mongodb.password")
  val servers = config.getString("mongodb.servers").split(",")
  lazy val servicedb = Mongo db(servers, database, username, password)
}
