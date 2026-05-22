package com.innowise.userservice.dao;

import com.innowise.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    @Query("SELECT u from User u LEFT JOIN FETCH u.cards WHERE u.id = :id")
    Optional<User> findById(@Param("id") Long id);
    @Query(nativeQuery = true, value = "select * from users as u where u.email = :email")
    Optional<User> findUserByEmail(@Param("email") String email);
}
