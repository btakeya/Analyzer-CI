package com.kstreee.ci.app;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.tasks.Builder;

// This code has originated from https://github.com/jenkinsci/hello-world-scala-plugin
public abstract class BuilderScalaAdapter extends Builder {
    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        try {
            return scala_perform((FreeStyleBuild) build, launcher, listener);
        } catch (ClassCastException e) {
            return false;
        }
    }

    public abstract boolean scala_perform(FreeStyleBuild build, Launcher launcher, BuildListener listener);
}