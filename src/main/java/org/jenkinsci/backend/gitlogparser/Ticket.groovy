package org.jenkinsci.backend.gitlogparser

import com.fasterxml.jackson.annotation.JsonProperty

abstract class Ticket {

    /**
     * Relevant commit IDs for this change. This doesn't participate to the comparison
     */
    @JsonProperty
    List<String> commits = [];

    public boolean equals(Object rhs) {
        return toString().equals(rhs.toString())
    }

    @Override
    int hashCode() {
        return toString().hashCode()
    }
}


