package backendservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import backendservice.entities.HtmlPage;

public interface HTMLRepository extends MongoRepository<HtmlPage, String>{

}
