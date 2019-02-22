package dev.choppers.mongo

import java.time._

import reactivemongo.bson._

trait JavaDateTimeHandlers {

  implicit object InstantReader extends BSONReader[BSONDateTime, Instant] {
    def read(bson: BSONDateTime): Instant = Instant.ofEpochMilli(bson.value)
  }

  implicit object InstantWriter extends BSONWriter[Instant, BSONDateTime] {
    def write(t: Instant): BSONDateTime = BSONDateTime(t.toEpochMilli)
  }

  implicit object LocalTimeReader extends BSONReader[BSONString, LocalTime] {
    def read(bson: BSONString): LocalTime = LocalTime.parse(bson.value)
  }

  implicit object LocalTimeWriter extends BSONWriter[LocalTime, BSONString] {
    def write(t: LocalTime): BSONString = BSONString(t.toString)
  }

  implicit object LocalDateReader extends BSONReader[BSONDateTime, LocalDate] {
    def read(bson: BSONDateTime): LocalDate = LocalDateTime.ofInstant(Instant.ofEpochMilli(bson.value), ZoneOffset.UTC).toLocalDate
  }

  implicit object LocalDateWriter extends BSONWriter[LocalDate, BSONDateTime] {
    def write(ld: LocalDate): BSONDateTime = BSONDateTime(ld.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli)
  }

  implicit object LocalDateTimeReader extends BSONReader[BSONDateTime, LocalDateTime] {
    def read(bson: BSONDateTime): LocalDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(bson.value), ZoneOffset.UTC)
  }

  implicit object LocalDateTimeWriter extends BSONWriter[LocalDateTime, BSONDateTime] {
    def write(ldt: LocalDateTime): BSONDateTime = BSONDateTime(ldt.toInstant(ZoneOffset.UTC).toEpochMilli)
  }

}
