package subway.line;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.DtoConverter;
import subway.station.Station;
import subway.station.StationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LineService {

    private final LineRepository lineRepository;

    private final DtoConverter dtoConverter;

    private final StationRepository stationRepository;

    public LineService(LineRepository lineRepository, StationRepository stationRepository, DtoConverter dtoConverter) {
        this.lineRepository = lineRepository;
        this.stationRepository = stationRepository;
        this.dtoConverter = dtoConverter;
    }

    @Transactional
    public LineResponse saveLine(LineCreateRequest lineCreateRequest) {
        Station upStation = findStationEntity(lineCreateRequest.getUpStationId());
        Station downStation = findStationEntity(lineCreateRequest.getDownStationId());
        Line line = dtoConverter.of(lineCreateRequest, upStation, downStation);
        Line saveLine = lineRepository.save(line);

        return dtoConverter.of(saveLine);
    }

    private Station findStationEntity(Long id) {
        return stationRepository.findById(id).orElseThrow(() -> new RuntimeException("없는 Station Id입니다."));
    }

    public List<LineResponse> findAllLines() {
        List<Line> lineList = lineRepository.findAll();
        return lineList.stream().map(dtoConverter::of).collect(Collectors.toList());
    }

    private Line findLineEntity(Long id) {
        return lineRepository.findById(id).orElseThrow(() -> new RuntimeException("없는 Line Id입니다"));
    }

    public LineResponse findLine(Long id) {
        Line lineEntity = findLineEntity(id);
        return dtoConverter.of(lineEntity);
    }

    @Transactional
    public void updateLine(Long lineId, LineUpdateRequest lineUpdateRequest) {
        Line lineEntity = findLineEntity(lineId);
        lineEntity.update(lineUpdateRequest.getName(), lineUpdateRequest.getColor());
    }

    @Transactional
    public void deleteLine(Long lineId) {
        lineRepository.deleteById(lineId);
    }
}