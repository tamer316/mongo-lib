package dev.choppers.mongo

import com.github.limansky.mongoquery.reactive._
import org.specs2.matcher.Scope
import org.specs2.mutable.Specification
import reactivemongo.bson.{BSONDocumentReader, BSONDocumentWriter, BSONObjectID, Macros}

import scala.concurrent.Await
import scala.concurrent.duration._

class RepositorySpec extends Specification with EmbeddedMongoSpecification {

  trait Context extends Scope {
    case class Example(_id: BSONObjectID = BSONObjectID.generate, key: String)

    val repository = new Repository[Example] with TestMongo {
      val collectionName = "tests"

      implicit def writer: BSONDocumentWriter[Example] = Macros.writer[Example]

      implicit def reader: BSONDocumentReader[Example] = Macros.reader[Example]
    }
  }

  "Repository" should {
    "find nothing" in new Context {
      Await.result(repository.findAll(), 5 seconds) must beEmpty
    }

    "save and find 1 documents" in new Context {
      Await.result(repository insert Example(key = "value"), 5 seconds)

      val res = Await.result(repository.findAll(), 5 seconds)
      res.head.key mustEqual "value"
    }

    "save and find 2 documents" in new Context {
      Await.result(repository insert Example(key = "1"), 5 seconds)
      Await.result(repository insert Example(key = "2"), 5 seconds)

      val res = Await.result(repository.findAll(), 5 seconds)
      res.head.key mustEqual "1"
      res(1).key mustEqual "2"
    }

    "apply sort and limit for findAll" in new Context {
      Await.result(repository insert Example(key = "1"), 5 seconds)
      Await.result(repository insert Example(key = "2"), 5 seconds)

      val res = Await.result(repository.findAll(Some(mq"{key:1}"), 0, 1), 5 seconds)
      res.size mustEqual 1
      res.head.key mustEqual "1"
    }

    "apply sort and offset for findAll" in new Context {
      Await.result(repository insert Example(key = "1"), 5 seconds)
      Await.result(repository insert Example(key = "2"), 5 seconds)

      val res = Await.result(repository.findAll(Some(mq"{key:1}"), 1, 5), 5 seconds)
      res.size mustEqual 1
      res.head.key mustEqual "2"
    }

    "save and find 1 document by findOne" in new Context {
      Await.result(repository insert Example(key = "1"), 5 seconds)
      Await.result(repository insert Example(key = "2"), 5 seconds)

      val res = Await.result(repository.findOne(mq"""{key:"1"}"""), 5 seconds)
      res.get.key mustEqual "1"
    }

    "save and find 1 document by Selector" in new Context {
      Await.result(repository insert Example(key = "1"), 5 seconds)
      Await.result(repository insert Example(key = "2"), 5 seconds)

      val res = Await.result(repository.find(mq"""{key:"2"}"""), 5 seconds)
      res.size mustEqual 1
      res.head.key mustEqual "2"
    }

    "apply sort and limit for find" in new Context {
      Await.result(repository insert Example(key = "1"), 5 seconds)
      Await.result(repository insert Example(key = "2"), 5 seconds)

      val res = Await.result(repository.find(mq"{}", Some(mq"{key:1}"), 0, 1), 5 seconds)
      res.size mustEqual 1
      res.head.key mustEqual "1"
    }

    "apply sort and offset for find" in new Context {
      Await.result(repository insert Example(key = "1"), 5 seconds)
      Await.result(repository insert Example(key = "2"), 5 seconds)

      val res = Await.result(repository.find(mq"{}", Some(mq"{key:1}"), 1, 5), 5 seconds)
      res.size mustEqual 1
      res.head.key mustEqual "2"
    }

    "save and find 1 document by Id" in new Context {
      val document1 = Example(key = "1")
      val document2 = Example(key = "2")
      Await.result(repository insert document1, 5 seconds)
      Await.result(repository insert document2, 5 seconds)

      val res = Await.result(repository.findById(document2._id), 5 seconds)
      res.get.key mustEqual "2"
    }

    "save and find 1 document by Id String" in new Context {
      val document1 = Example(key = "1")
      val document2 = Example(key = "2")
      Await.result(repository insert document1, 5 seconds)
      Await.result(repository insert document2, 5 seconds)

      val res = Await.result(repository.findById(document1._id.stringify), 5 seconds)
      res.get.key mustEqual "1"
    }

    "update 1 document by Id" in new Context {
      val document1 = Example(key = "1")
      val document2 = Example(key = "2")
      Await.result(repository insert document1, 5 seconds)
      Await.result(repository insert document2, 5 seconds)

      Await.result(repository.updateById(document2._id, mq"""{$$set:{key:"4"}}"""), 5 seconds)
      val res = Await.result(repository.findById(document2._id), 5 seconds)
      res.get.key mustEqual "4"
    }

    "update 1 document by Selector" in new Context {
      val document1 = Example(key = "1")
      val document2 = Example(key = "2")
      Await.result(repository insert document1, 5 seconds)
      Await.result(repository insert document2, 5 seconds)

      Await.result(repository.update(mq"""{key:"1"}""", mq"""{$$set:{key:"3"}}"""), 5 seconds)
      val res = Await.result(repository.findById(document1._id), 5 seconds)
      res.get.key mustEqual "3"
    }

    "delete 1 document by Id" in new Context {
      val document1 = Example(key = "1")
      val document2 = Example(key = "2")
      Await.result(repository insert document1, 5 seconds)
      Await.result(repository insert document2, 5 seconds)

      Await.result(repository.deleteById(document2._id), 5 seconds)
      val res = Await.result(repository.findAll(), 5 seconds)
      res.size mustEqual 1
      res.head.key mustEqual "1"
    }

    "delete 1 document by Selector" in new Context {
      val document1 = Example(key = "1")
      val document2 = Example(key = "2")
      Await.result(repository insert document1, 5 seconds)
      Await.result(repository insert document2, 5 seconds)

      Await.result(repository.delete(mq"""{key:"1"}"""), 5 seconds)
      val res = Await.result(repository.findAll(), 5 seconds)
      res.size mustEqual 1
      res.head.key mustEqual "2"
    }
  }
}