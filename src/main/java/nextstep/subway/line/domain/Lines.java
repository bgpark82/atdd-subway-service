package nextstep.subway.line.domain;

import nextstep.subway.station.domain.Station;

import java.util.List;
import java.util.stream.Collectors;

public class Lines {

  private final List<Line> lines;

  public Lines(List<Line> lines) {
    this.lines = lines;
  }

  public List<Station> getAllStations() {
    return lines.stream()
        .flatMap(line -> line.getEndToEndStations().stream())
        .distinct()
        .collect(Collectors.toList());
  }

  public List<Section> getAllSections() {
    return lines.stream()
        .flatMap(line -> line.getSections().stream())
        .collect(Collectors.toList());
  }
}