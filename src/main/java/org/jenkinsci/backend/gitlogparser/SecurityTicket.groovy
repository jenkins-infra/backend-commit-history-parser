package org.jenkinsci.backend.gitlogparser

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
class SecurityTicket extends OSSTicket {

    String getDisplayName() {
        return "SECURITY-${id}"
    }
}
