package com.bk.service;

import java.time.LocalDate;
import java.util.List;

public record VotingResult(
        LocalDate votingDate,
        List<VoteCount> counts,
        List<String> winners,
        boolean tied
) {
    public VotingResult {
        counts = List.copyOf(counts);
        winners = List.copyOf(winners);
    }
}
