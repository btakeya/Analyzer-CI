package com.kstreee.ci.app

import com.kstreee.ci.analysis.AnalysisConfig
import java.util.concurrent.{CompletionStage, Executor, ExecutorService}
import java.util.Optional

import scala.compat.java8.FutureConverters
import scala.concurrent.ExecutionContext

object AppJavaCompatible extends App {
  def analysisjavaCompatible(analysisConfig: AnalysisConfig, es: ExecutorService): CompletionStage[Optional[Void]] = {
    analysisJavaCompatible(analysisConfig)(ExecutionContext.fromExecutorService(es))
  }

  def analysisJavaCompatible(analysisConfig: AnalysisConfig, e: Executor): CompletionStage[Optional[Void]] = {
    analysisJavaCompatible(analysisConfig)(ExecutionContext.fromExecutor(e))
  }

  private def analysisJavaCompatible(analysisConfig: AnalysisConfig)(implicit ctx: ExecutionContext): CompletionStage[Optional[Void]] = {
    FutureConverters.toJava(App.analysis(analysisConfig).map(_ => Optional.empty()))
  }
}