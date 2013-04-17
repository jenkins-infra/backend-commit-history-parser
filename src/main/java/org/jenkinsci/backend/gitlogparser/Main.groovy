package org.jenkinsci.backend.gitlogparser

@Grab("org.jenkins-ci:jira-api:1.2")
class Main {
    static def main(args) {
        new App().main(args);
    }
}
