package com.drive.integration.service;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;

@Service
public class AllDocumentListServiceImpl implements IAllDocumentListService {
	
	//batch size of the files to fetch from Google Drive
	@Value("${all.file.fetch.batch.size}")
    int fileFetchBatchSize;

	@Override
	public void fetchAllDocuments(Drive service) throws IOException {		
		
		List<File> allFiles = fetchFilesList(service);
        if (allFiles == null || allFiles.isEmpty()) {
            System.out.println("Drive is empty.");
        } else {
        	for (File file : allFiles) {
        		System.out.println("File Name: " + file.getName() + "\t Created Date: " + file.getCreatedTime() 
        		+ "\t Last Modified Date: " + file.getModifiedTime() + "\t File extension: " + file.getFileExtension());
        	}
        }
	}
	
	private List<File> fetchFilesList(Drive service) throws IOException {
		
		//Retrieve all the files metadata from Google Drive
		return service.files().list().setPageSize(fileFetchBatchSize)
				.setFields("nextPageToken, files(name, createdTime, modifiedTime, fileExtension)")
				.execute().getFiles();
	}
}
