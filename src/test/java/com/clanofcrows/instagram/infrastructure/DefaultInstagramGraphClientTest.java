package com.clanofcrows.instagram.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.clanofcrows.instagram.application.port.InstagramGraphClient;
import com.clanofcrows.instagram.config.InstagramProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class DefaultInstagramGraphClientTest {

    @Test
    void shouldCreateContainerUsingFormEncodedRequest() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://graph.facebook.com/v24.0/ig-user/media"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("image_url=https%3A%2F%2Fexample.com%2Fimage.jpg")))
                .andRespond(withSuccess("{\"id\":\"container-1\"}", MediaType.APPLICATION_JSON));

        DefaultInstagramGraphClient client = new DefaultInstagramGraphClient(builder, properties());

        String containerId = client.createMediaContainer(new InstagramGraphClient.CreateMediaContainerRequest(
                "ig-user",
                "token",
                "https://example.com/image.jpg",
                null,
                null
        ));

        assertThat(containerId).isEqualTo("container-1");
        server.verify();
    }

    @Test
    void shouldReadStatusCodeFromMetaResponse() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo("https://graph.facebook.com/v24.0/container-1?fields=status_code,status&access_token=token"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"status_code\":\"FINISHED\",\"status\":\"FINISHED\"}", MediaType.APPLICATION_JSON));

        DefaultInstagramGraphClient client = new DefaultInstagramGraphClient(builder, properties());

        InstagramGraphClient.MediaContainerStatus status = client.getMediaContainerStatus("container-1", "token");

        assertThat(status.statusCode()).isEqualTo("FINISHED");
        assertThat(status.isFinished()).isTrue();
        server.verify();
    }

    private InstagramProperties properties() {
        InstagramProperties properties = new InstagramProperties();
        properties.setAccessToken("token");
        properties.setIgUserId("ig-user");
        return properties;
    }
}
