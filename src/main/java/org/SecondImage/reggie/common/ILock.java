package org.SecondImage.reggie.common;

public interface ILock {
    boolean trylock(long timeout);

    void unlock();
}
