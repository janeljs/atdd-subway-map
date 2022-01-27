package nextstep.subway.acceptance;

import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import nextstep.subway.step.LineStep;
import nextstep.subway.step.SectionStep;
import nextstep.subway.step.StationStep;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("지하철 노선 관리 기능")
class LineAcceptanceTest extends AcceptanceTest {

    private final static Integer NUMBER_ONE = 1;

    private ExtractableResponse<Response> 수원역;
    private ExtractableResponse<Response> 사당역;
    private ExtractableResponse<Response> 신도림;
    private ExtractableResponse<Response> 신림역;
    private ExtractableResponse<Response> 가산역;

    @BeforeEach
    void init() {
        수원역 = StationStep.saveStation("수원역");
        사당역 = StationStep.saveStation("사당역");
        신도림 = StationStep.saveStation("신도림");
        신림역 = StationStep.saveStation("신림역");
        가산역 = StationStep.saveStation("가산역");
    }

    /**
     * When 지하철 노선 생성을 요청 하면
     * Then 지하철 노선 생성이 성공한다.
     */
    @DisplayName("지하철 노선 생성")
    @Test
    void createLine() {

        long upStationId = 수원역.jsonPath().getLong("id");
        long downStationId = 사당역.jsonPath().getLong("id");

        // 요청 후, 노선을 생성하다
        ExtractableResponse<Response> extract
                = LineStep.saveLine("하늘색", "4호선", upStationId,downStationId,3);

        // 상태 코드
        assertThat(extract.response().statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(extract.header("Location")).isNotBlank();
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

        long upStationId = 수원역.jsonPath().getLong("id");
        long downStationId = 사당역.jsonPath().getLong("id");

        // 요청 후, 노선을 생성하다
        LineStep.saveLine("하늘색", "4호선", upStationId,downStationId,3);
        LineStep.saveLine("파란색", "1호선", upStationId,downStationId,3);

        ExtractableResponse<Response> response = LineStep.showLines();

        // 조회 포함 확인
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        List<String> resultResponseData = response.jsonPath().getList("color");
        assertThat(resultResponseData).contains("하늘색", "파란색");
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 생성한 지하철 노선 조회를 요청 하면
     * Then 생성한 지하철 노선을 응답받는다
     */
    @DisplayName("지하철 노선 조회")
    @Test
    void getLine() {

        long upStationId = 수원역.jsonPath().getLong("id");
        long downStationId = 사당역.jsonPath().getLong("id");

        // 요청 후, 노선을 생성하다
        LineStep.saveLine("하늘색", "4호선", upStationId,downStationId,3);

        // 조회 결과
        ExtractableResponse<Response> response = LineStep.showLine(NUMBER_ONE);

        // then
        assertThat(response.statusCode()).isEqualTo(HttpStatus.OK.value());
        String responseResultData = response.jsonPath().get("color");
        assertThat(responseResultData).isEqualTo("하늘색");
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 지하철 노선의 정보 수정을 요청 하면
     * Then 지하철 노선의 정보 수정은 성공한다.
     */
    @DisplayName("지하철 노선 수정")
    @Test
    void updateLine() {

        long upStationId = 수원역.jsonPath().getLong("id");
        long downStationId = 사당역.jsonPath().getLong("id");

        // 요청 후, 노선을 생성하다
        LineStep.saveLine("하늘색", "4호선", upStationId,downStationId,3);

        // 수정 요청
        ExtractableResponse<Response> response = LineStep.updateLine("파란색", "1호선", 1, 1L,2L,3);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    /**
     * Given 지하철 노선 생성을 요청 하고
     * When 생성한 지하철 노선 삭제를 요청 하면
     * Then 생성한 지하철 노선 삭제가 성공한다.
     */
    @DisplayName("지하철 노선 삭제")
    @Test
    void deleteLine() {

        long upStationId = 수원역.jsonPath().getLong("id");
        long downStationId = 사당역.jsonPath().getLong("id");

        // 요청 후, 노선을 생성하다
        LineStep.saveLine("하늘색", "4호선", upStationId,downStationId,3);

        // 노선을 삭제하다
        ExtractableResponse<Response> response = LineStep.deleteLine(NUMBER_ONE);

        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    /**
     * Given 지하철 노선을 생성 요청 한다.
     * When 같은 이름으로 지하철 역을 생성 요청한다.
     * Then 지하철 노선 생성이 실패한다.
     */
    @DisplayName("중복 지하철 노선 생성 실패")
    @Test
    void createLine_duplication() {

        long upStationId = 수원역.jsonPath().getLong("id");
        long downStationId = 사당역.jsonPath().getLong("id");


        // 노선을 생성한다.
        LineStep.saveLine("하늘색", "4호선", upStationId,downStationId,3);

        // 중복으로 생성할 때
        ExtractableResponse<Response> response = LineStep.saveLine("파란색", "4호선", 1L,2L,3);

        // 실패를 한다.
        assertThat(response.statusCode()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @DisplayName("구간을 생성")
    @Test
    void createSection() {

        // 첫 노선도에 들어갈 두 역
        long upStationId = 수원역.jsonPath().getLong("id");
        long downStationId = 사당역.jsonPath().getLong("id");

        // 노선 생성
        ExtractableResponse<Response> line =
            LineStep.saveLine("파란색", "1호선", upStationId, downStationId, 20);

        long lineId = line.jsonPath().getLong("id");

        // 구간 생성
        long newStationId = 신도림.jsonPath().getLong("id");
        ExtractableResponse<Response> extract = SectionStep.saveSection(downStationId, newStationId, 10, lineId);

        // 상태 코드
        assertThat(extract.response().statusCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(extract.header("Location")).isNotBlank();
    }

    @DisplayName("새로운 구간의 하행역은 현재 등록되어있는 역일 수 없다.")
    @Test
    void upStationDownStationRelation() {

        // 첫 노선도에 들어갈 두 역
        long upStationId = 수원역.jsonPath().getLong("id");
        long downStationId = 사당역.jsonPath().getLong("id");

        // 노선 생성
        ExtractableResponse<Response> line = LineStep.saveLine("파란색", "1호선", upStationId, downStationId, 20);
        long lineId = line.jsonPath().getLong("id");

        // 구간 생성
        ExtractableResponse<Response> extract = SectionStep.saveSection(downStationId, upStationId, 2, lineId);

        assertThat(extract.statusCode()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @DisplayName("새로운 구간의 상행역은 현재 등록되어있는 하행 종점역이어야한다.")
    @Test
    void newUpStationMustBeDownStation() {

        ExtractableResponse<Response> 성균관대역 = StationStep.saveStation("성균관대역");

        // 첫 노선도에 들어갈 두 역
        long upStationId = 수원역.jsonPath().getLong("id");
        long downStationId = 사당역.jsonPath().getLong("id");
        long newStationId = 성균관대역.jsonPath().getLong("id");

        // 노선 생성
        ExtractableResponse<Response> line =
                LineStep.saveLine("파란색", "1호선", upStationId, downStationId, 10);
        long lineId = line.jsonPath().getLong("id");

        // 구간 생성
        ExtractableResponse<Response> extract = SectionStep.saveSection(newStationId, downStationId, 2, lineId);

        assertThat(extract.statusCode()).isEqualTo(HttpStatus.CONFLICT.value());
    }

    @DisplayName("지하철 구간을 삭제한다.")
    @Test
    void deleteSection() {

        long 수원역_ID = 수원역.jsonPath().getLong("id"); // init
        long 사당역_ID = 사당역.jsonPath().getLong("id"); // init
        long 신도림_ID = 신도림.jsonPath().getLong("id");
        long 신림역_ID = 신림역.jsonPath().getLong("id");

        // 노선 생성
        ExtractableResponse<Response> line = LineStep.saveLine("파란색", "1호선", 수원역_ID, 사당역_ID, 10);
        long lineId = line.jsonPath().getLong("id");

        // 구간 생성
        SectionStep.saveSection(사당역_ID, 신도림_ID, 10, lineId);
        SectionStep.saveSection(신도림_ID, 신림역_ID, 10, lineId);

        ExtractableResponse<Response> response = SectionStep.deleteSection(lineId, 신림역_ID);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("지하철 노선에 등록된 마지막 역(하행 종점역)만 제거할 수 있다.")
    @Test
    void deleteSectionMustBeDownStation() {
        long 수원역_ID = 수원역.jsonPath().getLong("id"); // init
        long 사당역_ID = 사당역.jsonPath().getLong("id"); // init
        long 신도림_ID = 신도림.jsonPath().getLong("id");
        long 신림역_ID = 신림역.jsonPath().getLong("id");

        // 노선 생성
        ExtractableResponse<Response> line = LineStep.saveLine("파란색", "1호선", 수원역_ID, 사당역_ID, 10);
        long lineId = line.jsonPath().getLong("id");

        // 구간 생성
        SectionStep.saveSection(사당역_ID, 신도림_ID, 10, lineId);
        SectionStep.saveSection(신도림_ID, 신림역_ID, 10, lineId);

        ExtractableResponse<Response> response = SectionStep.deleteSection(lineId, 사당역_ID);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @DisplayName("지하철 노선에 상행 종점역과 하행 종점역만 있는 경우(구간이 1개인 경우) 역을 삭제할 수 없다.")
    @Test
    void deleteSectionMustBeTwoNode() {
        long 수원역_ID = 수원역.jsonPath().getLong("id"); // init
        long 사당역_ID = 사당역.jsonPath().getLong("id"); // init

        // 노선 생성
        ExtractableResponse<Response> line = LineStep.saveLine("파란색", "1호선", 수원역_ID, 사당역_ID, 10);
        long lineId = line.jsonPath().getLong("id");

        ExtractableResponse<Response> response = SectionStep.deleteSection(lineId, 사당역_ID);
        assertThat(response.statusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
