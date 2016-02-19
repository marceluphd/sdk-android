package io.kuzzle.sdk;

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
import io.kuzzle.sdk.core.KuzzleRoomOptions;
import io.kuzzle.sdk.enums.Mode;
import io.kuzzle.sdk.listeners.KuzzleResponseListener;
import io.kuzzle.sdk.listeners.OnQueryDoneListener;
import io.kuzzle.sdk.state.KuzzleStates;
import io.kuzzle.sdk.toolbox.KuzzleTestToolbox;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class KuzzleDocumentTest {

  private Kuzzle k;
  private KuzzleDocument doc;
  private KuzzleDataCollection  mockCollection;
  private KuzzleResponseListener mockListener;

  @Before
  public void setUp() throws URISyntaxException, JSONException {
    KuzzleOptions opts = new KuzzleOptions();
    opts.setConnect(Mode.MANUAL);
    k = new Kuzzle("http://localhost:7512", opts);
    KuzzleTestToolbox.forceConnectedState(k, KuzzleStates.CONNECTED);
    k = spy(k);
    mockCollection = mock(KuzzleDataCollection.class);
    mockListener = mock(KuzzleResponseListener.class);
    doc = new KuzzleDocument(new KuzzleDataCollection(k, "index", "test"));
  }

  @Test
  public void testConstructor() throws JSONException {
    doc = new KuzzleDocument(new KuzzleDataCollection(k, "index", "test"), "42");
    assertEquals(doc.getId(), "42");
  }

  @Test
  public void testCollection() throws JSONException {
    Kuzzle k = mock(Kuzzle.class);
    when(k.getHeaders()).thenReturn(new JSONObject());
    KuzzleDataCollection collection = new KuzzleDataCollection(k, "index", "test");
    KuzzleDocument doc = new KuzzleDocument(collection);
    assertEquals(doc.getCollection(), collection.getCollection());
  }

  @Test
  public void testDocumentWithContent() throws JSONException {
    JSONObject content = new JSONObject();
    content.put("foo", "bar");

    doc = new KuzzleDocument(new KuzzleDataCollection(k, "index", "test"), content);
    assertEquals(doc.getContent().getString("foo"), "bar");
  }

  @Test(expected = RuntimeException.class)
  public void testSaveQueryException() throws JSONException {
    doc.setId("42");
    doThrow(JSONException.class).when(k).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doc.save();
  }

  @Test(expected = RuntimeException.class)
  public void testSaveException() throws JSONException {
    doc.setId("42");
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(new JSONObject().put("result", new JSONObject().put("_id", "42").put("_version", "42")));
        ((OnQueryDoneListener) invocation.getArguments()[3]).onError(mock(JSONObject.class));
        return null;
      }
    }).when(k).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doThrow(JSONException.class).when(mockListener).onSuccess(any(KuzzleDocument.class));
    doc.save(mockListener);
  }

  @Test
  public void testSave() throws JSONException {
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        JSONObject response = new JSONObject();
        response.put("_id", "id-42");
        response.put("_version", "42");
        response.put("result", response);
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(response);
        ((OnQueryDoneListener) invocation.getArguments()[3]).onError(null);
        return null;
      }
    }).when(k).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doc.save();
    doc.save(new KuzzleOptions());
    doc.save(new KuzzleResponseListener<KuzzleDocument>() {
      @Override
      public void onSuccess(KuzzleDocument object) {
        assertEquals(object.getId(), "id-42");
      }

      @Override
      public void onError(JSONObject error) {

      }
    });
    ArgumentCaptor argument = ArgumentCaptor.forClass(Kuzzle.QueryArgs.class);
    verify(k, times(3)).query((Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).controller, "write");
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).action, "createOrReplace");
  }

  @Test(expected = RuntimeException.class)
  public void testDeleteException() throws JSONException {
    doc.setId("42");
    doThrow(JSONException.class).when(k).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doc.delete();
  }

  @Test
  public void testDelete() throws JSONException {
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        JSONObject response = new JSONObject();
        response.put("result", "foo");
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(response);
        ((OnQueryDoneListener) invocation.getArguments()[3]).onError(null);
        return null;
      }
    }).when(k).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doc.setId("id-42");
    doc.delete(mock(KuzzleResponseListener.class));
    doc.setId("id-42");
    doc.delete();
    doc.setId("id-42");
    doc.delete(mock(KuzzleOptions.class));
    assertNull(doc.getId());
    ArgumentCaptor argument = ArgumentCaptor.forClass(Kuzzle.QueryArgs.class);
    verify(k, times(3)).query((Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).controller, "write");
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).action, "delete");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRefreshNullListenerException() throws IllegalArgumentException {
    doc.setId("42");
    doc.refresh(null);
  }

  @Test(expected = RuntimeException.class)
  public void testRefreshQueryException() throws JSONException {
    doc.setId("42");
    doThrow(JSONException.class).when(k).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doc.refresh(mockListener);
  }

  @Test(expected = RuntimeException.class)
  public void testRefreshException() throws JSONException {
    doc.setId("42");
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(mock(JSONObject.class));
        ((OnQueryDoneListener) invocation.getArguments()[3]).onError(mock(JSONObject.class));
        return null;
      }
    }).when(k).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doThrow(JSONException.class).when(mockListener).onSuccess(any(KuzzleDocument.class));
    doc.refresh(mockListener);
  }

  @Test(expected = RuntimeException.class)
  public void testRefreshWithoutId() {
    doc.refresh(null, null);
  }

  @Test
  public void testRefresh() throws JSONException {
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        JSONObject response = new JSONObject()
          .put("result", new JSONObject()
            .put("_id", "42")
            .put("_version", 1337)
            .put("_source", new JSONObject().put("foo", "bar")));
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(response);
        ((OnQueryDoneListener) invocation.getArguments()[3]).onError(null);
        return null;
      }
    }).when(k).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doc.setId("42");
    doc.setContent("foo", "baz");
    doc.refresh(new KuzzleResponseListener<KuzzleDocument>() {
      @Override
      public void onSuccess(KuzzleDocument object) {
        try {
          assertEquals(1337, object.getVersion());
          assertEquals("bar", object.getContent().getString("foo"));
          assertNotEquals(doc, object);
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onError(JSONObject error) {

      }
    });
    doc.refresh(mockListener);
    doc.refresh(mock(KuzzleOptions.class), mockListener);
    ArgumentCaptor argument = ArgumentCaptor.forClass(Kuzzle.QueryArgs.class);
    verify(k, times(3)).query((Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).controller, "read");
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).action, "get");

  }

  @Test(expected = RuntimeException.class)
  public void testPublishException() throws JSONException {
    doThrow(JSONException.class).when(k).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doc.publish();
  }

  @Test
  public void testPublish() throws JSONException {
    doc.publish();
    ArgumentCaptor argument = ArgumentCaptor.forClass(Kuzzle.QueryArgs.class);
    verify(k, times(1)).query((Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).controller, "write");
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).action, "publish");
  }

  @Test(expected = RuntimeException.class)
  public void testSetContentPutException() throws JSONException {
    doc = spy(doc);
    doc.setContent(mock(JSONObject.class));
  }

  @Test
  public void testSetContent() throws JSONException {
    assertEquals(doc.getContent().toString(), new JSONObject().toString());
    JSONObject data = new JSONObject();
    data.put("test", "some content");
    doc.setContent(data, false);
    assertEquals(doc.getContent().get("test"), "some content");
    data = new JSONObject();
    data.put("test 2", "some other content");
    doc.setContent(data, true);
    assertEquals(doc.getContent().get("test 2"), "some other content");
    assertTrue(doc.getContent().isNull("test"));
  }

  @Test(expected = RuntimeException.class)
  public void testSubscribeException() throws JSONException {
    doc = new KuzzleDocument(mockCollection);
    doc.setId("42");
    doThrow(JSONException.class).when(mockCollection).subscribe(any(JSONObject.class), any(KuzzleRoomOptions.class), any(KuzzleResponseListener.class));
    doc.subscribe(mock(KuzzleResponseListener.class));
  }

  @Test(expected = RuntimeException.class)
  public void testSubscribeNullId() {
    doc.subscribe(mock(KuzzleResponseListener.class));
  }

  @Test
  public void testSubscribe() throws JSONException {
    doc.setId("42");
    doc.subscribe(mock(KuzzleResponseListener.class));
    doc.subscribe(new KuzzleRoomOptions(), mock(KuzzleResponseListener.class));
    ArgumentCaptor argument = ArgumentCaptor.forClass(Kuzzle.QueryArgs.class);
    verify(k, times(2)).query((Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).controller, "subscribe");
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).action, "on");
  }

  @Test
  public void testGetContent() throws JSONException {
    doc.setContent(null);
    assertNotNull(doc.getContent());
    doc.setContent("foo", "bar");
    assertEquals(doc.getContent().getString("foo"), "bar");
    assertNull(doc.getContent("!exist"));
  }

  @Test
  public void testSetHeaders() throws JSONException {
    JSONObject headers = new JSONObject();
    headers.put("foo", "bar");
    doc.setHeaders(headers, true);
    assertEquals(doc.getHeaders().getString("foo"), "bar");
    headers.put("oof", "baz");
    doc.setHeaders(headers);
    assertEquals(doc.getHeaders().getString("foo"), "bar");
    assertEquals(doc.getHeaders().getString("oof"), "baz");
  }

  @Test
  public void testGetHeaders() throws JSONException {
    doc.setHeaders(null);
    assertNotNull(doc.getHeaders());
    JSONObject headers = new JSONObject();
    headers.put("foo", "bar");
    doc.setHeaders(headers);
    assertEquals(doc.getHeaders().getString("foo"), "bar");
  }

  @Test
  public void testGetVersion() throws JSONException {
    assertEquals(-1, doc.getVersion());
    doc.setVersion(42);
    assertEquals(42, doc.getVersion());
  }
}
