package org.jenkinsci.backend.gitlogparser

import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import hudson.util.VersionNumber

/**
 * Finds all the releases from Jenkins core git repository and generates {@code List<Release>}
 * that represents that.
 *
 * @author Kohsuke Kawaguchi
 */
@Grapes([
    @Grab("com.fasterxml.jackson.core:jackson-databind:2.1.1"),
    @Grab("org.jenkins-ci:version-number:1.1")
])
class ReleaseParser {
    def numDigits(v) {
        return v.toString().split("\\.").length;
    }

    List<Release> parse() {
        def releases = []

        def p = "git tag -l".execute()
        p.out.close()
        p.consumeProcessErrorStream(System.err)

        // collect all the version numbers
        def versions = [:] as Map<VersionNumber,String>
        p.in.eachLine { tag ->
            tag = tag.trim()
            if (tag ==~ /jenkins-(1(\.[0-9]+)*)/) {
                def v = new VersionNumber(tag.substring("jenkins-".length()))
                versions[v] = tag
            }
        }

        versions.each { v,tag ->
            def vn = v.toString()
            def prev = dec(vn)
            def revList = "$tag ^jenkins-$prev"

            if (numDigits(vn)==3 && numDigits(prev)==2) {
                // first branch off of new LTS baseline. add the last LTS release to the mix as well
                def lts = versions.keySet().findAll { it.compareTo(v)<0 && numDigits(it)==3 } as TreeSet
                if (!lts.isEmpty())
                    revList += " ^jenkins-${lts.last()}"
            }
            releases << new Release(version:v, displayName: vn, rc: false, ref: tag, revList: revList)
        }
        p.in.close()

        // add the tip of the active branches
        ["origin/rc","origin/stable"].each { branch ->
            // pick up the version in the POM
            p = "git show $branch:pom.xml".execute()
            p.out.close()
            p.consumeProcessErrorStream(System.err)
            def pom = new XmlSlurper().parse(p.in)

            String v = pom.version.text()
            v = v.substring(0,v.length()-"-SNAPSHOT".length())
            def prev = dec(v)
            releases << new Release(version:new VersionNumber(v), displayName: "$v RC", rc:true, ref: branch, revList: "$branch ^jenkins-$prev")
        }

        App app = new App()
        releases.each { Release r -> r.extractTickets(app) }

        return releases.sort();
    }

    // decrement a version number
    def dec(String ver) {
        def a = ver.split("\\.") as List
        a[-1] = (a.last() as Integer)-1
        if (a[-1]==0)
            a.pop()
        return a.join(".")
    }

    public static void main(String[] args) {
        def x = new ObjectMapper();
        // requires explicit annotation since Groovy generates all kinds of getters&setters
        // that confuses Jackson
        x.disable(MapperFeature.AUTO_DETECT_GETTERS, MapperFeature.AUTO_DETECT_FIELDS)
        x.enable(SerializationFeature.INDENT_OUTPUT)

        def lst = new ReleaseParser().parse();

        x.writeValue(args.length>0 ? new File(args[0]) : System.out,lst)
    }
}
