package nextstep.subway.acceptance;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("구간 관련 기능")
public class SectionAcceptanceTest extends BaseAcceptanceTest {

    @BeforeEach
    void dataSetUp() {
        StationAcceptanceTest.지하철역_등록_요청("강남역");
        StationAcceptanceTest.지하철역_등록_요청("역삼역");
        StationAcceptanceTest.지하철역_등록_요청("판교역");

        LineAcceptanceTest.노선_생성_요청("2호선", "bg-green-600", 1L, 2L, 10);
    }

    /**
     * `Given`  지하철 노선을 조회하고
     * `When`   구간(`상행역` : 조회한 노선의 하행 종점역이 아닌 역, `하행역` : 노선에 등록되어 있지 않은 역)을 등록하면
     * `Then`   400 에러 코드를 응답받는다.
     */
    @Test
    @DisplayName("구간 등록 예외 - 잘못된 상행역")
    void createSection_invalid() {
        // given
        final long _2호선_노선_ID = 1L;
        ExtractableResponse<Response> response = LineAcceptanceTest.노선_조회_요청(_2호선_노선_ID);
        List<Long> _2호선_지하철역_ID_목록 = response.jsonPath().getList("stations.id", Long.class);
        final int 상행역_INDEX = 0;
        final long _2호선_상행종점역_ID = _2호선_지하철역_ID_목록.get(상행역_INDEX);

        // when
        ExtractableResponse<Response> 구간_생성_요청_결과 =
                구간_등록_요청(_2호선_노선_ID, _2호선_상행종점역_ID, 3L, 5, HttpStatus.BAD_REQUEST);

        // then
        assertThat(구간_생성_요청_결과.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * `Given` 지하철 노선을 조회하고
     * `When` 구간(`상행역` : 조회한 노선의 하행 종점역, `하행역` : 노선에 등록되어 있는 역)을 등록하면
     * `Then` 400 에러 코드를 응답받는다.
     */
    @Test
    @DisplayName("구간 등록 예외 - 잘못된 하행역")
    void createSection_invalid_wrong_downStation() {
        // given
        final long _2호선_노선_ID = 1L;
        ExtractableResponse<Response> response = LineAcceptanceTest.노선_조회_요청(_2호선_노선_ID);
        List<Long> _2호선_지하철역_ID_목록 = response.jsonPath().getList("stations.id", Long.class);
        final int 하행역_INDEX = 1;
        final long _2호선_하행종점역_ID = _2호선_지하철역_ID_목록.get(하행역_INDEX);

        // when
        ExtractableResponse<Response> 구간_생성_요청_결과 =
                구간_등록_요청(_2호선_노선_ID, _2호선_하행종점역_ID, 1L, 5, HttpStatus.BAD_REQUEST);

        // then
        assertThat(구간_생성_요청_결과.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * `Given` 지하철 노선을 조회하고
     * `When` 구간(`상행역` : 조회한 노선의 하행 종점역, `하행역` : 노선에 등록되어 있지 않은 역)을 등록하면
     * `Then` 해당 지하철 노선을 조회 시 등록한 구간을 찾을 수 있다.
     */
    @Test
    @DisplayName("구간 등록")
    void createSection() {
        // given
        final long _2호선_노선_ID = 1L;
        ExtractableResponse<Response> response = LineAcceptanceTest.노선_조회_요청(_2호선_노선_ID);
        List<Long> _2호선_지하철역_ID_목록 = response.jsonPath().getList("stations.id", Long.class);
        final int 하행역_INDEX = 1;
        final long _2호선_하행종점역_ID = _2호선_지하철역_ID_목록.get(하행역_INDEX);

        // when
        구간_등록_요청(_2호선_노선_ID, _2호선_하행종점역_ID, 3L, 5, HttpStatus.CREATED);

        // then
        ExtractableResponse<Response> 노선_조회_응답 = LineAcceptanceTest.노선_조회_요청(_2호선_노선_ID);
        List<String> _2호선_지하철역_이름_목록 = 노선_조회_응답.jsonPath().getList("stations.name", String.class);
        assertThat(_2호선_지하철역_이름_목록).containsOnly("강남역", "역삼역", "판교역");
    }

    private ExtractableResponse<Response> 구간_등록_요청(long id, long upStationId, long downStationId, int distance, HttpStatus httpStatus) {
        final Map<String, Object> params = new HashMap<>();
        params.put("downStationId", downStationId);
        params.put("upStationId", upStationId);
        params.put("distance", distance);

        ExtractableResponse<Response> response = RestAssured.given().log().all()
                .body(params)
                .contentType(MediaType.APPLICATION_JSON_VALUE)
                .when().post("/lines/{id}/sections", id)
                .then().log().all()
                .extract();
        assertThat(response.statusCode()).isEqualTo(httpStatus.value());
        return response;
    }
}
