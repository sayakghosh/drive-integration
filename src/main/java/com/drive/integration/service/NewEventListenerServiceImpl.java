package com.drive.integration.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.drive.integration.common.CommonUtil;
import com.drive.integration.model.FileMetadata;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.Change;
import com.google.api.services.drive.model.ChangeList;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.StartPageToken;
import com.google.gson.Gson;

@Service
public class NewEventListenerServiceImpl implements INewEventListenerService{
	
	Logger logger = LoggerFactory.getLogger(NewEventListenerServiceImpl.class);

	//Batch size of the new files events.
	@Value("${new.file.events.batch.size}")
	int newEventBatchSize;
	
	//Number of threads to be used.
	@Value("${fork.join.pool.size}")
	int forkJoinPoolSize;

	@Autowired
	CommonUtil commonUtil;

	@Override
	public void newFileEventProcessor(Drive service) throws IOException {

		System.out.println("\nListening for new events.");
		StartPageToken response = service.changes().getStartPageToken().execute();
		//Save the start page
		String savedStartPageToken = response.getStartPageToken();
		String pageToken = savedStartPageToken;
		List<FileMetadata> fileMetadataList = new ArrayList<>();
		int eventCounter = 0;

		while (true) {
			//Iterate every page to pick up new file events.
			ChangeList changes = service.changes().list(pageToken).execute();

			for (Change change : changes.getChanges()) {
				// Process the change found
				System.out.println("\nChange found for file/folder: " + change.getFileId() + "\t Change time: " + change.getTime());
				eventCounter++;
				FileMetadata fileMetadata = fetchFileMetaData(service, change.getFileId());
				fileMetadataList.add(fileMetadata);
				System.out.println("JSON: " + new Gson().toJson(fileMetadata));
				if(newEventBatchSize == eventCounter) {
					writeTheFiles(service, fileMetadataList);
					eventCounter = 0;
					fileMetadataList.clear();
				}
			}
			if (changes.getNewStartPageToken() != null) {
				// Last page, save this token for the next polling interval
				savedStartPageToken = changes.getNewStartPageToken();
			}
			pageToken = changes.getNextPageToken();
			if (pageToken == null) {
				//Start again from saved start page token
				pageToken = savedStartPageToken;
			}

			try {
				//5 second sleep between each API call, so that the system doesn't keep polling the Google Drive API and hit the quota.
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	private FileMetadata fetchFileMetaData(Drive service, String fileId) throws IOException {

		FileMetadata fileMetadata = new FileMetadata();

		//retrieve all the file details from Google Drive
		File file = service.files().get(fileId)
				.setFields("id, name, createdTime, modifiedTime, size, fileExtension")
				.execute();

		fileMetadata.setCreatedTime(file.getCreatedTime().toStringRfc3339());
		fileMetadata.setFileExtension(file.getFileExtension());
		fileMetadata.setId(file.getId());
		fileMetadata.setModifiedTime(file.getModifiedTime().toStringRfc3339());
		fileMetadata.setFilename(file.getName());
		fileMetadata.setSize(file.getSize());

		return fileMetadata;
	}
	
	//Write Metadata into local and download file from Google Drive to local.
	public void writeTheFiles(Drive service, List<FileMetadata> fileMetadataList) {
		
		long startTime = System.currentTimeMillis();
		//Write the metadata into file once the configured batch size is reached.
		commonUtil.writeMetadataListIntoFile(fileMetadataList);
		//Download the new file and place into configured location.
		downloadAllNewFiles(service, fileMetadataList);		
		logger.info("Jobs successfully completed within {}ms", System.currentTimeMillis() - startTime);
	}

	private void downloadAllNewFiles(Drive service, List<FileMetadata> fileMetadataList) {
//		fileMetadataList.forEach(fileMetadata -> {
//			try {
//				commonUtil.downloadFileIntoLocal(service, fileMetadata);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		});

		ForkJoinPool forkJoinPool = null; 
		try {
			//Initialize number of threads to fork for the download.
			forkJoinPool = new ForkJoinPool(forkJoinPoolSize);
			forkJoinPool.submit(() ->
			fileMetadataList.parallelStream().forEach(fileMetadata -> {
				try {
					commonUtil.downloadFileIntoLocal(service, fileMetadata);
				} catch (IOException e) {
					e.printStackTrace();
				}
			})).get();
		} catch (Exception ex) {
			ex.printStackTrace();	
		} finally {
			if (forkJoinPool != null) {
				forkJoinPool.shutdown();
			}
		}
	}

}
