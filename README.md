Mongo Library - Scala library to work with Mongodb
==================================================

Application built with the following (main) technologies:

- Scala

- SBT

- ReactiveMongo

Introduction
------------
A library to easily "configure" your application to interact with Mongodb, currently via the ReactiveMongo driver.

Configuration, as well as using the standard reference/application.conf, is done via mixins to your application and test code.

Prerequisites
-------------
The following applications are installed and running:

* [Scala 2.11.8](http://www.scala-lang.org/)
* [SBT](http://www.scala-sbt.org/)
    - For Mac:
      ```
      brew install sbt
      ```

Publishing
-------
- Publish to your local repository
  ```
  sbt publish-local
  ```
  
Testing
---------
- Run Unit tests
  ```
  sbt test
  ```
  
- Run one test
  ```
  sbt test-only *RepositoryEmbeddedMongoSpec
  ```

Repository Example
------------------
```scala
import dev.choppers.mongo.Repository

trait ThingsRepository extends Repository[Thing] {
  val collectionName = "things"
  implicit val reader: BSONDocumentReader[Thing] = Macros.reader[Thing]
  implicit val writer: BSONDocumentWriter[Thing] = Macros.writer[Thing]
}

val thingsRepository = new ThingsRepository with MyMongo
thingsRepository insert Thing()
```

where Thing would be one of your case classes.

Testing
-------
EmbeddedMongoSpecification is a specification for tests that interact with Mongo.

Via EmbeddedMongoSpecification, each example, within a specification, is run sequentially, as a test database is created and dropped.

EmbeddedMongoSpecification Example
----------------------------------
This trait must be mixed into a Specs2 specification and will give you a TestMongo to mix into your repository.
```scala
class RepositoryEmbeddedMongoSpec extends Specification with EmbeddedMongoSpecification {
  trait Context extends Scope {
    val repository = new Repository[Thing] with TestMongo {
      val collectionName = "tests"
    }
  }

  "Repository" should {
    "find nothing" in new Context {
      val res = Await.result(repository.findAll(), 5 seconds)
      res must beEmpty
    }
  }
}
```

Code Coverage
-------------
SBT-scoverage a SBT auto plugin: https://github.com/scoverage/sbt-scoverage
- Run tests with coverage enabled by entering:
  ```
  sbt clean coverage test
  ```

After the tests have finished, find the coverage reports inside target/scala-2.11/scoverage-report
