package com.bk.service;

import com.bk.model.Product;
import com.bk.model.ProductName;
import com.bk.model.Vote;
import com.bk.repository.ProductRepository;
import com.bk.repository.VoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VotingServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private VoteRepository voteRepository;

    @Mock
    private SettingsService settingsService;

    private VotingService votingService;

    @BeforeEach
    void setUp() {
        votingService = new VotingService(productRepository, voteRepository, settingsService);
    }

    @Test
    void countsVotesAndReportsEveryProductInATie() {
        LocalDate today = LocalDate.now();
        Product bread = product(ProductName.BREAD);
        Product chapati = product(ProductName.CHAPATI);
        when(voteRepository.findByVotingDateOrderByIdAsc(today)).thenReturn(List.of(
                new Vote("Alex", bread, today),
                new Vote("Casey", chapati, today)
        ));

        VotingResult result = votingService.getResult(today);

        assertEquals(3, result.counts().size());
        assertEquals(List.of("Bread", "Chapati"), result.winners());
        assertTrue(result.tied());
        assertEquals(0, result.counts().get(2).votes());
    }

    @Test
    void reportsNoWinnerWhenThereAreNoVotes() {
        LocalDate today = LocalDate.now();
        when(voteRepository.findByVotingDateOrderByIdAsc(today)).thenReturn(List.of());

        VotingResult result = votingService.getResult(today);

        assertTrue(result.winners().isEmpty());
        assertFalse(result.tied());
    }

    @Test
    void rejectsVoteWhenVotingIsClosed() {
        when(settingsService.isVotingOpen()).thenReturn(false);

        assertThrows(VotingClosedException.class,
                () -> votingService.castVote("Alex", ProductName.BREAD, LocalDate.now()));
        verify(voteRepository, never()).saveAndFlush(any());
    }

    @Test
    void normalizesNameBeforeCheckingForADuplicateVote() {
        LocalDate today = LocalDate.now();
        when(settingsService.isVotingOpen()).thenReturn(true);
        when(voteRepository.existsByNormalizedVoterNameAndVotingDate("alex", today)).thenReturn(true);

        assertThrows(DuplicateVoteException.class,
                () -> votingService.castVote("  Alex  ", ProductName.BREAD, today));
        verify(productRepository, never()).findByName(any());
    }

    @Test
    void recordsVoteWithOriginalDisplayNameAfterNormalizedDuplicateCheck() {
        LocalDate today = LocalDate.now();
        Product bread = product(ProductName.BREAD);
        when(settingsService.isVotingOpen()).thenReturn(true);
        when(voteRepository.existsByNormalizedVoterNameAndVotingDate("alex", today)).thenReturn(false);
        when(productRepository.findByName(ProductName.BREAD)).thenReturn(Optional.of(bread));
        when(voteRepository.saveAndFlush(any(Vote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vote savedVote = votingService.castVote("  Alex  ", ProductName.BREAD, today);

        assertEquals("Alex", savedVote.getVoterName());
        assertEquals("alex", savedVote.getNormalizedVoterName());
    }

    private Product product(ProductName name) {
        return new Product(name, BigDecimal.valueOf(200));
    }
}
