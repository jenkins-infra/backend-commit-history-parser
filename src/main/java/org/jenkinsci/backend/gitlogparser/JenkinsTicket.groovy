package org.jenkinsci.backend.gitlogparser

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
class JenkinsTicket extends OSSTicket {
    String getDisplayName() {
        return "JENKINS-${id}"
    }
}
