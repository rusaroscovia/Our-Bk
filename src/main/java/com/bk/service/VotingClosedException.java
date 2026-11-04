package com.bk.service;

public class VotingClosedException extends ServiceException {

    public VotingClosedException() {
        super("Voting is currently closed.");
    }
}
