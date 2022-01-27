package nextstep.subway.acceptance;

import static nextstep.subway.step.LineStep.구간_삭제;
import static nextstep.subway.step.LineStep.구간_생성;
import static nextstep.subway.step.LineStep.노선_목록_조회;
import static nextstep.subway.step.LineStep.노선_변경;
import static nextstep.subway.step.LineStep.노선_삭제;
import static nextstep.subway.step.LineStep.노선_생성;
import static nextstep.subway.step.StationStep.역_생성;
import static org.assertj.core.api.Assertions.assertThat;

import io.restassured.RestAssured;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.applicaion.dto.ChangeLineRequest;
import nextstep.subway.domain.SectionRequest;
import nextstep.subway.utils.RequestMethod;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@DisplayName("지하철 노선 관리 기능")
class LineAcceptanceTest extends AcceptanceTest {

    private static final String DEFAULT_PATH = "/lines";
    private static final String JSON_PATH_ID = "id";

    private static final String 강남역 = "강남역";
    private static final String 역삼역 = "역삼역";
    private static final String 신촌역 = "신촌역";
    private static final String 교대역 = "교대역";

    private static final String 신분당선 = "신분당선";
    private static final String 수인분당선 = "수인분당선";

    private static final String SINBUNDANGLINE_COLOR = "bg-red-600";
    private static final String SUINBUNDANGLINE_COLOR = "bg-blue-700";

    private static final int DEFAULT_DISTANCE = 5;

    /**
     * When 지하철 노선 생성을 요청 하면
     * Then 지하철 노선 생성이 성공한다.
     */
    @DisplayName("지하철 노선 생성")
    @Test
    void createLineTest() {
        // given

        Long 강남역_id = 역_생성(강남역).jsonPath().getLong(JSON_PATH_ID);
        Long 역삼역_id = 역_생성(역삼역).jsonPath().getLong(JSON_PATH_ID);

        // when
        ExtractableResponse<Response> response = 노선_생성(신분당선, SINBUNDANGLINE_COLOR, 강남역_id, 역삼역_id,
            DEFAULT_DISTANCE);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CREATED.value());
    }

    /**
     * Given
     * When 노선 생성 시 두 종점역을 모두 입력하지 않으면
     * Then 생성에 실패한다
     */
    @Test
    @DisplayName("지하철 노선 생성 실패")
    void createLineExceptionTest() {
        //given
        // when
        ExtractableResponse<Response> response = RequestMethod.post(DEFAULT_PATH, param1);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * Given 새로운 지하철 노선 생성을 요청 하고
     * When 지하철 노선 목록 조회를 요청 하면
     * Then 두 노선이 포함된 지하철 노선 목록을 응답받는다
     */
    @DisplayName("지하철 노선 목록 조회")
    @Test
    void getLines() {
        //given
        RequestMethod.post(DEFAULT_PATH, param1);
        RequestMethod.post(DEFAULT_PATH, param2);

        //when
        ExtractableResponse<Response> response = RequestMethod.get(DEFAULT_PATH);

        //then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.jsonPath().getList("name")).contains(PARAM1_NAME_VALUE, PARAM2_NAME_VALUE);
        assertThat(response.jsonPath().getList("color")).contains(PARAM1_COLOR_VALUE, PARAM2_COLOR_VALUE);
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 생성한 지하철 노선 조회를 요청 하면
     * Then 생성한 지하철 노선을 응답받는다
     */
    @DisplayName("지하철 노선 조회")
    @Test
    void getLine() {
        //given
        ExtractableResponse<Response> createResponse = RequestMethod.post(DEFAULT_PATH, param1);

        // when
        ExtractableResponse<Response> response = RequestMethod.get(
           DEFAULT_PATH + "/" + createResponse.jsonPath().getString("id"));

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 지하철 노선의 정보 수정을 요청 하면
     * Then 지하철 노선의 정보 수정은 성공한다.
     */
    @DisplayName("지하철 노선 수정")
    @Test
    void updateLine() {
        //given
        ExtractableResponse<Response> createResponse = RequestMethod.post(DEFAULT_PATH, param1);

        // when
        ExtractableResponse<Response> response = RequestMethod.put(
            DEFAULT_PATH + "/" + createResponse.jsonPath().getString("id"), param2);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 생성한 지하철 노선 삭제를 요청 하면
     * Then 생성한 지하철 노선 삭제가 성공한다.
     */
    @DisplayName("지하철 노선 삭제")
    @Test
    void deleteLine() {
        //given
        ExtractableResponse<Response> createResponse = RequestMethod.post(DEFAULT_PATH, param1);

        // when
        ExtractableResponse<Response> response = RequestMethod.delete(
             DEFAULT_PATH + "/" + createResponse.jsonPath().getString("id"));

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 같은 이름으로 지하철 노선 생성을 요청 하면
     * Then 지하철 노선 생성이 실패한다.
     */
    @Test
    @DisplayName("중복이름으로 지하철 노선 생성 실패")
    void duplicationLineNameExceptionTest() {
        // given
        RequestMethod.post(DEFAULT_PATH, param1);

        // when
        ExtractableResponse<Response> response = RequestMethod.post(DEFAULT_PATH, param1);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.FORBIDDEN.value());
    }

}
