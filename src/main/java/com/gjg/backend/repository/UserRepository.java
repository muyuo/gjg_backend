package com.gjg.backend.repository;

import com.gjg.backend.model.User;
import org.springframework.data.repository.CrudRepository;

import java.rmi.server.UID;

public interface UserRepository extends CrudRepository<User, UID> {
}
