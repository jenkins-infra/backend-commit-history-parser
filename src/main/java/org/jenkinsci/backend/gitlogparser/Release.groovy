package org.jenkinsci.backend.gitlogparser

import com.fasterxml.jackson.annotation.JsonProperty
import hudson.util.VersionNumber

/**
 *
 *
 * @author Kohsuke Kawaguchi
 */
class Release implements Comparable<Release> {
    /**
     * Release version number. Used as the sort key.
     */
    VersionNumber version;

    /**
     * Human readable release name like "1.510" or "1.512 RC"
     */
    @JsonProperty
    String displayName;

    /**
     * True if this is an RC, which means the changelog for this is subject to change.
     */
    @JsonProperty
    boolean rc;

    /**
     * Git tag/branch/commitId that identifies the commit where the release was coming from.
     */
    @JsonProperty
    String ref;

    /**
     * Git rev-list for listing up commits in this release.
     */
    @JsonProperty
    String revList;

    /**
     * Tickets that are marked as fixed.
     */
    @JsonProperty
    List<Ticket> fixedTickets = []

    @JsonProperty("version")
    String getVersionString() {
        return version.toString();
    }

    /**
     * Number of milliseconds since the epoch that represents when this release was made
     */
    @JsonProperty
    Long getTimestamp() {
        def p = "git log -1 --format=format:%ct $revList".execute()
        p.consumeProcessErrorStream(System.err)
        try {
            return p.text.trim() as Long
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Figure out tickets that are fixed in this release, compared to the previous release.
     */
    void extractTickets(App app) {
        fixedTickets = app.extractTickets(app.&parseGitLog.curry(revList)) as List
        // fill in details from JIRA
        fixedTickets.findAll { it instanceof OSSTicket }.each { it.fill(app.ticketDetailLoader) }
    }

    int compareTo(Release that) {
        return this.version.compareTo(that.version);
    }
}
