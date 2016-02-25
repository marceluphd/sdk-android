package io.kuzzle.test.security.KuzzleSecurity;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import io.kuzzle.sdk.core.Kuzzle;
import io.kuzzle.sdk.listeners.KuzzleResponseListener;
import io.kuzzle.sdk.security.KuzzleProfile;
import io.kuzzle.sdk.security.KuzzleRole;
import io.kuzzle.sdk.security.KuzzleSecurity;
import io.kuzzle.sdk.security.KuzzleUser;

import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class factoriesTest {
  private Kuzzle kuzzle;
  private KuzzleSecurity kuzzleSecurity;
  private KuzzleResponseListener listener;

  @Before
  public void setUp() {
    kuzzle = mock(Kuzzle.class);
    kuzzleSecurity = new KuzzleSecurity(kuzzle);
    listener = mock(KuzzleResponseListener.class);
  }

  @Test
  public void testRoleFactory() throws JSONException {
    assertThat(kuzzleSecurity.roleFactory("id"), instanceOf(KuzzleRole.class));
    assertThat(kuzzleSecurity.roleFactory("id", new JSONObject()), instanceOf(KuzzleRole.class));
  }

  @Test
  public void testProfileFactory() throws JSONException {
    assertThat(kuzzleSecurity.profileFactory("id"), instanceOf(KuzzleProfile.class));
    assertThat(kuzzleSecurity.profileFactory("id", new JSONObject()), instanceOf(KuzzleProfile.class));
  }

  @Test
  public void testUserFactory() throws JSONException {
    assertThat(kuzzleSecurity.userFactory("id"), instanceOf(KuzzleUser.class));
    assertThat(kuzzleSecurity.userFactory("id", new JSONObject()), instanceOf(KuzzleUser.class));
  }
}