package backendservice.repositories;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import backendservice.models.Role;
import backendservice.models.UserRole;

public interface RoleRepository extends MongoRepository<Role, String> {
	  Optional<Role> findByName(UserRole name);
	}