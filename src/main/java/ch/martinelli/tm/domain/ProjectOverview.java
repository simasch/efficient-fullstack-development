package ch.martinelli.tm.domain;

import java.util.List;

public record ProjectOverview(String name, String ownerName, List<TaskSummary> tasks) {
}
