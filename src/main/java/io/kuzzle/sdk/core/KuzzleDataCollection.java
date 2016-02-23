package io.kuzzle.sdk.core;

import android.support.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.kuzzle.sdk.listeners.KuzzleResponseListener;
import io.kuzzle.sdk.listeners.OnQueryDoneListener;
import io.kuzzle.sdk.responses.KuzzleDocumentList;
import io.kuzzle.sdk.responses.KuzzleNotificationResponse;

/**
 * The type Kuzzle data collection.
 */
public class KuzzleDataCollection {

  private final Kuzzle kuzzle;
  private final String collection;
  private final String index;
  protected JSONObject headers;

  /**
   * A data collection is a set of data managed by Kuzzle. It acts like a data table for persistent documents,
   * or like a room for pub/sub messages.
   *
   * @param kuzzle     the kuzzle
   * @param index      the index
   * @param collection the collection
   */
  public KuzzleDataCollection(@NonNull final Kuzzle kuzzle, @NonNull final String index, @NonNull final String collection) {
    if (kuzzle == null) {
      throw new IllegalArgumentException("KuzzleDataCollection: need a Kuzzle instance to initialize");
    }

    if (index == null || collection == null) {
      throw new IllegalArgumentException("KuzzleDataCollection: index and collection required");
    }
    this.kuzzle = kuzzle;
    this.collection = collection;
    this.index = index;

    try {
      this.headers = new JSONObject(kuzzle.getHeaders().toString());
    }
    catch (JSONException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Executes an advanced search on the data collection.
   * /!\ There is a small delay between documents creation and their existence in our advanced search layer,
   * usually a couple of seconds.
   * That means that a document that was just been created won’t be returned by this function.
   *
   * @param filter   the filter
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection advancedSearch(final JSONObject filter, final KuzzleResponseListener<KuzzleDocumentList> listener) {
    return this.advancedSearch(filter, null, listener);
  }

  /**
   * Executes an advanced search on the data collection.
   * /!\ There is a small delay between documents creation and their existence in our advanced search layer,
   * usually a couple of seconds.
   * That means that a document that was just been created won’t be returned by this function.
   *
   * @param filters  the filters
   * @param options  the options
   * @param listener the listener
   * @return KuzzleDataCollection object
   */
  public KuzzleDataCollection advancedSearch(final JSONObject filters, final KuzzleOptions options, @NonNull final KuzzleResponseListener<KuzzleDocumentList> listener) {
    if (listener == null) {
      throw new IllegalArgumentException("listener cannot be null");
    }
    this.kuzzle.isValid();
    JSONObject data = new JSONObject();
    try {
      if (filters != null) {
        data.put("body", filters);
      }

      this.kuzzle.addHeaders(data, this.getHeaders());

      this.kuzzle.query(makeQueryArgs("read", "search"), data, options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject object) {
          try {
            JSONArray hits = object.getJSONObject("result").getJSONArray("hits");
            List<KuzzleDocument> docs = new ArrayList<KuzzleDocument>();
            for (int i = 0; i < hits.length(); i++) {
              JSONObject hit = hits.getJSONObject(i);
              KuzzleDocument doc = new KuzzleDocument(KuzzleDataCollection.this, hit.getString("_id"), hit.getJSONObject("_source"));
              docs.add(doc);
            }
            KuzzleDocumentList response = new KuzzleDocumentList(docs, object.getJSONObject("result").getInt("total"));
            listener.onSuccess(response);
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void onError(JSONObject error) {
          listener.onError(error);
        }
      });
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Make query args kuzzle . query args.
   *
   * @param controller the controller
   * @param action     the action
   * @return the kuzzle . query args
   */
  public io.kuzzle.sdk.core.Kuzzle.QueryArgs makeQueryArgs(final String controller, final String action) {
    io.kuzzle.sdk.core.Kuzzle.QueryArgs args = new io.kuzzle.sdk.core.Kuzzle.QueryArgs();
    args.action = action;
    args.controller = controller;
    args.index = this.index;
    args.collection = this.collection;
    return args;
  }

  /**
   * Count kuzzle data collection.
   *
   * @param filters  the filters
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection count(final JSONObject filters, @NonNull final KuzzleResponseListener<Integer> listener) {
    return this.count(filters, null, listener);
  }

  /**
   * Count kuzzle data collection.
   *
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection count(@NonNull final KuzzleResponseListener<Integer> listener) {
    return this.count(null, null, listener);
  }

  /**
   * Returns the number of documents matching the provided set of filters.
   * There is a small delay between documents creation and their existence in our advanced search layer,
   * usually a couple of seconds.
   * That means that a document that was just been created won’t be returned by this function
   *
   * @param filters  the filters
   * @param options  the options
   * @param listener the cb
   * @return KuzzleDataCollection kuzzle data collection
   */
  public KuzzleDataCollection count(final JSONObject filters, final KuzzleOptions options, @NonNull final KuzzleResponseListener<Integer> listener) {
    if (listener == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.count: listener required");
    }
    JSONObject data = new JSONObject();
    try {
      this.kuzzle.addHeaders(data, this.getHeaders());
      data.put("body", filters);
      this.kuzzle.query(makeQueryArgs("read", "count"), data, options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject response) {
          try {
            listener.onSuccess(response.getJSONObject("result").getInt("count"));
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void onError(JSONObject error) {
          listener.onError(error);
        }
      });
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Create a new empty data collection, with no associated mapping.
   * Kuzzle automatically creates data collections when storing documents, but there are cases where we want to create and prepare data collections before storing documents in it.
   *
   * @param options the options
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection create(final KuzzleOptions options) {
    return this.create(options, null);
  }

  /**
   * Create a new empty data collection, with no associated mapping.
   * Kuzzle automatically creates data collections when storing documents, but there are cases where we want to create and prepare data collections before storing documents in it.
   *
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection create(final KuzzleResponseListener<JSONObject> listener) {
    return this.create(null, listener);
  }

  /**
   * Create a new empty data collection, with no associated mapping.
   * Kuzzle automatically creates data collections when storing documents, but there are cases where we want to create and prepare data collections before storing documents in it.
   *
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection create() {
    return this.create(null, null);
  }

  /**
   * Create a new empty data collection, with no associated mapping.
   * Kuzzle automatically creates data collections when storing documents, but there are cases where we want to create and prepare data collections before storing documents in it.
   *
   * @param options  the options
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection create(final KuzzleOptions options, final KuzzleResponseListener<JSONObject> listener) {
    JSONObject data = new JSONObject();
    try {
      this.kuzzle.addHeaders(data, this.getHeaders());
      this.kuzzle.query(makeQueryArgs("write", "createCollection"), data, options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject response) {
          if (listener != null) {
            try {
              listener.onSuccess(response.getJSONObject("result"));
            } catch (JSONException e) {
              throw new RuntimeException(e);
            }
          }
        }

        @Override
        public void onError(JSONObject error) {
          if (listener != null) {
            listener.onError(error);
          }
        }
      });
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   *
   * @param id - document ID
   * @param content - document content
   * @return this object
   * @throws JSONException
   */
  public KuzzleDataCollection createDocument(final String id, @NonNull final JSONObject content) throws JSONException {
    return this.createDocument(id, content, null, null);
  }

  /**
   *
   * @param id - document ID
   * @param content - document content
   * @param opts - optional arguments
   * @return this object
   * @throws JSONException
   */
  public KuzzleDataCollection createDocument(final String id, @NonNull final JSONObject content, KuzzleOptions opts) throws JSONException {
    return this.createDocument(id, content, opts, null);
  }

  /**
   *
   * @param id - document ID
   * @param content - document content
   * @param listener - result listener
   * @return this object
   * @throws JSONException
   */
  public KuzzleDataCollection createDocument(final String id, @NonNull final JSONObject content, final KuzzleResponseListener<KuzzleDocument> listener) throws JSONException {
    return this.createDocument(id, content, null, listener);
  }

  /**
   *
   * @param content - document content
   * @return this object
   * @throws JSONException
   */
  public KuzzleDataCollection createDocument(@NonNull final JSONObject content) throws JSONException {
    return this.createDocument(null, content, null, null);
  }

  /**
   *
   * @param content - document content
   * @param opts - optional arguments
   * @return this object
   * @throws JSONException
   */
  public KuzzleDataCollection createDocument(@NonNull final JSONObject content, KuzzleOptions opts) throws JSONException {
    return this.createDocument(null, content, opts, null);
  }

  /**
   *
   * @param content - document content
   * @param listener - result listener
   * @return this object
   * @throws JSONException
   */
  public KuzzleDataCollection createDocument(@NonNull final JSONObject content, final KuzzleResponseListener<KuzzleDocument> listener) throws JSONException {
    return this.createDocument(null, content, null, listener);
  }

  /**
   *
   * @param content - document content
   * @param opts - optional arguments
   * @param listener - result listener
   * @return this object
   * @throws JSONException
   */
  public KuzzleDataCollection createDocument(@NonNull final JSONObject content, KuzzleOptions opts, final KuzzleResponseListener<KuzzleDocument> listener) throws JSONException {
    return this.createDocument(null, content, opts, listener);
  }

  /**
   *
   * @param id - document ID
   * @param content - document content
   * @param opts - optional arguments
   * @param listener - result listener
   * @return this object
   * @throws JSONException
   */
  public KuzzleDataCollection createDocument(final String id, @NonNull final JSONObject content, KuzzleOptions opts, final KuzzleResponseListener<KuzzleDocument> listener) throws JSONException {
    if (content == null) {
      throw new IllegalArgumentException("Cannot create an empty document");
    }

    KuzzleDocument doc = new KuzzleDocument(this, id, content);
    return this.createDocument(doc, opts, listener);
  }

  /**
   * Create a new document in Kuzzle
   *
   * @param document the document
   * @return kuzzle data collection
   */
  public KuzzleDataCollection createDocument(final KuzzleDocument document) {
    return this.createDocument(document, null, null);
  }

  /**
   * Create a new document in kuzzle
   *
   * @param document the document
   * @param options  the options
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection createDocument(final KuzzleDocument document, final KuzzleOptions options) {
    return this.createDocument(document, options, null);
  }

  /**
   * Create document kuzzle data collection.
   *
   * @param document the document
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection createDocument(final KuzzleDocument document, final KuzzleResponseListener<KuzzleDocument> listener) {
    return this.createDocument(document, null, listener);
  }

  /**
   * Create a new document in kuzzle
   *
   * @param document the document
   * @param options  the options
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection createDocument(final KuzzleDocument document, final KuzzleOptions options, final KuzzleResponseListener<KuzzleDocument> listener) {
    String create = (options != null && options.isUpdateIfExists()) ? "createOrReplace" : "create";
    JSONObject data = document.serialize();

    this.kuzzle.addHeaders(data, this.getHeaders());

    try {
      this.kuzzle.query(makeQueryArgs("write", create), data, options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject response) {
          if (listener != null) {
            try {
              JSONObject result = response.getJSONObject("result");
              KuzzleDocument document = new KuzzleDocument(KuzzleDataCollection.this, result.getString("_id"), result.getJSONObject("_source"));
              document.setVersion(result.getLong("_version"));
              listener.onSuccess(document);
            } catch (JSONException e) {
              throw new RuntimeException(e);
            }
          }
        }

        @Override
        public void onError(JSONObject error) {
          if (listener != null) {
            listener.onError(error);
          }
        }
      });
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * @return the kuzzle data mapping
   */
  public KuzzleDataMapping dataMappingFactory() {
    return new KuzzleDataMapping(this);
  }

  /**
   * Data mapping factory kuzzle data mapping.
   *
   * @param mapping the mapping
   * @return the kuzzle data mapping
   */
  public KuzzleDataMapping dataMappingFactory(JSONObject mapping) {
    return new KuzzleDataMapping(this, mapping);
  }

  /**
   * Delete kuzzle data collection.
   *
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection delete() {
    return this.delete(null, null);
  }

  /**
   * Delete kuzzle data collection.
   *
   * @param options the options
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection delete(final KuzzleOptions options) {
    return this.delete(options, null);
  }

  /**
   * Delete kuzzle data collection.
   *
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection delete(final KuzzleResponseListener<JSONObject> listener) {
    return this.delete(null, listener);
  }

  /**
   * Delete kuzzle data collection.
   *
   * @param options  the options
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection delete(final KuzzleOptions options, final KuzzleResponseListener<JSONObject> listener) {
    JSONObject data = new JSONObject();
    try {
      this.kuzzle.addHeaders(data, this.getHeaders());
      this.kuzzle.query(makeQueryArgs("admin", "deleteCollection"), data, options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject response) {
          if (listener != null) {
            listener.onSuccess(response);
          }
        }

        @Override
        public void onError(JSONObject error) {
          if (listener != null) {
            listener.onError(error);
          }
        }
      });
    }  catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Delete a persistent document.
   * There is a small delay between documents creation and their existence in our advanced search layer,
   * usually a couple of seconds.
   * That means that a document that was just been created won’t be returned by this function
   *
   * @param documentId the document id
   * @return KuzzleDataCollection kuzzle data collection
   */
  public KuzzleDataCollection deleteDocument(@NonNull final String documentId) {
    return this.deleteDocument(documentId, null, null);
  }

  /**
   * Delete document kuzzle data collection.
   *
   * @param documentId the document id
   * @param options    the options
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection deleteDocument(@NonNull final String documentId, KuzzleOptions options) {
    return this.deleteDocument(documentId, options, null);
  }

  /**
   * Delete a persistent document.
   * There is a small delay between documents creation and their existence in our advanced search layer,
   * usually a couple of seconds.
   * That means that a document that was just been created won’t be returned by this function
   *
   * @param documentId the document id
   * @param listener   the listener
   * @return KuzzleDataCollection kuzzle data collection
   */
  public KuzzleDataCollection deleteDocument(@NonNull final String documentId, final KuzzleResponseListener<String> listener) {
    return this.deleteDocument(documentId, null, listener);
  }

  /**
   * Delete document kuzzle data collection.
   *
   * @param documentId the document id
   * @param options    the options
   * @param listener   the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection deleteDocument(@NonNull final String documentId, final KuzzleOptions options, final KuzzleResponseListener<String> listener) {
    if (documentId == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.deleteDocument: documentId required");
    }
    return this.deleteDocument(documentId, null, options, listener, null);
  }

  /**
   * Delete a persistent document.
   * There is a small delay between documents creation and their existence in our advanced search layer,
   * usually a couple of seconds.
   * That means that a document that was just been created won’t be returned by this function
   *
   * @param filters the filters
   * @return KuzzleDataCollection kuzzle data collection
   */
  public KuzzleDataCollection deleteDocument(@NonNull final JSONObject filters) {
    return this.deleteDocument(filters, null, null);
  }

  /**
   * Delete document kuzzle data collection.
   *
   * @param filters the filters
   * @param options the options
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection deleteDocument(@NonNull final JSONObject filters, final KuzzleOptions options) {
    return this.deleteDocument(filters, options, null);
  }

  /**
   * Delete a persistent document.
   * There is a small delay between documents creation and their existence in our advanced search layer,
   * usually a couple of seconds.
   * That means that a document that was just been created won’t be returned by this function
   *
   * @param filters  the filters
   * @param listener the listener
   * @return KuzzleDataCollection kuzzle data collection
   */
  public KuzzleDataCollection deleteDocument(@NonNull final JSONObject filters, final KuzzleResponseListener<String[]> listener) {
    return this.deleteDocument(filters, null, listener);
  }

  /**
   * Delete document kuzzle data collection.
   *
   * @param filters  the filters
   * @param options  the options
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection deleteDocument(@NonNull final JSONObject filters, final KuzzleOptions options, final KuzzleResponseListener<String[]> listener) {
    if (filters == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.deleteDocument: filters required");
    }
    return this.deleteDocument(null, filters, options, null, listener);
  }

  /**
   * Delete a persistent document.
   * There is a small delay between documents creation and their existence in our advanced search layer,
   * usually a couple of seconds.
   * That means that a document that was just been created won’t be returned by this function
   *
   * @param documentId the document id
   * @param filter     the filter
   * @return KuzzleDataCollection kuzzle data collection
   */
  protected KuzzleDataCollection deleteDocument(final String documentId, final JSONObject filter, final KuzzleOptions options, final KuzzleResponseListener<String> listener, final KuzzleResponseListener<String[]> listener2) {
    JSONObject data = new JSONObject();
    String action;
    try {
      this.kuzzle.addHeaders(data, this.getHeaders());
      if (documentId != null) {
        data.put("_id", documentId);
        action = "delete";
      } else {
        data.put("body", filter);
        action = "deleteByQuery";
      }
      this.kuzzle.query(makeQueryArgs("write", action), data, options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject response) {
          try {
            if (listener != null) {
              listener.onSuccess(response.getJSONObject("result").getString("_id"));
            } else if (listener2 != null) {
              JSONArray array = response.getJSONObject("result").getJSONArray("hits");
              int length = array.length();
              String[] ids = new String[length];
              for (int i = 0; i < length; i++) {
                ids[i] = array.getString(i);
              }
              listener2.onSuccess(ids);
            }
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void onError(JSONObject error) {
          if (listener != null) {
            listener.onError(error);
          } else if (listener2 != null) {
            listener2.onError(error);
          }
        }
      });
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public KuzzleDocument documentFactory() throws JSONException {
    return new KuzzleDocument(this);
  }

  public KuzzleDocument documentFactory(final String id) throws JSONException {
    return new KuzzleDocument(this, id);
  }

  public KuzzleDocument documentFactory(final JSONObject content) throws JSONException {
    return new KuzzleDocument(this, content);
  }

  public KuzzleDocument documentFactory(final String id, final JSONObject content) throws JSONException {
    return new KuzzleDocument(this, id, content);
  }

  /**
   * Fetch document kuzzle data collection.
   *
   * @param documentId the document id
   * @param listener   the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection fetchDocument(@NonNull final String documentId, @NonNull final KuzzleResponseListener<KuzzleDocument> listener) {
    return this.fetchDocument(documentId, null, listener);
  }

  /**
   * Retrieve a single stored document using its unique document ID.
   *
   * @param documentId the document id
   * @param options    the options
   * @param listener   the listener
   * @return KuzzleDataCollection kuzzle data collection
   */
  public KuzzleDataCollection fetchDocument(@NonNull final String documentId, final KuzzleOptions options, final KuzzleResponseListener<KuzzleDocument> listener) {
    if (documentId == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.fetchDocument: documentId required");
    }
    if (listener == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.fetchDocument: listener required");
    }

    try {
      JSONObject data = new JSONObject().put("_id", documentId);
      this.kuzzle.addHeaders(data, this.getHeaders());

      this.kuzzle.query(makeQueryArgs("read", "get"), data, options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject response) {
          try {
            JSONObject result = response.getJSONObject("result");
            KuzzleDocument document = new KuzzleDocument(KuzzleDataCollection.this, result.getString("_id"), result.getJSONObject("_source"));

            document.setVersion(result.getLong("_version"));
            listener.onSuccess(document);
          } catch (JSONException e) {
            throw new RuntimeException(e);
          }
        }

        @Override
        public void onError(JSONObject error) {
          listener.onError(error);
        }
      });
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Retrieves all documents stored in this data collection.
   *
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection fetchAllDocuments(@NonNull final KuzzleResponseListener<KuzzleDocumentList> listener) {
    return this.fetchAllDocuments(null, listener);
  }

  /**
   * Retrieves all documents stored in this data collection.
   *
   * @param options  the options
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection fetchAllDocuments(final KuzzleOptions options, @NonNull final KuzzleResponseListener<KuzzleDocumentList> listener) {
    if (listener == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.fetchAllDocuments: listener required");
    }
    return this.advancedSearch(null, options, listener);
  }

  /**
   * Instantiates a KuzzleDataMapping object containing the current mapping of this collection.
   *
   * @param listener the listener
   * @return the mapping
   */
  public KuzzleDataCollection getMapping(@NonNull final KuzzleResponseListener<KuzzleDataMapping> listener) {
    return this.getMapping(null, listener);
  }

  /**
   * Instantiates a KuzzleDataMapping object containing the current mapping of this collection.
   *
   * @param options  the options
   * @param listener the listener
   * @return the mapping
   */
  public KuzzleDataCollection getMapping(final KuzzleOptions options, @NonNull final KuzzleResponseListener<KuzzleDataMapping> listener) {
    if (listener == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.getMapping: listener required");
    }
    new KuzzleDataMapping(this).refresh(options, listener);
    return this;
  }

  /**
   * Publish a realtime message
   *
   * @param document the document
   * @return kuzzle data collection
   */
  public KuzzleDataCollection publishMessage(final KuzzleDocument document) {
    return this.publishMessage(document, null);
  }

  /**
   * Publish a realtime message
   *
   * @param document the document
   * @param options  the options
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection publishMessage(@NonNull final KuzzleDocument document, final KuzzleOptions options) {
    if (document == null) {
      throw new IllegalArgumentException("Cannot publish a null document");
    }

    return this.publishMessage(document.getContent(), options);
  }

  /**
   * Publish a realtime message
   *
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection publishMessage(@NonNull final JSONObject content) {
    return this.publishMessage(content, null);
  }

  /**
   * Publish a realtime message
   *
   * @param options  the options
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection publishMessage(@NonNull final JSONObject content, final KuzzleOptions options) {
    if (content == null) {
      throw new IllegalArgumentException("Cannot publish null content");
    }

    try {
      JSONObject data = new JSONObject().put("body", content);
      this.kuzzle.addHeaders(data, this.getHeaders());
      this.kuzzle.query(makeQueryArgs("write", "publish"), data, options, null);
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Replace an existing document with a new one.
   *
   * @param documentId the document id
   * @param content    the content
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection replaceDocument(@NonNull final String documentId, final JSONObject content) {
    return this.replaceDocument(documentId, content, null, null);
  }

  /**
   * Replace document kuzzle data collection.
   *
   * @param documentId the document id
   * @param content    the content
   * @param listener   the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection replaceDocument(@NonNull final String documentId, final JSONObject content, final KuzzleResponseListener listener) {
    return this.replaceDocument(documentId, content, null, listener);
  }

  /**
   * Replace document kuzzle data collection.
   *
   * @param documentId the document id
   * @param options    the options
   * @param content    the content
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection replaceDocument(@NonNull final String documentId, final JSONObject content, final KuzzleOptions options) {
    return this.replaceDocument(documentId, content, options, null);
  }

  /**
   * Replace an existing document with a new one.
   *
   * @param documentId the document id
   * @param content    the content
   * @param options    the options
   * @param listener   the listener
   * @return KuzzleDataCollection kuzzle data collection
   */
  public KuzzleDataCollection replaceDocument(@NonNull final String documentId, final JSONObject content, final KuzzleOptions options, final KuzzleResponseListener<KuzzleDocument> listener) {
    if (documentId == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.replaceDocument: documentId required");
    }

    try {
      JSONObject data = new JSONObject().put("_id", documentId).put("body", content);
      this.kuzzle.addHeaders(data, this.getHeaders());
      this.kuzzle.query(makeQueryArgs("write", "createOrReplace"), data, options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject response) {
          if (listener != null) {
            try {
              JSONObject result = response.getJSONObject("result");
              KuzzleDocument document = new KuzzleDocument(KuzzleDataCollection.this, result.getString("_id"), result.getJSONObject("_source"));
              document.setVersion(result.getLong("_version"));
              listener.onSuccess(document);
            } catch (JSONException e) {
              throw new RuntimeException(e);
            }
          }
        }

        @Override
        public void onError(JSONObject error) {
          if (listener != null) {
            listener.onError(error);
          }
        }
      });
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  public KuzzleRoom roomFactory() {
    return this.roomFactory(null);
  }

  public KuzzleRoom roomFactory(KuzzleRoomOptions options) {
    return new KuzzleRoom(this, options);
  }

  /**
   * Sets headers.
   *
   * @param content the content
   * @return the headers
   */
  public KuzzleDataCollection setHeaders(final JSONObject content) {
    return this.setHeaders(content, false);
  }

  /**
   * Sets headers.
   *
   * @param content the content
   * @param replace the replace
   * @return the headers
   */
  public KuzzleDataCollection setHeaders(final JSONObject content, final boolean replace) {
    try {
      if (content == null) {
        if (replace) {
          this.headers = new JSONObject();
        }

        return this;
      }

      if (replace) {
        this.headers = new JSONObject(content.toString());
      } else {
        for (Iterator ite = content.keys(); ite.hasNext(); ) {
          String key = (String) ite.next();
          this.headers.put(key, content.get(key));
        }
      }
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Subscribes to this data collection with a set of filters.
   * To subscribe to the entire data collection, simply provide an empty filter.
   *
   * @param filters  the filters
   * @param listener the listener
   * @return the kuzzle room
   */
  public KuzzleRoom subscribe(final JSONObject filters, @NonNull final KuzzleResponseListener<KuzzleNotificationResponse> listener) {
    return this.subscribe(filters, null, listener);
  }

  /**
   * Subscribes to this data collection with a set of filters.
   * To subscribe to the entire data collection, simply provide an empty filter.
   *
   * @param options  the options
   * @param listener the listener
   * @return the kuzzle room
   */
  public KuzzleRoom subscribe(final KuzzleRoomOptions options, @NonNull final KuzzleResponseListener<KuzzleNotificationResponse> listener) {
    return this.subscribe(null, options, listener);
  }

  /**
   * Subscribes to this data collection with a set of filters.
   * To subscribe to the entire data collection, simply provide an empty filter.
   *
   * @param filters  the filters
   * @param options  the options
   * @param listener the listener
   * @return kuzzle room
   */
  public KuzzleRoom subscribe(final JSONObject filters, final KuzzleRoomOptions options, @NonNull final KuzzleResponseListener<KuzzleNotificationResponse> listener) {
    if (listener == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.subscribe: listener required");
    }
    this.kuzzle.isValid();
    KuzzleRoom room = new KuzzleRoom(this, options);
    return room.renew(filters, listener);
  }

  /**
   * Truncate the data collection, removing all stored documents but keeping all associated mappings.
   * This method is a lot faster than removing all documents using a query.
   *
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection truncate() {
    return this.truncate(null, null);
  }

  /**
   * Truncate the data collection, removing all stored documents but keeping all associated mappings.
   * This method is a lot faster than removing all documents using a query.
   *
   * @param options the options
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection truncate(final KuzzleOptions options) {
    return this.truncate(options, null);
  }

  /**
   * Truncate the data collection, removing all stored documents but keeping all associated mappings.
   * This method is a lot faster than removing all documents using a query.
   *
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection truncate(final KuzzleResponseListener<JSONObject> listener) {
    return this.truncate(null, listener);
  }

  /**
   * Truncate the data collection, removing all stored documents but keeping all associated mappings.
   * This method is a lot faster than removing all documents using a query.
   *
   * @param options  the options
   * @param listener the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection truncate(final KuzzleOptions options, final KuzzleResponseListener<JSONObject> listener) {
    JSONObject  data = new JSONObject();
    try {
      this.kuzzle.addHeaders(data, this.getHeaders());
      this.kuzzle.query(makeQueryArgs("admin", "truncateCollection"), data, options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject response) {
          if (listener != null) {
            try {
              listener.onSuccess(response.getJSONObject("result"));
            } catch (JSONException e) {
              throw new RuntimeException(e);
            }
          }
        }

        @Override
        public void onError(JSONObject error) {
          if (listener != null) {
            listener.onError(error);
          }
        }
      });
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Update parts of a document
   *
   * @param documentId the document id
   * @param content    the content
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection updateDocument(@NonNull final String documentId, @NonNull final JSONObject content) {
    return this.updateDocument(documentId, content, null, null);
  }

  /**
   * Update parts of a document
   *
   * @param documentId the document id
   * @param content    the content
   * @param options    the options
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection updateDocument(@NonNull final String documentId, @NonNull final JSONObject content, final KuzzleOptions options) {
    return this.updateDocument(documentId, content, options, null);
  }

  /**
   * Update parts of a document
   *
   * @param documentId the document id
   * @param content    the content
   * @param listener   the listener
   * @return the kuzzle data collection
   */
  public KuzzleDataCollection updateDocument(@NonNull final String documentId, @NonNull final JSONObject content, final KuzzleResponseListener<KuzzleDocument> listener) {
    return this.updateDocument(documentId, content, null, listener);
  }

  /**
   * Update parts of a document
   *
   * @param documentId the document id
   * @param content    the content
   * @param options    the options
   * @param listener   the listener
   * @return kuzzle data collection
   */
  public KuzzleDataCollection updateDocument(@NonNull final String documentId, @NonNull final JSONObject content, final KuzzleOptions options, final KuzzleResponseListener<KuzzleDocument> listener) {
    if (documentId == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.updateDocument: documentId required");
    }
    if (content == null) {
      throw new IllegalArgumentException("KuzzleDataCollection.updateDocument: content required");
    }

    try {
      JSONObject data = new JSONObject().put("_id", documentId).put("body", content);
      this.kuzzle.addHeaders(data, this.getHeaders());

      this.kuzzle.query(makeQueryArgs("write", "update"), data, options, new OnQueryDoneListener() {
        @Override
        public void onSuccess(JSONObject response) {
          if (listener != null) {
            try {
              JSONObject result = response.getJSONObject("result");
              KuzzleDocument document = new KuzzleDocument(KuzzleDataCollection.this, result.getString("_id"), result.getJSONObject("_source"));
              document.setVersion(result.getLong("_version"));
              listener.onSuccess(document);
            } catch (JSONException e) {
              throw new RuntimeException(e);
            }
          }
        }

        @Override
        public void onError(JSONObject error) {
          if (listener != null) {
            listener.onError(error);
          }
        }
      });
    } catch (JSONException e) {
      throw new RuntimeException(e);
    }
    return this;
  }

  /**
   * Gets kuzzle.
   *
   * @return the kuzzle
   */
  public Kuzzle getKuzzle() {
    return kuzzle;
  }

  /**
   * Gets collection.
   *
   * @return the collection
   */
  public String getCollection() {
    return collection;
  }

  /**
   * Getter for the "index" property
   *
   * @return
   */
  public String getIndex() {
    return this.index;
  }

  /**
   * Gets headers.
   *
   * @return the headers
   */
  public JSONObject getHeaders() {
    return this.headers;
  }
}
