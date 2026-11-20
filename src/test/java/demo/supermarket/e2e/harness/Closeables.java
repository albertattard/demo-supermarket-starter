package demo.supermarket.e2e.harness;

import java.util.Objects;

public final class Closeables {

    public static void closeAllSuppressExceptions(final AutoCloseable... closeables) {
        for (final AutoCloseable closeable : closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (final InterruptedException _) {
                Thread.currentThread().interrupt();
            } catch (final Exception _) {
            }
        }
    }

    public static void closeAllSuppressing(final Throwable primaryFailure, final AutoCloseable... closeables) {
        Objects.requireNonNull(primaryFailure);

        for (final AutoCloseable closeable : closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                primaryFailure.addSuppressed(e);
            } catch (final Exception e) {
                primaryFailure.addSuppressed(e);
            }
        }
    }

    public static void closeAll(final AutoCloseable... closeables) {
        RuntimeException failedToCloseException = null;

        for (final AutoCloseable closeable : closeables) {
            try {
                if (closeable != null) {
                    closeable.close();
                }
            } catch (final Exception e) {
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }

                if (failedToCloseException == null) {
                    failedToCloseException = e instanceof RuntimeException re
                            ? re
                            : new RuntimeException(e.getMessage(), e);
                } else {
                    failedToCloseException.addSuppressed(e);
                }
            }
        }

        if (failedToCloseException != null) {
            throw failedToCloseException;
        }
    }

    private Closeables() {
    }
}
