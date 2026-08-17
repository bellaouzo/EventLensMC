package dev.bellaouzo.eventlens.application.port;

import dev.bellaouzo.eventlens.domain.listener.EventSearchResult;
import dev.bellaouzo.eventlens.domain.listener.ListenerRegistration;
import java.util.List;

public interface ListenerRegistryPort {

    EventSearchResult searchEvents(String query);

    List<ListenerRegistration> getListeners(String eventClassName);

    List<String> listKnownEventSimpleNames();

    List<String> listKnownEventClassNames();
}
