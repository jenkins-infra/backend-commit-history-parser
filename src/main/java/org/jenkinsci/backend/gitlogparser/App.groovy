package org.jenkinsci.backend.gitlogparser

import hudson.plugins.jira.soap.RemoteIssue
import org.jenkinsci.jira.JIRA

/**
 * Takes two arguments, feed them to "git log", and obtain two lists of commits.
 * Look for changes that are marked as fixed on one side, but not on the other, list them up,
 * and create the "cherry pick cheat sheet" that contains commits for backporting those fixes.
 */
class App {
    def ticketDetailLoader = new TicketDetailLoader()

    /**
     * Generates a changelog fragment into the specified file.
     *
     * @param gitRevList
     *      git-rev-list option to specify a range of commits from which changes are discovered.
     * @param file
     *      The file to be created.
     */
    def generateChangeLog(gitRevList, Writer w) {
        def tickets = extractTickets(this.&parseGitLog.curry(gitRevList));
        def issues = retrieveJiraTicketDetails(tickets)

        issues.each { RemoteIssue i ->
            if (!i.key.startsWith("JENKINS-")
             && !i.key.startsWith("SECURITY-"))  return;   // ignore non-Jenkins tickets

            int n = Integer.parseInt(i.key.split("-")[1])

            def clazz = jiraType(i.type)=="bug" ? "bug" : "rfe";

            if (i.priority=="P1")   clazz = "major "+clazz;
            w << "  <li class='${clazz}'>\n"
            w << "    ${i.summary}\n"
            if (i.key.startsWith("JENKINS-"))
                w << "    (<a href='https://issues.jenkins-ci.org/browse/${i.key}'>issue ${n}</a>)\n"
            else
                w << "    (SECURITY-${n})\n"
        }
    }

    def generateChangeLog(gitRevList, File file) {
        file.withWriter("UTF-8") { w ->
            generateChangeLog(gitRevList,w)
        }
    }

    static def jiraStatus(String code) {
        switch(code) {
        case "1":       return "open";
        case "3":       return "progress";
        case "4":       return "reopened";
        case "5":       return "resolved";
        case "6":       return "closed";
        case "10000":   return "verified";
        return code;
        }
    }

    static def jiraResolution(String code) {
        if (code==null) return "";
        switch(code) {
        case "1":   return "fixed";
        case "2":   return "won't fix";
        case "3":   return "duplicate";
        case "4":   return "incomplete";
        case "5":   return "cant repro";
        case "6":   return "postponed";
        case "7":   return "notadefect";
        return code;
        }
    }

    static def jiraType(String code) {
        if (code==null) return "";
        switch(code) {
        case "1":   return "bug";
        case "2":   return "feature";
        case "3":   return "task";
        case "4":   return "improve";
        case "5":   return "patch";
        return code;
        }
    }

    /**
     * Executes a command via shell and
     */
    def system(String cmd) {
        def p = cmd.execute()
        p.consumeProcessOutput()
        return p.waitFor()==0;
    }

    /**
     * Connect to JIRA and retrieve details for all the given tickets.
     */
    List<RemoteIssue> retrieveJiraTicketDetails(Collection tickets) {
        return tickets
            .findAll { it instanceof OSSTicket }
            .collect { OSSTicket t -> ticketDetailLoader.retrieve(t) }
    }

    // find tickets mentioned in the given string
    Set<Ticket> findTickets(String msg) {
        def tickets = [] as Set;
        (msg =~ /JENKINS-(\d+)/).each { m ->
            tickets << new JenkinsTicket(id:m[1] as int);
        }
        (msg =~ /SECURITY-(\d+)/).each { m ->
            tickets << new SecurityTicket(id:m[1] as int);
        }
        return tickets;
    }


    /**
     * Use the generator to produce commits, then look for fixed tickets in those commits.
     */
    Collection<Ticket> extractTickets(Closure generator) {
        Map<Ticket,List<String/*sha1*/>> commits = [:];
        Set<Ticket> fixed = [];

        generator { String sha1, String log -> // for each commit given by a generator...

            // println "${sha1}:${log.substring(0,Math.min(log.length(),50))}"

            // look for commits related to a ticket, including those that aren't fixed
            findTickets(log).each { Ticket t ->
                def v = commits[t];
                if (v==null)    commits[t]=v=[];
                v << sha1;
            }

            // look for fixed tickets
            (log =~ /\[FIXED ([^\]]+)\]/).each { m ->
                fixed.addAll(findTickets(m[1]));
            }
        }

        fixed.each { it.commits = commits[it] }

        return fixed
    }

    def parseGitLog(String cmdline, Closure consumer) {
        def p = "git log ${cmdline}".execute()
        p.out.close()
        p.consumeProcessErrorStream(System.err)
        parse(p.in.newReader(),consumer)
        p.in.close()
    }


    def parseStdin(Closure consumer) {
        parse(System.in.newReader(),consumer)
    }

    /**
     * Parses the "git log" output from the reader and pass sha1 and log message to the given closure
     */
    def parse(Reader r, Closure consumer) {
        def commitId = null;
        def buf = new StringBuilder();

        def pass = {
            if (commitId!=null)
                consumer(commitId,buf.toString());
        }

        r.eachLine { line ->
            if (line.startsWith("commit ")) {
                pass();
                commitId = line.substring(6).trim();
                buf.setLength(0);
            } else
            if (line.startsWith("    ")) {
                buf.append(line.substring(4));
            }
        }
        pass();
    }
}
