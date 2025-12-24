package com.zenika.tech.lab.ingester.indicators.youtube;

import java.time.temporal.ChronoUnit;

import io.quarkus.cache.CacheResult;
import io.quarkus.rest.client.reactive.ClientQueryParam;
import io.smallrye.faulttolerance.api.RateLimit;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(configKey = "youtube-api")
@RateLimit(value = 60, window = 1, windowUnit = ChronoUnit.MINUTES)
@ClientQueryParam(name = "key", value = "${tech-lab-ingester.youtube.api.key}")
public interface YouTubeApiClient {


    @GET
    @Path("/search")
    YouTubeSearchResponse searchVideos(
            @QueryParam("q") String keyword,
            @QueryParam("part") String part,
            @QueryParam("maxResults") int maxResults
    );

    @GET
    @Path("/videos")
    YouTubeVideosResponse searchVideoIndicators(
            @QueryParam("id") String keyword,
            @QueryParam("part") String part,
            @QueryParam("maxResults") int maxResults
    );

    class YouTubeSearchResponse {
        public SearchItem[] items;
    }
    class YouTubeVideosResponse {
        public VideoItem[] items;
    }

    class SearchItem {
        public Id id;
    }
    class Id {
        public String videoId;
        public String kind;
    }
    class VideoItem {
        public Statistics statistics;
    }
    class Statistics {
        public Long viewCount;
        public Long likeCount;
        public Long commentCount;
    }

}
