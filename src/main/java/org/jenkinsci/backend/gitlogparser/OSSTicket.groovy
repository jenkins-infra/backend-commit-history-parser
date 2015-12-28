package org.jenkinsci.backend.gitlogparser

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Tickets for OSS Jenkins
 *
 * @author Kohsuke Kawaguchi
 */
abstract class OSSTicket extends Ticket {
    int id;

    /**
     * JIRA ticket ID, like "JENKINS-1234"
     */
    @JsonProperty
    abstract String getDisplayName();

    /**
     * JIRA issue title
     */
    @JsonProperty
    String summary;

    /**
     * JIRA issue type
     *
     * bug/feature/task/improve/patch
     */
    String type;

    /**
     * Priority.
     *
     * P1/P2/...
     */
    String priority;

    def fill(TicketDetailLoader ticketDetailLoader) {
        def i = ticketDetailLoader.retrieve(this)
        summary = i?.summary
        priority = i?.priority
        if (i != null) {
            type = App.jiraType(i.type)
        }
    }

    String toString() {
        return displayName;
    }
}
