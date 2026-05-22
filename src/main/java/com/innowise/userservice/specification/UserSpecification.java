package com.innowise.userservice.specification;

import com.innowise.userservice.model.User;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {
    public static Specification<User> hasName(String name){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("name"), name);
    }

    public static Specification<User> hasSurname(String surname){
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("surname"), surname);
    }
}
