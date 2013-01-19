package org.maltparser.core.helper;

import java.io.OutputStream;

/**
 *
 *
 * @author Johan Hall
 */
public final class NoOutputStream extends OutputStream {

    public static final OutputStream DEVNULL = new NoOutputStream();

    private NoOutputStream() {
    }

    public void write(int b) {
    }

    @Override
    public void write(byte[] b) {
    }

    @Override
    public void write(byte[] b, int off, int len) {
    }
}