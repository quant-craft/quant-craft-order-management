package com.quant.craft.ordermanagement.repository;

import com.quant.craft.ordermanagement.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
