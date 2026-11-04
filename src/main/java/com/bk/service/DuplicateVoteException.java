package com.bk.service;

public class DuplicateVoteException extends ServiceException {

    public DuplicateVoteException() {
        super("A vote has already been recorded for this name today.");
    }
}
