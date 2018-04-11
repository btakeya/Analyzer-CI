package com.kstreee.ci.util

import net.jcazevedo.moultingyaml.{YamlObject, YamlString}
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification

import scala.util.{Success, Try}

class UtilSpec(implicit ee: ExecutionEnv) extends Specification {
  "yaml utility methods" should {
    "yaml map should work" in {
      val name = "test"
      val yaml = s"name: $name"
      val r = yamlMap(parseYaml(yaml), x => s"hihi $x")
      println(r)
      Try(r.asYamlObject) must anInstanceOf[Success[YamlObject]]
      r.asYamlObject.getFields(YamlString("name")) match {
        case Seq(YamlString(res)) => res mustEqual s"hihi $name"
        case e => failure(s"Failed to map yaml data, $e")
      }
      // To avoid scala specs2 error
      ok
    }
  }
}