package dev.choppers.mongo

import reactivemongo.api.{DefaultDB, MongoDriver}
import reactivemongo.core.nodeset.Authenticate

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

trait Mongo {
  def db: Future[DefaultDB]
}

object Mongo {
  val db: (Seq[String], String, String, String) => Future[DefaultDB] =
    (servers, databaseName, username, password) => {
      val driver = new MongoDriver

      val credentials = if (username.isEmpty) {
        Seq.empty
      } else {
        Seq(Authenticate(databaseName, username, password))
      }

      val connection = driver.connection(servers, authentications = credentials)
      connection.database(databaseName)
    }
}