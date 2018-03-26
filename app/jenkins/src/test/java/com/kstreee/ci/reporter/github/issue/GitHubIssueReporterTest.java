package com.kstreee.ci.reporter.github.issue;

import akka.stream.ConnectionException;
import com.kstreee.ci.analysis.AnalysisReport;
import com.kstreee.ci.analysis.AnalysisReportItem;
import com.kstreee.ci.analyzer.AnalyzerConfig;
import com.kstreee.ci.analyzer.pylint.PylintAnalyzerConfig;
import com.kstreee.ci.app.CurrentThreadExecutor;
import com.kstreee.ci.common.AhcActorSystem;
import com.kstreee.ci.reporter.Reporter;
import com.kstreee.ci.reporter.Reporter$;
import com.kstreee.ci.reporter.ReporterConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import scala.collection.JavaConverters;
import scala.compat.java8.OptionConverters;
import scala.concurrent.ExecutionContext$;

import java.util.Collections;
import java.util.Optional;

public class GitHubIssueReporterTest {
  public AhcActorSystem ahcActorSystem;

  @Before
  public void setUp() {
    ahcActorSystem = new AhcActorSystem(Thread.currentThread().getContextClassLoader(), ExecutionContext$.MODULE$.fromExecutor(new CurrentThreadExecutor()));
  }

  @After
  public void tearDown() {
    ahcActorSystem.destroy();
  }

  @Test
  public void testHttpClient() {
    ReporterConfig reporterConfig = new GitHubIssueReporterConfig(
            "http://localhost:10201",
            "http://localhost:10201",
            "owner",
            "repo",
            0,
            "token",
            OptionConverters.toScala(Optional.empty()),
            OptionConverters.toScala(Optional.empty())
    );

    AnalyzerConfig analyzerConfig = new PylintAnalyzerConfig(
            JavaConverters.asScalaIterator(Collections.<String>emptyList().iterator()).toSeq());
    AnalysisReportItem analysisReportItem = new AnalysisReportItem(
            "path",
            "filename",
            0,
            0,
            "test",
            OptionConverters.toScala(Optional.empty()));
    scala.collection.immutable.List<AnalysisReportItem> analysisReportItems =
            JavaConverters.asScalaIterator(Collections.singletonList(analysisReportItem).iterator()).toList();
    AnalysisReport analysisReport = new AnalysisReport(analyzerConfig, analysisReportItems);

    try {
      Optional<Reporter> reporter = OptionConverters.toJava(Reporter$.MODULE$.apply(reporterConfig, OptionConverters.toScala(Optional.of(ahcActorSystem))));
      Assert.assertTrue(reporter.isPresent());
      reporter.get().report(analysisReport, ExecutionContext$.MODULE$.fromExecutor(new CurrentThreadExecutor()));
    } catch(Exception e) {
      if (e instanceof ConnectionException) {
        Assert.assertEquals("Connection refused: localhost/0:0:0:0:0:0:0:1:10201", e.getMessage());
      } else {
        throw e;
      }
    }
  }
}