package me.jackson.exceptions;

import java.io.IOException;

/**
 * @author Jackson Chen
 * @version 1.0
 * @date 2023/2/22
 */
public class ConfigNotFoundException extends IOException {
    public ConfigNotFoundException(String errorMessage) {
        super(errorMessage);
    }
}
