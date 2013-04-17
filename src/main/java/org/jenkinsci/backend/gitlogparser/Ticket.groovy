package org.jenkinsci.backend.gitlogparser

abstract class Ticket {

    /**
     * Relevant commit IDs for this change. This doesn't participate to the comparison
     */
    List<String> commits = [];

    public boolean equals(Object rhs) {
        return toString().equals(rhs.toString())
    }

    @Override
    int hashCode() {
        return toString().hashCode()
    }

}


