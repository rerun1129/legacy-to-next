package com.freightos.admin.adapter.in.web.auth.dto;

public record LoginResponse(String accessToken, String refreshToken, MeResponse me) {}
