package org.aksw.commons.util.life_cycle;

/**
 * A very basic life cycle for things that can start and end
 *
 * @author raven
 *
 */
public interface LifeCycle {
    void start();
    void finish();
}