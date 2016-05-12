package io.kuzzle.test.core.Kuzzle;

import java.net.URISyntaxException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;


import io.kuzzle.sdk.core.Kuzzle;
import io.kuzzle.sdk.core.KuzzleOptions;
import io.kuzzle.sdk.enums.Mode;
import io.kuzzle.sdk.listeners.KuzzleResponseListener;
import io.kuzzle.sdk.listeners.OnQueryDoneListener;
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


public class refreshIndexTest {
  private KuzzleExtend kuzzle;
  private KuzzleResponseListener listener;

  @Before
  public void setup() throws URISyntaxException {
    KuzzleOptions options = new KuzzleOptions();
    options.setConnect(Mode.MANUAL);
    options.setDefaultIndex("testIndex");

    kuzzle = new KuzzleExtend("http://localhost:7512", options, null);
    kuzzle.setSocket(mock(Socket.class));

    listener = mock(KuzzleResponseListener.class);
  }

  @Test
  public void testAllSignatureVariants() {
    KuzzleOptions options = mock(KuzzleOptions.class);

    kuzzle = spy(kuzzle);

    kuzzle.refreshIndex();
    kuzzle.refreshIndex(options);
    kuzzle.refreshIndex(options, listener);
    kuzzle.refreshIndex(listener);
    kuzzle.refreshIndex("foo");
    kuzzle.refreshIndex("foo", options);
    kuzzle.refreshIndex("foo", listener);

    verify(kuzzle, times(7)).refreshIndex(any(String.class), any(KuzzleOptions.class), any(KuzzleResponseListener.class));
  }

  @Test(expected = RuntimeException.class)
  public void testRefreshIndexException() throws JSONException {
    kuzzle = spy(kuzzle);
    doThrow(JSONException.class).when(listener).onSuccess(any(JSONArray.class));
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(new JSONObject().put("result", new JSONObject().put("_shards", new JSONObject())));
        return null;
      }
    }).when(kuzzle).query(any(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    kuzzle.refreshIndex(listener);
  }

  @Test(expected = RuntimeException.class)
  public void testRefreshIndexQueryException() throws JSONException {
    kuzzle = spy(kuzzle);
    doThrow(JSONException.class).when(kuzzle).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    kuzzle.refreshIndex(listener);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testRefreshIndexIllegalDefaultIndex() {
    kuzzle = spy(kuzzle);
    kuzzle.setSuperDefaultIndex(null);
    kuzzle.refreshIndex(null, mock(KuzzleOptions.class), listener);
  }

  @Test
  public void testRefreshIndex() throws URISyntaxException, JSONException {
    kuzzle = spy(kuzzle);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(new JSONObject().put("result", new JSONObject().put("_shards", new JSONObject())));
        ((OnQueryDoneListener) invocation.getArguments()[3]).onError(mock(JSONObject.class));
        return null;
      }
    }).when(kuzzle).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));

    kuzzle.refreshIndex();
    kuzzle.refreshIndex(listener);
    kuzzle.refreshIndex(mock(KuzzleOptions.class), listener);
    ArgumentCaptor argument = ArgumentCaptor.forClass(Kuzzle.QueryArgs.class);
    verify(kuzzle, times(3)).query((Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).controller, "admin");
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).action, "refreshIndex");
    assertEquals(((Kuzzle.QueryArgs) argument.getValue()).index, kuzzle.getDefaultIndex());
  }

}