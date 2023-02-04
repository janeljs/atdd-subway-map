package subway;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import subway.line.LineCreateRequest;
import subway.line.LineResponse;
import subway.section.SectionCreateRequest;
import subway.station.StationResponse;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class SectionAcceptanceTest extends AcceptanceTest {

    Long lineId;
    Long firstStationId;
    Long secondStationId;
    Long thirdStationId;

    @BeforeEach
    public void init() {
        firstStationId = StationRestAssuredTest.createStation("지하철역1");
        secondStationId = StationRestAssuredTest.createStation("지하철역2");
        thirdStationId = StationRestAssuredTest.createStation("지하철역3");
        LineCreateRequest lineCreateRequest = new LineCreateRequest("신분당선", "bg-red-600", firstStationId, secondStationId, 10L);
        lineId = LineRestAssuredTest.createLine(lineCreateRequest);
    }
    /**
     * When 지하철 노선에 구간을 등록하면
     * Then 지하철 노선에 구간이 등록된다.
     * Then 지하철 노선을 조회하면 추가된 구간을 확인할 수 있다.
     */
    @DisplayName("지하철 노선에 구간을 등록하고 그 구간을 조회할 수 있다.")
    @Test
    public void sectionCreateTest() {
        var param = new SectionCreateRequest(secondStationId, thirdStationId, 10L);
        SectionRestAssuredTest.createSection(lineId, param);

        LineResponse line = LineRestAssuredTest.getLine(lineId);
        List<Long> ids = line.getStationResponseList().stream().map(StationResponse::getId).collect(Collectors.toList());
        assertThat(ids).containsAnyOf(thirdStationId);
    }

    /**
     * When 지하철 노선에 새로운 구간의 상행역이 해당 노선에 하행 종점역이 아닌 구간을 등록하면
     * Then 지하철 노선을 등록할 수 없다.
     */
    @DisplayName("지하철 노선에 추가 구간의 상행역이 해당 노선의 하행 종점역이 아니면 추가할 수 없다")
    @Test
    public void sectionCreateFail_1() {
        var param = new SectionCreateRequest(firstStationId, thirdStationId, 10L);
        SectionRestAssuredTest.createSectionFail(lineId, param);
    }

    @DisplayName("지하철 노선에 추가 구간의 하행역이 해당 노선에 등록되어 있는 역일 수 없다.")
    @Test
    public void sectionCreateFail_2() {
        var param = new SectionCreateRequest(secondStationId, firstStationId, 10L);
        SectionRestAssuredTest.createSectionFail(lineId, param);
    }
    /**
     * When 지하철 노선에 구간을 제거하면
     * Then 지하철 노선에 구간이 제거된다.
     * Then 제거된 노선을 조회시 에러가 발생한다.
     */
    @DisplayName("지하철 노선에 구간을 제거하고 그 구간을 조회 시 삭제한 구간을 발견할 수 없다.")
    @Test
    public void sectionDeleteTest() {
        var param = new SectionCreateRequest(secondStationId, thirdStationId, 10L);
        SectionRestAssuredTest.createSection(lineId, param);
        SectionRestAssuredTest.deleteSection(lineId, thirdStationId);

        LineResponse line = LineRestAssuredTest.getLine(lineId);
        List<Long> ids = line.getStationResponseList().stream().map(StationResponse::getId).collect(Collectors.toList());
        assertThat(ids).doesNotContain(thirdStationId);
    }

    /**
     * Given 지하철 노선을 생성하고
     * When 하행종점역이 아닌 지하철역을 삭제하면
     * Then 에러가 발생한다.
     */
    @DisplayName("하행종점역이 아닌 노선을 삭제하면 에러가 발생한다.")
    @Test
    public void deleteLineFailTest_1() {
        var param = new SectionCreateRequest(secondStationId, thirdStationId, 10L);
        SectionRestAssuredTest.createSection(lineId, param);

        SectionRestAssuredTest.deleteSectionFail(lineId, secondStationId);
    }
    /**
     * Given 지하철 노선을 생성하고
     * When 한개 남은 지하철 역을 삭제하면
     * Then 에러가 발생한다.
     */
    @DisplayName("한개 남은 노선을 삭제하면 에러가 발생한다.")
    @Test
    public void deleteLineFailTest_2() {
        SectionRestAssuredTest.deleteSectionFail(lineId, secondStationId);
    }


}