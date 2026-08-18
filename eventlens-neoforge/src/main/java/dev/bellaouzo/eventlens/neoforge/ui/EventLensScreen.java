package dev.bellaouzo.eventlens.neoforge.ui;

import dev.bellaouzo.eventlens.modcommon.ModTraceCoordinator;
import dev.bellaouzo.eventlens.modcommon.ModTraceResults;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EventLensScreen extends Screen {

    public enum Tab {
        HOME,
        EVENTS,
        SESSIONS,
        SESSION
    }

    private Tab tab = Tab.HOME;
    private String sessionId = "";
    private int sessionGeneration = -1;
    private int dispatchSequence = -1;
    private int refreshTicks;
    private EventLensUi.Frame frame = EventLensUi.frame(320, 240);

    public EventLensScreen() {
        super(Component.literal("EventLens"));
    }

    public static void open() {
        EventLensScreen screen = new EventLensScreen();
        screen.restoreLocation();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            minecraft.gui.setScreen(screen);
        }
    }

    public static void openSession(String sessionId) {
        EventLensScreen screen = new EventLensScreen();
        screen.tab = Tab.SESSION;
        screen.sessionId = sessionId;
        screen.persistLocation();
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft != null) {
            minecraft.gui.setScreen(screen);
        }
    }

    public static boolean isOpen() {
        return Minecraft.getInstance().gui.screen() instanceof EventLensScreen;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public ModTraceCoordinator coordinator() {
        return EventLensClientAccess.coordinator();
    }

    public EventLensUiPreferences preferences() {
        return EventLensClientAccess.preferences();
    }

    public EventLensUi.Frame frame() {
        return frame;
    }

    public String sessionId() {
        return sessionId;
    }

    public int sessionGeneration() {
        return sessionGeneration;
    }

    public Optional<Integer> sessionGenerationOption() {
        return sessionGeneration < 0 ? Optional.empty() : Optional.of(sessionGeneration);
    }

    public int dispatchSequence() {
        return dispatchSequence;
    }

    public void show(Tab next) {
        this.tab = next;
        if (next != Tab.SESSION) {
            dispatchSequence = -1;
        }
        persistLocation();
        rebuildWidgets();
    }

    public void showSession(String id) {
        showSessionRun(id, -1);
    }

    public void showSessionRun(String id, int generation) {
        this.sessionId = id;
        this.sessionGeneration = generation;
        this.dispatchSequence = -1;
        this.tab = Tab.SESSION;
        persistLocation();
        rebuildWidgets();
    }

    public void showDispatch(int sequence) {
        this.dispatchSequence = sequence;
        this.tab = Tab.SESSION;
        persistLocation();
        rebuildWidgets();
    }

    public Font textFont() {
        return font;
    }

    public Minecraft client() {
        return Minecraft.getInstance();
    }

    public <T extends AbstractWidget> T add(T widget) {
        return addRenderableWidget(widget);
    }

    public Button action(int x, int y, int width, String label, Button.OnPress press) {
        return add(Button.builder(Component.literal(label), press).bounds(x, y, width, 20).build());
    }

    @Override
    protected void init() {
        frame = EventLensUi.frame(width, height);
        int tabX = frame.x() + 10;
        tabButton(tabX, "Home", Tab.HOME);
        tabButton(tabX + 68, "Events", Tab.EVENTS);
        tabButton(tabX + 136, "Sessions", tab == Tab.SESSION ? Tab.SESSION : Tab.SESSIONS);
        switch (tab) {
            case HOME -> EventLensHomeTab.init(this, frame);
            case EVENTS -> EventLensEventsTab.init(this, frame);
            case SESSIONS -> EventLensSessionsTab.init(this, frame);
            case SESSION -> EventLensSessionTab.init(this, frame);
        }
    }

    private void tabButton(int x, String label, Tab target) {
        Button button = add(Button.builder(Component.literal(label), ignored -> show(target == Tab.SESSION ? Tab.SESSIONS : target))
                .bounds(x, frame.tabY(), 64, 20)
                .build());
        button.active = tab != target && !(tab == Tab.SESSION && target == Tab.SESSION);
    }

    @Override
    public void removed() {
        persistLocation();
    }

    private void restoreLocation() {
        EventLensUiPreferences prefs = preferences();
        if (prefs == null) {
            return;
        }
        try {
            tab = Tab.valueOf(prefs.lastTab());
        } catch (IllegalArgumentException ignored) {
            tab = Tab.HOME;
        }
        sessionId = prefs.lastSessionId();
        sessionGeneration = prefs.lastGeneration();
        dispatchSequence = prefs.lastDispatch();
        if (tab != Tab.SESSION) {
            return;
        }
        if (sessionId.isBlank()
                || coordinator() == null
                || coordinator().sessionManager().getSessionDetail(sessionId).isEmpty()) {
            tab = sessionId.isBlank() ? Tab.HOME : Tab.SESSIONS;
            dispatchSequence = -1;
            if (sessionId.isBlank()) {
                sessionGeneration = -1;
            }
            return;
        }
        if (sessionGeneration >= 0
                && coordinator()
                        .sessionManager()
                        .getSessionDetail(sessionId, Optional.of(sessionGeneration))
                        .isEmpty()) {
            sessionGeneration = -1;
            dispatchSequence = -1;
        }
    }

    private void persistLocation() {
        EventLensUiPreferences prefs = preferences();
        if (prefs != null) {
            prefs.setLastLocation(tab.name(), sessionId, sessionGeneration, dispatchSequence);
        }
    }

    @Override
    public void tick() {
        refreshTicks++;
        if (refreshTicks < 20) {
            return;
        }
        refreshTicks = 0;
        if (tab == Tab.SESSIONS) {
            EventLensSessionsTab.refresh(this);
        } else if (tab == Tab.SESSION) {
            EventLensSessionTab.refresh(this);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        EventLensUi.dimWorld(graphics, width, height);
        EventLensUi.panel(graphics, frame);
        if (tab != Tab.HOME) {
            EventLensUi.well(graphics, frame.contentX(), frame.contentY() + 22, frame.contentW(), frame.contentH() - 22);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        ModTraceResults.Status status = coordinator().status();
        EventLensUi.beginHits();
        EventLensUi.header(graphics, font, frame, status, coordinator().environmentPort().runtimeKind());
        int underlineX = frame.x() + 10;
        if (tab == Tab.EVENTS) {
            underlineX += 68;
        } else if (tab == Tab.SESSIONS || tab == Tab.SESSION) {
            underlineX += 136;
        }
        EventLensUi.tabUnderline(graphics, underlineX, frame.tabY(), 64);
        if (tab == Tab.HOME) {
            EventLensHomeTab.render(this, graphics, frame);
        } else if (tab == Tab.SESSION) {
            EventLensSessionTab.render(this, graphics, frame);
        }
        EventLensUi.renderHover(graphics, font, mouseX, mouseY);
    }
}
