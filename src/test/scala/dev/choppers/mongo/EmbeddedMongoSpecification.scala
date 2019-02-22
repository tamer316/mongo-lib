package dev.choppers.mongo

import java.util
import java.util.Collections
import java.util.concurrent.TimeUnit

import de.flapdoodle.embed.mongo.config.{MongodConfigBuilder, Net, RuntimeConfigBuilder}
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.mongo.{Command, MongodStarter}
import de.flapdoodle.embed.process.config.io.ProcessOutput
import de.flapdoodle.embed.process.runtime.Network._
import grizzled.slf4j.Logging
import org.specs2.execute.{AsResult, Result}
import org.specs2.mutable.SpecificationLike
import org.specs2.specification.AroundEach
import reactivemongo.api.MongoDriver

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

object EmbeddedMongoSpecification {
  private val ports = Collections.synchronizedSet(new util.HashSet[Int])

  lazy val runtimeConfig = new RuntimeConfigBuilder()
    .defaults(Command.MongoD)
    .processOutput(ProcessOutput.getDefaultInstanceSilent)
    .build()

  lazy val runtime = MongodStarter getInstance runtimeConfig
}

/**
  * Mix in this trait (at specification level) to provide a connection to an embedded Mongo for testing.
  * Every running example will be given its own unique instance of Mongo.
  */
trait EmbeddedMongoSpecification extends Logging with AroundEach {
  self: SpecificationLike =>

  import EmbeddedMongoSpecification._

  isolated
  sequential

  lazy val network: Net = {
    def freeServerPort: Int = {
      val port = getFreeServerPort

      // Avoid standard Mongo ports in case a standalone Mongo is running.
      if ((27017 to 27027) contains port) {
        MILLISECONDS.sleep(10)
        freeServerPort
      } else {
        if (ports.add(port)) {
          info(s"Mongo ports in use: $ports")
          port
        } else {
          freeServerPort
        }
      }
    }

    new Net(freeServerPort, localhostIsIPv6)
  }

  lazy val mongodConfig = new MongodConfigBuilder()
    .version(Version.Main.PRODUCTION)
    .net(network)
    .build

  lazy val mongodExecutable = runtime prepare mongodConfig
  lazy val database = "embedded-database"
  lazy val driver = new MongoDriver
  lazy val connection = driver.connection(Seq(s"127.0.0.1:${network.getPort}"))

  override def around[R: AsResult](r: => R): Result = try {
    upMongo()
    AsResult(r)
  } finally {
    downMongo()
  }

  def upMongo(): Unit = {
    def startMongo(attempt: Int, sleepTime: Int = 2): Unit = try {
      mongodExecutable.start()
      info(s"Started Mongo running on ${network.getPort}")
    } catch {
      case t: Throwable =>
        error(s"Failed to start Mongo on attempt number $attempt")
        val nextAttempt = attempt + 1

        if (nextAttempt <= 10) {
          SECONDS.sleep(sleepTime)
          startMongo(nextAttempt, sleepTime + 1)
        } else {
          throw new Exception("Failed to start Mongo after 10 attempts", t)
        }
    }

    startMongo(1)
  }

  def downMongo(): Unit = {
    info(s"Stopping Mongo running on ${network.getPort}")
    mongodExecutable.stop()
    TimeUnit.SECONDS.sleep(2)
    ports.remove(network.getPort)
  }

  trait TestMongo extends Mongo {
    lazy val db = connection.database(database)
  }

}