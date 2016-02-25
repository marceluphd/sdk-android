package io.kuzzle.test.core.KuzzleDataCollection;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class publishMessageTest {
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
    KuzzleDocument doc = mock(KuzzleDocument.class);

    when(doc.getContent()).thenReturn(new JSONObject());
    collection = spy(collection);

    collection.publishMessage(doc);
    collection.publishMessage(doc, mock(KuzzleOptions.class));
    collection.publishMessage(mock(JSONObject.class));
    verify(collection, times(3)).publishMessage(any(JSONObject.class), any(KuzzleOptions.class));
  }


  @Test(expected = IllegalArgumentException.class)
  public void publishWithNoDocument() {
    collection.publishMessage((KuzzleDocument) null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void publishWithNoContent() {
    collection.publishMessage((JSONObject)null);
  }

  @Test(expected = RuntimeException.class)
  public void testPublishMessageException() throws JSONException {
    doThrow(JSONException.class).when(kuzzle).query(any(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    collection.publishMessage(mock(KuzzleDocument.class), mock(KuzzleOptions.class));
  }

  @Test
  public void testPublishMessage() throws JSONException {
    KuzzleDocument doc = new KuzzleDocument(collection);
    doc.setContent("foo", "bar");
    collection.publishMessage(doc);
    collection.publishMessage(doc, new KuzzleOptions());
    ArgumentCaptor argument = ArgumentCaptor.forClass(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class);
    verify(kuzzle, times(2)).query((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).controller, "write");
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).action, "publish");
  }

  @Test(expected = RuntimeException.class)
  public void testPublishMEssageException() {
    doThrow(JSONException.class).when(kuzzle).addHeaders(any(JSONObject.class), any(JSONObject.class));
    collection.publishMessage(mock(JSONObject.class));
  }

}