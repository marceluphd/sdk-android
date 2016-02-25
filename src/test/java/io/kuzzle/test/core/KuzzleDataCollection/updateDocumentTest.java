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

public class updateDocumentTest {
  private Kuzzle kuzzle;
  private KuzzleDataCollection collection;
  private KuzzleResponseListener listener;

  @Before
  public void setUp() throws URISyntaxException {
    KuzzleOptions opts = new KuzzleOptions();
    opts.setConnect(Mode.MANUAL);
    KuzzleExtend extended = new KuzzleExtend("http://localhost:7512", opts, null);
    extended.setSocket(mock(Socket.class));
    extended.setState(KuzzleStates.CONNECTED);

    kuzzle = spy(extended);
    when(kuzzle.getHeaders()).thenReturn(new JSONObject());

    collection = new KuzzleDataCollection(kuzzle, "index", "test");
    listener = mock(KuzzleResponseListener.class);
  }

  @Test
  public void checkSignaturesVariants() {
    JSONObject content = mock(JSONObject.class);
    String id = "foo";
    collection = spy(collection);

    collection.updateDocument(id, content);
    collection.updateDocument(id, content, mock(KuzzleOptions.class));
    collection.updateDocument(id, content, listener);

    verify(collection, times(3)).updateDocument(any(String.class), any(JSONObject.class), any(KuzzleOptions.class), any(KuzzleResponseListener.class));
  }


  @Test(expected = IllegalArgumentException.class)
  public void testUpdateDocumentIllegalDocumentId() {
    collection.updateDocument(null, mock(JSONObject.class));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testUpdateDocumentIllegalContent() {
    collection.updateDocument("id", null);
  }

  @Test(expected = RuntimeException.class)
  public void testupdateDocumentQueryException() throws JSONException {
    doThrow(JSONException.class).when(kuzzle).query(any(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    collection.updateDocument("id", mock(JSONObject.class), listener);
  }

  @Test(expected = RuntimeException.class)
  public void testupdateDocumentException() throws JSONException {
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(new JSONObject().put("result", new JSONObject()));
        return null;
      }
    }).when(kuzzle).query(any(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doThrow(JSONException.class).when(listener).onSuccess(any(String.class));
    collection.updateDocument("id", mock(JSONObject.class), listener);
  }

  @Test
  public void testUpdateDocument() throws JSONException {
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        JSONObject response = new JSONObject()
          .put("result", new JSONObject()
              .put("_id", "42")
              .put("_version", 1337)
              .put("_source", new JSONObject())
          );
        if (invocation.getArguments()[3] != null) {
          ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(response);
          ((OnQueryDoneListener) invocation.getArguments()[3]).onError(new JSONObject());
        }
        return null;
      }
    }).when(kuzzle).query(any(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));

    KuzzleDocument doc = new KuzzleDocument(collection);
    collection.updateDocument("42", doc.serialize());
    collection.updateDocument("42", doc.serialize(), new KuzzleOptions());
    collection.updateDocument("42", doc.serialize(), new KuzzleResponseListener<KuzzleDocument>() {
      @Override
      public void onSuccess(KuzzleDocument document) {
        assertEquals(document.getId(), "42");
        assertEquals(document.getVersion(), 1337);
      }

      @Override
      public void onError(JSONObject error) {

      }
    });
    collection.updateDocument("42", doc.serialize(), new KuzzleOptions(), new KuzzleResponseListener<KuzzleDocument>() {
      @Override
      public void onSuccess(KuzzleDocument document) {
        assertEquals(document.getId(), "42");
      }

      @Override
      public void onError(JSONObject error) {

      }
    });
    ArgumentCaptor argument = ArgumentCaptor.forClass(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class);
    verify(kuzzle, times(4)).query((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).controller, "write");
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).action, "update");
  }
}