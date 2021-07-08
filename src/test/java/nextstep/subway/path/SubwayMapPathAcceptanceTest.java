package nextstep.subway.path;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.AcceptanceTest;
import nextstep.subway.auth.dto.TokenRequest;
import nextstep.subway.auth.dto.TokenResponse;
import nextstep.subway.line.acceptance.LineAcceptanceTest;
import nextstep.subway.line.dto.LineRequest;
import nextstep.subway.line.dto.LineResponse;
import nextstep.subway.station.dto.PathResponse;
import nextstep.subway.station.dto.StationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static nextstep.subway.auth.acceptance.AuthAcceptanceTest.로그인_요청;
import static nextstep.subway.line.acceptance.LineSectionAcceptanceTest.지하철_노선에_지하철역_등록_요청;
import static nextstep.subway.member.MemberAcceptanceTest.회원_생성을_요청;
import static nextstep.subway.station.StationAcceptanceTest.지하철역_등록되어_있음;
import static org.assertj.core.api.Assertions.assertThat;


@DisplayName("지하철 경로 조회")
public class SubwayMapPathAcceptanceTest extends AcceptanceTest {

    private LineResponse 신분당선;
    private LineResponse 이호선;
    private LineResponse 삼호선;
    private LineResponse 구호선;
    private StationResponse 강남역;
    private StationResponse 양재역;
    private StationResponse 교대역;
    private StationResponse 남부터미널역;
    private StationResponse 신논현역;
    private StationResponse 언주역;
    private StationResponse 선정릉역;

    /**
     * 교대역    --- *2호선* ---   강남역
     * |                        |
     * *3호선*                   *신분당선*
     * |                        |
     * 남부터미널역  --- *3호선* ---   양재
     */
    @BeforeEach
    public void setUp() {
        super.setUp();

        강남역 = 지하철역_등록되어_있음("강남역").as(StationResponse.class);
        양재역 = 지하철역_등록되어_있음("양재역").as(StationResponse.class);
        교대역 = 지하철역_등록되어_있음("교대역").as(StationResponse.class);
        남부터미널역 = 지하철역_등록되어_있음("남부터미널역").as(StationResponse.class);
        신논현역 = 지하철역_등록되어_있음("신논현역").as(StationResponse.class);
        언주역 = 지하철역_등록되어_있음("언주역").as(StationResponse.class);
        선정릉역 = 지하철역_등록되어_있음("선정릉역").as(StationResponse.class);

        신분당선 = 지하철_노선_등록되어_있음("신분당선", "bg-red-600", 900, 강남역.getId(), 양재역.getId(), 10);
        이호선 = 지하철_노선_등록되어_있음("이호선", "bg-green-600", 0, 교대역.getId(), 강남역.getId(), 10);
        삼호선 = 지하철_노선_등록되어_있음("삼호선", "bg-orange-600", 0, 교대역.getId(), 양재역.getId(), 5);
        구호선 = 지하철_노선_등록되어_있음("구호선", "bg-gold-600", 0, 신논현역.getId(), 언주역.getId(), 7);

        지하철_노선에_지하철역_등록_요청(삼호선, 교대역, 남부터미널역, 3);
        지하철_노선에_지하철역_등록_요청(구호선, 언주역, 선정릉역, 10);
    }

    @DisplayName("최단경로 조회")
    @Test
    void paths() {
        // when
        ExtractableResponse<Response> response = 최단경로_조회_요청(양재역, 교대역);

        // then
        최단경로_조회됨(response);
        최단경로_일치(response, Arrays.asList(양재역, 남부터미널역, 교대역), 5, 1250);
    }

    @DisplayName("최단경로 조회 - 존재하지 않는 역")
    @Test
    void findPaths_stationIsNotExists() {
        // when
        StationResponse 미등록역 = new StationResponse(999L, "미등록역", LocalDateTime.now(), LocalDateTime.now());
        ExtractableResponse<Response> response = 최단경로_조회_요청(양재역, 미등록역);

        // then
        최단경로_조회_실패됨(response);
    }

    @DisplayName("최단경로 조회 - 출발역과 도착역이 동일한 경우")
    @Test
    void findPaths_sourceAndTargetAreSame() {
        // when
        ExtractableResponse<Response> response = 최단경로_조회_요청(양재역, 양재역);

        // then
        최단경로_조회_실패됨(response);
    }

    @DisplayName("최단경로 조회 - 출발역과 도착역이 연결되어 있지 않은 경우")
    @Test
    void findPaths_sourceAndTargetAreNotConnected() {
        // when
        ExtractableResponse<Response> response = 최단경로_조회_요청(양재역, 신논현역);

        // then
        최단경로_조회_실패됨(response);
    }

    @DisplayName("요금조회 - 거리비례 요금")
    @Test
    void findPath_distanceBasedFare() {
        // when
        ExtractableResponse<Response> response = 최단경로_조회_요청(신논현역, 선정릉역);

        // then
        최단경로_조회됨(response);
        최단경로_일치(response, Arrays.asList(신논현역, 언주역, 선정릉역), 17, 1350);
    }

    @DisplayName("요금조회 - 추가요금이 존재하는 노선")
    @Test
    void findPath_lineHasExtraFare() {
        // when
        ExtractableResponse<Response> response = 최단경로_조회_요청(강남역, 양재역);

        // then
        최단경로_조회됨(response);
        최단경로_일치(response, Arrays.asList(강남역, 양재역), 10, 2150);
    }

    @DisplayName("요금조회 - 청소년 요금할인")
    @Test
    void findPath_youthDiscount() {
        // given
        회원_생성을_요청("youth@test.com", "1234", 15);
        TokenResponse tokenResponse = 로그인_요청(new TokenRequest("youth@test.com", "1234")).as(TokenResponse.class);

        // when
        ExtractableResponse<Response> response = 최단경로_조회_요청(양재역, 교대역, tokenResponse);

        // then
        최단경로_조회됨(response);
        최단경로_일치(response, Arrays.asList(양재역, 남부터미널역, 교대역), 5, 800);
    }

    private ExtractableResponse<Response> 최단경로_조회_요청(StationResponse source, StationResponse target) {
        return 최단경로_조회_요청(source, target, new TokenResponse("dummy"));
    }

    private ExtractableResponse<Response> 최단경로_조회_요청(StationResponse source, StationResponse target, TokenResponse tokenResponse) {
        return RestAssured
                .given().log().all()
                .auth().oauth2(tokenResponse.getAccessToken())
                .accept(MediaType.APPLICATION_JSON_VALUE)
                .when().get("/stations/paths?source={sourceId}&target={targetId}", source.getId(), target.getId())
                .then().log().all()
                .extract();
    }

    private void 최단경로_조회됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    private void 최단경로_조회_실패됨(ExtractableResponse<Response> response) {
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    private void 최단경로_일치(ExtractableResponse<Response> response, List<StationResponse> expectedList, int distance, int fare) {
        PathResponse pathResponse = response.as(PathResponse.class);
        List<String> actual = pathResponse.getStations().stream()
                .map(StationResponse::getName)
                .collect(Collectors.toList());
        List<String> expected = expectedList.stream()
                .map(StationResponse::getName)
                .collect(Collectors.toList());
        assertThat(actual).isEqualTo(expected);
        assertThat(pathResponse.getDistance()).isEqualTo(distance);
        assertThat(pathResponse.getFare()).isEqualTo(fare);
    }

    private LineResponse 지하철_노선_등록되어_있음(String lineName, String lineColor, Integer extraFare, Long upStationId, Long downStationId, int distance) {
        LineRequest lineRequest = new LineRequest(lineName, lineColor, extraFare, upStationId, downStationId, distance);
        return LineAcceptanceTest.지하철_노선_등록되어_있음(lineRequest).as(LineResponse.class);
    }
}