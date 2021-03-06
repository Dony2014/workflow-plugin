package org.jenkinsci.plugins.workflow.steps;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import jenkins.util.BuildListenerAdapter;

/**
 * @author Kohsuke Kawaguchi
 */
public class ArtifactArchiverStepExecution extends AbstractSynchronousStepExecution<Void> {

    @StepContextParameter
    private transient TaskListener listener;

    @StepContextParameter
    private transient FilePath ws;

    @StepContextParameter
    private transient EnvVars envVars;

    @StepContextParameter
    private transient Run build;

    @StepContextParameter
    private transient Launcher launcher;

    @Inject
    ArtifactArchiverStep step;

    @Override
    protected Void run() throws Exception {
        String includes = envVars.expand(step.getIncludes());
        Map<String,String> files = ws.act(new ListFiles(includes, step.getExcludes()));
        build.pickArtifactManager().archive(ws, launcher, new BuildListenerAdapter(listener), files);
        return null;
    }

    private static final class ListFiles implements FilePath.FileCallable<Map<String,String>> {
        private static final long serialVersionUID = 1;
        private final String includes, excludes;
        ListFiles(String includes, String excludes) {
            this.includes = includes;
            this.excludes = excludes;
        }
        @Override public Map<String,String> invoke(File basedir, VirtualChannel channel) throws IOException, InterruptedException {
            Map<String,String> r = new HashMap<String,String>();
            for (String f : Util.createFileSet(basedir, includes, excludes).getDirectoryScanner().getIncludedFiles()) {
                f = f.replace(File.separatorChar, '/');
                r.put(f, f);
            }
            return r;
        }
    }
}
