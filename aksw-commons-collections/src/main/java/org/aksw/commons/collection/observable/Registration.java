package org.aksw.commons.collection.observable;

/** A listener registration */
public interface Registration{
    Registration fire();
    Registration remove();

    static Registration from(Runnable fireAction, Runnable removeAction) {
        return new Registration() {
            @Override
            public Registration fire() {
                fireAction.run();
                return this;
            }
            @Override
            public Registration remove() {
                removeAction.run();
                return this;
            }
        };
    }
}
