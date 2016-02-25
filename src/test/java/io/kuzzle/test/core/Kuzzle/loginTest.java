package io.kuzzle.test.core.Kuzzle;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.net.URISyntaxException;

import io.kuzzle.sdk.core.Kuzzle;
import io.kuzzle.sdk.core.KuzzleOptions;
import io.kuzzle.sdk.enums.KuzzleEvent;
import io.kuzzle.sdk.enums.Mode;
import io.kuzzle.sdk.listeners.IKuzzleEventListener;
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

public class loginTest {
  private KuzzleExtend kuzzle;
  private Socket s;
  private KuzzleResponseListener listener;

  @Before
  public void setUp() throws URISyntaxException {
    KuzzleOptions options = new KuzzleOptions();
    options.setConnect(Mode.MANUAL);
    options.setDefaultIndex("testIndex");

    s = mock(Socket.class);
    kuzzle = new KuzzleExtend("http://localhost:7512", options, null);
    kuzzle.setSocket(s);

    listener = new KuzzleResponseListener<Object>() {
      @Override
      public void onSuccess(Object object) {

      }

      @Override
      public void onError(JSONObject error) {

      }
    };
  }

  @Test
  public void checkAllSignaturesVariants() {
    JSONObject stubCredentials = new JSONObject();
    kuzzle = spy(kuzzle);
    listener = spy(listener);
    kuzzle.login("foo", stubCredentials);
    kuzzle.login("foo", stubCredentials, 42);
    kuzzle.login("foo", stubCredentials, listener);
    verify(kuzzle, times(3)).login(any(String.class), any(JSONObject.class), any(int.class), any(KuzzleResponseListener.class));
  }

  @Test
  public void testLogin() throws JSONException {
    kuzzle = spy(kuzzle);

    KuzzleResponseListener listenerSpy = spy(listener);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(new JSONObject().put("result", new JSONObject().put("_type", "type")));
        ((OnQueryDoneListener) invocation.getArguments()[3]).onError(mock(JSONObject.class));
        return null;
      }
    }).when(kuzzle).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    kuzzle.login("local", new JSONObject().put("username", "username").put("password", "password"));
    kuzzle.login("local", new JSONObject().put("username", "username").put("password", "password"), 42);
    kuzzle.login("local", new JSONObject().put("username", "username").put("password", "password"), 42, listenerSpy);
    ArgumentCaptor argument = ArgumentCaptor.forClass(io.kuzzle.sdk.core.Kuzzle.QueryArgs.class);
    verify(kuzzle, times(3)).query((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.capture(), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).controller, "auth");
    assertEquals(((io.kuzzle.sdk.core.Kuzzle.QueryArgs) argument.getValue()).action, "login");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoStrategy() {
    kuzzle.login(null, new JSONObject());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testNoCredentials() {
    kuzzle.login("foo", null);
  }

  @Test
  public void testLoginAttemptEvent() throws JSONException {
    kuzzle = spy(kuzzle);
    kuzzle.addListener(KuzzleEvent.loginAttempt, mock(IKuzzleEventListener.class));
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(new JSONObject().put("result", new JSONObject().put("jwt", "token")));
        ((OnQueryDoneListener) invocation.getArguments()[3]).onError(mock(JSONObject.class));
        return null;
      }
    }).when(kuzzle).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    kuzzle.login("local", new JSONObject());
    verify(kuzzle, times(2)).emitEvent(any(KuzzleEvent.class), any(Object.class));
  }

  @Test(expected = RuntimeException.class)
  public void testLoginQueryException() throws JSONException {
    kuzzle = spy(kuzzle);
    doThrow(JSONException.class).when(kuzzle).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    kuzzle.login("local", new JSONObject());
  }

  @Test(expected = RuntimeException.class)
  public void testLoginOnSuccessException() throws JSONException {
    kuzzle = spy(kuzzle);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((OnQueryDoneListener) invocation.getArguments()[3]).onSuccess(new JSONObject().put("result", new JSONObject().put("jwt", "token")));
        return null;
      }
    }).when(kuzzle).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doThrow(JSONException.class).when(kuzzle).emitEvent(any(KuzzleEvent.class), any(Object.class));
    kuzzle.login("local", new JSONObject());
  }

  @Test(expected = RuntimeException.class)
  public void testLoginOnErrorException() throws JSONException {
    kuzzle = spy(kuzzle);
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        ((OnQueryDoneListener) invocation.getArguments()[3]).onError(mock(JSONObject.class));
        return null;
      }
    }).when(kuzzle).query(any(Kuzzle.QueryArgs.class), any(JSONObject.class), any(KuzzleOptions.class), any(OnQueryDoneListener.class));
    doThrow(JSONException.class).when(kuzzle).emitEvent(any(KuzzleEvent.class), any(Object.class));
    kuzzle.login("local", new JSONObject());
  }
}