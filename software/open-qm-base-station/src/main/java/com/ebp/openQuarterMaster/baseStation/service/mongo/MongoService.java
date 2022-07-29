package com.ebp.openQuarterMaster.baseStation.service.mongo;

import com.ebp.openQuarterMaster.baseStation.mongoUtils.exception.DbNotFoundException;
import com.ebp.openQuarterMaster.baseStation.rest.search.SearchObject;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.PagingOptions;
import com.ebp.openQuarterMaster.baseStation.service.mongo.search.SearchResult;
import com.ebp.openQuarterMaster.lib.core.MainObject;
import com.ebp.openQuarterMaster.lib.core.user.User;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.InsertOneResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.opentracing.Traced;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;

/**
 * Abstract Service that implements all basic functionality when dealing with mongo collections.
 *
 * @param <T> The type of object stored.
 */
@AllArgsConstructor
@Slf4j
@Traced
public abstract class MongoService<T extends MainObject, S extends SearchObject<T>> {
	
	public static String getCollectionName(Class<?> clazz) {
		return clazz.getSimpleName();
	}
	
	public static final String NULL_USER_EXCEPT_MESSAGE = "User must exist to perform action.";
	private static final Validator VALIDATOR;//TODO:: move to constructor?
	
	static {
		try (ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory()) {
			VALIDATOR = validatorFactory.getValidator();
		}
	}
	
	/**
	 * TODO:: check if real user. Get userService in constructor?
	 *
	 * @param user
	 */
	private static void assertNotNullUser(User user) {
		if (user == null) {
			throw new IllegalArgumentException(NULL_USER_EXCEPT_MESSAGE);
		}
	}
	
	protected final ObjectMapper objectMapper;
	protected final MongoClient mongoClient;
	protected final String database;
	protected final String collectionName;
	@Getter
	protected final Class<T> clazz;
	
	private MongoCollection<T> collection = null;
	
	protected MongoService(
		ObjectMapper objectMapper,
		MongoClient mongoClient,
		String database,
		Class<T> clazz
	) {
		this(
			objectMapper,
			mongoClient,
			database,
			getCollectionName(clazz),
			clazz,
			null
		);
	}
	
	protected MongoCollection<T> getCollection() {
		if (this.collection == null) {
			this.collection = mongoClient.getDatabase(this.database).getCollection(this.collectionName, this.clazz);
		}
		return this.collection;
	}
	
	/**
	 * Method to check that an object is [still] valid before applying creation or update
	 * @param newOrChangedObject
	 */
	public void ensureObjectValid(boolean newObject, T newOrChangedObject){
	}
	
	/**
	 * Gets a list of entries based on the options given.
	 * <p>
	 * TODO:: look into better, faster paging methods: https://dzone.com/articles/fast-paging-with-mongodb
	 *
	 * @param filter The filter to use for the search. Nullable, no filter if null.
	 * @param sort The bson used to describe the sorting behavior. Nullable, no explicit sorting if null.
	 * @param pageOptions The paging options. Nullable, not used if null.
	 *
	 * @return a list of entries based on the options given.
	 */
	public List<T> list(Bson filter, Bson sort, PagingOptions pageOptions) {
		List<T> list = new ArrayList<>();
		
		FindIterable<T> results;
		
		if (filter == null) {
			results = getCollection().find();
		} else {
			results = getCollection().find(filter);
		}
		
		if (sort != null) {
			results = results.sort(sort);
		}
		if (pageOptions != null) {
			results = results.skip(pageOptions.getSkipVal()).limit(pageOptions.pageSize);
		}
		
		results.into(list);
		
		return list;
	}
	
	@Deprecated
	protected SearchResult<T> searchResult(List<Bson> filters, Bson sort, PagingOptions pagingOptions) {
		Bson filter = (filters.isEmpty() ? null : and(filters));
		
		List<T> list = this.list(
			filter,
			sort,
			pagingOptions
		);
		
		return new SearchResult<>(
			list,
			this.count(filter),
			!filters.isEmpty()
		);
	}
	
	/**
	 * Gets a list of all elements in the collection.
	 * <p>
	 * Wrapper for {@link #list(Bson, Bson, PagingOptions)}, with all null arguments.
	 *
	 * @return a list of all elements in the collection.
	 */
	public List<T> list() {
		return this.list(null, null, null);
	}
	
	public boolean collectionEmpty() {
		return this.getCollection().countDocuments() == 0;
	}
	
	/**
	 * Gets the count of records in the collection using a filter.
	 *
	 * @param filter The filter to use. Nullable, gets the whole collection size if null.
	 *
	 * @return the count of records in the collection
	 */
	public long count(Bson filter) {
		if (filter == null) {
			return getCollection().countDocuments();
		}
		return this.getCollection().countDocuments(filter);
	}
	
	/**
	 * Gets the count of all records in the collection.
	 * <p>
	 * Wrapper for {@link #count(Bson)}.
	 *
	 * @return the count of all records in the collection.
	 */
	public long count() {
		return this.count(null);
	}
	
	/**
	 * Gets an object with a particular id.
	 *
	 * @param objectId The id of the object to get
	 *
	 * @return The object found. Null if not found.
	 */
	public T get(ObjectId objectId) throws DbNotFoundException {
		T found = getCollection()
					  .find(eq("_id", objectId))
					  .limit(1)
					  .first();
		
		if(found == null){
			throw new DbNotFoundException(this.clazz, objectId);
		}
		
		return found;
	}
	
	/**
	 * Gets an object with a particular id.
	 * <p>
	 * Wrapper for {@link #get(ObjectId)}, to be able to use String representation of ObjectId.
	 *
	 * @param objectId The id of the object to get
	 *
	 * @return The object found. Null if not found.
	 */
	public T get(String objectId) {
		return this.get(new ObjectId(objectId));
	}
	
	public SearchResult<T> search(S searchObject){
		log.info("Searching for {} with: {}", this.clazz.getSimpleName(), searchObject);
		
		List<Bson> filters = searchObject.getSearchFilters();
		Bson filter = (filters.isEmpty() ? null : and(filters));
		
		List<T> list = this.list(
			filter,
			searchObject.getSortBson(),
			searchObject.getPagingOptions(false)
		);
		
		return new SearchResult<>(
			list,
			this.count(filter),
			!filters.isEmpty()
		);
	}
	
	public T update(ObjectId id, ObjectNode updateJson) {
		if (updateJson.has("id") && !id.toHexString().equals(updateJson.get("id").asText())) {
			throw new IllegalArgumentException("Not allowed to update id of an object.");
		}
		
		T object = this.get(id);
		
		ObjectReader reader = objectMapper
								  .readerForUpdating(object)
								  .with(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
			//TODO:: enable different types, for InventoryItem (fails to deal with the abstract type)
			reader.readValue(updateJson, object.getClass());
		} catch(IOException e) {
			throw new IllegalArgumentException("Unable to update with data given: " + e.getMessage(), e);
		}
		
		Set<ConstraintViolation<T>> validationErrs = VALIDATOR.validate(object);
		if (!validationErrs.isEmpty()) {
			throw new IllegalArgumentException("Unable to update with data given. Resulting object is invalid: " +
											   validationErrs.stream()
															 .map(ConstraintViolation::getMessage)
															 .collect(Collectors.joining(", ")));
		}
		this.ensureObjectValid(false, object);
		
		this.getCollection().findOneAndReplace(eq("_id", id), object);
		return object;
	}
	
	public T update(String id, ObjectNode updateJson) {
		return this.update(new ObjectId(id), updateJson);
	}
	
	/**
	 * Adds an object to the collection. Adds a created history event and the object's new object id to that object in-place.
	 *
	 * @param object The object to add
	 *
	 * @return The id of the newly added object.
	 */
	public ObjectId add(T object) {
		if (object == null) {
			throw new NullPointerException("Object cannot be null.");
		}
		
		this.ensureObjectValid(true, object);
		
		InsertOneResult result = getCollection().insertOne(object);
		
		object.setId(result.getInsertedId().asObjectId().getValue());
		
		return object.getId();
	}
	
	/**
	 * Removes the object with the id given.
	 *
	 * @param objectId The id of the object to remove
	 *
	 * @return The object that was removed
	 */
	public T remove(ObjectId objectId) {
		T toRemove = this.get(objectId);
		
		DeleteResult result = this.getCollection().deleteOne(eq("_id", objectId));
		
		{//TODO: ignore this in coverage
			if (!result.wasAcknowledged()) {
				log.warn("Delete of obj {} was not acknowledged.", objectId);
			}
			if (result.getDeletedCount() != 1) {
				log.warn("Delete of obj {} returned delete count != 1: {}", objectId, result.getDeletedCount());
			}
		}
		
		return toRemove;
	}
	
	/**
	 * Removes the object with the id given.
	 * <p>
	 * Wrapper for {@link #remove(ObjectId)}, to be able to use String representation of ObjectId.
	 *
	 * @param objectId The id of the object to remove
	 *
	 * @return The object that was removed
	 */
	public T remove(String objectId) {
		return this.remove(new ObjectId(objectId));
	}
	
	/**
	 * Removes all items from the collection.
	 *
	 * @return The number of items that were removed.
	 */
	public long removeAll() {
		return this.getCollection().deleteMany(new BsonDocument()).getDeletedCount();
	}
}
