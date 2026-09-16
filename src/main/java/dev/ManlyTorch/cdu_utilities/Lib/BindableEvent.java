package dev.ManlyTorch.cdu_utilities.Lib;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BindableEvent<T> {
    private final List<Consumer<T>> listeners = new ArrayList<>();

    public void onEvent(Consumer<T> callback) { listeners.add(callback); }
    public void fire(T data) { for (Consumer<T> listener : listeners) { listener.accept(data); } }
    public void fire() { fire(null); }
}