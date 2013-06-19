package org.jenkinsci.backend.gitlogparser

import groovy.text.GStringTemplateEngine
import hudson.plugins.jira.soap.RemoteIssue

/**
 * Compares two rev-lists (think the 'master' branch and the 'stable' branch) and
 * identify a set of bug fixes that are in the former but not in the latter.
 *
 * Produce a list of commits related to those commits to assist merging those changes.
 *
 * @author Kohsuke Kawaguchi
 */
class BackportCandidateListGenerator extends App {
    def main(args) {
        def issuesFixedInBase = extractTickets(this.&parseGitLog.curry(args[0]));
        def issuesFixedInLts = extractTickets(this.&parseGitLog.curry(args[1]));

        def cherrypicks = issuesFixedInBase - issuesFixedInLts

        // write out the cheatsheet for the cherry picking
        system "rm -rf ./cherry-pick"
        system "mkdir ./cherry-pick"
        cherrypicks.each { Ticket t ->
            // git-log lists changes from new to old, but to make cherry-picking easier we want to save them old to new
            new File("./cherry-pick/"+t).text = t.commits.reverse().join("\n")
        }

        def issues = retrieveJiraTicketDetails(cherrypicks)
        issues.sort { RemoteIssue a, RemoteIssue b -> b.votes.compareTo(a.votes)}

        def template = new GStringTemplateEngine().createTemplate(getClass().getResource("cherry-pick.html.gsp")).make([issues:issues, cherrypicks:cherrypicks, app:this])

        new File("./cherry-pick.html").withWriter { template.writeTo(it) }

        println "List generated. See ./cherry-pick.html for details"
    }


}
