package spck.engine.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

// @see: https://github.com/casid/jusecase-ui-opengl/blob/master/src/main/java/org/jusecase/ui/opengl/util/ByteBufferUtils.java
public class ResourceLoader {
    public static String load(String filePath) {
        InputStream in = ResourceLoader.class.getResourceAsStream(filePath);
        Scanner scanner = new Scanner(in, StandardCharsets.UTF_8);
        return scanner.useDelimiter("\\A").next();
    }

    public static ByteBuffer loadToByteBuffer(String filePath) {
        ByteBuffer buffer = from(ResourceLoader.class.getResourceAsStream(filePath));
        ((Buffer) buffer).flip(); // https://github.com/plasma-umass/doppio/issues/497#issuecomment-334740243
        return buffer;
    }

    public static ByteBuffer from(InputStream inputStream) {
        try (ReadableByteChannel byteChannel = Channels.newChannel(inputStream)) {
            if (byteChannel instanceof SeekableByteChannel) {
                return from((SeekableByteChannel) byteChannel);
            } else {
                return from(byteChannel);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    public static ByteBuffer from(SeekableByteChannel byteChannel) {
        try {
            ByteBuffer buffer = org.lwjgl.BufferUtils.createByteBuffer((int) byteChannel.size() + 1);
            while (byteChannel.read(buffer) != -1) ;
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ByteBuffer from(ReadableByteChannel byteChannel) {
        return from(byteChannel, 1024 * 128);
    }

    public static ByteBuffer from(ReadableByteChannel byteChannel, int initialCapacity) {
        try {
            ByteBuffer buffer = org.lwjgl.BufferUtils.createByteBuffer(initialCapacity);
            while (true) {
                int bytes = 0;
                bytes = byteChannel.read(buffer);
                if (bytes == -1) {
                    break;
                }
                if (buffer.remaining() == 0) {
                    buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                }
            }
            return buffer;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = org.lwjgl.BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }
}
