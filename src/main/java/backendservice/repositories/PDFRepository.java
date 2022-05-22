package backendservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import backendservice.entities.Pdf;

public interface PDFRepository extends MongoRepository<Pdf, String>{

	
}
