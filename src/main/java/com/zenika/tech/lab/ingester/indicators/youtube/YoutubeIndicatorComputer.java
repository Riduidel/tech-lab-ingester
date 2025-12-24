package com.zenika.tech.lab.ingester.indicators.youtube;

import com.zenika.tech.lab.ingester.indicators.IndicatorComputer;
import com.zenika.tech.lab.ingester.indicators.downloads.DownloadCountIndicatorComputer;
import com.zenika.tech.lab.ingester.indicators.downloads.DownloadCounterForPackageManager;
import com.zenika.tech.lab.ingester.indicators.youtube.YouTubeApiClient;
import com.zenika.tech.lab.ingester.model.Indicator;
import com.zenika.tech.lab.ingester.model.IndicatorNamed;
import com.zenika.tech.lab.ingester.model.IndicatorRepositoryFacade;
import com.zenika.tech.lab.ingester.model.Technology;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.camel.Exchange;
import org.apache.camel.builder.endpoint.EndpointRouteBuilder;
import org.apache.camel.builder.endpoint.dsl.DirectEndpointBuilderFactory.DirectEndpointBuilder;
import org.apache.camel.support.processor.idempotent.MemoryIdempotentRepository;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;


@ApplicationScoped
public class YoutubeIndicatorComputer extends EndpointRouteBuilder implements IndicatorComputer {

    public static final String ROUTE_NAME = "compute-youtube-indicators";
    public static final String YOUTUBE_SEARCH = "youtube.videos";
    public static final String YOUTUBE_COMMENTS = "youtube.comments";
    public static final String YOUTUBE_LIKES = "youtube.likes";
    public static final String YOUTUBE_VIEWS = "youtube.views";
    @Inject @IndicatorNamed(DownloadCountIndicatorComputer.DOWNLOAD_COUNT)
    IndicatorRepositoryFacade indicators;
    @Inject
    @RestClient
    YouTubeApiClient youtubeClient; // REST client with static token injected
    Map<String, DownloadCounterForPackageManager> urlsToDownloaders;
    private DirectEndpointBuilder getFromRoute() {
        return direct(ROUTE_NAME);
    }

    @Override
    public String getFromRouteName() {
        return getFromRoute().getUri();
    }

    @Override
    public boolean canCompute(Technology technology) {
        return technology.name != null;
    }

    @Override
    public void configure() throws Exception {
        from(getFromRoute())
                .routeId("youtube-indicators-route")
                .idempotentConsumer()
                .body(Technology.class, r ->
                        String.format("youtube-%s", r.packageManagerUrl))
                .idempotentRepository(
                        MemoryIdempotentRepository.memoryIdempotentRepository(20))
                .process(this::fetchIndicators)
                .end();
    }

    private void fetchIndicators(Exchange exchange) {
        System.out.println(exchange.getMessage().getBody(Technology.class));
        fetchIndicators(exchange.getMessage().getBody(Technology.class));
    }

    private void fetchIndicators(Technology body) {

        String key = body.name;
        YouTubeApiClient.YouTubeSearchResponse searchResponse =
                youtubeClient.searchVideos(key, "snippet", 50);

        Long videoCount = searchResponse.items == null ? 0L : searchResponse.items.length;
        Long commentCount = 0L;
        Long likeCount = 0L;
        Long viewCount = 0L;
        List<String> videoIds = new ArrayList<>();
        for (YouTubeApiClient.SearchItem item : searchResponse.items) {
            if (item.id != null && item.id.videoId != null) {
                videoIds.add(item.id.videoId);
            }
        }
//        YouTubeApiClient.YouTubeVideosResponse commentResponse =
//                youtubeClient.searchVideoIndicators(String.join(",", videoIds), "statistics",1000);
        System.out.println(videoIds);
//        for (YouTubeApiClient.VideoItem item : commentResponse.items) {
//            commentCount = commentCount + (item.statistics != null && item.statistics.commentCount != null ? item.statistics.commentCount.longValue() : 0);
//            likeCount = likeCount + (item.statistics != null && item.statistics.likeCount != null ? item.statistics.likeCount.longValue() : 0);
//            viewCount = viewCount + (item.statistics != null && item.statistics.likeCount != null ? item.statistics.viewCount.longValue() : 0);
//        }

        Indicator videos = new Indicator(body,
                YOUTUBE_SEARCH,
                new Date(),
                Long.toString(videoCount));
        indicators.maybePersist(videos);

//        Indicator comments = new Indicator(body,
//                YOUTUBE_COMMENTS,
//                new Date(),
//                Long.toString(commentCount));
//        indicators.maybePersist(comments);
//
//        Indicator likes = new Indicator(body,
//                YOUTUBE_LIKES,
//                new Date(),
//                Long.toString(likeCount));
//        indicators.maybePersist(likes);
//
//        Indicator views = new Indicator(body,
//                YOUTUBE_VIEWS,
//                new Date(),
//                Long.toString(viewCount));
//        indicators.maybePersist(views);
    }
}
