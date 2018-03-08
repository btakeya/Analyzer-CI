package com.kstreee.ci.app

import com.kstreee.ci.analysis.AnalysisConfig
import hudson.model.{BuildListener, FreeStyleBuild}
import hudson.util.FormValidation
import hudson.{Extension, Launcher}
import net.sf.json.JSONObject
import org.kohsuke.stapler.{DataBoundConstructor, QueryParameter, StaplerRequest}

import scala.beans.BeanProperty
import scala.concurrent.Future

object JenkinsApp extends App {
  import scala.concurrent.ExecutionContext.Implicits.global

  override type T = String

  override def loadConfig(args: String): Future[Option[AnalysisConfig]] = {
    Future(None)
  }

  @Extension
  class DescriptorScalaImpl extends BuildDescriptorScalaAdapter {
    @BeanProperty
    var useFrench: Boolean = _

    def doCheckName(@QueryParameter value: String): FormValidation = {
      if (value.length == 0) return FormValidation.error("Please set a name")
      if (value.length < 4) return FormValidation.warning("Isn't the name too short?")
      FormValidation.ok()
    }

    override def getDisplayName: String = "Say hello world"

    override def configure(req: StaplerRequest, formData: JSONObject): Boolean = {
      useFrench = formData.getBoolean("useFrench")
      super.configure(req, formData)
    }
  }
}

@DataBoundConstructor
class JenkinsApp(@BeanProperty val name: String) extends BuilderScalaAdapter {
  override def scala_perform(build: FreeStyleBuild, launcher: Launcher, listener: BuildListener): Boolean = {
    if (getDescriptor.getUseFrench) listener.getLogger.println("Bonjour, " + name + "!") else listener.getLogger.println("Hello, " + name + "!")
    true
  }

  override def getDescriptor: JenkinsApp.DescriptorScalaImpl = {
    super.getDescriptor.asInstanceOf[JenkinsApp.DescriptorScalaImpl]
  }
}