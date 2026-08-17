package dev.bellaouzo.eventlens.domain.listener;

import java.util.List;

public sealed interface ListenerInventoryResult
        permits ListenerInventoryResult.Success,
                ListenerInventoryResult.NotFound,
                ListenerInventoryResult.Ambiguous,
                ListenerInventoryResult.InvalidPage {

    record Success(ListenerInventoryPage page) implements ListenerInventoryResult {}

    record NotFound(String query) implements ListenerInventoryResult {}

    record Ambiguous(List<String> candidateClassNames) implements ListenerInventoryResult {}

    record InvalidPage(int requestedPage, int totalPages) implements ListenerInventoryResult {}
}
