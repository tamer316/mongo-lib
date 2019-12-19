package dev.choppers.mongo

import com.github.limansky.mongoquery.reactive._
import reactivemongo.api.collections.bson.BSONCollection
import reactivemongo.api.{Cursor, QueryOpts}
import reactivemongo.bson.{BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Success

abstract class Repository[T] extends Mongo with JavaDateTimeHandlers {
  val collectionName: String

  implicit def writer: BSONDocumentWriter[T]

  implicit def reader: BSONDocumentReader[T]

  protected lazy val collection: Future[BSONCollection] = db.map(_.collection(collectionName))

  def insert(obj: T): Future[Unit] = collection.flatMap(_.insert(obj).map(_ => {}))

  def findOne(bsonDoc: BSONDocument): Future[Option[T]] = collection.flatMap(_.find(bsonDoc).one[T])

  def find(query: BSONDocument, sortOpt: Option[BSONDocument] = None, offset: Int = 0, limit: Int = -1): Future[Seq[T]] = {
    collection.flatMap { col =>
      val queryDoc = sortOpt match {
        case Some(sort) => col.find(query).options(QueryOpts().skip(offset)).sort(sort)
        case _ => col.find(query).options(QueryOpts().skip(offset))
      }
      queryDoc.cursor[T]().collect[Seq](limit, Cursor.FailOnError[Seq[T]]())
    }
  }

  def findAll(sortOpt: Option[BSONDocument] = None, offset: Int = 0, limit: Int = -1): Future[Seq[T]] = {
    find(mq"{}", sortOpt, offset, limit)
  }

  def findById(id: String): Future[Option[T]] = {
    BSONObjectID.parse(id) match {
      case Success(objectId) => findById(objectId)
      case _ => Future.successful(None)
    }
  }

  def findById(objectId: BSONObjectID): Future[Option[T]] = {
    findOne(mq"{_id:$objectId}")
  }

  def updateOne(selector: BSONDocument, updateQuery: BSONDocument): Future[Unit] = {
    collection.flatMap(_.update(selector, updateQuery).map(_ => {}))
  }

  def updateMany(selector: BSONDocument, updateQuery: BSONDocument): Future[Unit] = {
    collection.flatMap(_.update(selector, updateQuery, multi = true).map(_ => {}))
  }

  def updateById(objectId: BSONObjectID, updateQuery: BSONDocument): Future[Unit] = {
    updateOne(mq"{_id:$objectId}", updateQuery)
  }

  def deleteById(objectId: BSONObjectID): Future[Unit] = delete(mq"{_id:$objectId}")

  def delete(selector: BSONDocument): Future[Unit] = collection.flatMap(_.remove(selector).map(_ => {}))

  def count(selector: Option[BSONDocument] = None): Future[Int] = collection.flatMap(_.count(selector))
}
