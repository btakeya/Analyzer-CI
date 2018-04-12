package com.kstreee.ci.coordinator.file

import java.nio.file.Files

import com.kstreee.ci.util._
import com.kstreee.ci.analysis.{AnalysisConfig, AnalysisReport}
import com.kstreee.ci.analyzer.Analyzer
import com.kstreee.ci.analyzer.pylint.{PylintAnalyzerConfig, PylintAnalyzerReportFormat}
import com.kstreee.ci.coordinator.Coordinator
import com.kstreee.ci.reporter.cli.plain.CLIPlainReporterConfig
import com.kstreee.ci.sourcecode.loader.fs.FileSystemSourcecodeLoaderConfig
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import scalaz.OptionT._
import scalaz.std.scalaFuture._

class FileCoordinatorSpec(implicit ee: ExecutionEnv) extends Specification {
  "file coordinator" should {
    "valid pylint json report" in {
      val json =
        s"""
           |[
           |    {
           |        "type": "error",
           |        "module": "python_test",
           |        "obj": "",
           |        "line": 107,
           |        "column": 0,
           |        "path": "python_test/__init__.py",
           |        "symbol": "continuation-line-under-indented-for-visual-indent",
           |        "message": "PEP8 E128: continuation line under-indented for visual indent",
           |        "message-id": "E8128"
           |    },
           |    {
           |        "type": "error",
           |        "module": "python_test",
           |        "obj": "",
           |        "line": 108,
           |        "column": 0,
           |        "path": "python_test/__init__.py",
           |        "symbol": "continuation-line-under-indented-for-visual-indent",
           |        "message": "PEP8 E128: continuation line under-indented for visual indent",
           |        "message-id": "E8128"
           |    }
           |]
         """.stripMargin

      val tempFilePath = Files.createTempFile("test", "here")

      val analyzerConfig = PylintAnalyzerConfig(List(""), PylintAnalyzerReportFormat.Json)
      val coordinatorConfig = FileCoordinatorConfig(tempFilePath.toAbsolutePath.toString)
      val sourcecodeLoaderConfig = FileSystemSourcecodeLoaderConfig("")
      val reporterConfig = CLIPlainReporterConfig()
      val analysisConfig = AnalysisConfig(analyzerConfig, coordinatorConfig, sourcecodeLoaderConfig, reporterConfig)

      try {
        Files.write(tempFilePath, json.getBytes)
        val test =
          for {
            coordinator <- optionT(lift(Coordinator(analysisConfig)))
            report <- optionT(coordinator.coordinate)
            analyzer <- optionT(lift(Analyzer(analysisConfig)))
            pylintReport <- optionT(analyzer.parse(report))
          } yield (report, pylintReport)

        (for {
          r <- test.run
        } yield {
          r must beSome[(String, AnalysisReport)]
          val (report, pylintReport) = r.get
          json mustEqual report
          2 mustEqual pylintReport.items.size
        }).await
      } finally {
        Files.deleteIfExists(tempFilePath)
      }
    }
  }
}