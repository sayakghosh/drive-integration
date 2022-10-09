package com.drive.integration.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

	//batch size of the new files events
	@Value("${new.file.events.batch.size}")
	int newEventBatchSize;
	
	@Autowired
	CommonUtil commonUtil;

	@Override
	public void newFileEventProcessor(Drive service) throws IOException {

		System.out.println("\n Listening for new events.");
		StartPageToken response = service.changes().getStartPageToken().execute();
		// [END fetchStartPageToken]
		String savedStartPageToken = response.getStartPageToken();
		String pageToken = savedStartPageToken;
		List<FileMetadata> fileMetadataList = new ArrayList<>();
		int eventCounter = 0;

		while (newEventBatchSize > eventCounter) {
			//System.out.println("Start token: " + response.getStartPageToken());
			ChangeList changes = service.changes().list(pageToken).execute();
			//List<Change> changeStore = new ArrayList<>();	            
			for (Change change : changes.getChanges()) {
				// Process the change found
				System.out.println("Change found for file/folder: " + change.getFileId() + "\t Change time: " + change.getTime());
				eventCounter++;
				FileMetadata fileMetadata = fetchFileMetaData(service, change.getFileId());
				fileMetadataList.add(fileMetadata);
				System.out.println("JSON: " + new Gson().toJson(fileMetadata));
			}
			if (changes.getNewStartPageToken() != null) {
				// Last page, save this token for the next polling interval
				savedStartPageToken = changes.getNewStartPageToken();
			}
			pageToken = changes.getNextPageToken();
			if (pageToken == null) {
				pageToken = savedStartPageToken;
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		commonUtil.writeIntoFile(fileMetadataList);
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
	
}
