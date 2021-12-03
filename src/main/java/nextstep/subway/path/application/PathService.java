package nextstep.subway.path.application;

import nextstep.exception.BusinessException;
import nextstep.exception.ErrorCode;
import nextstep.subway.line.application.LineService;
import nextstep.subway.line.domain.Sections;
import nextstep.subway.path.dto.PathResponse;
import nextstep.subway.path.dto.PathResult;
import nextstep.subway.station.application.StationService;
import nextstep.subway.station.domain.Station;
import org.springframework.stereotype.Service;

@Service
public class PathService {

    private final LineService lineService;
    private final StationService stationService;
    private final PathFinder pathFinder;

    public PathService(LineService lineService, StationService stationService, PathFinder pathFinder) {
        this.lineService = lineService;
        this.stationService = stationService;
        this.pathFinder = pathFinder;
    }

    public PathResponse findShortestPath(Long source, Long target, int age) {
        check(source, target);
        Station sourceStation = stationService.findById(source);
        Station targetStation = stationService.findById(target);
        Sections sections = lineService.getSections();

        sections.checkConnected(sourceStation, targetStation);
        PathResult pathResult = pathFinder.findShortestPath(sections, sourceStation, targetStation);
        pathResult.calculateFare(age);

        return PathResponse.of(pathResult);
    }

    private void check(Long source, Long target) {
        if (source == target) {
            throw new BusinessException(ErrorCode.SAME_SOURCE_AND_TARGET);
        }
    }
}
