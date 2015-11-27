package io.kuzzle.sdk;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.IOException;

import io.kuzzle.sdk.core.Kuzzle;
import io.kuzzle.sdk.core.KuzzleDataCollection;
import io.kuzzle.sdk.core.KuzzleDataMapping;
import io.kuzzle.sdk.core.KuzzleOptions;
import io.kuzzle.sdk.exceptions.KuzzleException;
import io.kuzzle.sdk.listeners.ResponseListener;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class KuzzleDataMappingTest {

  private Kuzzle k;
  private KuzzleDataCollection dataCollection;
  private KuzzleDataMapping dataMapping;

  @Before
  public void setUp() {
    k = mock(Kuzzle.class);
    dataCollection = new KuzzleDataCollection(k, "test");
    dataMapping = new KuzzleDataMapping(dataCollection);
  }

  @Test
  public void testApply() throws IOException, JSONException, KuzzleException {
    dataMapping.apply();
    verify(k, times(1)).query(eq("test"), eq("admin"), eq("putMapping"), any(JSONObject.class), any(KuzzleOptions.class), any(ResponseListener.class));
  }

  @Test
  public void testRefresh() throws IOException, JSONException, KuzzleException {
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        JSONObject response = new JSONObject("{\"mainindex\": {\"mappings\": {" +
            "        \"test\": {" +
            "          \"properties\": {" +
            "            \"available\": {" +
            "              \"type\": \"boolean\"" +
            "            }," +
            "            \"foo\": {" +
            "              \"type\": \"string\"" +
            "            }," +
            "            \"type\": {" +
            "              \"type\": \"string\"" +
            "            }," +
            "            \"userId\": {" +
            "              \"type\": \"string\"" +
            "            }" +
            "          }" +
            "        }}}}");
        ((ResponseListener) invocation.getArguments()[5]).onSuccess(response);
        return null;
      }
    }).when(k).query(eq("test"), eq("admin"), eq("getMapping"), any(JSONObject.class), any(KuzzleOptions.class), any(ResponseListener.class));

    dataMapping.refresh();
    dataMapping.refresh(new KuzzleOptions());
    dataMapping.refresh(new ResponseListener() {
      @Override
      public void onSuccess(JSONObject object) throws Exception {
        assertEquals(object.getJSONObject("test").getJSONObject("properties").getJSONObject("foo").getString("type"), "string");
      }

      @Override
      public void onError(JSONObject error) throws Exception {

      }
    });
    dataMapping.refresh(new KuzzleOptions(), new ResponseListener() {
      @Override
      public void onSuccess(JSONObject object) throws Exception {

      }

      @Override
      public void onError(JSONObject error) throws Exception {

      }
    });
    verify(k, times(4)).query(eq("test"), eq("admin"), eq("getMapping"), any(JSONObject.class), any(KuzzleOptions.class), any(ResponseListener.class));
  }

}
