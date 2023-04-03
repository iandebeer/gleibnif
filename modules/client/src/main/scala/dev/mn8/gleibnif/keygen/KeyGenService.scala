package dev.mn8.gleibnif.keygen

import com.google.crypto.tink.config.TinkConfig
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.KeysetHandle

class KeyGenService() {
  val conf = TinkConfig.register()
  def genKeySet():String = ???
}
