package com.kstreee.ci.app;

import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

// This code has originated from https://github.com/jenkinsci/hello-world-scala-plugin
public abstract class BuildDescriptorScalaAdapter extends BuildStepDescriptor<Builder> {
    @Override
    public boolean isApplicable(Class<? extends AbstractProject> aClass) {
        return true;
    }

    public BuildDescriptorScalaAdapter() {
        load();
    }

    @Override
    public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
        save();
        return super.configure(req,formData);
    }
}