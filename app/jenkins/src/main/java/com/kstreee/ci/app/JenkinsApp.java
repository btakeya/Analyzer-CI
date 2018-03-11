package com.kstreee.ci.app;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import java.util.concurrent.Executors;
import javax.annotation.Nonnull;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;


public class JenkinsApp extends Builder implements SimpleBuildStep {
  private final String name;

  @DataBoundConstructor
  public JenkinsApp(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public void perform(@Nonnull Run<?,?> run,
                      @Nonnull FilePath workspace,
                      @Nonnull Launcher launcher,
                      @Nonnull TaskListener listener) {
    try {
      listener.getLogger().println("Hello, " + name + "!");
      AppJavaCompatible.analysisJavaCompatible(null, Executors.newFixedThreadPool(1)).wait();
    } catch (Exception e) {
      listener.getLogger().flush();
    }
  }

  @Extension
  public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
    public FormValidation doCheckName(@QueryParameter String value,
                                      @QueryParameter boolean useFrench) {
      return FormValidation.ok();
    }

    @Override
    public boolean isApplicable(Class<? extends AbstractProject> clazz) {
      return true;
    }

    @Override
    public String getDisplayName() {
      return "hello world";
    }
  }
}