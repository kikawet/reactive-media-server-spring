package com.kikawet.reactiveMediaServer.routes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RouterFunction;

import com.kikawet.reactiveMediaServer.dto.WatchedVideoDTO;
import com.kikawet.reactiveMediaServer.exception.UnauthorizedUserException;
import com.kikawet.reactiveMediaServer.model.User;
import com.kikawet.reactiveMediaServer.model.Video;
import com.kikawet.reactiveMediaServer.model.WatchedVideo;
import com.kikawet.reactiveMediaServer.service.UserService;

@SpringBootTest
public class UserRouterTests extends BaseRouterTests {

	@MockBean
	UserService us;

	private final User testUser = new User("test", null, null);
	private final Video testVideo = new Video("testVideo");
	private final List<WatchedVideo> watchedVideos = List.of(
			new WatchedVideo(testUser, testVideo, LocalDateTime.now(), 17),
			new WatchedVideo(testUser, testVideo, LocalDateTime.now(), 69),
			new WatchedVideo(testUser, testVideo, LocalDateTime.now(), 33));

	@BeforeEach
	void setUp() {
		when(us.getHistoryByLogin(anyString(), any())).thenThrow(UnauthorizedUserException.class);
		when(us.getHistoryByLogin(eq("test"), any())).thenReturn(watchedVideos.stream());
	}

	@Test
	void getHistoryByLoginTest(@Autowired RouterFunction<?> getHistoryByLogin) {
		getWebTestClient(getHistoryByLogin)
				.get()
				.uri("/user/test/history")
				.exchange()
				.expectStatus()
				.isOk()
				.expectHeader()
				.contentType(MediaType.APPLICATION_JSON)
				.expectBodyList(WatchedVideoDTO.class)
				.isEqualTo(watchedVideos
						.stream()
						.map(WatchedVideo::toDTO)
						.collect(Collectors.toList()));
	}

	@Test
	void getHistoryByLoginThrowsTest(@Autowired RouterFunction<?> getHistoryByLogin) {
		getWebTestClient(getHistoryByLogin)
				.get()
				.uri("/user/null/history")
				.exchange()
				.expectStatus()
				.isUnauthorized()
				.expectBody()
				.isEmpty();
	}

}
