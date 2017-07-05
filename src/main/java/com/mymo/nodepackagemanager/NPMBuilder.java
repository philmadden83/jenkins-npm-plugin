package com.mymo.nodepackagemanager;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

public class NPMBuilder extends Builder implements SimpleBuildStep {
    private final String command;

    @DataBoundConstructor
    public NPMBuilder(String command) {
        this.command = command;
    }


    @Override
    public void perform(Run<?,?> build, FilePath workspace, Launcher launcher, TaskListener listener) {
        try {
            launcher.launch()
                    .cmdAsSingleString(String.format("%s %s", getDescriptor().getNpmHome(), command))
                    .join();
        } catch (IOException | InterruptedException e) {
            listener.getLogger().append(e.getMessage());
        }
    }

    @Override
    public NPMBuildStepDescriptor getDescriptor() {
        return (NPMBuildStepDescriptor) super.getDescriptor();
    }

    public String getCommand() {
        return command;
    }

    @Extension
    public static final class NPMBuildStepDescriptor extends BuildStepDescriptor<Builder> {
        private String npmHome;

        public NPMBuildStepDescriptor() {
            load();
        }

        public String getNpmHome() {
            return npmHome;
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        public String getDisplayName() {
            return "Invoke top-level NPM commands";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            npmHome = formData.getString("npmHome");
            save();
            return super.configure(req,formData);
        }
    }
}

