package io.kuzzle.sdk.core;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import io.kuzzle.sdk.listeners.ResponseListener;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by kblondel on 22/10/15.
 */
public class KuzzleDataMappingTest {

    private Kuzzle k;
    private KuzzleDataCollection    dataCollection;
    private KuzzleDataMapping dataMapping;

    @Before
    public void setUp() {
        k = mock(Kuzzle.class);
        dataCollection = new KuzzleDataCollection(k, "test");
        dataMapping = new KuzzleDataMapping(dataCollection);
    }

    @Test
    public void testApply() throws IOException, JSONException {
        dataMapping.apply();
        verify(k, times(1)).query(eq("test"), eq("admin"), eq("putMapping"), any(JSONObject.class), any(ResponseListener.class));
    }

    @Test
    public void testRefresh() throws IOException, JSONException {
        dataMapping.refresh();
        verify(k, times(1)).query(eq("test"), eq("admin"), eq("getMapping"), any(JSONObject.class), any(ResponseListener.class));
    }

}
