package com.bk.service;

import com.bk.model.Product;
import com.bk.model.ProductName;
import com.bk.model.Vote;
import com.bk.repository.ProductRepository;
import com.bk.repository.VoteRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VotingService {

    private final ProductRepository productRepository;
    private final VoteRepository voteRepository;
    private final SettingsService settingsService;

    public VotingService(
            ProductRepository productRepository,
            VoteRepository voteRepository,
            SettingsService settingsService
    ) {
        this.productRepository = productRepository;
        this.voteRepository = voteRepository;
        this.settingsService = settingsService;
    }

    @Transactional
    public Vote castVote(String voterName, ProductName productName, LocalDate votingDate) {
        String normalizedName = validateVoterName(voterName);
        if (productName == null) {
            throw new IllegalArgumentException("Choose one of the available breakfast options.");
        }
        if (votingDate == null || votingDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Voting date is required and cannot be in the future.");
        }
        if (!settingsService.isVotingOpen()) {
            throw new VotingClosedException();
        }
        if (voteRepository.existsByNormalizedVoterNameAndVotingDate(normalizedName, votingDate)) {
            throw new DuplicateVoteException();
        }

        Product product = productRepository.findByName(productName)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productName));
        try {
            return voteRepository.saveAndFlush(new Vote(voterName.trim(), product, votingDate));
        } catch (DataIntegrityViolationException exception) {
            if (DatabaseConstraint.matches(exception, "uk_votes_voter_name_voting_date")) {
                throw new DuplicateVoteException();
            }
            throw exception;
        }
    }

    @Transactional
    public VotingResult closeVoting(LocalDate votingDate) {
        validateVotingDate(votingDate);
        settingsService.setVotingOpen(false);
        return getResult(votingDate);
    }

    @Transactional
    public void openVoting() {
        settingsService.setVotingOpen(true);
    }

    @Transactional(readOnly = true)
    public VotingResult getResult(LocalDate votingDate) {
        validateVotingDate(votingDate);
        Map<ProductName, Long> counts = voteRepository.findByVotingDateOrderByIdAsc(votingDate).stream()
                .collect(Collectors.groupingBy(vote -> vote.getProduct().getName(), Collectors.counting()));
        List<VoteCount> resultCounts = Arrays.stream(ProductName.values())
                .map(name -> new VoteCount(name, name.getDisplayName(), counts.getOrDefault(name, 0L)))
                .toList();
        long highestCount = resultCounts.stream().mapToLong(VoteCount::votes).max().orElse(0);
        List<String> winners = highestCount == 0 ? List.of() : resultCounts.stream()
                .filter(count -> count.votes() == highestCount)
                .map(VoteCount::displayName)
                .toList();
        return new VotingResult(votingDate, resultCounts, winners, winners.size() > 1);
    }

    @Transactional(readOnly = true)
    public boolean isVotingOpen() {
        return settingsService.isVotingOpen();
    }

    private String validateVoterName(String voterName) {
        if (voterName == null || voterName.isBlank() || voterName.trim().length() > 100) {
            throw new IllegalArgumentException("Voter name must contain 1 to 100 characters.");
        }
        return NameNormalizer.normalize(voterName);
    }

    private void validateVotingDate(LocalDate votingDate) {
        if (votingDate == null || votingDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Voting date is required and cannot be in the future.");
        }
    }
}
