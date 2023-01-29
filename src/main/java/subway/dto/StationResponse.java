package subway.dto;

import subway.domain.Station;

public class StationResponse {
    private final Long id;
    private final String name;

    public static StationResponse createStationResponse(Station station) {
        return new StationResponse(station.getId(), station.getName());
    }

    protected StationResponse(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
