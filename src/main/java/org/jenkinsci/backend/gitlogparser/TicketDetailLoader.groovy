package org.jenkinsci.backend.gitlogparser

import hudson.plugins.jira.soap.JiraSoapService
import hudson.plugins.jira.soap.RemoteIssue
import org.jenkinsci.jira.JIRA

/**
 * Connect to JIRA and retrieve details for all the given tickets.
 *
 * @author Kohsuke Kawaguchi
 */
@Grapes([
    @Grab("org.jenkins-ci:jira-api:1.2"),
])
class TicketDetailLoader {
    private JiraSoapService jira;
    private String token;
    private Map<String,RemoteIssue> cache = [:];

    TicketDetailLoader() {
        jira = JIRA.connect(new URL("https://issues.jenkins-ci.org/"))
        def props = new Properties()
        props.load(new FileReader("${System.properties["user.home"]}/.jenkins-ci.org"))
        token = jira.login(props['userName'], props['password'])
    }

    RemoteIssue retrieve(OSSTicket t) {
        def k = t.displayName
        def v = cache[k]
        if (!cache.containsKey(k)) {
            try {
                cache[k]=v=jira.getIssue(token, k)
            } catch (Exception e) {
                e.printStackTrace();
                cache[k]=v=null;
            }
        }
        return v;
    }
}
