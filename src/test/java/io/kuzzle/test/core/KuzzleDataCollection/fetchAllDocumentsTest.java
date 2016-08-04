package io.kuzzle.test.core.KuzzleDataCollection;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.net.URISyntaxException;

import io.kuzzle.sdk.core.Kuzzle;
import io.kuzzle.sdk.core.KuzzleDataCollection;
import io.kuzzle.sdk.core.KuzzleOptions;
import io.kuzzle.sdk.enums.Mode;
import io.kuzzle.sdk.listeners.KuzzleResponseListener;
import io.kuzzle.sdk.listeners.OnQueryDoneListener;
import io.kuzzle.sdk.state.KuzzleStates;
import io.kuzzle.test.testUtils.KuzzleExtend;
import io.socket.client.Socket;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class fetchAllDocumentsTest {
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

    collection = new KuzzleDataCollection(kuzzle, "index", "test");
    listener = mock(KuzzleResponseListener.class);
  }

  @Test
  public void checkSignaturesVariants() {
    collection = spy(collection);
    collection.fetchAllDocuments(listener);
    verify(collection).fetchAllDocuments(eq((KuzzleOptions)null), eq(listener));
  }

  @Test(expected = IllegalArgumentException.class)
  public void testFetchAllDocumentsIllegalListener() {
    collection.fetchAllDocuments(null);
  }

  @Test
  public void testFetchAllDocuments() throws JSONException {
    collection.fetchAllDocuments(new KuzzleOptions(), mock(KuzzleResponseListener.class));
    collection.fetchAllDocuments(mock(KuzzleResponseListener.class));
    ArgumentCaptor argument = ArgumentCaptor.forClass(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class);
    verify(kuzzle, times(2)).query((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).controller, "read");
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).action, "search");
  }

  @Test
  public void testPagination() throws JSONException {
    KuzzleOptions options = new KuzzleOptions();
    options.setFrom(1L);
    options.setSize(42L);
    collection.fetchAllDocuments(options, mock(KuzzleResponseListener.class));
    ArgumentCaptor argument = ArgumentCaptor.forClass(JSONObject.class);
    verify(kuzzle).query(any(Kuzzle.QueryArgs.class), (JSONObject)argument.capture(), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((JSONObject) argument.getValue()).getJSONObject("body").getLong("from"), 1);
    assertEquals(((JSONObject) argument.getValue()).getJSONObject("body").getLong("size"), 42);
  }

}
