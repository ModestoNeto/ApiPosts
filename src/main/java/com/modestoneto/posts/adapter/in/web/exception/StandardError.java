package com.modestoneto.posts.adapter.in.web.exception;

import java.time.Instant;

public record StandardError(Instant timestamp, int status, String error, String message, String path) {
}
