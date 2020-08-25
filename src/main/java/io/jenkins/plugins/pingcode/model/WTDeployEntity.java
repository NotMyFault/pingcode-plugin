package io.jenkins.plugins.pingcode.model;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;
import io.jenkins.plugins.pingcode.WTHelper;
import io.jenkins.plugins.pingcode.resolver.WorkItemResolver;

public class WTDeployEntity {
    public String releaseName;
    public String status;
    public String envId;
    public String releaseUrl;
    public long startAt;
    public long endAt;
    public long duration;
    public String[] workItemIdentifiers;

    public static WTDeployEntity from(Run<?, ?> run, FilePath workspace, TaskListener listener, String releaseName,
            String releaseUrl, String envId, boolean isTagged) {
        return WTDeployEntity.from(run, workspace, listener, null, releaseName, releaseUrl, envId, isTagged);
    }

    public static WTDeployEntity from(Run<?, ?> run, FilePath workspace, TaskListener listener, String status,
            String releaseName, String releaseUrl, String envId, boolean isTagged) {
        WTDeployEntity entity = new WTDeployEntity();

        if (status == null) {
            String autoStatus = WTHelper.statusOfRun(run);
            status = autoStatus.equals("success") ? Status.Deployed.getValue() : Status.NotDeployed.getValue();
        }

        EnvVars vars = WTHelper.safeEnvVars(run);
        entity.releaseName = vars.expand(releaseName);
        entity.releaseUrl = vars.expand(releaseUrl);
        entity.envId = envId;
        entity.status = status;
        entity.startAt = WTHelper.toSafeTs(run.getStartTimeInMillis());
        entity.endAt = WTHelper.toSafeTs(System.currentTimeMillis());
        entity.duration = Math.subtractExact(entity.endAt, entity.startAt);
        entity.workItemIdentifiers = new WorkItemResolver(run, workspace, listener, isTagged)//
                .resolve()//
                .toArray(new String[0]);
        return entity;
    }

    public String toString() {
        return WTHelper.prettyJSON(this);
    }

    public enum Status {
        Deployed("deployed"), NotDeployed("not_deployed");

        private final String value;

        Status(String deploy) {
            this.value = deploy;
        }

        public String getValue() {
            return value;
        }
    }
}
