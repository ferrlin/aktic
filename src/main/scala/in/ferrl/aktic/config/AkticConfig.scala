package in.ferrl.aktic.config

import com.typesafe.config.ConfigFactory

object AkticConfig {
  val defaultConfig = ConfigFactory.load().getConfig("aktic")
}
