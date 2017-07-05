package com.mymo.npm;

import hudson.Launcher;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.IOException;

public class NPMBuilder extends Builder {
    private static final int SUCCESS = 0;

    private final String command;
    private final boolean sudo;

    @DataBoundConstructor
    public NPMBuilder(String command, boolean sudo) {
        this.command    = command;
        this.sudo       = sudo;
    }

    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
        try {

            int result = launcher.launch()
                    .cmdAsSingleString(String.format("%s %s", getDescriptor().getNpmHome(), buildCommand()))
                    .stdout(listener.getLogger())
                    .stderr(listener.getLogger())
                    .pwd(build.getWorkspace())
                    .join();

            return result == SUCCESS;

        } catch (IOException | InterruptedException e) {
            listener.getLogger().append(e.getMessage());
        }

        return false;
    }

    private String buildCommand() {
        StringBuilder stringBuilder;

        if (isSudo()) {
            stringBuilder = new StringBuilder(String.format("sudo %s %s", getDescriptor().getNpmHome(), getCommand()));
        } else {
            stringBuilder = new StringBuilder(String.format("%s %s", getDescriptor().getNpmHome(), getCommand()));
        }

        return stringBuilder.toString();
    }

    @Override
    public NPMBuildStepDescriptor getDescriptor() {
        return (NPMBuildStepDescriptor) super.getDescriptor();
    }

    public String getCommand() {
        return command;
    }

    public boolean isSudo() {
        return sudo;
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

