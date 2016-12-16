package io.kuzzle.test.core.KuzzleDataCollection;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URISyntaxException;

import io.kuzzle.sdk.core.Kuzzle;
import io.kuzzle.sdk.core.KuzzleDataCollection;
import io.kuzzle.sdk.core.KuzzleDocument;
import io.kuzzle.sdk.core.KuzzleOptions;
import io.kuzzle.sdk.enums.Mode;
import io.kuzzle.sdk.listeners.KuzzleResponseListener;
import io.kuzzle.sdk.listeners.OnQueryDoneListener;
import io.kuzzle.sdk.state.KuzzleStates;
import io.kuzzle.test.testUtils.KuzzleExtend;
import io.socket.client.Socket;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class createDocumentTest {
  private Kuzzle kuzzle;
  private KuzzleDataCollection collection;
  private KuzzleResponseListener listener;

  @Before
  public void setUp() throws URISyntaxException {
    KuzzleOptions opts = new KuzzleOptions();
    opts.setConnect(Mode.MANUAL);
    KuzzleExtend extended = new KuzzleExtend("localhost", opts, null);
    extended.setSocket(mock(Socket.class));
    extended.setState(KuzzleStates.CONNECTED);

    kuzzle = spy(extended);
    when(kuzzle.getHeaders()).thenReturn(new JSONObject());

    collection = new KuzzleDataCollection(kuzzle, "test", "index");
    listener = mock(KuzzleResponseListener.class);
  }

  @Test
  public void checkSignaturesVariants() throws JSONException {
    KuzzleDocument doc = mock(KuzzleDocument.class);
    JSONObject content = new JSONObject();
    String id = "foo";
    KuzzleOptions opts = mock(KuzzleOptions.class);

    collection = spy(collection);

    collection.createDocument(doc);
    collection.createDocument(doc, opts);
    collection.createDocument(doc, listener);
    collection.createDocument(doc, opts, listener);

    collection.createDocument(id, content);
    collection.createDocument(id, content, opts);
    collection.createDocument(id, content, listener);
    collection.createDocument(id, content, opts, listener);

    collection.createDocument(content);
    collection.createDocument(content, opts);
    collection.createDocument(content, listener);
    collection.createDocument(content, opts, listener);

    verify(collection, times(12)).createDocument(any(KuzzleDocument.class), any(KuzzleOptions.class), any(KuzzleResponseListener.class));
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDocumentQueryException() throws JSONException {
    doThrow(JSONException.class).when(kuzzle).query(any(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    collection.createDocument(mock(KuzzleDocument.class), listener);
  }

  @Test(expected = RuntimeException.class)
  public void testCreateDocumentException() throws JSONException {
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(new JSONObject().put("result", new JSONObject()));
        return null;
      }
    }).when(kuzzle).query(any(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doThrow(JSONException.class).when(listener).onSuccess(any(JSONObject.class));
    collection.createDocument(mock(KuzzleDocument.class), listener);
  }

  @Test
  public void testCreateDocument() throws JSONException {
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        JSONObject result = new JSONObject()
          .put("result", new JSONObject()
            .put("_id", "foo")
            .put("_version", 1337)
            .put("_source", new JSONObject()));

        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(result);
        ((OnQueryDoneListener) invocation.getArguments()[3]).onError(mock(JSONObject.class));
        return null;
      }
    }).when(kuzzle).query(any(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    KuzzleDocument doc = new KuzzleDocument(collection);
    doc.setContent("foo", "bar");
    collection.createDocument(doc);
    collection.createDocument(doc, mock(KuzzleResponseListener.class));
    ArgumentCaptor argument = ArgumentCaptor.forClass(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class);
    verify(kuzzle, times(2)).query((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).controller, "document");
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).action, "create");
  }

  @Test
  public void testCreateDocumentWithOptions() throws JSONException {
    KuzzleDocument doc = new KuzzleDocument(collection);
    doc.setContent("foo", "bar");
    KuzzleOptions options = new KuzzleOptions();
    options.setUpdateIfExists(true);
    collection.createDocument(doc, options);
    ArgumentCaptor argument = ArgumentCaptor.forClass(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class);
    verify(kuzzle, times(1)).query((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).controller, "document");
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).action, "createOrReplace");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testCreateDocumentWithIllegalContent() throws JSONException {
    collection.createDocument("id", null, mock(KuzzleOptions.class), listener);
  }

}
