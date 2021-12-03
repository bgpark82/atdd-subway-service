package nextstep.subway.path.domain;

import nextstep.subway.line.domain.Section;
import org.jgrapht.graph.DefaultWeightedEdge;

public class SectionEdge extends DefaultWeightedEdge {

    private Section section;

    public Section getSection() {
        return section;
    }

    public void addSection(Section section) {
        this.section = section;
    }
}