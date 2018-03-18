package com.kstreee.ci.app;

import com.kstreee.ci.analysis.Analysis$;
import com.kstreee.ci.analysis.AnalysisConfig;
import com.kstreee.ci.analyzer.AnalyzerConfig;
import com.kstreee.ci.coordinator.CoordinatorConfig;
import com.kstreee.ci.reporter.ReporterConfig;
import com.kstreee.ci.reporter.github.issue.GitHubIssueReporterConfig;
import com.kstreee.ci.sourcecode.loader.SourcecodeLoaderConfig;
import com.kstreee.ci.sourcecode.loader.fs.FileSystemSourcecodeLoaderConfig;
import com.kstreee.ci.storage.json.AnalyzerConfigLoad;
import com.kstreee.ci.storage.json.CoordinatorConfigLoad;
import hudson.*;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;

import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

import hudson.util.FormValidation;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.libs.json.Json;
import scala.NotImplementedError;
import scala.Option;
import scala.concurrent.Future;
import scala.compat.java8.FutureConverters;
import scala.compat.java8.OptionConverters;
import scala.concurrent.ExecutionContext$;

import javax.annotation.Nonnull;

// public class JenkinsApp extends Builder implements SimpleBuildStep, Analysis {
public class AnalyzerCI extends Builder implements SimpleBuildStep {
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  // Github issue
  private final String githubBaseUrl;
  private final String githubApiBaseUrl;
  private final String githubOwner;
  private final String githubRepo;
  private final String githubIssueNumber;
  private final String githubToken;
  // Configs
  private final String analyzerConfig;
  private final String coordinatorConfig;
  // Other options
  private final Boolean runOnBackground;

  @DataBoundConstructor
  public AnalyzerCI(
          final String githubBaseUrl,
          final String githubApiBaseUrl,
          final String githubOwner,
          final String githubRepo,
          final String githubIssueNumber,
          final String githubToken,
          final String analyzerConfig,
          final String coordinatorConfig,
          final Boolean runOnBackground
  ) {
    this.githubBaseUrl = githubBaseUrl;
    this.githubApiBaseUrl = githubApiBaseUrl;
    this.githubOwner = githubOwner;
    this.githubRepo = githubRepo;
    this.githubIssueNumber = githubIssueNumber;
    this.githubToken = githubToken;
    this.analyzerConfig = analyzerConfig;
    this.coordinatorConfig = coordinatorConfig;
    this.runOnBackground = runOnBackground;
  }

  public String getGithubBaseUrl() {
    return githubBaseUrl;
  }

  public String getGithubApiBaseUrl() {
    return githubApiBaseUrl;
  }

  public String getGithubOwner() {
    return githubOwner;
  }

  public String getGithubRepo() {
    return githubRepo;
  }

  public String getGithubIssueNumber() {
    return githubIssueNumber;
  }

  public String getGithubToken() {
    return githubToken;
  }

  public String getAnalyzerConfig() {
    return analyzerConfig;
  }

  public String getCoordinatorConfig() {
    return coordinatorConfig;
  }

  public Boolean getRunOnBackground() {
    return runOnBackground;
  }

  @Override
  public synchronized void perform(
          @Nonnull Run<?,?> run,
          @Nonnull FilePath workspace,
          @Nonnull Launcher launcher,
          @Nonnull TaskListener listener) {
    try {
      DescriptorImpl checker = new DescriptorImpl();
      if (checker.doCheckGithubIssueNumber(githubIssueNumber).kind != FormValidation.Kind.OK) {
        listener.getLogger().println("Failed to handle github issue number");
      } else if (checker.doCheckAnalyzerConfig(analyzerConfig).kind != FormValidation.Kind.OK) {
        listener.getLogger().println("Failed to handle analyzer config");
      } else if (checker.doCheckCoordinatorConfig(coordinatorConfig).kind != FormValidation.Kind.OK) {
        listener.getLogger().println("Failed to handle coordinator config");
      }

      // Analyzer config
      Future<Option<AnalyzerConfig>>  analyzerConfigF = AnalyzerConfigLoad.load(
              Json.parse(analyzerConfig),
              ExecutionContext$.MODULE$.fromExecutor(new CurrentThreadExecutor()));

      // Coordinator config
      Future<Option<CoordinatorConfig>> coordinatorConfigF = CoordinatorConfigLoad.load(
              Json.parse(coordinatorConfig),
              ExecutionContext$.MODULE$.fromExecutor(new CurrentThreadExecutor()));

      // Sourcecode loader config
      SourcecodeLoaderConfig sourcecodeLoaderConfig = new FileSystemSourcecodeLoaderConfig(
              workspace.toURI().getPath(),
              OptionConverters.toScala(Optional.empty()));

      // Reporter config
      ReporterConfig reporterConfig = new GitHubIssueReporterConfig(
              githubBaseUrl,
              githubApiBaseUrl,
              githubOwner,
              githubRepo,
              Integer.parseInt(githubIssueNumber),
              githubToken,
              OptionConverters.toScala(Optional.empty()),
              OptionConverters.toScala(Optional.empty()));

      // Perform analysis
      CompletableFuture<Optional<Void>> performedAnalysis =
              performAnalysis(listener, analyzerConfigF, coordinatorConfigF, sourcecodeLoaderConfig, reporterConfig)
                      .toCompletableFuture()
                      .thenApply(r -> {
                        listener.getLogger().println("Analysis Done.");
                        return r;
                      })
                      .exceptionally(e -> {
                        listener.getLogger().println("Failed to execute, " + e.getMessage());
                        e.printStackTrace(listener.getLogger());
                        return Optional.empty();
                      });
      if (!runOnBackground) {
        performedAnalysis.join();
      }
    } catch (Exception e) {
      listener.getLogger().println(String.format("Failed to analyze, %s", e));
      e.printStackTrace(listener.getLogger());
      listener.getLogger().flush();
    }
  }

  private CompletionStage<Optional<Void>> performAnalysis(
          @Nonnull final TaskListener listener,
          @Nonnull final Future<Option<AnalyzerConfig>> analyzerConfigF,
          @Nonnull final Future<Option<CoordinatorConfig>> coordinatorConfigF,
          @Nonnull final SourcecodeLoaderConfig sourcecodeLoaderConfig,
          @Nonnull final ReporterConfig reporterConfig) {
    CompletionStage<Optional<AnalyzerConfig>> analyzerConfigC = FutureConverters
            .toJava(analyzerConfigF)
            .thenApply(OptionConverters::toJava);
    CompletionStage<Optional<CoordinatorConfig>> coordinatorConfigC = FutureConverters
            .toJava(coordinatorConfigF)
            .thenApply(OptionConverters::toJava);

    return analyzerConfigC.thenCompose(analyzerConfigO ->
      coordinatorConfigC.thenCompose(coordinatorConfigO -> {
        if (!analyzerConfigO.isPresent()) {
          listener.getLogger().println("Failed to load analyzer config.");
          return CompletableFuture.completedFuture(Optional.empty());
        } else if (!coordinatorConfigO.isPresent()) {
          listener.getLogger().println("Failed to load coordinator config.");
          return CompletableFuture.completedFuture(Optional.empty());
        } else {
          AnalysisConfig analysisConfig = new AnalysisConfig(analyzerConfigO.get(), coordinatorConfigO.get(), sourcecodeLoaderConfig, reporterConfig);
          listener.getLogger().println(String.format("Start to analyze target program.\n%s\n", analysisConfig.toString()));
          return FutureConverters
                  .toJava(Analysis$.MODULE$.analysis(
                          analysisConfig,
                          Jenkins.getInstance().getPlugin("AnalyzerCI").getWrapper().classLoader,
                          ExecutionContext$.MODULE$.fromExecutor(new CurrentThreadExecutor())))
                  .thenApply(o -> Optional.empty());
        }
      })
    );
  }

  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
    @Override
    public boolean isApplicable(Class<? extends AbstractProject> clazz) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "Analyzer-CI";
    }

    public FormValidation doCheckGithubIssueNumber(@QueryParameter("githubIssueNumber") final String githubIssueNumber) {
      try {
        Integer.parseInt(githubIssueNumber);
        return FormValidation.ok();
      } catch (NumberFormatException e) {
        return FormValidation.error("Not a number.");
      }
    }

    public FormValidation doCheckAnalyzerConfig(@QueryParameter("analyzerConfig") final String analyzerConfig) {
      try {
        return doCheckFO(AnalyzerConfigLoad.load(
                Json.parse(analyzerConfig),
                ExecutionContext$.MODULE$.fromExecutor(new CurrentThreadExecutor())));
      } catch (Exception e) {
        return FormValidation.error("Not a valid json.");
      }
    }

    public FormValidation doCheckCoordinatorConfig(@QueryParameter("coordinatorConfig") final String coordinatorConfig) {
      try {
        return doCheckFO(CoordinatorConfigLoad.load(
                Json.parse(coordinatorConfig),
                ExecutionContext$.MODULE$.fromExecutor(new CurrentThreadExecutor())));
      } catch (Exception e) {
        return FormValidation.error("Not a valid json.");
      }
    }

    private <T> FormValidation doCheckFO(Future<Option<T>> data) {
      try {
        Boolean succeeded = FutureConverters
                .toJava(data)
                .thenApply(OptionConverters::toJava)
                .toCompletableFuture()
                .get()
                .isPresent();
        return succeeded ? FormValidation.ok() : FormValidation.error("Not a valid config.");
      } catch (ExecutionException | InterruptedException e) {
        return FormValidation.error(String.format("Unknown error while parsing config, %s", e.getMessage()));
      } catch (NotImplementedError e) {
        return FormValidation.error("Not implemented.");
      } catch (Exception e) {
        return FormValidation.error(String.format("Internal error, %s", e.getMessage()));
      }
    }
  }
}