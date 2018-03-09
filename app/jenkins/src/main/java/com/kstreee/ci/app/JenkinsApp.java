package com.kstreee.ci.app;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import java.io.IOException;

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
						@Nonnull TaskListener listener) throws InterruptedException, IOException {
		listener.getLogger().println("Hello, " + name + "!");
	}

	@Extension
	public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
		public FormValidation doCheckName(@QueryParameter String value,
										  @QueryParameter boolean useFrench) throws IOException, ServletException {
			return FormValidation.ok();
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass) {
			return true;
		}

		@Override
		public String getDisplayName() {
			return "hello world";
		}
	}
}