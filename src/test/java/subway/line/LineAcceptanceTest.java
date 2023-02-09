package subway.line;

import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import subway.common.AcceptanceTest;

import java.util.List;

import static subway.common.constants.ErrorConstant.*;
import static subway.common.constants.ErrorConstant.ONLY_ONE_SECTION;
import static subway.utils.AssertUtil.*;
import static subway.utils.AssertUtil.assertFailBadRequest;
import static subway.utils.LineUtil.*;
import static subway.utils.StationUtil.createStationResultResponse;

@DisplayName("지하철 노선 관련 기능")
public class LineAcceptanceTest extends AcceptanceTest {

    /**
     * When 지하철 노선을 생성하면
     * Then 지하철 노선 목록 조회 시 생성한 노선을 찾을 수 있다.
     */
    @Test
    @DisplayName("지하철노선 생성")
    void createLine() {
        // when
        createStationResultResponse("지하철역");
        createStationResultResponse("새로운지하철역");

        createLineResultResponse("신분당선", "bg-red-600", 1L, 2L, 10);

        // then
        List<String> lineNames = showLinesResultResponse().getList("name", String.class);
        assertEqualToNames(lineNames, "신분당선");
    }

    /**
     * Given 2개의 지하철 노선을 생성하고
     * When 지하철 노선 목록을 조회하면
     * Then 지하철 노선 목록 조회 시 2개의 노선을 조회할 수 있다.
     */
    @Test
    @DisplayName("지하철노선 목록 조회")
    void showLines() {
        // given
        Long stationId = createStationResultResponse("지하철역").getLong("id");
        Long newStationId = createStationResultResponse("새로운지하철역").getLong("id");
        Long otherStationId = createStationResultResponse("또다른지하철역").getLong("id");

        createLineResultResponse("신분당선", "bg-red-600", stationId, newStationId, 10);
        createLineResultResponse("분당선", "bg-green-600", stationId, otherStationId, 10);

        // when
        List<String> lineNames = showLinesResultResponse().getList("name", String.class);

        // then
        assertEqualToNames(lineNames, "신분당선", "분당선");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 조회하면
     * Then 생성한 지하철 노선의 정보를 응답받을 수 있다.
     */
    @Test
    @DisplayName("지하철노선 조회")
    void showLine() {
        // given
        createStationResultResponse("지하철역");
        createStationResultResponse("새로운지하철역");

        Long lineId = createLineResultResponse("신분당선", "bg-red-600", 1L, 2L, 10).getLong("id");

        // when
        JsonPath line = showLineResultResponse(lineId);

        // then
        assertEqualToLine(line, "신분당선", "bg-red-600");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 수정하면
     * Then 해당 지하철 노선 정보는 수정된다.
     */
    @Test
    @DisplayName("지하철노선 수정")
    void updateLine() {
        // given
        createStationResultResponse("지하철역");
        createStationResultResponse("새로운지하철역");

        Long lineId = createLineResultResponse("신분당선", "bg-red-600", 1L, 2L, 10).getLong("id");

        // when
        updateLineResult(lineId, "다른분당선", "bg-red-600");

        // then
        JsonPath line = showLineResultResponse(lineId);
        assertEqualToLine(line, "다른분당선", "bg-red-600");
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 생성한 지하철 노선을 삭제하면
     * Then 해당 지하철 노선 정보는 삭제된다.
     */
    @Test
    @DisplayName("지하철노선 삭제")
    void deleteLine() {
        // given
        createStationResultResponse("지하철역");
        createStationResultResponse("새로운지하철역");

        Long lineId = createLineResultResponse("신분당선", "bg-red-600", 1L, 2L, 10).getLong("id");

        // when
        deleteLineResult(lineId);

        // then
        List<String> lineNames = showLinesResultResponse().getList("name", String.class);
        assertEqualToNames(lineNames);
    }

    /**
     * Given 노선을 생성한다.
     * When 노선에 새로운 구간을 등록한다.
     * Then 노선 구간 목록에서 찾을 수 있다.
     */
    @Test
    @DisplayName("지하철 구간 등록")
    void addSection() {
        // Given
        Long upStationId = createStationResultResponse("강남역").getLong("id");
        Long downStationId = createStationResultResponse("역삼역").getLong("id");
        Long newStationId = createStationResultResponse("선릉역").getLong("id");

        Long lineId = createLineResultResponse("2호선", "bg-green-600", upStationId, downStationId, 7).getLong("id");

        // When
        ExtractableResponse<Response> response = addSectionResponse(lineId, downStationId, newStationId, 3);
        assertSuccessOk(response);

        // Then
        JsonPath line = showLineResultResponse(lineId);
        assertEqualToSections(line, new String[]{"역삼역", "선릉역"}, new String[]{"강남역", "역삼역"}, 7, 3);
    }

    /**
     * Given 노선을 생성한다.
     * When 노선의 하행 종점역이 상행역이 아닌 새로운 구간을 등록한다.
     * Then 에러가 발생한다.
     */
    @Test
    @DisplayName("지하철 구간등록 실패 - 하행종점-상행역이 아닌 구간")
    void addSection_failure_notSameStation() {
        // Given
        Long upStationId = createStationResultResponse("강남역").getLong("id");
        Long downStationId = createStationResultResponse("역삼역").getLong("id");
        Long newUpStationId = createStationResultResponse("선릉역").getLong("id");
        Long newDownStationId = createStationResultResponse("삼성역").getLong("id");

        Long lineId = createLineResultResponse("2호선", "bg-green-600", upStationId, downStationId, 7).getLong("id");

        // When
        ExtractableResponse<Response> response = addSectionResponse(lineId, newDownStationId, newUpStationId, 3);

        // Then
        assertFailBadRequest(response, NOT_ADD_LAST_STATION);
    }

    /**
     * Given 노선을 생성한다.
     * When 노선에 이미 등록된 역을 하행역으로 하는 새로운 구간을 등록한다.
     * Then 에러가 발생한다.
     */
    @Test
    @DisplayName("지하철 구간등록 실패 - 이미 등록된 구간 하행역")
    void addSection_failure_enrollStation() {
        // Given
        Long upStationId = createStationResultResponse("강남역").getLong("id");
        Long downStationId = createStationResultResponse("역삼역").getLong("id");

        Long lineId = createLineResultResponse("2호선", "bg-green-600", upStationId, downStationId, 7).getLong("id");

        // When
        ExtractableResponse<Response> response = addSectionResponse(lineId, downStationId, upStationId, 3);

        // Then
        assertFailBadRequest(response, ALREADY_ENROLL_STATION);
    }

    /**
     * Given 노선에 구간을 등록한다.
     * When 등록한 구간을 삭제한다.
     * Then 노선의 구간 목록에서 찾을 수 없다.
     */
    @Test
    @DisplayName("지하철 구간 삭제")
    void deleteSection() {
        // Given
        Long upStationId = createStationResultResponse("강남역").getLong("id");
        Long downStationId = createStationResultResponse("역삼역").getLong("id");
        Long newStationId = createStationResultResponse("선릉역").getLong("id");

        Long lineId = createLineResultResponse("2호선", "bg-green-600", upStationId, downStationId, 7).getLong("id");

        addSectionResponse(lineId, downStationId, newStationId, 3);

        // When
        ExtractableResponse<Response> response = deleteSectionResponse(lineId, 3L);
        assertSuccessNoContent(response);

        // Then
        JsonPath line = showLineResultResponse(lineId);
        assertEqualToSections(line, new String[]{"역삼역"}, new String[]{"강남역"}, 7);
    }

    /**
     * Given 노선을 생성한다.
     * When 마지막이 아닌 구간을 삭제한다.
     * Then 에러가 발생한다.
     */
    @Test
    @DisplayName("지하철 삭제등록 실패 - 마지막이 아닌 구간")
    void deleteSection_failure_notLastStation() {
        // Given
        Long upStationId = createStationResultResponse("강남역").getLong("id");
        Long downStationId = createStationResultResponse("역삼역").getLong("id");
        Long newStationId = createStationResultResponse("선릉역").getLong("id");

        Long lineId = createLineResultResponse("2호선", "bg-green-600", upStationId, downStationId, 7).getLong("id");

        addSectionResponse(lineId, downStationId, newStationId, 3);

        // When
        ExtractableResponse<Response> response = deleteSectionResponse(lineId, 2L);

        // Then
        assertFailBadRequest(response, NOT_DELETE_LAST_STATION);
    }

    /**
     * Given 노선을 생성한다.
     * When 1개의 구간만 가지는 노선의 구간 삭제
     * Then 에러가 발생한다.
     */
    @Test
    @DisplayName("지하철 구간삭제 실패 - 구간이 1개")
    void deleteSection_failure_onlyOneStation() {
        // Given
        Long upStationId = createStationResultResponse("강남역").getLong("id");
        Long downStationId = createStationResultResponse("역삼역").getLong("id");

        Long lineId = createLineResultResponse("2호선", "bg-green-600", upStationId, downStationId, 7).getLong("id");

        // When
        ExtractableResponse<Response> response = deleteSectionResponse(lineId, 2L);

        // Then
        assertFailBadRequest(response, ONLY_ONE_SECTION);
    }
}