package com.innowise.userservice.dao;

import com.innowise.userservice.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {
    @Query("SELECT c FROM Card c JOIN FETCH c.user WHERE c.user.id = :id")
    List<Card> findAllByUserId(@Param("id") Long id);
}
