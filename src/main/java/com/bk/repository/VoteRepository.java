package com.bk.repository;

import com.bk.model.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    boolean existsByNormalizedVoterNameAndVotingDate(String normalizedVoterName, LocalDate votingDate);

    List<Vote> findByVotingDateOrderByIdAsc(LocalDate votingDate);

    long countByProductIdAndVotingDate(Long productId, LocalDate votingDate);
}
