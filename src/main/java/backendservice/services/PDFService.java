package backendservice.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.BsonBinarySubType;
import org.bson.types.Binary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import backendservice.entities.Pdf;
import backendservice.repositories.GridRepository;
import backendservice.repositories.PDFRepository;

@Service
//this service is for saving and extracting pdf files from and into the mongo db
public class PDFService {
	
	//Initializing the logger 
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    
    
    @Autowired
    DownloadService downloadService;
	@Autowired
	private PDFRepository pdfRep;

	@Autowired
	private GridRepository gridRep;

	//this method reads the PDF file from the source in the JSON 
	public ResponseEntity<String> savePDF(Pdf pdf, String json) {

		byte[] pdfFileBytes = null;
		// here we differ betwenn whether the content is on another source for example internet, if that is the case we have to download the content
		//in a different way compared to if the source is for example on the server
			if(pdf.getUrl().contains("http")) {
				pdfFileBytes = downloadService.downloadContent(pdf.getUrl());
			}
			else {
				try {
					pdfFileBytes = Files.readAllBytes(Paths.get(pdf.getUrl()));
				} catch (IOException e) {
					logger.log(Level.SEVERE,"not able to read the file");
					e.printStackTrace();
					return new ResponseEntity<>(
						      "The pdf file could not be saved",HttpStatus.BAD_REQUEST);
				}
			}
			
			//this method checks the size of the file due to different procedure for saving the Pdf file 
			//in the DB if the size of the file is bigger than 16MB
			pdf.setPdfFile( pdfFileBytes);
			if(pdfFileBytes.length <= 16000000) {
				logger.log(Level.INFO,"inserting the pdf file into the DB");
				pdfRep.insert(pdf);
			}
			else {
				logger.log(Level.INFO,"the size of the pdf file is larger than 16MB");
				logger.log(Level.INFO,"inserting the pdf file into the DB");
				 gridRep.saveLargeEntites(false,pdf.getTitle(), pdfFileBytes,json);
			}
			 logger.log(Level.INFO,"The pdf file has been successfully saved");
			 return new ResponseEntity<>(
				      "The pdf file has been successfully saved",HttpStatus.OK);
	
		
		
	}
	
	//for pdf file smaller than 16MB, we use the pdf repository to get the content
	public Optional<Pdf> getPDFFile(String titel,String id) {
		gridRep.getLargeEntity(false, titel, id);
		return pdfRep.findById(id);
	}
	
	//returns the source pdf file in form of  byte Array
	public byte[] getPDFFileAsBytes(String id) {
		return gridRep.getLargeEntityByte(false, id);
	}
	
	//provides a stream for the pdf file 
	public InputStream  getPDFFileAsStream(String titel,String id) {
		return gridRep.getLargeEntity(false, titel, id);
	}
	

}
