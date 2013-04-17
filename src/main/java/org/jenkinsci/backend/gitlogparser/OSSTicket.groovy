package org.jenkinsci.backend.gitlogparser

/**
 * Tickets for OSS Jenkins
 *
 * @author Kohsuke Kawaguchi
 */
abstract class OSSTicket extends Ticket {
    int id;

    abstract String getDisplayName();

    String toString() {
        return displayName;
    }
}
