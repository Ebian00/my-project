package backendservice.repositories;

import static org.bson.codecs.configuration.CodecRegistries.fromProviders;

import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;

import org.bson.BsonValue;
import org.bson.Document;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.gridfs.model.GridFSUploadOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.gridfs.GridFSDBFile;

import backendservice.entities.Pdf;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@Repository
//for saving and extracting large file into mongoDB we need to use Grid due to the limit of the content size in Mongo
//which is 16MB
public class GridRepository {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	@Value("${spring.data.mongodb.database}")
	private String db;

	@Value("${spring.data.mongodb.host}")
	private String host;

	@Value("${spring.data.mongodb.port}")
	private String port;

	private MongoDatabase mongoDB= null;
	
	private boolean initialize = false;
	
	@Autowired
	GridFsTemplate gridFsTemplate;
	@Autowired
	private PDFRepository pdfRepo;
	@Autowired
	private GridFsOperations operations;

	//inorder to use grid we need to initialize the DB, here for the configurations form the properties file are read and the DB is initialized
	
	public void initializeDB() {
		logger.log(Level.INFO, "Initializing  MongoDB");
		//above the initializing we also have to register our entities to push them into the DB
		CodecProvider pojoCodecProvider = PojoCodecProvider.builder().register("backendservice.entities").build();
		CodecRegistry pojoCodecRegistry = fromRegistries(MongoClient.getDefaultCodecRegistry(),
				fromProviders(PojoCodecProvider.builder().automatic(true).build()));
		MongoClient mongoClient = new MongoClient(host,
				MongoClientOptions.builder().codecRegistry(pojoCodecRegistry).build());

		this.mongoDB = mongoClient.getDatabase(db);
		
		logger.log(Level.INFO, "Initializing  was successful");
	}

	//this method saves larges content >16Mb into the MongoDB, for this we have made use of GridFSBucket
	public void saveLargeEntites(boolean bool, String titel, byte[] pdfFileBytes, String json) {

		if(!initialize) {
			this.initializeDB();
			this.initialize=true;
		}
		logger.log(Level.INFO, "Trying to save the content " + titel + "to the MongoDB");
		logger.log(Level.INFO, "size of the byte" + pdfFileBytes.length);
		GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDB, bool ? "largeImage" : "largePdf");

		GridFSUploadOptions options = new GridFSUploadOptions();

		options.metadata(new Document().parse(json));
		GridFSUploadStream uploadStream = gridFSBucket.openUploadStream(titel, options);

		uploadStream.write(pdfFileBytes);
		uploadStream.close();
	}

	//this methods gets the content with his id from the DB and returns a stream for the frontend, for this we also use GridFSBucket 
	public InputStream getLargeEntity(boolean bool, String titel, String id) {
		
		if(!initialize) {
			this.initializeDB();
			this.initialize=true;
		}
		//initializing the Grid 
		GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDB, bool ? "largeImage" : "largePdf");
		//A Bson filter that searches in the DB in the metadata of the contents
		Bson filter = Filters.and(Filters.eq("filename", titel), Filters.eq("metadata.id", id));
		GridFSFindIterable downloadStream = gridFSBucket.find(filter);
		//after initilizing the grid and searching the DB we assign the oupt to a GridFile
		GridFSFile  gridFile = downloadStream.first();
		GridFSDownloadStream gfsFile = gridFSBucket.openDownloadStream(gridFile.getId());
		InputStream is = gfsFile;
		try {
			return is;
		} catch (IllegalStateException e) {
			logger.log(Level.WARNING, "the input stream for the desired content could not be returned");
		}
		return null;
	}
	
	//this methods gets the content with his id from the DB and returns content as Byte array for the frontend, for this we also use GridFSBucket 
	public byte[] getLargeEntityByte(boolean bool, String id) {
		Pdf pdf =  pdfRepo.findById(id).orElse(null);
		if(!(pdf == null)) {
			 
			 return pdf.getPdfFile();
		}
		if(!initialize) {
			this.initializeDB();
		}
		/*first we create a GridBucket to get the content form the Mongo directly
		 * the point about this content is that the id is saved in the metadata of the object in the DB so 
		 * we have to use a filter in order to get the content
		 * if the repository return does return the content, we map it to the responseObject
		*/
		GridFSBucket gridFSBucket = GridFSBuckets.create(mongoDB, bool ? "largeImage" : "largePdf");
		GridFSFindIterable downloadStream = gridFSBucket.find(Filters.and(Filters.eq("metadata.id", id)));
		GridFSFile  gridFile = downloadStream.first();
		GridFSDownloadStream gfsFile = gridFSBucket.openDownloadStream(gridFile.getId());
		InputStream is = gfsFile;
		try {
			return  ByteStreams.toByteArray(is);
		} catch (IOException e) {
			logger.log(Level.WARNING, "the Byte array for the desired content could not be returned");
		}
		return null;
	}
}
