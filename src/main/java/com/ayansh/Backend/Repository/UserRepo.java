package com.ayansh.Backend.Repository;


import com.ayansh.Backend.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {


    User findByName(String name);

    boolean existsByEmail(String email);

    boolean existsByName(String userName);
}
