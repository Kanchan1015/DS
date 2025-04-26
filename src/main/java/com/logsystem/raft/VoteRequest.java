package com.logsystem.raft;

public class VoteRequest {
    private String candidateId;
    private long term;

    public VoteRequest() {}

    public VoteRequest(String candidateId, long term) {
        this.candidateId = candidateId;
        this.term = term;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }
}