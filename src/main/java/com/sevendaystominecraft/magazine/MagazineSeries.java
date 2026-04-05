package com.sevendaystominecraft.magazine;

import java.util.List;

public record MagazineSeries(
        String id,
        String displayName,
        int issueCount,
        List<String> issueDescriptions,
        String masteryDescription
) {
    public String getIssueDescription(int issue) {
        if (issue < 1 || issue > issueCount) return "";
        return issueDescriptions.get(issue - 1);
    }
}
