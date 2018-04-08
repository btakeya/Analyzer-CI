package com.kstreee.ci.storage.yaml

import com.kstreee.ci.analysis.AnalysisConfig
import com.kstreee.ci.analyzer.pylint.{PylintAnalyzerConfig, PylintAnalyzerReportFormat}
import com.kstreee.ci.coordinator.cli.CLICoordinatorConfig
import com.kstreee.ci.reporter.cli.plain.CLIPlainReporterConfig
import com.kstreee.ci.sourcecode.loader.fs.FileSystemSourcecodeLoaderConfig
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import net.jcazevedo.moultingyaml._

class AnalysisConfigYamlLoadSpec(implicit ee: ExecutionEnv) extends Specification {
  "analysis reads" should {
    "valid analysis yaml" in {
      // PylintAnalyzer
      val analyzerName = AnalyzerConfigYamlLoad.pylintName
      val analysisCmd = Seq("1", "2", "3")
      val reportFormat = "json"
      // CLICoordinator
      val coordinatorName = CoordinatorConfigYamlLoad.cliName
      val timeoutSeconds = 123
      // FileSystemSymbolic
      val sourcecodeLoaderName = SourcecodeLoaderConfigYamlLoad.fileSystemName
      val sourcePath = "TEST_SOURCE_PATH"
      // CLIReporter
      val reporterName = ReporterConfigYamlLoad.cliPlainName
      val yaml = s"""
                    |analyzer:
                    |  name: $analyzerName
                    |  analysis_cmd: ${analysisCmd.mkString(" ")}
                    |  report_format: $reportFormat
                    |coordinator:
                    |  name: $coordinatorName
                    |  timeout_seconds: $timeoutSeconds
                    |sourcecode_loader:
                    |  name: $sourcecodeLoaderName
                    |  source_path: $sourcePath
                    |reporter:
                    |  name: $reporterName
      """.stripMargin
      (for {
        result <- AnalysisConfigYamlLoad.load(yaml.parseYaml)
      } yield {
        result must beSome[AnalysisConfig]

        result.get.analyzerConfig must anInstanceOf[PylintAnalyzerConfig]
        result.get.analyzerConfig.asInstanceOf[PylintAnalyzerConfig].analysisCmd must containTheSameElementsAs(analysisCmd)
        result.get.analyzerConfig.asInstanceOf[PylintAnalyzerConfig].reportFormat mustEqual PylintAnalyzerReportFormat.Json

        result.get.coordinatorConfig must anInstanceOf[CLICoordinatorConfig]
        result.get.coordinatorConfig.asInstanceOf[CLICoordinatorConfig].timeoutSeconds must beSome(timeoutSeconds)

        result.get.sourcecodeLoaderConfig must anInstanceOf[FileSystemSourcecodeLoaderConfig]
        result.get.sourcecodeLoaderConfig.asInstanceOf[FileSystemSourcecodeLoaderConfig].sourcePath mustEqual sourcePath

        result.get.reporterConfig must anInstanceOf[CLIPlainReporterConfig]
      }).await
    }
  }
}