package backendservice.repositories;

import org.springframework.data.mongodb.repository.MongoRepository;

import backendservice.entities.Image;

public interface  ImageRespository extends MongoRepository<Image, String> {

}
