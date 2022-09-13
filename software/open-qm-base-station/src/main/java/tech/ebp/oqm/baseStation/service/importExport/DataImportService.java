package tech.ebp.oqm.baseStation.service.importExport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.lang3.time.StopWatch;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;
import tech.ebp.oqm.baseStation.rest.dataImportExport.DataImportResult;
import tech.ebp.oqm.baseStation.rest.dataImportExport.ImportBundleFileBody;
import tech.ebp.oqm.baseStation.rest.search.SearchObject;
import tech.ebp.oqm.baseStation.service.mongo.ImageService;
import tech.ebp.oqm.baseStation.service.mongo.InventoryItemService;
import tech.ebp.oqm.baseStation.service.mongo.MongoHistoriedService;
import tech.ebp.oqm.baseStation.service.mongo.StorageBlockService;
import tech.ebp.oqm.lib.core.Utils;
import tech.ebp.oqm.lib.core.object.MainObject;
import tech.ebp.oqm.lib.core.object.user.User;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Traced
@Slf4j
@ApplicationScoped
public class DataImportService {
	
	private static final String IMPORT_TEMP_DIR_PREFIX = "oqm-data-import";
	
	
	private static Path zipSlipProtect(ArchiveEntry entry, Path targetDir)
		throws IOException {
		
		Path targetDirResolved = targetDir.resolve(entry.getName());
		
		// make sure normalized file still has targetDir as its prefix,
		// else throws exception
		Path normalizePath = targetDirResolved.normalize();
		
		if (!normalizePath.startsWith(targetDir)) {
			throw new IOException("Bad entry: " + entry.getName());
		}
		
		return normalizePath;
	}
	
	
	private static List<File> getObjectFiles(Path directory) throws IOException {
		try (
			Stream<Path> paths = Files.walk(
				directory,
				1
			)
		) {
			return paths
					   .filter(Files::isRegularFile)
					   .filter((Path path)->{
						   return path.toString().endsWith(".json");
					   })
					   .map(Path::toFile)
					   .collect(Collectors.toList());
		}
	}
	
	private static List<File> getObjectHistoryFiles(Path directory) throws IOException {
		Path historyDir = directory.resolve(DataImportExportUtils.OBJECT_HISTORY_DIR_NAME);
		if (historyDir.toFile().exists()) {
			return getObjectFiles(historyDir);
		}
		return List.of();
	}
	
	@Inject
	ImageService imageService;
	
	@Inject
	StorageBlockService storageBlockService;
	
	@Inject
	InventoryItemService inventoryItemService;
	
	
	private <T extends MainObject, S extends SearchObject<T>> void readInObject(
		File curFile,
		MongoHistoriedService<T, S> objectService,
		User importingUser
	) throws IOException {
		T curObj = Utils.OBJECT_MAPPER.readValue(curFile, objectService.getClazz());
		
		ObjectId oldId = curObj.getId();
		ObjectId newId = objectService.add(curObj, importingUser);
		log.info("Read in object. new id == old? {}", newId.equals(oldId));
	}
	
	private <T extends MainObject, S extends SearchObject<T>> long readInObjects(
		Path directory,
		MongoHistoriedService<T, S> objectService,
		User importingUser
	) throws IOException {
		Path objectDirPath = directory.resolve(objectService.getCollectionName());
		List<File> filesForObject = getObjectFiles(objectDirPath);
		
		log.info("Found {} files for {} in {}", filesForObject.size(), objectService.getCollectionName(), objectDirPath);
		StopWatch sw = StopWatch.createStarted();
		for (File curObjFile : filesForObject) {
			readInObject(curObjFile, objectService, importingUser);
		}
		sw.stop();
		log.info(
			"Read in {} {} objects in {}",
			filesForObject.size(),
			objectService.getCollectionName(),
			sw
		);
		
		
		return filesForObject.size();
	}
	
	public DataImportResult importBundle(
		InputStream bundleInputStream,
		String fileName,
		User importingUser
	) throws IOException {
		if (!fileName.endsWith(".tar.gz")) {
			throw new IllegalArgumentException("Invalid file type given.");
		}
		
		java.nio.file.Path tempDirPath = Files.createTempDirectory(IMPORT_TEMP_DIR_PREFIX);
		File tempDir = tempDirPath.toFile();
		tempDir.deleteOnExit();
		
		StopWatch sw = StopWatch.createStarted();
		log.info("Decompressing given bundle.");
		try (
			BufferedInputStream bi = new BufferedInputStream(bundleInputStream);
			GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
			TarArchiveInputStream ti = new TarArchiveInputStream(gzi)
		) {
			
			ArchiveEntry entry;
			while ((entry = ti.getNextEntry()) != null) {
				
				// create a new path, zip slip validate
				Path newPath = zipSlipProtect(entry, tempDirPath);
				
				if (entry.isDirectory()) {
					Files.createDirectories(newPath);
				} else {
					
					// check parent folder again
					Path parent = newPath.getParent();
					if (parent != null) {
						if (Files.notExists(parent)) {
							Files.createDirectories(parent);
						}
					}
					
					// copy TarArchiveInputStream to Path newPath
					Files.copy(ti, newPath, StandardCopyOption.REPLACE_EXISTING);
				}
			}
		}
		sw.stop();
		log.info("Finished decompressing bundle, took {}", sw);
		
		// TODO:: validate data to read in. Ensure no errors will happen when adding to database. No optimal way to do this?
		
		
		log.info("Reading in objects.");
		sw = StopWatch.createStarted();
		DataImportResult.DataImportResultBuilder<?, ?> resultBuilder = DataImportResult.builder();
		
		resultBuilder.numImages(this.readInObjects(tempDirPath, this.imageService, importingUser));
		resultBuilder.numStorageBlocks(this.readInObjects(tempDirPath, this.storageBlockService, importingUser));
		resultBuilder.numInventoryItems(this.readInObjects(tempDirPath, this.inventoryItemService, importingUser));
		//TODO:: history
		
		sw.stop();
		log.info("Finished reading in objects. Took {}", sw);
		
		return resultBuilder.build();
	}
	
	public DataImportResult importBundle(
		ImportBundleFileBody body,
		User importingUser
	) throws IOException {
		return this.importBundle(body.file, body.fileName, importingUser);
	}
	
}