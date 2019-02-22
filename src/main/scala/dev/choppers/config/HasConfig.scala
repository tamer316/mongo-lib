package dev.choppers.config

import com.typesafe.config.ConfigFactory

trait HasConfig {
  val config = ConfigFactory.load
}
