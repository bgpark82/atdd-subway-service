package nextstep.subway.path.dto;

import nextstep.subway.station.domain.Station;
import nextstep.subway.station.dto.StationResponse;

import java.util.List;
import java.util.stream.Collectors;

public class PathResponse {

    private List<StationResponse> stations;
    private int distance;

    public PathResponse() {
    }

    public PathResponse(List<Station> stations, int distance) {
        this.stations = stations.stream()
                .map(StationResponse::of)
                .collect(Collectors.toList());
        this.distance = distance;
    }

    public static PathResponse of(PathResult pathResult) {
        return new PathResponse(pathResult.getStations(), pathResult.getDistance());
    }

    public List<StationResponse> getStations() {
        return stations;
    }

    public int getDistance() {
        return distance;
    }
}
