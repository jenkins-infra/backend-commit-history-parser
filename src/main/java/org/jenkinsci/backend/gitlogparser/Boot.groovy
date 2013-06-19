package org.jenkinsci.backend.gitlogparser

import groovy.grape.Grape;

static def boot(clazz,args) {
    [
        "org.jenkins-ci:jira-api:1.2",
        "org.jenkins-ci:version-number:1.1",
        "com.fasterxml.jackson.core:jackson-databind:2.1.1"
    ].each { dep ->
        def (g,a,v) = dep.split(":")
        Grape.grab(group:g,module:a,version:v)
    }

    def b = Boot.class
    b.classLoader.loadClass(b.package.name+'.'+clazz).newInstance().main(args)
}
