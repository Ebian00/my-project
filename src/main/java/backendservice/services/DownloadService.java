package backendservice.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bson.BsonMaximumSizeExceededException;
import org.springframework.stereotype.Service;

import com.google.common.io.ByteStreams;


@Service
//the download service facilitates the download of content with the secure https address
public class DownloadService {

    private final Logger logger = Logger.getLogger(this.getClass().getName());

	public byte[] downloadContent(String path) {
		
		byte[] contentBytes = null;
		InputStream is = null ;
		try {
			if(path.contains("http")) {
				System.setProperty("http.agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/28.0.1500.29 Safari/537.36"); 
				 is = new URL(path).openStream();
				 contentBytes = ByteStreams.toByteArray(is);
				 return contentBytes;
			}
			else {
				contentBytes = Files.readAllBytes(Paths.get(path));
				return contentBytes;
			}
			}
	 catch (BsonMaximumSizeExceededException | IOException e) {
		 logger.log(Level.SEVERE,"The content could not be downloaded");
		 e.printStackTrace();
		return contentBytes;
	}
	}
}
