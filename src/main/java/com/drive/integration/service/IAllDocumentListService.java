package com.drive.integration.service;

import java.io.IOException;

import com.google.api.services.drive.Drive;

public interface IAllDocumentListService {

	public void fetchAllDocuments(Drive service) throws IOException;
}
