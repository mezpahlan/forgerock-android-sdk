/*
 * Copyright (c) 2019 ForgeRock. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license. See the LICENSE file for details.
 */

package org.forgerock.android.auth;

import android.net.Uri;

import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.forgerock.android.auth.callback.NameCallback;
import org.forgerock.android.auth.callback.PasswordCallback;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.HttpURLConnection;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.forgerock.android.auth.Action.AUTHENTICATE;

@RunWith(RobolectricTestRunner.class)
public class FRSessionMockTest extends BaseTest {

    @After
    public void closeSession() throws Exception {
        if (FRSession.getCurrentSession() != null) {
            FRSession.getCurrentSession().logout();
        }
    }

    @Test
    public void frSessionHappyPath() throws InterruptedException, ExecutionException {

        enqueue("/authTreeMockTest_Authenticate_NameCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_PasswordCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_success.json", HttpURLConnection.HTTP_OK);

        Config.getInstance().setUrl(getUrl());
        Config.getInstance().setEncryptor(new MockEncryptor());

        NodeListenerFuture nodeListenerFuture = new NodeListenerFuture() {

            @Override
            public void onCallbackReceived(Node state) {
                if (state.getCallback(NameCallback.class) != null) {
                    state.getCallback(NameCallback.class).setName("tester");
                    state.next(context, this);
                    return;
                }

                if (state.getCallback(PasswordCallback.class) != null) {
                    state.getCallback(PasswordCallback.class).setPassword("password".toCharArray());
                    state.next(context, this);
                }
            }
        };

        FRSession.authenticate(context, "Example", nodeListenerFuture);
        Assert.assertTrue(nodeListenerFuture.get() instanceof FRSession);
        Assert.assertNotNull(FRSession.getCurrentSession());
        Assert.assertNotNull(FRSession.getCurrentSession().getSessionToken());
    }

    @Test
    public void frSessionWithPolicyAdvice() throws InterruptedException, ExecutionException {

        //First Round
        enqueue("/authTreeMockTest_Authenticate_NameCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_PasswordCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_success.json", HttpURLConnection.HTTP_OK);

        //Second Round
        enqueue("/authTreeMockTest_Authenticate_NameCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_PasswordCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_success.json", HttpURLConnection.HTTP_OK);

        Config.getInstance().setUrl(getUrl());
        Config.getInstance().setEncryptor(new MockEncryptor());

        NodeListenerFuture nodeListenerFuture = new NodeListenerFuture() {

            @Override
            public void onCallbackReceived(Node state) {
                if (state.getCallback(NameCallback.class) != null) {
                    state.getCallback(NameCallback.class).setName("tester");
                    state.next(context, this);
                    return;
                }

                if (state.getCallback(PasswordCallback.class) != null) {
                    state.getCallback(PasswordCallback.class).setPassword("password".toCharArray());
                    state.next(context, this);
                }
            }
        };

        PolicyAdvice advice = PolicyAdvice.builder()
                .type("TransactionConditionAdvice")
                .value("3b8c1b2b-0aed-461a-a49b-f35da8276d12").build();

        FRSession.authenticate(context, "Example", nodeListenerFuture);
        Assert.assertTrue(nodeListenerFuture.get() instanceof FRSession);
        Assert.assertNotNull(FRSession.getCurrentSession());
        Assert.assertNotNull(FRSession.getCurrentSession().getSessionToken());

        nodeListenerFuture.reset();
        FRSession.getCurrentSession().authenticate(context, advice, nodeListenerFuture);
        Assert.assertTrue(nodeListenerFuture.get() instanceof FRSession);

        server.takeRequest();
        server.takeRequest();
        server.takeRequest();

        RecordedRequest recordedRequest = server.takeRequest(); //The one with step up
        Uri uri = Uri.parse(recordedRequest.getPath());
        assertThat(uri.getQueryParameter("authIndexType")).isEqualTo("composite_advice");
        assertThat(uri.getQueryParameter("authIndexValue")).isEqualTo(advice.toString());

        //Make sure we have sent the request.
        server.takeRequest();
        server.takeRequest();


    }

    @Test
    public void testFRSessionReAuthenticate() throws Exception {

        //First Round
        enqueue("/authTreeMockTest_Authenticate_NameCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_PasswordCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_success.json", HttpURLConnection.HTTP_OK);

        //Second Round
        enqueue("/authTreeMockTest_Authenticate_NameCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_PasswordCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_success.json", HttpURLConnection.HTTP_OK);

        Config.getInstance().setUrl(getUrl());
        Config.getInstance().setEncryptor(new MockEncryptor());

        NodeListenerFuture nodeListenerFuture = new NodeListenerFuture() {

            @Override
            public void onCallbackReceived(Node state) {
                if (state.getCallback(NameCallback.class) != null) {
                    state.getCallback(NameCallback.class).setName("tester");
                    state.next(context, this);
                    return;
                }

                if (state.getCallback(PasswordCallback.class) != null) {
                    state.getCallback(PasswordCallback.class).setPassword("password".toCharArray());
                    state.next(context, this);
                }
            }
        };

        FRSession.authenticate(context, "Example", nodeListenerFuture);

        Assert.assertTrue(nodeListenerFuture.get() instanceof FRSession);

        nodeListenerFuture.reset();

        FRSession.authenticate(context, "Example", nodeListenerFuture);

        Assert.assertTrue(nodeListenerFuture.get() instanceof FRSession);

    }

    @Test
    public void testWithNoSession() throws ExecutionException, InterruptedException {

        enqueue("/authTreeMockTest_Authenticate_NameCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_PasswordCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_success_withNoSession.json", HttpURLConnection.HTTP_OK);

        Config.getInstance().setUrl(getUrl());
        Config.getInstance().setEncryptor(new MockEncryptor());

        NodeListenerFuture nodeListenerFuture = new NodeListenerFuture() {

            @Override
            public void onCallbackReceived(Node state) {
                if (state.getCallback(NameCallback.class) != null) {
                    state.getCallback(NameCallback.class).setName("tester");
                    state.next(context, this);
                    return;
                }

                if (state.getCallback(PasswordCallback.class) != null) {
                    state.getCallback(PasswordCallback.class).setPassword("password".toCharArray());
                    state.next(context, this);
                }
            }
        };

        RequestInterceptorRegistry.getInstance().register((FRRequestInterceptor<Action>) (request, tag) -> {
            if (tag.getType().equals(AUTHENTICATE)) {
                return request.newBuilder()
                        .url(Uri.parse(request.url().toString())
                                .buildUpon()
                                .appendQueryParameter("noSession", "true").toString())
                        .build();
            }
            return request;
        });

        FRSession.authenticate(context, "Example", nodeListenerFuture);
        assertThat(nodeListenerFuture.get()).isNull();
        assertThat(FRSession.getCurrentSession()).isNull();
        assertThat(FRUser.getCurrentUser()).isNull();

        RecordedRequest recordedRequest = server.takeRequest(); //NameCallback
        recordedRequest = server.takeRequest(); //PasswordCallback without Session
        assertThat(Uri.parse(recordedRequest.getPath()).getQueryParameter("noSession")).isEqualTo("true");
    }

    @Test
    public void testWithSessionThenWithoutSession() throws ExecutionException, InterruptedException {
        frSessionHappyPath();

        enqueue("/authTreeMockTest_Authenticate_NameCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_PasswordCallback.json", HttpURLConnection.HTTP_OK);
        enqueue("/authTreeMockTest_Authenticate_success_withNoSession.json", HttpURLConnection.HTTP_OK);

        Config.getInstance().setUrl(getUrl());
        Config.getInstance().setEncryptor(new MockEncryptor());

        NodeListenerFuture nodeListenerFuture = new NodeListenerFuture() {

            @Override
            public void onCallbackReceived(Node state) {
                if (state.getCallback(NameCallback.class) != null) {
                    state.getCallback(NameCallback.class).setName("tester");
                    state.next(context, this);
                    return;
                }

                if (state.getCallback(PasswordCallback.class) != null) {
                    state.getCallback(PasswordCallback.class).setPassword("password".toCharArray());
                    state.next(context, this);
                }
            }
        };

        RequestInterceptorRegistry.getInstance().register((FRRequestInterceptor<Action>) (request, tag) -> {
            if (tag.getType().equals(AUTHENTICATE)) {
                return request.newBuilder()
                        .url(Uri.parse(request.url().toString())
                                .buildUpon()
                                .appendQueryParameter("noSession", "true").toString())
                        .build();
            }
            return request;
        });

        FRSession.authenticate(context, "Example", nodeListenerFuture);
        assertThat(nodeListenerFuture.get()).isNull(); //The without session oen should return null
        //Retrieve the previous session
        Assert.assertNotNull(FRSession.getCurrentSession()); //Retrieve the previous Session
        Assert.assertNotNull(FRSession.getCurrentSession().getSessionToken());

        RecordedRequest recordedRequest = server.takeRequest(); //NameCallback with Session
        recordedRequest = server.takeRequest(); //PasswordCallback with Session
        recordedRequest = server.takeRequest(); //End of tree with Session

        recordedRequest = server.takeRequest(); //NameCallback without Session
        recordedRequest = server.takeRequest(); //PasswordCallback without Session
        recordedRequest = server.takeRequest(); //PasswordCallback without Session
        assertThat(Uri.parse(recordedRequest.getPath()).getQueryParameter("noSession")).isEqualTo("true");

    }


    @Test
    public void testLogout() throws ExecutionException, InterruptedException {

        frSessionHappyPath();
        enqueue("/sessions_logout.json", HttpURLConnection.HTTP_OK);

        FRSession.getCurrentSession().logout();

        //Check SSOToken Storage
        final SingleSignOnManager singleSignOnManager = DefaultSingleSignOnManager.builder()
                .context(context)
                .build();

        assertThat(singleSignOnManager.getToken()).isNull();

        assertThat(FRSession.getCurrentSession()).isNull();


    }
}
