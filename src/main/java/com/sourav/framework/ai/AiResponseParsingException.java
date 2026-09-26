package com.sourav.framework.ai;

import java.io.IOException;

public class AiResponseParsingException extends IOException {
    public AiResponseParsingException(String message) {
        super(message);
    }

    public AiResponseParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
