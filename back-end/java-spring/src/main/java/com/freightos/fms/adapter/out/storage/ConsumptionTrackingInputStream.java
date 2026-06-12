package com.freightos.fms.adapter.out.storage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 읽힌 바이트 수를 추적하는 FilterInputStream.
 *
 * FailoverStoragePort의 부분소비 가드에서 사용한다.
 * S3가 스트림 일부만 읽고 실패할 경우 동일 스트림으로 로컬 폴백 시 잘린 파일이 저장되는
 * 문제를 방지하기 위해 bytesRead > 0 이면 폴백을 거부하고 예외를 전파한다.
 */
class ConsumptionTrackingInputStream extends FilterInputStream {

    private long bytesRead = 0;

    ConsumptionTrackingInputStream(InputStream in) {
        super(in);
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b != -1) {
            bytesRead++;
        }
        return b;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int n = super.read(b, off, len);
        if (n > 0) {
            bytesRead += n;
        }
        return n;
    }

    long bytesRead() {
        return bytesRead;
    }
}
