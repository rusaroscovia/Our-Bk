package com.bk.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Locale;

@Entity
@Table(
        name = "votes",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_votes_voter_name_voting_date",
                columnNames = {"normalized_voter_name", "voting_date"}
        )
)
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "voter_name", nullable = false, length = 100)
    private String voterName;

    @Column(name = "normalized_voter_name", nullable = false, length = 100)
    private String normalizedVoterName;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @NotNull
    @Column(name = "voting_date", nullable = false)
    private LocalDate votingDate;

    protected Vote() {
    }

    public Vote(String voterName, Product product, LocalDate votingDate) {
        setVoterName(voterName);
        this.product = product;
        this.votingDate = votingDate;
    }

    @PrePersist
    @PreUpdate
    private void normalizeVoterName() {
        if (voterName != null) {
            voterName = voterName.trim();
            normalizedVoterName = voterName.toLowerCase(Locale.ROOT);
        }
    }

    public Long getId() {
        return id;
    }

    public String getVoterName() {
        return voterName;
    }

    public String getNormalizedVoterName() {
        return normalizedVoterName;
    }

    private void setVoterName(String voterName) {
        this.voterName = voterName == null ? null : voterName.trim();
        this.normalizedVoterName = this.voterName == null
                ? null
                : this.voterName.toLowerCase(Locale.ROOT);
    }

    public Product getProduct() {
        return product;
    }

    public LocalDate getVotingDate() {
        return votingDate;
    }
}
