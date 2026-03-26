package com.github.arthurdeka.cedromoderndock.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SingleInstanceGuardTest {

    @TempDir
    Path tempDir;

    @Test
    void blocksSecondGuardWhileFirstIsHoldingLock() {
        Path lockFile = tempDir.resolve("app.lock");
        SingleInstanceGuard firstGuard = new SingleInstanceGuard(lockFile);
        SingleInstanceGuard secondGuard = new SingleInstanceGuard(lockFile);

        try {
            assertTrue(firstGuard.tryAcquire());
            assertFalse(secondGuard.tryAcquire());
        } finally {
            firstGuard.close();
            secondGuard.close();
        }
    }

    @Test
    void allowsAcquireAgainAfterFirstGuardReleasesLock() {
        Path lockFile = tempDir.resolve("app.lock");
        SingleInstanceGuard firstGuard = new SingleInstanceGuard(lockFile);
        SingleInstanceGuard secondGuard = new SingleInstanceGuard(lockFile);

        try {
            assertTrue(firstGuard.tryAcquire());
            firstGuard.close();
            assertTrue(secondGuard.tryAcquire());
        } finally {
            firstGuard.close();
            secondGuard.close();
        }
    }
}
