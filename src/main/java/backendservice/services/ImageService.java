package backendservice.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.google.common.io.ByteStreams;

import backendservice.entities.Image;
import backendservice.repositories.GridRepository;
import backendservice.repositories.ImageRespository;

@Service
public class ImageService {

	@Autowired
	private ImageRespository imageRepo;

	@Autowired
	private GridRepository gridRep;

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	
	@Autowired
	DownloadService downloadService;

	public ResponseEntity<String> saveImage(Image image, String json) {

		byte[] imageBytes = null;
		if (image.getUrl().contains("http")) {
			imageBytes = downloadService.downloadContent(image.getUrl());
		} else {
			try {
				imageBytes = Files.readAllBytes(Paths.get(image.getUrl()));
			} catch (IOException e) {
				logger.log(Level.SEVERE, "not able to read the file");
				e.printStackTrace();
				return new ResponseEntity<>("The image could not be saved", HttpStatus.BAD_REQUEST);
			}
		}

		image.setImage(imageBytes);

		if (imageBytes.length <= 16777216) {
			logger.log(Level.INFO, "inserting the image into the DB");
			imageRepo.insert(image);
		} else {
			logger.log(Level.INFO, "the size of the image  is larger than 16MB");
			logger.log(Level.INFO, "inserting the image into the DB");
			gridRep.saveLargeEntites(true, image.getTitle(), imageBytes, json);
		}
		logger.log(Level.INFO, "The pdf file has been successfully saved");
		return new ResponseEntity<>("The image  has been successfully saved", HttpStatus.OK);
	}

	public Image getImage(String uuid) {
		return imageRepo.findById(uuid).orElse(null);
	}

	}
